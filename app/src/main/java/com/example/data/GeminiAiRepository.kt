package com.example.data

import android.graphics.Bitmap
import android.os.Build
import com.example.ui.localization.AppLanguage
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
 * - Bilingual Thai / English system prompts & responses across all AI features
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

    // Construction Site Safety Risk Analysis Models (TH & EN)
    private val siteSafetyPreferOnDeviceModelTh: GenerativeModel by lazy {
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

    private val siteSafetyPreferOnDeviceModelEn: GenerativeModel by lazy {
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
                    You are a Senior Construction Occupational Safety Engineer (OSH / OSHA / Thai Ministerial Regulation B.E. 2564) in CAREER SHIELD OS.
                    Assess jobsite hazards concisely in English with immediate Stop-Work and engineering control guidance.
                    """.trimIndent()
                )
            }
        )
    }

    private val siteSafetyOfflineOnlyModelTh: GenerativeModel by lazy {
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

    private val siteSafetyOfflineOnlyModelEn: GenerativeModel by lazy {
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
                    You are a construction site safety engineer (On-Device Offline Mode) in CAREER SHIELD OS.
                    Respond concisely in English with immediate hazard controls.
                    """.trimIndent()
                )
            }
        )
    }

    // General Hybrid Ops Models (TH & EN)
    private val hybridPreferOnDeviceModelTh: GenerativeModel by lazy {
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

    private val hybridPreferOnDeviceModelEn: GenerativeModel by lazy {
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
                    You are the CAREER SHIELD OS construction site risk assistant.
                    Always reply in crisp, professional English ready for field execution or formal notice dispatch.
                    """.trimIndent()
                )
            }
        )
    }

    private val hybridOfflineOnlyModelTh: GenerativeModel by lazy {
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

    private val hybridOfflineOnlyModelEn: GenerativeModel by lazy {
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
                    You are the CAREER SHIELD OS site risk assistant (On-Device Offline Mode).
                    Always reply in clear, concise English for immediate field action.
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

    // Cloud Fallback Multimodal Models (TH & EN)
    private val multimodalPhotoInspectorModelTh: GenerativeModel by lazy {
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

    private val multimodalPhotoInspectorModelEn: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            onDeviceConfig = OnDeviceConfig(mode = InferenceMode.ONLY_IN_CLOUD),
            systemInstruction = content {
                text(
                    """
                    You are a Senior Construction QA/QC Engineer and Contract Risk Specialist (VO & LD).
                    Analyze the attached jobsite photograph thoroughly in English covering:
                    1. 🚨 Observed defects / safety hazards (OSH, structural, materials, progress)
                    2. ⚖️ Contractual exposure (Liquidated Damages / LD) and evidence value for VO / EOT claims
                    3. ✅ Immediate corrective actions and required timestamped photo documentation
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

    // Search-Grounded Models (TH & EN)
    private val groundedContractModelTh: GenerativeModel by lazy {
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

    private val groundedContractModelEn: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    You are a Senior Construction Contract Counsel & Project Director specializing in FIDIC standards and Thai Civil & Commercial Code (Hire of Work Sections 587-607) / public procurement law.
                    Analyze the construction contract clause immediately in English, focusing on:
                    - Liquidated Damages (LD) exposure & cap risks
                    - Extension of Time (EOT) notice bars & force majeure
                    - Variation Order (VO) verbal-instruction traps
                    - Retention forfeiture & termination clauses
                    Provide an actionable Counter-Proposal Clause and formal reservation notice wording ready for immediate use.
                    """.trimIndent()
                )
            }
        )
    }

    // Multi-turn Chatbot Models (TH & EN)
    private val chatModelTh: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    คุณคือ "CAREER SHIELD AI" ที่ปรึกษาประจำตัวผู้จัดการฝ่ายก่อสร้างและวิศวกรโครงการในประเทศไทย
                    คุณมีความเชี่ยวชาญด้านกฎหมายก่อสร้าง สัญญาจ้างทำของ การบริหาร SLA หน้างาน การป้องกันค่าปรับ LD การทำหนังสือ EOT และการควบคุมผู้รับเหมาช่วง
                    ตอบคำถามเป็นภาษาไทยอย่างกระชับ ชัดเจน ตรงประเด็น ใช้งานหน้างานได้จริง และอ้างอิงหลักกฎหมายหรือมาตรฐานวิชาชีพเมื่อเหมาะสม
                    """.trimIndent()
                )
            }
        )
    }

    private val chatModelEn: GenerativeModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.googleSearch()),
            systemInstruction = content {
                text(
                    """
                    You are "CAREER SHIELD AI", an executive advisor for Construction Project Managers and Site Engineers.
                    You specialize in construction contract law (FIDIC & Thai Civil Code), site SLA governance, Liquidated Damages (LD) mitigation, EOT notices, VO controls, and subcontractor management.
                    Always respond in crisp, practical English with legal/industry references where appropriate.
                    """.trimIndent()
                )
            }
        )
    }

    // Live API Voice Models (TH & EN)
    private val liveVoiceModelTh by lazy {
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

    private val liveVoiceModelEn by lazy {
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
                    "You are the CAREER SHIELD OS construction site advisor. Converse concisely in English audio to help the construction project manager resolve urgent LD, contract, VO, and subcontractor issues hands-free."
                )
            }
        )
    }

    private val actionPackSchema = Schema.obj(
        properties = mapOf(
            "scriptTitle" to Schema.string(description = "Title of SOS warning script or reservation notice"),
            "scriptContent" to Schema.string(description = "Formal construction notice text with [placeholder] fields"),
            "scriptCategory" to Schema.enumeration(
                values = listOf("CONTRACT", "EOT_LD", "SLA", "SAFETY"),
                description = "Category of the script"
            ),
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Site checkpoint title prefixed with time, e.g. 09:00 Capture timestamped photo..."),
                        "description" to Schema.string(description = "Actionable mitigation steps"),
                        "category" to Schema.enumeration(
                            values = listOf("SAFETY", "SLA", "FINANCE", "CONTRACT")
                        ),
                        "timeSlot" to Schema.string(description = "Time in HH:mm format e.g. 09:00")
                    )
                ),
                description = "2-3 daily site checkpoints to mitigate this crisis"
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

    suspend fun analyzeSiteSafetyRisks(
        siteHazardText: String,
        offlineOnly: Boolean = false,
        language: AppLanguage = AppLanguage.TH
    ): SiteSafetyRiskResult {
        val trimmedInput = siteHazardText.trim()
        require(trimmedInput.isNotEmpty()) {
            if (language == AppLanguage.EN) {
                "Please enter site conditions or observed safety hazards first."
            } else {
                "กรุณาระบุรายละเอียดสภาพหน้างานหรือจุดเสี่ยงที่ต้องการวิเคราะห์"
            }
        }

        val model = when {
            offlineOnly && language == AppLanguage.EN -> siteSafetyOfflineOnlyModelEn
            offlineOnly -> siteSafetyOfflineOnlyModelTh
            language == AppLanguage.EN -> siteSafetyPreferOnDeviceModelEn
            else -> siteSafetyPreferOnDeviceModelTh
        }
        val prompt = if (language == AppLanguage.EN) {
            """
            Analyze construction site occupational safety & health risks from this field report:
            "$trimmedInput"

            Summarize cleanly in English across 4 sections for the Project Manager & Safety Officer:
            1. 🚨 Risk Severity (CRITICAL / HIGH / MEDIUM) and immediate hazard to workers or structure
            2. ⚖️ Applicable Safety Standards / Regulations (Thai Ministerial Reg. B.E. 2564, EIT, OSHA — working at height / scaffolding / excavation / electrical / crane)
            3. 🦺 Immediate Controls & Mandatory PPE (Engineering Controls + PPE + Stop-Work trigger if needed)
            4. 🛡️ Legal Liability & LD Protection (Timestamped photo documentation & written subcontractor warning)
            """.trimIndent()
        } else {
            """
            วิเคราะห์ความเสี่ยงด้านความปลอดภัยอาชีวอนามัยในไซต์งานก่อสร้างจากข้อมูลต่อไปนี้:
            "$trimmedInput"
            
            กรุณาสรุปผลเป็น 4 หัวข้อที่ชัดเจนสำหรับผู้จัดการโครงการและ จป.หน้างาน:
            1. 🚨 ระดับความเสี่ยง (CRITICAL / HIGH / MEDIUM) และอันตรายเฉียบพลันที่อาจเกิดกับคนงานหรือโครงสร้าง
            2. ⚖️ กฎหมาย/มาตรฐานความปลอดภัยที่อาจละเมิด (เช่น กฎกระทรวงความปลอดภัยงานก่อสร้าง พ.ศ. 2564, วสท., งานที่สูง/นั่งร้าน/ดินขุด/ไฟฟ้า/ปั้นจั่น)
            3. 🦺 มาตรการแก้ไขเร่งด่วน & PPE บังคับ (Engineering Control + PPE + เงื่อนไข Stop-Work หากจำเป็น)
            4. 🛡️ การป้องกันความรับผิดทางกฎหมาย & LD (วิธีบันทึกหลักฐาน Timestamp Photo และแจ้งเตือนผู้รับเหมาช่วงเป็นลายลักษณ์อักษร)
            """.trimIndent()
        }

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) "Empty safety risk analysis from AI model"
                else "ไม่ได้รับผลการวิเคราะห์ความเสี่ยงความปลอดภัยจากโมเดล AI"
            )
        }

        return SiteSafetyRiskResult(
            hazardInput = trimmedInput,
            analysis = text,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun generateHybridText(
        prompt: String,
        offlineOnly: Boolean,
        language: AppLanguage = AppLanguage.TH
    ): AiTextResponse {
        val model = when {
            offlineOnly && language == AppLanguage.EN -> hybridOfflineOnlyModelEn
            offlineOnly -> hybridOfflineOnlyModelTh
            language == AppLanguage.EN -> hybridPreferOnDeviceModelEn
            else -> hybridPreferOnDeviceModelTh
        }
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) "Empty response from AI model"
                else "ไม่ได้รับผลลัพธ์จากโมเดล AI"
            )
        }
        return AiTextResponse(
            text = text,
            origin = response.inferenceSource.toOrigin()
        )
    }

    fun buildMaterialSanityCheckPrompt(
        areaScope: String,
        requestDescription: String,
        baselineExcerpt: String,
        language: AppLanguage = AppLanguage.TH
    ): String = if (language == AppLanguage.EN) {
        """
        You are the material requisition anomaly detector for "${CertifiedBaselineData.ROLE_TITLE}".
        Strict Rules:
        1) Reference ONLY the Verified Read-Only Certified Baseline below and numbers provided in this request. Do NOT search external web sources or invent norms.
        2) If details are missing (e.g. slab thickness, notch size, layout drawing), explicitly flag what information is missing so the manager can question staff.
        3) This output is a preliminary "Anomaly Signal" only — the Project Manager makes the final approval, and this exception will NEVER overwrite the central baseline.

        $baselineExcerpt

        [Ephemeral Requisition Input - This Session Only]
        - Scope / Work Zone: ${areaScope.ifBlank { "Unspecified zone" }}
        - Requisition Details: "$requestDescription"

        Respond concisely in English across 3 sections:
        1. 🔍 Anomaly Signal (Normal / Suspicious Over-claim / Insufficient Data) comparing variance vs. the safe baseline above
        2. ❓ Targeted Verification Questions the manager should ask the foreman before approving
        3. 📝 Audit Justification Note Guidance (if an exception must be approved above norm, e.g., urgent schedule recovery to avoid LD)
        """.trimIndent()
    } else {
        """
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
    }

    suspend fun auditMaterialRequisition(
        areaScope: String,
        requestDescription: String,
        offlineOnly: Boolean,
        language: AppLanguage = AppLanguage.TH
    ): MaterialSanityCheckResult {
        val trimmedReq = requestDescription.trim()
        require(trimmedReq.isNotEmpty()) {
            if (language == AppLanguage.EN) "Please enter the material requisition or budget details to audit."
            else "กรุณาระบุรายการขอเบิกวัสดุหรือรายละเอียดงบที่ต้องการตรวจ"
        }
        val cleanArea = areaScope.trim().ifBlank {
            if (language == AppLanguage.EN) "Site Work Zone" else "พื้นที่หน้างานตามใบขอเบิก"
        }
        val baselineExcerpt = CertifiedBaselineData.findRelevantBaselineText("$cleanArea $trimmedReq", language)
        val prompt = buildMaterialSanityCheckPrompt(
            areaScope = cleanArea,
            requestDescription = trimmedReq,
            baselineExcerpt = baselineExcerpt,
            language = language
        )

        val model = when {
            offlineOnly && language == AppLanguage.EN -> hybridOfflineOnlyModelEn
            offlineOnly -> hybridOfflineOnlyModelTh
            language == AppLanguage.EN -> hybridPreferOnDeviceModelEn
            else -> hybridPreferOnDeviceModelTh
        }
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) "Empty sanity check response from AI model"
                else "ไม่ได้รับผลการตรวจสอบใบขอเบิกวัสดุจากโมเดล AI"
            )
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
        focusNote: String,
        language: AppLanguage = AppLanguage.TH
    ): AiTextResponse {
        val model = if (language == AppLanguage.EN) multimodalPhotoInspectorModelEn else multimodalPhotoInspectorModelTh
        val promptText = if (language == AppLanguage.EN) {
            """
            Please inspect this construction site photo for safety hazards, workmanship compliance, delay/LD risks, and VO/EOT evidence:
            Inspection focus requested by PM: ${focusNote.ifBlank { "Inspect structural quality, OSH safety hazards, workmanship, and site evidence" }}
            Respond in English.
            """.trimIndent()
        } else {
            """
            โปรดตรวจสอบภาพถ่ายหน้างานก่อสร้างนี้เพื่อประเมินความเสี่ยงด้านความปลอดภัย มาตรฐานงาน ความล่าช้า (LD) และหลักฐาน VO/EOT:
            จุดที่ผู้จัดการโครงการต้องการให้เน้นตรวจ: ${focusNote.ifBlank { "ตรวจสอบความเสี่ยงโครงสร้าง ความปลอดภัย ความเรียบร้อย และหลักฐานหน้างาน" }}
            """.trimIndent()
        }

        val requestContent = content {
            image(bitmap)
            text(promptText)
        }

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(requestContent)
        }
        val analysis = response.text?.trim().orEmpty()
        if (analysis.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) "No photo analysis returned from AI model. Please try again."
                else "โมเดลไม่ได้ส่งผลการวิเคราะห์ภาพกลับมา กรุณาลองอีกครั้ง"
            )
        }
        return AiTextResponse(
            text = analysis,
            origin = response.inferenceSource.toOrigin()
        )
    }

    suspend fun analyzeContractClauseWithSearch(
        clauseText: String,
        language: AppLanguage = AppLanguage.TH
    ): AiContractAnalysisResult {
        val prompt = buildContractRiskPrompt(clauseText, language)
        val model = if (language == AppLanguage.EN) groundedContractModelEn else groundedContractModelTh
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(prompt)
        }
        val text = response.text?.trim().orEmpty()
        if (text.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) "Empty contract analysis returned. Please try again."
                else "โมเดลส่งคำตอบว่างกลับมา กรุณาลองใหม่อีกครั้ง"
            )
        }

        val groundingMeta = response.candidates.firstOrNull()?.groundingMetadata
        val queries = groundingMeta?.webSearchQueries.orEmpty()
        val sources = groundingMeta?.groundingChunks
            .orEmpty()
            .mapNotNull { it.web }
            .map { web ->
                AiWebSource(
                    title = web.title ?: web.domain ?: if (language == AppLanguage.EN) "Reference Source" else "แหล่งข้อมูลอ้างอิง",
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

    fun buildContractRiskPrompt(
        clauseText: String,
        language: AppLanguage = AppLanguage.TH
    ): String = if (language == AppLanguage.EN) {
        """
        Analyze the legal risk and Liquidated Damages (LD) exposure of this construction contract clause immediately in English:

        Contract Clause:
        "$clauseText"

        Summarize clearly across 4 sections:
        1. 🚨 Risk Severity (CRITICAL / HIGH / MEDIUM) & key commercial/LD traps
        2. ⚖️ Applicable Construction Law / FIDIC / Thai Civil Code Hire-of-Work principles
        3. 🛡️ Recommended Counter-Proposal Clause to cap LD or preserve EOT rights
        4. 📋 Site Action Plan to protect the Project Manager if this clause is in force
        """.trimIndent()
    } else {
        """
        โปรดวิเคราะห์ความเสี่ยงทางกฎหมายก่อสร้างและค่าปรับ LD จากข้อสัญญาต่อไปนี้แบบทันที:
        
        ข้อความในสัญญา:
        "$clauseText"
        
        กรุณาสรุปเป็นหัวข้อชัดเจน:
        1. 🚨 ระดับความเสี่ยง (CRITICAL / HIGH / MEDIUM) และจุดอันตรายที่อาจทำให้ผู้รับจ้าง/ผู้จัดการโครงการเสียเปรียบหรือโดนค่าปรับ LD
        2. ⚖️ ข้อกฎหมายก่อสร้างไทย / หลักเกณฑ์สัญญาจ้างทำของ หรือแนวคำพิพากษาที่เกี่ยวข้อง
        3. 🛡️ ข้อความแก้ไขสัญญาที่แนะนำ (Counter-Proposal Clause) เพื่อลดเพดานค่าปรับ LD หรือเปิดช่องขอขยายเวลา (EOT)
        4. 📋 แนวทางปฏิบัติหน้างานเพื่อป้องกันตัวหากต้องเซ็นสัญญานี้
        """.trimIndent()
    }

    fun streamHighThinking(prompt: String): Flow<AiThinkingStreamEvent> {
        return deepThinkingModel.generateContentStream(prompt).let { flow ->
            kotlinx.coroutines.flow.flow {
                flow.collect { chunk ->
                    emit(
                        AiThinkingStreamEvent(
                            thoughtChunk = chunk.thoughtSummary.orEmpty(),
                            textChunk = chunk.text.orEmpty(),
                            origin = chunk.inferenceSource.toOrigin()
                        )
                    )
                }
            }
        }
    }

    suspend fun generateActionPack(
        situation: String,
        language: AppLanguage = AppLanguage.TH
    ): AiActionPackResult {
        val prompt = if (language == AppLanguage.EN) {
            """
            Generate an actionable construction manager survival pack in English for this situation: "$situation"
            1. Draft a formal, concise English SOS notice/script ready to send via email or messaging.
            2. Generate 2-3 Daily Site Checkpoints (with HH:mm timeSlot) to document evidence and mitigate LD/contract exposure.
            Respond strictly matching the JSON schema, with all script and task text in English.
            """.trimIndent()
        } else {
            """
            สร้างชุดเอาตัวรอดสำหรับผู้จัดการฝ่ายก่อสร้างในสถานการณ์: "$situation"
            1. ร่างสคริปต์ SOS ภาษาไทยที่เป็นทางการและรัดกุมเพื่อใช้ส่ง LINE หรือหนังสือราชการแจ้งผู้เกี่ยวข้อง
            2. สร้างรายการเช็กลิสต์หน้างาน (Daily Checkpoints) 2-3 ข้อเพื่อป้องกันความเสียหายจากสถานการณ์นี้
            """.trimIndent()
        }

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            structuredGeneratorModel.generateContent(prompt)
        }
        val jsonString = response.text ?: throw IllegalStateException(
            if (language == AppLanguage.EN) "Empty structured JSON response" else "ได้รับข้อมูลว่างจากโมเดล"
        )
        val json = JSONObject(jsonString)

        val scriptTitle = json.getString("scriptTitle")
        val scriptContent = json.getString("scriptContent")
        val scriptCategory = json.optString("scriptCategory", "CONTRACT")

        val tasksArray = json.getJSONArray("tasks")
        val defaultTaskDesc = if (language == AppLanguage.EN) {
            "Inspect and record timestamped site evidence"
        } else {
            "ตรวจสอบและบันทึกหลักฐานหน้างาน"
        }
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
                description = item.optString("description", defaultTaskDesc),
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

    fun createChatSession(language: AppLanguage = AppLanguage.TH): Chat {
        return if (language == AppLanguage.EN) {
            chatModelEn.startChat(
                history = listOf(
                    content("model") {
                        text("Hello, I am CAREER SHIELD AI — your construction contract & site risk advisor. Ask about LD mitigation, EOT notices, VO disputes, or subcontractor management.")
                    }
                )
            )
        } else {
            chatModelTh.startChat(
                history = listOf(
                    content("model") {
                        text("สวัสดีครับ ผมคือที่ปรึกษากฎหมายสัญญาก่อสร้างและผู้เชี่ยวชาญการบริหารความเสี่ยงหน้างาน (CAREER SHIELD AI) พร้อมช่วยวิเคราะห์ข้อสัญญาและปัญหาหน้างานครับ")
                    }
                )
            )
        }
    }

    suspend fun sendChatMessage(
        chat: Chat,
        messageText: String,
        language: AppLanguage = AppLanguage.TH
    ): AiChatResponse {
        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            chat.sendMessage(messageText)
        }
        val replyText = response.text?.takeIf { it.isNotBlank() }
            ?: if (language == AppLanguage.EN) {
                "Sorry, unable to generate a response right now."
            } else {
                "ขออภัยครับ ระบบไม่สามารถสร้างคำตอบได้ในขณะนี้"
            }
        val groundingMeta = response.candidates.firstOrNull()?.groundingMetadata
        val sources = groundingMeta?.groundingChunks
            .orEmpty()
            .mapNotNull { it.web }
            .map { web ->
                AiWebSource(
                    title = web.title ?: web.domain ?: if (language == AppLanguage.EN) "Reference" else "แหล่งอ้างอิง",
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

    suspend fun connectLiveVoice(language: AppLanguage = AppLanguage.TH): LiveSession {
        val model = if (language == AppLanguage.EN) liveVoiceModelEn else liveVoiceModelTh
        return withTimeout(LIVE_CONNECT_TIMEOUT_MS) {
            model.connect()
        }
    }

    fun buildDiagnosticPrompt(language: NanoDiagnosticLanguage): String = when (language) {
        NanoDiagnosticLanguage.ENGLISH ->
            "List 3 concise construction scaffolding fall-protection checks before workers climb."
        NanoDiagnosticLanguage.THAI ->
            "สรุป 3 ข้อตรวจสอบความปลอดภัยนั่งร้านก่อสร้างก่อนเริ่มงานอย่างกระชับ"
    }

    suspend fun checkOnDeviceCapability(language: AppLanguage = AppLanguage.TH): OnDeviceCapabilityState {
        val deviceSummary = "${Build.MANUFACTURER} ${Build.MODEL} (Android SDK ${Build.VERSION.SDK_INT})".trim()
        return try {
            val extension = nano4PreviewDiagnosticModel.onDeviceExtension
                ?: return OnDeviceCapabilityState(
                    statusLabel = if (language == AppLanguage.EN) {
                        "OnDeviceExtension unavailable (Using Cloud Fallback)"
                    } else {
                        "ไม่พบ OnDeviceExtension (ใช้ Cloud Fallback)"
                    },
                    isDownloadable = false,
                    isAvailable = false,
                    deviceSummary = deviceSummary
                )
            val status = extension.checkStatus()
            val detectedName = runCatching { extension.getModelName() }.getOrNull()?.takeIf { it.isNotBlank() }
            when (status) {
                OnDeviceModelStatus.AVAILABLE -> {
                    val label = if (language == AppLanguage.EN) {
                        if (detectedName != null) "AICore On-Device Ready ($detectedName)"
                        else "AICore On-Device Ready (Preview model name unverified)"
                    } else {
                        if (detectedName != null) "AICore On-Device พร้อมใช้งาน ($detectedName)"
                        else "AICore On-Device พร้อมใช้งาน (ยังไม่ได้ยืนยันชื่อโมเดล Preview)"
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
                    statusLabel = if (language == AppLanguage.EN) {
                        "On-Device Preview model downloadable"
                    } else {
                        "ดาวน์โหลดโมเดล On-Device Preview ได้"
                    },
                    isDownloadable = true,
                    isAvailable = false,
                    detectedModelName = detectedName,
                    deviceSummary = deviceSummary
                )
                OnDeviceModelStatus.DOWNLOADING -> OnDeviceCapabilityState(
                    statusLabel = if (language == AppLanguage.EN) {
                        "Downloading On-Device Preview model..."
                    } else {
                        "กำลังดาวน์โหลดโมเดล On-Device Preview..."
                    },
                    isDownloadable = false,
                    isAvailable = false,
                    detectedModelName = detectedName,
                    deviceSummary = deviceSummary
                )
                else -> OnDeviceCapabilityState(
                    statusLabel = if (language == AppLanguage.EN) {
                        "AICore Preview unsupported ($status) • Using Cloud Fallback"
                    } else {
                        "อุปกรณ์ไม่รองรับ AICore Preview ($status) • ใช้ Cloud Fallback"
                    },
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
                statusLabel = if (language == AppLanguage.EN) {
                    "AICore Preview unavailable (${e::class.simpleName ?: "Unavailable"}) • Cloud Fallback active"
                } else {
                    "AICore Preview ไม่พร้อมใช้งาน (${e::class.simpleName ?: "Unavailable"}) • ใช้ Cloud Fallback"
                },
                isDownloadable = false,
                isAvailable = false,
                deviceSummary = deviceSummary
            )
        }
    }

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
                    errorMessage = "On-device model returned an empty response.",
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

    fun buildHybridDefectTriagePrompt(
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String,
        language: AppLanguage = AppLanguage.TH
    ): String = if (language == AppLanguage.EN) {
        """
        You are NANO 4oK, a next-generation AI Home Inspector & Site Intelligence Assistant powered by Gemini Nano 4.
        Convert this quick field note into a concise, actionable defect card:
        - Floor / Zone: ${floorZone.ifBlank { "General Site Zone" }}
        - Trade / Scope: $tradeCategory
        - Opening / Element Code: ${openingCode.ifBlank { "N/A" }}
        - Measured Field Reading: ${measuredInput.ifBlank { "Not specified" }}
        - Inspector Note: "$inspectorNote"

        Respond concisely in English across 4 short lines:
        1. Severity: [CRITICAL / MAJOR / MINOR] + Defect Type (e.g. Plaster Crack / Structural Crack / Undersized Rough Opening / Safety Hazard)
        2. Responsible Party: [MASONRY / ALUMINUM / STRUCTURAL / MEP / CONTRACTOR]
        3. Action & Repair Spec: Exact practical rectification method and SLA deadline (e.g. 24h)
        4. Contractor Notice: 1-2 sentences ready to email or send to the sub-contractor and PM
        """.trimIndent()
    } else {
        """
        คุณคือ NANO 4oK ผู้ช่วย AI ตรวจบ้านและหน้างานอัจฉริยะ (Home Inspector & Site Intelligence)
        แปลงบันทึกสั้นๆ ของผู้ตรวจให้เป็นรายงานจุดเสีย (Defect Card) ที่ชัดเจนและสั่งแก้ได้ทันที:
        - ชั้น / โซน: ${floorZone.ifBlank { "พื้นที่หน้างานทั่วไป" }}
        - หมวดงาน: $tradeCategory
        - รหัสช่องเปิด / ตำแหน่ง: ${openingCode.ifBlank { "ไม่ระบุ" }}
        - ค่าวัดหน้างาน: ${measuredInput.ifBlank { "ไม่ได้กรอกระยะ" }}
        - บันทึกผู้ตรวจ: "$inspectorNote"

        สรุปกระชับ 4 ข้อเป็นภาษาไทย:
        1. ระดับความเสี่ยง: [CRITICAL / MAJOR / MINOR] + ประเภท Defect (เช่น รอยร้าวผิวฉาบ / รอยร้าวโครงสร้าง / ช่องเปิดแคบกว่าแบบ / ความปลอดภัย)
        2. ฝั่งที่รับผิดชอบ: [งานปูน (MASONRY) / อลูมิเนียม (ALUMINUM) / โครงสร้าง / MEP / ผู้รับเหมาหลัก]
        3. วิธีแก้ไขมาตรฐาน & กำหนดส่ง: แนวทางซ่อมที่ถูกต้องและระยะเวลาแก้ไข (เช่น 24 ชม.)
        4. ข้อความแจ้งผู้รับเหมา / PM: สรุป 1-2 ประโยคพร้อมส่งอีเมลทันที
        """.trimIndent()
    }

    suspend fun triageSiteWalkDefectHybrid(
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String,
        offlineOnly: Boolean,
        language: AppLanguage = AppLanguage.TH
    ): AiTextResponse {
        val prompt = buildHybridDefectTriagePrompt(
            floorZone = floorZone,
            tradeCategory = tradeCategory,
            openingCode = openingCode,
            measuredInput = measuredInput,
            inspectorNote = inspectorNote,
            language = language
        )
        return generateHybridText(
            prompt = prompt,
            offlineOnly = offlineOnly,
            language = language
        )
    }

    suspend fun comparePhotoWithPdfBlueprint(
        sitePhotoBitmap: Bitmap?,
        pdfPageBitmap: Bitmap?,
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String,
        language: AppLanguage = AppLanguage.TH
    ): AiTextResponse {
        require(sitePhotoBitmap != null || pdfPageBitmap != null) {
            if (language == AppLanguage.EN) {
                "Please attach a site photo or load an architectural PDF plan page first."
            } else {
                "กรุณาถ่ายภาพหน้างาน หรือเปิดไฟล์แบบแปลน PDF ก่อนวิเคราะห์เทียบแบบ"
            }
        }
        val model = if (language == AppLanguage.EN) {
            multimodalPhotoInspectorModelEn
        } else {
            multimodalPhotoInspectorModelTh
        }

        val promptText = if (language == AppLanguage.EN) {
            """
            You are NANO 4oK, an AI Home Inspector & Site Intelligence Specialist.
            ${if (pdfPageBitmap != null && sitePhotoBitmap != null) {
                "Image 1 is the Architectural PDF Plan / Door-Window Schedule. Image 2 is the actual jobsite photo (e.g. tape measure on door/window opening or observed defect)."
            } else if (pdfPageBitmap != null) {
                "The image provided is the Architectural PDF Plan / Door-Window Schedule."
            } else {
                "The image provided is the actual jobsite defect / measurement photo (no PDF blueprint page was attached)."
            }}

            Field Inspection Context:
            - Floor / Zone: ${floorZone.ifBlank { "General Inspection Zone" }}
            - Trade Category: $tradeCategory
            - Target Opening / Item Code: ${openingCode.ifBlank { "Inspect visible elements" }}
            - Manual Tape/Laser Reading Entered by Inspector: ${measuredInput.ifBlank { "Read directly from photo if visible" }}
            - Inspector Field Note: ${inspectorNote.ifBlank { "Verify dimensions against architectural spec and inspect for defects." }}

            Provide a structured English inspection report across 4 concise sections:
            1. 📐 Spec vs. Field Variance: State the drawing/PDF spec (if readable, e.g. width x height & tolerance) vs. measured site value and exact variance (e.g. -25 mm). If no PDF/reading is clear, state what visual evidence shows.
            2. 🚨 Severity & Diagnosis: [CRITICAL / MAJOR / MINOR] — distinguish plaster/cosmetic issues from structural cracks, waterproofing risks, or out-of-tolerance openings.
            3. ⚖️ Fault Allocation & Fix Method: Clarify clearly whether Masonry/Plasterer, Aluminum/Door installer, or Main Contractor must rectify and state the technical repair step.
            4. ✉️ Email-Ready Action Line: 2 crisp sentences ready for the Contractor & Project Manager.
            """.trimIndent()
        } else {
            """
            คุณคือ NANO 4oK ผู้เชี่ยวชาญ AI ตรวจบ้านและตรวจสอบช่องเปิดสถาปัตยกรรมเทียบแบบแปลน
            ${if (pdfPageBitmap != null && sitePhotoBitmap != null) {
                "ภาพที่ 1 คือแบบแปลน PDF / ตารางแบบขยายประตู-หน้าต่างของโครงการ และ ภาพที่ 2 คือภาพถ่ายจริงหน้างาน (เช่น ตลับเมตรวัดช่องเปิด หรือจุด Defect ที่พบ)"
            } else if (pdfPageBitmap != null) {
                "ภาพที่แนบคือแบบแปลน PDF / ตารางแบบขยายประตู-หน้าต่างของโครงการ"
            } else {
                "ภาพที่แนบคือภาพถ่ายจุด Defect / วัดระยะหน้างานจริง (ยังไม่ได้แนบหน้าแบบแปลน PDF)"
            }}

            ข้อมูลหน้างานจากผู้ตรวจ:
            - ชั้น / โซน: ${floorZone.ifBlank { "พื้นที่หน้างาน" }}
            - หมวดงาน: $tradeCategory
            - รหัสช่องเปิด / จุดอ้างอิง: ${openingCode.ifBlank { "ตรวจสอบตามภาพที่เห็น" }}
            - ค่าวัดจากตลับเมตร/เลเซอร์ที่ผู้ตรวจกรอก: ${measuredInput.ifBlank { "อ่านจากตัวเลขในภาพถ่าย (ถ้ามี)" }}
            - หมายเหตุผู้ตรวจ: ${inspectorNote.ifBlank { "ตรวจสอบความคลาดเคลื่อนเทียบแบบและวิเคราะห์วิธีแก้ไข" }}

            กรุณาสรุปรายงานเป็นภาษาไทย 4 หัวข้อกระชับชัดเจน:
            1. 📐 เทียบสเปกแบบ PDF กับหน้างานจริง: ระบุขนาดตามแบบ (ถ้าอ่านได้จากแบบแปลน) เทียบกับค่าวัดหน้างานและระยะคลาดเคลื่อน (เช่น แคบกว่าแบบกี่ มม./ซม.) หรือลักษณะความเสียหายที่เห็นในภาพ
            2. 🚨 ระดับความรุนแรง & การวินิจฉัย: [CRITICAL / MAJOR / MINOR] — แยกให้ชัดว่ากระทบโครงสร้าง/งานติดตั้งอลูมิเนียม/กันซึม หรือเป็นแค่ผิวงานฉาบ
            3. ⚖️ ชี้ผู้รับผิดชอบ & วิธีแก้ (จบข้อโต้แย้ง): ระบุชัดว่าฝั่งปูน (Masonry), ฝั่งอลูมิเนียม (Aluminum), หรือผู้รับเหมาหลัก ต้องแก้อย่างไรก่อนติดตั้งต่อ
            4. ✉️ ข้อความพร้อมส่งอีเมลแจ้งผู้รับเหมา & PM: สรุปสั้นๆ 2 ประโยคพร้อมสั่งงาน
            """.trimIndent()
        }

        val requestContent = content {
            if (pdfPageBitmap != null) {
                image(pdfPageBitmap)
            }
            if (sitePhotoBitmap != null) {
                image(sitePhotoBitmap)
            }
            text(promptText)
        }

        val response = withTimeout(REQUEST_TIMEOUT_MS) {
            model.generateContent(requestContent)
        }
        val analysis = response.text?.trim().orEmpty()
        if (analysis.isBlank()) {
            throw IllegalStateException(
                if (language == AppLanguage.EN) {
                    "Empty comparison response from Cloud Gemini."
                } else {
                    "โมเดลไม่ได้ส่งผลเปรียบเทียบกลับมา กรุณาลองอีกครั้ง"
                }
            )
        }
        return AiTextResponse(
            text = analysis,
            origin = response.inferenceSource.toOrigin()
        )
    }
}
