package com.example.data

import android.graphics.Bitmap
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.InferenceMode
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceConfig
import com.google.firebase.ai.OnDeviceModelOption
import com.google.firebase.ai.OnDeviceModelStatus
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.liveGenerationConfig
import com.google.firebase.ai.type.thinkingConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

enum class AiInferenceOrigin {
    ON_DEVICE,
    IN_CLOUD
}

@OptIn(PublicPreviewAPI::class)
fun InferenceSource?.toOrigin(): AiInferenceOrigin =
    if (this == InferenceSource.ON_DEVICE) AiInferenceOrigin.ON_DEVICE else AiInferenceOrigin.IN_CLOUD

data class AiTextResponse(
    val text: String,
    val origin: AiInferenceOrigin
)

data class SiteSafetyRiskResult(
    val hazardInput: String,
    val analysis: String,
    val origin: AiInferenceOrigin
)

data class AiWebSource(
    val title: String,
    val uri: String,
    val domain: String
)

data class AiContractAnalysisResult(
    val analysis: String,
    val searchQueries: List<String>,
    val sources: List<AiWebSource>,
    val origin: AiInferenceOrigin
)

data class AiThinkingStreamEvent(
    val thoughtChunk: String,
    val textChunk: String,
    val origin: AiInferenceOrigin
)

data class AiActionPackResult(
    val scriptTitle: String,
    val scriptContent: String,
    val scriptCategory: String,
    val suggestedTasks: List<TaskEntity>,
    val origin: AiInferenceOrigin
)

data class AiChatResponse(
    val text: String,
    val sources: List<AiWebSource>,
    val origin: AiInferenceOrigin
)

data class OnDeviceCapabilityState(
    val statusLabel: String,
    val isDownloadable: Boolean,
    val isAvailable: Boolean = false,
    val detectedModelName: String? = null,
    val configuredOptionLabel: String = "OnDeviceModelOption.PREVIEW (nano-v4-full / Gemma 4 E4B)",
    val androidSdkInt: Int = Build.VERSION.SDK_INT,
    val deviceSummary: String = "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
)

enum class NanoDiagnosticLanguage {
    ENGLISH,
    THAI
}

data class Nano4DiagnosticResult(
    val language: NanoDiagnosticLanguage,
    val prompt: String,
    val isSuccess: Boolean,
    val responseText: String = "",
    val inferenceSource: AiInferenceOrigin? = null,
    val preCallStatus: String,
    val detectedModelName: String?,
    val errorType: String? = null,
    val errorMessage: String? = null,
    val deviceInfo: String
)

data class MaterialSanityCheckResult(
    val areaScope: String,
    val requestInput: String,
    val baselineVersion: String,
    val baselineExcerpt: String,
    val assessment: String,
    val origin: AiInferenceOrigin
)

/**
 * Repository encapsulating all Firebase AI Logic integrations for Career Shield OS:
 * - Hybrid Inference targeting Gemini Nano 4 Full Preview (`OnDeviceModelOption.PREVIEW`,
 *   model family `nano-v4-full`, Gemma 4 E4B foundation) via AICore + Cloud Fallback (`gemini-3.8-flash`)
 * - Dedicated ONLY_ON_DEVICE Nano 4 Preview diagnostic path (never falls back to Cloud)
 * - Construction site safety risk analysis from text inputs
 * - Contract clause risk analysis (Google Search grounding & High Thinking)
 * - Multimodal site photo inspection, multi-turn chat, structured JSON action packs, and Live Voice
 */
@OptIn(PublicPreviewAPI::class)
class GeminiAiRepository {

    companion object {
        private const val REQUEST_TIMEOUT_MS = 60_000L
        private const val LIVE_CONNECT_TIMEOUT_MS = 30_000L
        const val HYBRID_CLOUD_FALLBACK_MODEL = "gemini-3.8-flash"
        const val ON_DEVICE_PREVIEW_TARGET = "OnDeviceModelOption.PREVIEW (nano-v4-full / Gemma 4 E4B)"
        const val ON_DEVICE_DIAGNOSTIC_MAX_TOKENS = 256
    }

    private val hybridAi by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
    }

    // Construction Site Safety Risk Analysis Models (Hybrid: PREFER_ON_DEVICE & ONLY_ON_DEVICE with PREVIEW)
    private val siteSafetyPreferOnDeviceModel: GenerativeModel by lazy {
        hybridAi.generativeModel(
            modelName = HYBRID_CLOUD_FALLBACK_MODEL,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.PREFER_ON_DEVICE,
                modelOption = OnDeviceModelOption.PREVIEW,
                maxOutputTokens = ON_DEVICE_DIAGNOSTIC_MAX_TOKENS
            ),
            systemInstruction = content {
                text(
                    """
                    คุณคือวิศวกรความปลอดภัยในการทำงานระดับวิชาชีพ (จป.วิชาชีพ) และผู้เชี่ยวชาญด้านกฎหมายความปลอดภัยงานก่อสร้างในประเทศไทย (กฎกระทรวงความปลอดภัยฯ งานก่อสร้าง พ.ศ. 2564, มาตรฐาน วสท., OSHA) ในระบบ CAREER SHIELD OS
                    หน้าที่ของคุณคือประเมินความเสี่ยงความปลอดภัยหน้างานก่อสร้างจากข้อความที่ได้รับอย่างแม่นยำ กระชับ และนำไปสั่งการหยุดความเสี่ยงหน้างานได้ทันที
                    """.trimIndent()
                )
            }
        )
    }

    private val siteSafetyOfflineOnlyModel: GenerativeModel by lazy {
        hybridAi.generativeModel(
            modelName = HYBRID_CLOUD_FALLBACK_MODEL,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.ONLY_ON_DEVICE,
                modelOption = OnDeviceModelOption.PREVIEW,
                maxOutputTokens = ON_DEVICE_DIAGNOSTIC_MAX_TOKENS
            ),
            systemInstruction = content {
                text(
                    """
                    คุณคือวิศวกรความปลอดภัยหน้างานก่อสร้าง (โหมดออฟไลน์บนเครื่อง) CAREER SHIELD OS
                    ประเมินความเสี่ยงความปลอดภัยและมาตรการแก้ไขด่วนเป็นภาษาไทยอย่างกระชับและชัดเจน
                    """.trimIndent()
                )
            }
        )
    }

    // General Hybrid Ops Models (PREFER_ON_DEVICE & ONLY_ON_DEVICE with OnDeviceModelOption.PREVIEW)
    private val hybridPreferOnDeviceModel: GenerativeModel by lazy {
        hybridAi.generativeModel(
            modelName = HYBRID_CLOUD_FALLBACK_MODEL,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.PREFER_ON_DEVICE,
                modelOption = OnDeviceModelOption.PREVIEW,
                maxOutputTokens = ON_DEVICE_DIAGNOSTIC_MAX_TOKENS
            ),
            systemInstruction = content {
                text(
                    """
                    คุณคือผู้ช่วยจัดการความเสี่ยงหน้างานก่อสร้าง CAREER SHIELD OS
                    ตอบเป็นภาษาไทยที่กระชับ ชัดเจน นำไปใช้หน้างานหรือส่งหนังสือ/LINE ได้ทันที
                    """.trimIndent()
                )
            }
        )
    }

    private val hybridOfflineOnlyModel: GenerativeModel by lazy {
        hybridAi.generativeModel(
            modelName = HYBRID_CLOUD_FALLBACK_MODEL,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.ONLY_ON_DEVICE,
                modelOption = OnDeviceModelOption.PREVIEW,
                maxOutputTokens = ON_DEVICE_DIAGNOSTIC_MAX_TOKENS
            ),
            systemInstruction = content {
                text(
                    """
                    คุณคือผู้ช่วยจัดการความเสี่ยงหน้างานก่อสร้าง CAREER SHIELD OS (โหมดออฟไลน์บนเครื่อง)
                    ตอบอย่างกระชับ ชัดเจน นำไปใช้หน้างานได้ทันที
                    """.trimIndent()
                )
            }
        )
    }

    // Dedicated Nano 4 Full Preview Diagnostic Model: strict ONLY_ON_DEVICE, NO Cloud Fallback
    private val nano4PreviewDiagnosticModel: GenerativeModel by lazy {
        hybridAi.generativeModel(
            modelName = HYBRID_CLOUD_FALLBACK_MODEL,
            onDeviceConfig = OnDeviceConfig(
                mode = InferenceMode.ONLY_ON_DEVICE,
                modelOption = OnDeviceModelOption.PREVIEW,
                maxOutputTokens = ON_DEVICE_DIAGNOSTIC_MAX_TOKENS
            )
        )
    }

    // Cloud Fallback Multimodal Model for heavy site photo inspections
    private val multimodalPhotoInspectorModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            onDeviceConfig = OnDeviceConfig(mode = InferenceMode.ONLY_IN_CLOUD),
            systemInstruction = content {
                text(
                    """
                    คุณคือวิศวกรตรวจสอบคุณภาพงานก่อสร้าง (QA/QC) และผู้เชี่ยวชาญด้านความปลอดภัย/ความเสี่ยงสัญญา (VO & LD)
                    วิเคราะห์ภาพถ่ายหน้างานที่แนบมาอย่างละเอียดเป็นภาษาไทย โดยระบุ:
                    1. 🚨 สิ่งผิดปกติ/ความเสี่ยงหน้างานที่ตรวจพบจากภาพ (Safety, โครงสร้าง, วัสดุ, ความคืบหน้า)
                    2. ⚖️ ผลกระทบต่อสัญญา ค่าปรับ LD หรือหลักฐานประกอบการขอ VO/EOT
                    3. ✅ ข้อแนะนำการแก้ไขด่วนและการบันทึกหลักฐานเพิ่มเติม
                    """.trimIndent()
                )
            }
        )
    }

    // High Thinking Model: gemini-4-carbon with ThinkingLevel.HIGH (no maxOutputTokens)
    private val deepThinkingModel: GenerativeModel by lazy {
        val config = generationConfig {
            thinkingConfig = thinkingConfig {
                thinkingLevel = ThinkingLevel.HIGH
                includeThoughts = true
            }
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-4-carbon",
            generationConfig = config
        )
    }

    // Search-Grounded Model: gemini-3.8-flash with Tool.googleSearch()
    private val groundedContractModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    คุณคือทนายความผู้เชี่ยวชาญกฎหมายสัญญาก่อสร้างไทย (ประมวลกฎหมายแพ่งและพาณิชย์ มาตรา 587-607 จ้างทำของ, พ.ร.บ.จัดซื้อจัดจ้างฯ, มาตรฐานสัญญา FIDIC) และผู้อำนวยการโครงการอาวุโส
                    หน้าที่ของคุณคือวิเคราะห์ข้อสัญญาก่อสร้าง (Contract Clause) ทันที เพื่อชี้จุดเสี่ยงคอขาดบาดตาย โดยเฉพาะ:
                    - ความเสี่ยงเรื่องค่าปรับล่าช้า (Liquidated Damages / LD) และเบี้ยปรับ
                    - เงื่อนไขการขอขยายเวลาก่อสร้าง (Extension of Time / EOT)
                    - การสั่งแก้ไขเปลี่ยนแปลงงาน (Variation Order / VO) และการห้ามสั่งงานปากเปล่า
                    - การริบเงินประกันผลงาน (Retention) หรือการบอกเลิกสัญญา
                    พร้อมแนะนำข้อความแก้ไขสัญญา (Counter-Clause) หรือหนังสือสงวนสิทธิ์ที่ผู้จัดการฝ่ายก่อสร้างนำไปใช้ได้ทันที
                    """.trimIndent()
                )
            }
        )
    }

    // Multi-turn Chatbot Model with Google Search Grounding
    private val chatModel: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    คุณคือ "CAREER SHIELD AI" ที่ปรึกษาประจำตัวผู้จัดการฝ่ายก่อสร้างและวิศวกรโครงการในประเทศไทย
                    คุณมีความเชี่ยวชาญด้านกฎหมายก่อสร้าง สัญญาจ้างทำของ การบริหาร SLA หน้างาน การป้องกันค่าปรับ LD การทำหนังสือ EOT และการควบคุมผู้รับเหมาช่วง
                    ตอบคำถามอย่างกระชับ ชัดเจน ตรงประเด็น ใช้งานหน้างานได้จริง และอ้างอิงหลักกฎหมายหรือมาตรฐานวิชาชีพเมื่อเหมาะสม
                    """.trimIndent()
                )
            }
        )
    }

    // Live API Voice Model: gemini-3.8-live
    private val liveVoiceModel by lazy {
        val config = liveGenerationConfig {
            responseModality = ResponseModality.AUDIO
            inputAudioTranscription = AudioTranscriptionConfig()
            outputAudioTranscription = AudioTranscriptionConfig()
        }
        Firebase.ai.liveModel(
            modelName = "gemini-3.8-live",
            generationConfig = config,
            systemInstruction = content {
                text(
                    "คุณคือที่ปรึกษาหน้างานก่อสร้าง CAREER SHIELD OS สนทนาโต้ตอบด้วยเสียงภาษาไทยอย่างกระชับ ชัดเจน เพื่อช่วยผู้จัดการฝ่ายก่อสร้างแก้ปัญหาเร่งด่วนหน้างาน เรื่องค่าปรับ LD สัญญาก่อสร้าง และการคุมผู้รับเหมา"
                )
            }
        )
    }

    private val actionPackSchema = Schema.obj(
        properties = mapOf(
            "scriptTitle" to Schema.string(description = "หัวข้อสคริปต์แจ้งเตือนหรือหนังสือสงวนสิทธิ์"),
            "scriptContent" to Schema.string(description = "เนื้อหาสคริปต์ภาษาราชการ/ธุรกิจก่อสร้าง พร้อมช่องกรอก [ระบุข้อมูล]"),
            "scriptCategory" to Schema.enumeration(
                values = listOf("CONTRACT", "EOT_LD", "SLA", "SAFETY"),
                description = "หมวดหมู่ของสคริปต์"
            ),
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "ชื่อภารกิจเช็กลิสต์หน้างานพร้อมเวลา เช่น 09:00 ถ่ายรูปหลักฐาน..."),
                        "description" to Schema.string(description = "รายละเอียดวิธีปฏิบัติเพื่อป้องกันความเสี่ยง"),
                        "category" to Schema.enumeration(
                            values = listOf("SAFETY", "SLA", "FINANCE", "CONTRACT")
                        ),
                        "timeSlot" to Schema.string(description = "เวลาในรูปแบบ HH:mm เช่น 09:00")
                    )
                ),
                description = "รายการเช็กลิสต์หน้างาน 2-3 ข้อเพื่อรับมือสถานการณ์นี้"
            )
        )
    )

    private val structuredGeneratorModel: GenerativeModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = actionPackSchema
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config
        )
    }

    /**
     * Analyzes construction site safety risks from provided text inputs using Firebase AI Logic
     * Hybrid Inference (Gemini Nano on-device when available, falling back to Cloud Gemini).
     */
    suspend fun analyzeSiteSafetyRisks(
        siteHazardText: String,
        offlineOnly: Boolean = false
    ): SiteSafetyRiskResult {
        val trimmedInput = siteHazardText.trim()
        require(trimmedInput.isNotEmpty()) { "กรุณาระบุรายละเอียดสภาพหน้างานหรือจุดเสี่ยงที่ต้องการวิเคราะห์" }

        val model = if (offlineOnly) siteSafetyOfflineOnlyModel else siteSafetyPreferOnDeviceModel
        val prompt = """
            วิเคราะห์ความเสี่ยงด้านความปลอดภัยอาชีวอนามัยในไซต์งานก่อสร้างจากข้อมูลต่อไปนี้:
            "$trimmedInput"
            
            กรุณาสรุปผลเป็น 4 หัวข้อที่ชัดเจนสำหรับผู้จัดการโครงการและ จป.หน้างาน:
            1. 🚨 ระดับความเสี่ยง (CRITICAL / HIGH / MEDIUM) และอันตรายเฉียบพลันที่อาจเกิดกับคนงานหรือโครงสร้าง
            2. ⚖️ กฎหมาย/มาตรฐานความปลอดภัยที่อาจละเมิด (เช่น กฎกระทรวงความปลอดภัยงานก่อสร้าง พ.ศ. 2564, วสท., งานที่สูง/นั่งร้าน/ดินขุด/ไฟฟ้า/ปั้นจั่น)
            3. 🦺 มาตรการแก้ไขเร่งด่วน & PPE บังคับ (Engineering Control + PPE + เงื่อนไข Stop-Work หากจำเป็น)
            4. 🛡️ การป้องกันความรับผิดทางกฎหมาย & LD (วิธีบันทึกหลักฐาน Timestamp Photo และแจ้งเตือนผู้รับเหมาช่วงเป็นลายลักษณ์อักษร)
        """.trimIndent()

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException("ไม่ได้รับผลการวิเคราะห์ความเสี่ยงความปลอดภัยจากโมเดล AI")
        }

        return SiteSafetyRiskResult(
            hazardInput = trimmedInput,
            analysis = text,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun generateHybridText(
        prompt: String,
        offlineOnly: Boolean
    ): AiTextResponse {
        val model = if (offlineOnly) hybridOfflineOnlyModel else hybridPreferOnDeviceModel
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException("ไม่ได้รับผลลัพธ์จากโมเดล AI")
        }
        return AiTextResponse(
            text = text,
            origin = response.inferenceSource.toOrigin()
        )
    }

    fun buildMaterialSanityCheckPrompt(
        areaScope: String,
        requestDescription: String,
        baselineExcerpt: String
    ): String = """
        คุณคือผู้ช่วยตรวจสอบความผิดปกติใบขอเบิกวัสดุของตำแหน่ง "${CertifiedBaselineData.ROLE_TITLE}"
        กฎเหล็ก:
        1) อ้างอิงเฉพาะฐานมาตรฐานรับรองด้านล่างและตัวเลขที่ผู้ใช้งานส่งเข้ามาในรอบนี้เท่านั้น ห้ามค้นเว็บภายนอกและห้ามแต่งตัวเลขหรือมาตรฐานขึ้นมาเอง
        2) หากข้อมูลในใบขอเบิกไม่เพียงพอ (เช่น ไม่ระบุความหนา ระยะเผื่อ หรือแบบหน้างาน) ให้ระบุชัดว่าส่วนใดยังไม่ครบ เพื่อให้ผู้จัดการถามลูกน้องเพิ่ม
        3) คำตอบนี้เป็น "สัญญาณตรวจจับความผิดปกติเบื้องต้น" เท่านั้น — ผู้จัดการโครงการเป็นผู้ตัดสินใจอนุมัติขั้นสุดท้าย และข้อยกเว้นรอบนี้จะไม่ถูกนำไปแก้ทับมาตรฐานกลาง

        $baselineExcerpt

        [คำขอเบิกชั่วคราวเฉพาะรอบนี้ (Ephemeral Input)]
        - พื้นที่/ขอบเขตงาน: ${areaScope.ifBlank { "ไม่ระบุพื้นที่" }}
        - รายการขอเบิก: "$requestDescription"

        กรุณาสรุปกระชับ 3 ข้อ:
        1. 🔍 สัญญาณความผิดปกติ (ปกติ / น่าสงสัย Over-claim / ข้อมูลไม่พอ) พร้อมเทียบส่วนต่างกับเกณฑ์ปลอดภัยด้านบน
        2. ❓ คำถามจี้จุดที่ผู้จัดการควรถามลูกน้อง/โฟร์แมนก่อนอนุมัติ
        3. 📝 คำแนะนำบันทึกเหตุผลข้อยกเว้น (หากจำเป็นต้องอนุมัติแพงกว่าเกณฑ์ เช่น งานเร่งด่วนกันค่าปรับ LD)
    """.trimIndent()

    suspend fun auditMaterialRequisition(
        areaScope: String,
        requestDescription: String,
        offlineOnly: Boolean
    ): MaterialSanityCheckResult {
        val trimmedReq = requestDescription.trim()
        require(trimmedReq.isNotEmpty()) { "กรุณาระบุรายการขอเบิกวัสดุหรือรายละเอียดงบที่ต้องการตรวจ" }
        val cleanArea = areaScope.trim().ifBlank { "พื้นที่หน้างานตามใบขอเบิก" }
        val baselineExcerpt = CertifiedBaselineData.findRelevantBaselineText("$cleanArea $trimmedReq")
        val prompt = buildMaterialSanityCheckPrompt(
            areaScope = cleanArea,
            requestDescription = trimmedReq,
            baselineExcerpt = baselineExcerpt
        )

        val model = if (offlineOnly) hybridOfflineOnlyModel else hybridPreferOnDeviceModel
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException("ไม่ได้รับผลการตรวจสอบใบขอเบิกวัสดุจากโมเดล AI")
        }

        return MaterialSanityCheckResult(
            areaScope = cleanArea,
            requestInput = trimmedReq,
            baselineVersion = CertifiedBaselineData.BASELINE_VERSION,
            baselineExcerpt = baselineExcerpt,
            assessment = text,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun inspectSitePhoto(
        bitmap: Bitmap,
        focusNote: String
    ): AiTextResponse {
        val requestContent = content {
            image(bitmap)
            text(
                """
                โปรดตรวจสอบภาพถ่ายหน้างานก่อสร้างนี้เพื่อประเมินความเสี่ยงด้านความปลอดภัย มาตรฐานงาน ความล่าช้า (LD) และหลักฐาน VO/EOT:
                จุดที่ผู้จัดการโครงการต้องการให้เน้นตรวจ: ${focusNote.ifBlank { "ตรวจสอบความเสี่ยงโครงสร้าง ความปลอดภัย ความเรียบร้อย และหลักฐานหน้างาน" }}
                """.trimIndent()
            )
        }

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            multimodalPhotoInspectorModel.generateContent(requestContent)
        }
        val analysis = response.text?.trim().orEmpty()
        if (analysis.isBlank()) {
            throw IllegalStateException("โมเดลไม่ได้ส่งผลการวิเคราะห์ภาพกลับมา กรุณาลองอีกครั้ง")
        }
        return AiTextResponse(
            text = analysis,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun analyzeContractClauseWithSearch(clauseText: String): AiContractAnalysisResult {
        val prompt = buildContractRiskPrompt(clauseText)
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            groundedContractModel.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException("โมเดลส่งคำตอบว่างกลับมา กรุณาลองใหม่อีกครั้ง")
        }

        val groundingMeta = response.candidates.firstOrNull()?.groundingMetadata
        val queries = groundingMeta?.webSearchQueries.orEmpty()
        val sources = groundingMeta?.groundingChunks
            .orEmpty()
            .mapNotNull { it.web }
            .map { web ->
                AiWebSource(
                    title = web.title ?: web.domain ?: "แหล่งข้อมูลอ้างอิง",
                    uri = web.uri ?: "",
                    domain = web.domain ?: ""
                )
            }
            .distinctBy { it.uri }

        return AiContractAnalysisResult(
            analysis = text,
            searchQueries = queries,
            sources = sources,
            origin = response.inferenceSource.toOrigin()
        )
    }

    fun buildContractRiskPrompt(clauseText: String): String = """
        โปรดวิเคราะห์ความเสี่ยงทางกฎหมายก่อสร้างและค่าปรับ LD จากข้อสัญญาต่อไปนี้แบบทันที:
        
        ข้อความในสัญญา:
        "$clauseText"
        
        กรุณาสรุปเป็นหัวข้อชัดเจน:
        1. 🚨 ระดับความเสี่ยง (CRITICAL / HIGH / MEDIUM) และจุดอันตรายที่อาจทำให้ผู้รับจ้าง/ผู้จัดการโครงการเสียเปรียบหรือโดนค่าปรับ LD
        2. ⚖️ ข้อกฎหมายก่อสร้างไทย / หลักเกณฑ์สัญญาจ้างทำของ หรือแนวคำพิพากษาที่เกี่ยวข้อง
        3. 🛡️ ข้อความแก้ไขสัญญาที่แนะนำ (Counter-Proposal Clause) เพื่อลดเพดานค่าปรับ LD หรือเปิดช่องขอขยายเวลา (EOT)
        4. 📋 แนวทางปฏิบัติหน้างานเพื่อป้องกันตัวหากต้องเซ็นสัญญานี้
    """.trimIndent()

    fun streamHighThinking(prompt: String): Flow<AiThinkingStreamEvent> {
        return deepThinkingModel.generateContentStream(prompt).map { chunk ->
            AiThinkingStreamEvent(
                thoughtChunk = chunk.thoughtSummary.orEmpty(),
                textChunk = chunk.text.orEmpty(),
                origin = chunk.inferenceSource.toOrigin()
            )
        }
    }

    suspend fun generateActionPack(situation: String): AiActionPackResult {
        val prompt = """
            สร้างชุดเอาตัวรอดสำหรับผู้จัดการฝ่ายก่อสร้างในสถานการณ์: "$situation"
            1. ร่างสคริปต์ SOS ภาษาไทยที่เป็นทางการและรัดกุมเพื่อใช้ส่ง LINE หรือหนังสือราชการแจ้งผู้เกี่ยวข้อง
            2. สร้างรายการเช็กลิสต์หน้างาน (Daily Checkpoints) 2-3 ข้อเพื่อป้องกันความเสียหายจากสถานการณ์นี้
        """.trimIndent()

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            structuredGeneratorModel.generateContent(prompt)
        }
        val jsonString = response.text ?: throw IllegalStateException("ได้รับข้อมูลว่างจากโมเดล")
        val json = JSONObject(jsonString)

        val scriptTitle = json.getString("scriptTitle")
        val scriptContent = json.getString("scriptContent")
        val scriptCategory = json.optString("scriptCategory", "CONTRACT")

        val tasksArray = json.getJSONArray("tasks")
        val parsedTasks = (0 until tasksArray.length()).map { idx ->
            val item = tasksArray.getJSONObject(idx)
            val timeSlot = item.optString("timeSlot", "09:00")
            val rawTitle = item.getString("title")
            val formattedTitle = if (rawTitle.matches(Regex("^\\d{2}:\\d{2}.*"))) {
                rawTitle
            } else {
                "$timeSlot $rawTitle"
            }
            TaskEntity(
                title = formattedTitle,
                description = item.optString("description", "ตรวจสอบและบันทึกหลักฐานหน้างาน"),
                category = item.optString("category", "CONTRACT"),
                timeSlot = timeSlot,
                isCompleted = false
            )
        }

        return AiActionPackResult(
            scriptTitle = scriptTitle,
            scriptContent = scriptContent,
            scriptCategory = scriptCategory,
            suggestedTasks = parsedTasks,
            origin = response.inferenceSource.toOrigin()
        )
    }

    fun createChatSession(): Chat {
        return chatModel.startChat(
            history = listOf(
                content("model") {
                    text("สวัสดีครับ ผมคือที่ปรึกษากฎหมายสัญญาก่อสร้างและผู้เชี่ยวชาญการบริหารความเสี่ยงหน้างาน (CAREER SHIELD AI) พร้อมช่วยวิเคราะห์ข้อสัญญาและปัญหาหน้างานครับ")
                }
            )
        )
    }

    suspend fun sendChatMessage(chat: Chat, messageText: String): AiChatResponse {
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            chat.sendMessage(messageText)
        }
        val replyText = response.text?.takeIf { it.isNotBlank() }
            ?: "ขออภัยครับ ระบบไม่สามารถสร้างคำตอบได้ในขณะนี้"
        val groundingMeta = response.candidates.firstOrNull()?.groundingMetadata
        val sources = groundingMeta?.groundingChunks
            .orEmpty()
            .mapNotNull { it.web }
            .map { web ->
                AiWebSource(
                    title = web.title ?: web.domain ?: "แหล่งอ้างอิง",
                    uri = web.uri ?: "",
                    domain = web.domain ?: ""
                )
            }
            .distinctBy { it.uri }

        return AiChatResponse(
            text = replyText,
            sources = sources,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun connectLiveVoice(): LiveSession {
        return withTimeout(LIVE_CONNECT_TIMEOUT_MS) {
            liveVoiceModel.connect()
        }
    }

    fun buildDiagnosticPrompt(language: NanoDiagnosticLanguage): String = when (language) {
        NanoDiagnosticLanguage.ENGLISH ->
            "List 3 concise construction scaffolding fall-protection checks before workers climb."
        NanoDiagnosticLanguage.THAI ->
            "สรุป 3 ข้อตรวจสอบความปลอดภัยนั่งร้านก่อสร้างก่อนเริ่มงานอย่างกระชับ"
    }

    suspend fun checkOnDeviceCapability(): OnDeviceCapabilityState {
        val deviceSummary = "${Build.MANUFACTURER} ${Build.MODEL} (Android SDK ${Build.VERSION.SDK_INT})".trim()
        return try {
            val extension = nano4PreviewDiagnosticModel.onDeviceExtension
                ?: return OnDeviceCapabilityState(
                    statusLabel = "ไม่พบ OnDeviceExtension (ใช้ Cloud Fallback)",
                    isDownloadable = false,
                    isAvailable = false,
                    deviceSummary = deviceSummary
                )
            val status = extension.checkStatus()
            val detectedName = runCatching { extension.getModelName() }.getOrNull()?.takeIf { it.isNotBlank() }
            when (status) {
                OnDeviceModelStatus.AVAILABLE -> {
                    val label = if (detectedName != null) {
                        "AICore On-Device พร้อมใช้งาน ($detectedName)"
                    } else {
                        "AICore On-Device พร้อมใช้งาน (ยังไม่ได้ยืนยันชื่อโมเดล Preview)"
                    }
                    OnDeviceCapabilityState(
                        statusLabel = label,
                        isDownloadable = false,
                        isAvailable = true,
                        detectedModelName = detectedName,
                        deviceSummary = deviceSummary
                    )
                }
                OnDeviceModelStatus.DOWNLOADABLE -> OnDeviceCapabilityState(
                    statusLabel = "ดาวน์โหลดโมเดล On-Device Preview ได้",
                    isDownloadable = true,
                    isAvailable = false,
                    detectedModelName = detectedName,
                    deviceSummary = deviceSummary
                )
                OnDeviceModelStatus.DOWNLOADING -> OnDeviceCapabilityState(
                    statusLabel = "กำลังดาวน์โหลดโมเดล On-Device Preview...",
                    isDownloadable = false,
                    isAvailable = false,
                    detectedModelName = detectedName,
                    deviceSummary = deviceSummary
                )
                else -> OnDeviceCapabilityState(
                    statusLabel = "อุปกรณ์ไม่รองรับ AICore Preview (${status}) • ใช้ Cloud Fallback",
                    isDownloadable = false,
                    isAvailable = false,
                    detectedModelName = detectedName,
                    deviceSummary = deviceSummary
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            OnDeviceCapabilityState(
                statusLabel = "AICore Preview ไม่พร้อมใช้งาน (${e::class.simpleName ?: "Unavailable"}) • ใช้ Cloud Fallback",
                isDownloadable = false,
                isAvailable = false,
                deviceSummary = deviceSummary
            )
        }
    }

    /**
     * Dedicated Gemini Nano 4 Full Preview diagnostic call (`OnDeviceModelOption.PREVIEW` +
     * `InferenceMode.ONLY_ON_DEVICE`, `maxOutputTokens = 256`).
     * Never falls back to Cloud — returns either the actual on-device response + `inferenceSource`
     * or the authentic SDK unavailability/error details.
     */
    suspend fun runNano4PreviewDiagnostic(language: NanoDiagnosticLanguage): Nano4DiagnosticResult {
        val prompt = buildDiagnosticPrompt(language)
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} | API ${Build.VERSION.SDK_INT} | ABI=${Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown"}"
        var preCallStatus = "NOT_CHECKED"
        var detectedModelName: String? = null

        return try {
            val extension = nano4PreviewDiagnosticModel.onDeviceExtension
            preCallStatus = try {
                extension?.checkStatus()?.toString() ?: "NO_ON_DEVICE_EXTENSION"
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                "STATUS_CHECK_FAILED: ${e::class.simpleName}: ${e.localizedMessage ?: e.message ?: "unknown"}"
            }
            detectedModelName = try {
                extension?.getModelName()?.takeIf { it.isNotBlank() }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                null
            }

            val response = withTimeout(REQUEST_TIMEOUT_MS) {
                nano4PreviewDiagnosticModel.generateContent(prompt)
            }
            val text = response.text?.trim().orEmpty()
            if (text.isBlank()) {
                Nano4DiagnosticResult(
                    language = language,
                    prompt = prompt,
                    isSuccess = false,
                    inferenceSource = response.inferenceSource.toOrigin(),
                    preCallStatus = preCallStatus,
                    detectedModelName = detectedModelName,
                    errorType = "EmptyResponse",
                    errorMessage = "โมเดลบนอุปกรณ์ส่งผลลัพธ์ว่างกลับมา",
                    deviceInfo = deviceInfo
                )
            } else {
                Nano4DiagnosticResult(
                    language = language,
                    prompt = prompt,
                    isSuccess = true,
                    responseText = text,
                    inferenceSource = response.inferenceSource.toOrigin(),
                    preCallStatus = preCallStatus,
                    detectedModelName = detectedModelName,
                    deviceInfo = deviceInfo
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Nano4DiagnosticResult(
                language = language,
                prompt = prompt,
                isSuccess = false,
                preCallStatus = preCallStatus,
                detectedModelName = detectedModelName,
                errorType = e::class.simpleName ?: "Throwable",
                errorMessage = e.localizedMessage ?: e.message ?: e.toString(),
                deviceInfo = deviceInfo
            )
        }
    }

    fun downloadOnDeviceModel(): Flow<DownloadStatus>? {
        val extension = nano4PreviewDiagnosticModel.onDeviceExtension ?: return null
        return extension.download()
    }
}
