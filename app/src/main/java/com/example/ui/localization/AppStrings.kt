package com.example.ui.localization

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import com.example.data.ScriptEntity
import com.example.data.TaskEntity

enum class AppLanguage(val code: String, val shortBadge: String) {
    TH("th", "ไทย"),
    EN("en", "EN")
}

object AppLanguagePrefs {
    private const val PREF_NAME = "career_shield_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun loadLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        return when (saved) {
            AppLanguage.EN.code -> AppLanguage.EN
            AppLanguage.TH.code -> AppLanguage.TH
            else -> AppLanguage.EN
        }
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }
val LocalAppStrings = compositionLocalOf { appStrings(AppLanguage.EN) }

data class LocalizedStrings(
    val headerTitle: String,
    val headerSubtitle: String,
    val switchLanguageContentDesc: String,
    val tabChecklist: String,
    val tabSosScripts: String,
    val tabContractScanner: String,
    val tabAiShield: String,
    val btnCopy: String,
    val btnShare: String,
    val btnClose: String,
    val btnCancel: String,
    val btnSave: String,
    val toastCopied: String,
    val inferenceStandbyReady: String,
    val inferenceCloudFallbackLabel: String,
    val inferenceOnDeviceDefaultLabel: String,
    val inferenceStandbyPill: String,
    val stripOfflineLabel: String,
    val stripHybridLabel: String,
    val stripToggleOfflineDesc: String,
    val offlineToggleText: String,
    // App Check Troubleshooter
    val appCheckBannerTitle: String,
    val appCheckBannerBody: String,
    val appCheckTokenLabel: String,
    val appCheckTokenUnavailable: String,
    val appCheckCopyTokenBtn: String,
    val appCheckRefreshTokenBtn: String,
    val appCheckCopiedToast: String,
    // Task List Screen
    val roomDbStatusBadge: String,
    val resetDayButton: String,
    val todayProgressLabel: String,
    val dailyLogButton: String,
    val antiBsButton: String,
    val redFlagsTitle: String,
    val tapToConsultAi: String,
    val redFlag1: String,
    val redFlag2: String,
    val redFlag3: String,
    val redFlag1Prompt: String,
    val redFlag2Prompt: String,
    val redFlag3Prompt: String,
    val dailyCheckpointsTitle: String,
    val taskSearchPlaceholder: String,
    val categoryAll: String,
    val categorySafety: String,
    val categorySla: String,
    val categoryFinance: String,
    val categoryContract: String,
    val emptyFilteredTasks: String,
    val emptyNoTasks: String,
    val emptyTasksHint: String,
    val showAllTasksBtn: String,
    val addTaskButton: String,
    val savedInRoomBadge: String,
    val photoSavedRoomTitle: String,
    val photoTapToPreview: String,
    val scanPhotoAiBtn: String,
    val retakePhotoBtn: String,
    val removePhotoBtn: String,
    val captureSiteRiskPhotoBtn: String,
    val galleryBtn: String,
    val photoPreviewDialogTitle: String,
    val deletePhotoBtn: String,
    val addTaskDialogTitle: String,
    val editTaskDialogTitle: String,
    val saveChangesBtn: String,
    val saveToDeviceBtn: String,
    val dialogTimeLabel: String,
    val dialogCategoryLabel: String,
    val dialogTaskTitleLabel: String,
    val dialogTaskTitlePlaceholder: String,
    val dialogTaskDescLabel: String,
    val dialogTaskDescPlaceholder: String,
    val dialogPhotoSectionLabel: String,
    val dialogPhotoReady: String,
    val dialogCaptureBtn: String,
    // Script Screen
    val sosScriptsTitle: String,
    val sosScriptsSubtitle: String,
    val variableReplacerTitle: String,
    val recipientLabel: String,
    val clauseRfiLabel: String,
    val reasonZoneLabel: String,
    val aiCustomInstructionLabel: String,
    val addScriptButton: String,
    val tailorWithAiBtn: String,
    val tailoredResultHeader: String,
    val saveToRoomBtn: String,
    val disableOfflineUseCloudBtn: String,
    val addScriptDialogTitle: String,
    val newScriptTitleLabel: String,
    val newScriptContentLabel: String,
    // Contract Risk & Photo Scanner
    val contractScannerTitle: String,
    val contractScannerSubtitle: String,
    val scannerHeaderSubBadge: String,
    val scannerModeContract: String,
    val scannerModePhoto: String,
    val sampleClausesHeader: String,
    val clauseInputLabel: String,
    val scanModeQuickSearch: String,
    val scanModeHighThinking: String,
    val analyzeClauseBtn: String,
    val defaultClauseText: String,
    val defaultPhotoFocusNote: String,
    val offlineMultimodalNotice: String,
    val selectPhotoStepTitle: String,
    val captureSitePhotoBtn: String,
    val pickFromGalleryBtn: String,
    val roomEvidencePhotosHeader: String,
    val photoFocusInputLabel: String,
    val runPhotoInspectionBtn: String,
    val thoughtsToggleTitle: String,
    val groundingSourcesTitle: String,
    val saveToSosScriptsBtn: String,
    val askInAiChatBtn: String,
    val askShortChatBtn: String,
    // AI Advisor Screen
    val subTabHybridOps: String,
    val subTabChat: String,
    val subTabLiveVoice: String,
    val subTabHighThinking: String,
    val hybridCardTitle: String,
    val deviceStatusPrefix: String,
    val deviceHardwarePrefix: String,
    val downloadOnDeviceBtn: String,
    val nanoDiagnosticTitle: String,
    val nanoDiagnosticSubtitle: String,
    val nanoDiagnosticNote: String,
    val nanoDiagnosticEnBtn: String,
    val nanoDiagnosticThBtn: String,
    val siteOpsRoomCardTitle: String,
    val summarizeDailyLogBtn: String,
    val antiBsCheckBtn: String,
    val singleTurnQaTitle: String,
    val singleTurnQaPlaceholder: String,
    val safetyCardTitle: String,
    val safetyCardSubtitle: String,
    val safetyInputLabel: String,
    val analyzeSafetyBtn: String,
    val defaultSafetyHazardInput: String,
    val safetyResultHeader: String,
    val addToChecklistBtn: String,
    val saveToRiskReportBtn: String,
    val sanityCardTitle: String,
    val sanityCardSubtitle: String,
    val sanityPreset60Label: String,
    val sanityPreset60Area: String,
    val sanityPreset60Req: String,
    val sanityPresetLot3Label: String,
    val sanityPresetLot3Area: String,
    val sanityPresetLot3Req: String,
    val sanityAreaInputLabel: String,
    val sanityRequestInputLabel: String,
    val runSanityCheckBtn: String,
    val sanityBaselineRefPrefix: String,
    val exceptionNoteLabel: String,
    val exceptionNotePlaceholder: String,
    val approveExceptionBtn: String,
    val holdAuditBtn: String,
    val auditLogTitlePrefix: String,
    val auditLogSubtitle: String,
    val auditLogEmpty: String,
    val auditReasonPrefix: String,
    val auditFooterImmutable: String,
    val chatHeaderTitle: String,
    val chatInputPlaceholder: String,
    val chatBubbleUserLabel: String,
    val chatBubbleAiLabel: String,
    val liveVoiceTitle: String,
    val liveVoiceIdleDesc: String,
    val liveVoiceConnectingDesc: String,
    val liveVoiceActiveDesc: String,
    val liveVoiceStopBtn: String,
    val liveVoiceStartBtn: String,
    val liveVoiceUserTranscriptLabel: String,
    val liveVoiceModelTranscriptLabel: String,
    val highThinkingTitle: String,
    val highThinkingSubtitle: String,
    val crisisScenariosHeader: String,
    val crisisInputLabel: String,
    val defaultCrisisInput: String,
    val runHighThinkingBtn: String,
    val generateActionPackBtn: String,
    val highThinkingReasoningHeader: String,
    val highThinkingResultHeader: String,
    val actionPackHeader: String,
    val actionPackTasksHeader: String,
    val saveActionPackBtn: String,
    // Risk & Inspection Room Panel
    val riskReportsTitle: String,
    val riskReportsOpenCritical: String,
    val riskReportsSubDefault: String,
    val btnCreateReport: String,
    val reportZoneLabel: String,
    val reportHazardLabel: String,
    val reportMitigationLabel: String,
    val submitRiskReportBtn: String,
    val riskReportsEmpty: String,
    val mitigationPrefix: String,
    val sourcePrefix: String,
    val reopenCaseBtn: String,
    val closeMitigatedBtn: String,
    val stopWorkBtn: String,
    val checklistsTitle: String,
    val checklistsSubtitle: String,
    val btnCreateChecklist: String,
    val checklistTitleLabel: String,
    val checklistZoneLabel: String,
    val checklistItemsLabel: String,
    val defaultChecklistItemsText: String,
    val submitChecklistBtn: String,
    val passRateLabel: String
)

fun appStrings(lang: AppLanguage): LocalizedStrings = when (lang) {
    AppLanguage.TH -> LocalizedStrings(
        headerTitle = "CAREER SHIELD OS",
        headerSubtitle = "Hybrid AI (Nano 4 Preview Target + Cloud) • Room DB",
        switchLanguageContentDesc = "สลับภาษา TH / EN",
        tabChecklist = "เช็กลิสต์",
        tabSosScripts = "สคริปต์ SOS",
        tabContractScanner = "สแกน LD & รูป",
        tabAiShield = "Hybrid AI",
        btnCopy = "คัดลอก",
        btnShare = "แชร์ด่วน",
        btnClose = "ปิด",
        btnCancel = "ยกเลิก",
        btnSave = "บันทึก",
        toastCopied = "คัดลอกข้อความสำเร็จ!",
        inferenceStandbyReady = "พร้อมทำงาน",
        inferenceCloudFallbackLabel = "[Cloud Fallback: Gemini]",
        inferenceOnDeviceDefaultLabel = "[On-Device: AICore]",
        inferenceStandbyPill = "Hybrid: PREFER_ON_DEVICE (Preview)",
        stripOfflineLabel = "🚧 Offline Site (ONLY_ON_DEVICE • PREVIEW)",
        stripHybridLabel = "⚡ Hybrid (PREFER_ON_DEVICE • PREVIEW)",
        stripToggleOfflineDesc = "สลับโหมดทดสอบออฟไลน์หน้างาน",
        offlineToggleText = "Offline",
        appCheckBannerTitle = "วิธีแก้ 'Firebase App Check token is invalid' บนมือถือ (Debug APK)",
        appCheckBannerBody = "Debug APK บนเครื่องจริงสร้าง Debug Token เฉพาะเครื่องอัตโนมัติ คัดลอก UUID ด้านล่างไปเพิ่มใน Firebase Console → App Check → Manage debug tokens (หรือใช้ Release APK ที่ลงทะเบียน reCAPTCHA/Play Integrity):",
        appCheckTokenLabel = "Debug Token ของเครื่องนี้:",
        appCheckTokenUnavailable = "ยังไม่มี Token ในแคช กด 'ดึง Debug Token' เพื่อสร้าง/อ่านค่า",
        appCheckCopyTokenBtn = "คัดลอก Debug Token",
        appCheckRefreshTokenBtn = "ดึง Debug Token",
        appCheckCopiedToast = "คัดลอก App Check Debug Token แล้ว!",
        roomDbStatusBadge = "Room DB: เก็บข้อมูล & ภาพหน้างานในเครื่อง",
        resetDayButton = "เริ่มวันใหม่",
        todayProgressLabel = "ความคืบหน้าการป้องกันความเสี่ยงวันนี้",
        dailyLogButton = "สรุป Log ประจำวัน",
        antiBsButton = "ตรวจ Anti-BS ซ้ำซ้อน",
        redFlagsTitle = "⚠️ 3 จุดตายคอขาดบาดตาย",
        tapToConsultAi = "แตะเพื่อปรึกษา AI",
        redFlag1 = "1. ผู้รับเหมาส่งสัญญาณทิ้งงาน (คนลดลง)",
        redFlag2 = "2. คำสั่งแก้งานปากเปล่า (ห้ามทำถ้าไม่มีลายเซ็น)",
        redFlag3 = "3. ค่าปรับ LD วันละ 1.5 แสน (ต้องยิงหนังสือ EOT)",
        redFlag1Prompt = "ผู้รับเหมาช่วงส่งสัญญาณทิ้งงาน จำนวนช่างลดลงต่อเนื่องเกิน 3 วัน และงานล่าช้ากว่าแผนหลัก ต้องล็อกสัญญาและตัดจ่ายเงินอย่างไรไม่ให้โครงการเสียหาย?",
        redFlag2Prompt = "ผู้ว่าจ้างหรือตัวแทนสั่งแก้งานหน้างานปากเปล่าเร่งด่วนโดยยังไม่มีเอกสาร VO และไม่มีลายเซ็นอนุมัติงบ/เวลาเพิ่ม ต้องรับมือและออกหนังสืออย่างไร?",
        redFlag3Prompt = "โครงการเสี่ยงโดนค่าปรับล่าช้า (LD) วันละ 150,000 บาท เนื่องจากส่งมอบพื้นที่ล่าช้าและฝนตกหนัก ต้องทำหนังสือสงวนสิทธิ์ขอขยายเวลา (EOT) และเก็บหลักฐานอย่างไร?",
        dailyCheckpointsTitle = "ภารกิจประจำวัน (Daily Action Checkpoints)",
        taskSearchPlaceholder = "ค้นหาชื่อภารกิจ หรือรายละเอียด (เช่น เทปูน, สต็อก, Cashflow)...",
        categoryAll = "ทั้งหมด",
        categorySafety = "SAFETY (สต็อก/หน้างาน)",
        categorySla = "SLA (ล็อกเวลา)",
        categoryFinance = "FINANCE (การเงิน)",
        categoryContract = "CONTRACT (สัญญา)",
        emptyFilteredTasks = "ไม่พบภารกิจที่ตรงกับการค้นหาหรือหมวดหมู่ที่เลือก",
        emptyNoTasks = "ยังไม่มีภารกิจในระบบ",
        emptyTasksHint = "ลองเปลี่ยนคำค้นหา หมวดหมู่ หรือกดปุ่ม '+ เพิ่มงานใหม่' ด้านล่าง",
        showAllTasksBtn = "แสดงภารกิจทั้งหมด",
        addTaskButton = "เพิ่มงานใหม่",
        savedInRoomBadge = "✓ บันทึกสำเร็จใน Room DB",
        photoSavedRoomTitle = "บันทึกภาพถ่ายหน้างานลง Room DB แล้ว",
        photoTapToPreview = "แตะเพื่อดูภาพขนาดเต็ม หรือถ่ายใหม่",
        scanPhotoAiBtn = "สแกนรูป AI",
        retakePhotoBtn = "ถ่ายใหม่",
        removePhotoBtn = "ลบรูป",
        captureSiteRiskPhotoBtn = "ถ่ายรูปจุดเสี่ยงหน้างาน",
        galleryBtn = "คลังภาพ",
        photoPreviewDialogTitle = "📷 หลักฐานภาพถ่ายจุดเสี่ยงหน้างาน",
        deletePhotoBtn = "ลบรูปภาพ",
        addTaskDialogTitle = "เพิ่มภารกิจใหม่ลง Room DB",
        editTaskDialogTitle = "แก้ไขภารกิจใน Room DB",
        saveChangesBtn = "บันทึกการแก้ไข",
        saveToDeviceBtn = "บันทึกลงเครื่อง",
        dialogTimeLabel = "เลือกเวลาแจ้งเตือนหน้างาน:",
        dialogCategoryLabel = "หมวดหมู่ความเสี่ยง:",
        dialogTaskTitleLabel = "ชื่อภารกิจเช็กลิสต์",
        dialogTaskTitlePlaceholder = "เช่น ตรวจระดับหัวเสาเข็มก่อนเทฟุตติ้ง",
        dialogTaskDescLabel = "รายละเอียดการป้องกันปัญหา",
        dialogTaskDescPlaceholder = "เช่น ถ่ายรูป Timestamp Camera พร้อมให้โฟร์แมนเซ็นรับทราบ",
        dialogPhotoSectionLabel = "รูปถ่ายหลักฐานความเสี่ยงหน้างาน (บันทึกลง Room DB):",
        dialogPhotoReady = "พร้อมบันทึกรูปลง Room DB",
        dialogCaptureBtn = "ถ่ายรูป",
        sosScriptsTitle = "ปุ่มลัดก๊อปปี้สคริปต์เอาตัวรอด (SOS Scripts)",
        sosScriptsSubtitle = "คัดลอกข้อความส่ง LINE หรือให้ Hybrid AI (AICore Preview On-Device / Cloud Fallback) ช่วยเกลาภาษาให้รัดกุมก่อนส่งทันที",
        variableReplacerTitle = "ตัวช่วยเติมข้อมูล & บริบทสำหรับ Hybrid AI",
        recipientLabel = "ชื่อผู้ว่าจ้าง/บริษัท",
        clauseRfiLabel = "ข้อสัญญา/เลขที่ RFI",
        reasonZoneLabel = "สาเหตุ/โซนหน้างาน (เช่น ฝนตกหนักน้ำท่วมขังบ่อฐานราก โซน B)",
        aiCustomInstructionLabel = "คำสั่งเพิ่มเติมให้ Hybrid AI (เช่น ขอสงวนสิทธิ์ EOT 5 วัน + แนบรูปถ่าย)",
        addScriptButton = "เพิ่มสคริปต์",
        tailorWithAiBtn = "ปรับด้วย AI",
        tailoredResultHeader = "✨ สคริปต์ปรับปรุงพร้อมส่ง:",
        saveToRoomBtn = "เก็บลง Room",
        disableOfflineUseCloudBtn = "ปิด Offline Mode เพื่อใช้ Cloud Gemini Fallback",
        addScriptDialogTitle = "เพิ่มสคริปต์ SOS ลง Room DB",
        newScriptTitleLabel = "หัวข้อสคริปต์เอาตัวรอด",
        newScriptContentLabel = "เนื้อหาสคริปต์",
        contractScannerTitle = "LD & Site Photo Scanner (Cloud Gemini Multimodal)",
        contractScannerSubtitle = "สแกนความเสี่ยงข้อสัญญาก่อสร้าง ค่าปรับ LD และตรวจวิเคราะห์ภาพถ่ายจุดเสี่ยงหน้างานผ่าน Cloud Gemini",
        scannerHeaderSubBadge = "Cloud Fallback Route • Multimodal Photo & Legal Search Grounding",
        scannerModeContract = "⚖️ สแกนสัญญา LD",
        scannerModePhoto = "📷 ตรวจรูปหน้างาน",
        sampleClausesHeader = "ตัวอย่างข้อสัญญาที่มีความเสี่ยงสูง (แตะเพื่อทดสอบ):",
        clauseInputLabel = "วางข้อความข้อสัญญาก่อสร้าง (Contract Clause) ที่ต้องการตรวจสอบ",
        scanModeQuickSearch = "⚡ สแกนด่วน + Google Search",
        scanModeHighThinking = "🧠 คิดเชิงลึก (High Thinking)",
        analyzeClauseBtn = "สแกนความเสี่ยงกฎหมาย & ค่าปรับ LD ทันที",
        defaultClauseText = "ข้อ 14. ค่าปรับกรณีงานล่าช้า (Liquidated Damages): หากผู้รับจ้างส่งมอบงานล่าช้ากว่ากำหนดในสัญญา ผู้รับจ้างยินยอมให้ผู้ว่าจ้างปรับเป็นรายวันในอัตราวันละ 0.25% ของมูลค่างานตามสัญญาทั้งหมด (คิดเป็นวันละ 150,000 บาท) โดยไม่มีเพดานสูงสุด และผู้รับจ้างตกลงสละสิทธิ์การขอขยายเวลาก่อสร้าง (EOT) จากเหตุฝนตกหรือการส่งมอบพื้นที่ล่าช้าทุกกรณี",
        defaultPhotoFocusNote = "ตรวจความเสี่ยงความปลอดภัย โครงสร้างเหล็ก/คอนกรีต และหลักฐานเพื่อขอ VO หรือป้องกันค่าปรับ LD",
        offlineMultimodalNotice = "ℹ️ งานวิเคราะห์ภาพถ่าย Multimodal ใช้ Cloud Fallback: Gemini (อัตโนมัติ) เพื่อความแม่นยำในการตรวจจับความเสี่ยงหน้างาน",
        selectPhotoStepTitle = "1. เลือกภาพถ่ายจุดเสี่ยงหน้างาน (ถ่ายใหม่ / คลังภาพ / จาก Room DB):",
        captureSitePhotoBtn = "ถ่ายรูปหน้างาน",
        pickFromGalleryBtn = "เลือกจากคลัง",
        roomEvidencePhotosHeader = "รูปภาพหลักฐานที่บันทึกไว้ใน Room DB",
        photoFocusInputLabel = "จุดที่ต้องการให้ AI ตรวจสอบในภาพ",
        runPhotoInspectionBtn = "วิเคราะห์ภาพจุดเสี่ยง (Cloud Gemini Multimodal)",
        thoughtsToggleTitle = "🧠 กระบวนการคิดเชิงลึก (High Thinking Reasoning)",
        groundingSourcesTitle = "อ้างอิงข้อมูลล่าสุดผ่าน Google Search Grounding",
        saveToSosScriptsBtn = "เก็บลงสคริปต์ SOS",
        askInAiChatBtn = "ถามต่อในแชท AI",
        askShortChatBtn = "ถามต่อในแชท",
        subTabHybridOps = "Hybrid Ops (Preview)",
        subTabChat = "แชทบอท AI",
        subTabLiveVoice = "คุยเสียงสด (Live)",
        subTabHighThinking = "High Thinking",
        hybridCardTitle = "Firebase AI Logic Hybrid Inference",
        deviceStatusPrefix = "สถานะเครื่อง:",
        deviceHardwarePrefix = "อุปกรณ์:",
        downloadOnDeviceBtn = "ดาวน์โหลดโมเดล On-Device Preview",
        nanoDiagnosticTitle = "Nano 4 Full Preview Diagnostic (ONLY_ON_DEVICE)",
        nanoDiagnosticSubtitle = "Target: OnDeviceModelOption.PREVIEW (nano-v4-full / Gemma 4 E4B) • maxOutputTokens=256 • ไม่สลับไป Cloud",
        nanoDiagnosticNote = "หมายเหตุเอกสาร Google: AICore Developer Preview รองรับอุปกรณ์และโปรแกรมที่ลงทะเบียน (EN/KO ได้รับการรับรองอย่างเป็นทางการ; ทดสอบ TH แยกเพื่อสังเกตผลจริง)",
        nanoDiagnosticEnBtn = "Test EN (On-Device)",
        nanoDiagnosticThBtn = "ทดสอบ TH",
        siteOpsRoomCardTitle = "งานหน้างานแบบ On-Device / Hybrid (จาก Room DB จริง)",
        summarizeDailyLogBtn = "สรุป Daily Log",
        antiBsCheckBtn = "ตรวจ Anti-BS",
        singleTurnQaTitle = "ถาม-ตอบด่วนหน้างาน (Single-Turn Hybrid Q&A):",
        singleTurnQaPlaceholder = "เช่น เช็กอะไรก่อนเทคอนกรีตฐานราก?",
        safetyCardTitle = "วิเคราะห์ความเสี่ยงปลอดภัยหน้างาน (Site Safety AI)",
        safetyCardSubtitle = "ประเมินอันตราย กฎกระทรวง 2564, PPE และมาตรการ Stop-Work จากข้อความ",
        safetyInputLabel = "ระบุสภาพหน้างาน / จุดเสี่ยงอุบัติเหตุที่พบ",
        analyzeSafetyBtn = "วิเคราะห์ความเสี่ยงปลอดภัย (Hybrid AI)",
        defaultSafetyHazardInput = "ช่างติดตั้งนั่งร้านชั้น 5 สูง 14 เมตร ไม่มีราวกันตกและคนงานไม่สวม Safety Harness",
        safetyResultHeader = "🦺 ผลการประเมินความเสี่ยงปลอดภัยหน้างาน",
        addToChecklistBtn = "เพิ่มเข้าเช็กลิสต์",
        saveToRiskReportBtn = "บันทึกลง Risk Report (Room v4)",
        sanityCardTitle = "ตรวจใบขอเบิกวัสดุ & ราคาผิดปกติ (Sanity Check)",
        sanityCardSubtitle = "เทียบเกณฑ์กลางอ่านอย่างเดียว • ข้อมูลชั่วคราวไม่ทับมาตรฐาน",
        sanityPreset60Label = "เคส 60 ตร.ม. (ปูนกาว 40 ถุง / กระเบื้อง 95 ตร.ม.)",
        sanityPreset60Area = "ชั้น 1 (พื้นที่ 60 ตร.ม.)",
        sanityPreset60Req = "ขอเบิกกระเบื้อง 95 ตร.ม. และปูนกาว 40 ถุง สำหรับปูพื้นชั้น 1",
        sanityPresetLot3Label = "เคส Lot 3 ราคาสูงกว่าปกติ (+22% งานเร่งด่วน)",
        sanityPresetLot3Area = "Lot 3 งานกระจกและผนังเบา",
        sanityPresetLot3Req = "สั่งซื้อกระจก Tempered 12mm Lot 3 ราคาสูงกว่างบกลาง +22% เพื่อส่งมอบทันงวดงาน",
        sanityAreaInputLabel = "พื้นที่ / Lot งานที่ขอเบิก (เช่น ชั้น 1 พื้นที่ 60 ตร.ม.)",
        sanityRequestInputLabel = "รายละเอียดใบขอเบิกวัสดุ/ราคาที่ลูกน้องส่งมา (ตรวจชั่วคราว)",
        runSanityCheckBtn = "ตรวจความผิดปกติเทียบเกณฑ์กลาง (Hybrid AI)",
        sanityBaselineRefPrefix = "🔒 เกณฑ์อ้างอิง:",
        exceptionNoteLabel = "หมายเหตุโปร่งใสของผู้จัดการ (เหตุผลที่อนุมัติแพงขึ้น / ระงับตรวจ)",
        exceptionNotePlaceholder = "เช่น Lot 3 ราคาสูงกว่าเกณฑ์ +22% แต่ต้องสั่งด่วนเพื่อส่งมอบทันงวด",
        approveExceptionBtn = "อนุมัติข้อยกเว้น",
        holdAuditBtn = "ระงับ/เรียกชี้แจง",
        auditLogTitlePrefix = "บันทึกหลักฐานข้อยกเว้น (Exception Audit Log:",
        auditLogSubtitle = "เก็บหลักฐานเหตุผลที่อนุมัติหรือระงับเฉพาะ Lot/เคส โดยไม่ไปแก้เกณฑ์มาตรฐานกลาง",
        auditLogEmpty = "ยังไม่มีบันทึกข้อยกเว้น — กด \"ตรวจความผิดปกติเทียบเกณฑ์กลาง\" แล้วเลือกอนุมัติข้อยกเว้นหรือระงับเพื่อเก็บหลักฐานโปร่งใส",
        auditReasonPrefix = "เหตุผล:",
        auditFooterImmutable = "ไม่ทับเกณฑ์มาตรฐานกลาง",
        chatHeaderTitle = "💬 แชทบอทที่ปรึกษากฎหมายก่อสร้าง (Cloud Gemini + Google Search)",
        chatInputPlaceholder = "ถามข้อกฎหมายก่อสร้าง ค่าปรับ LD หรือวิธีเจรจา...",
        chatBubbleUserLabel = "ผู้จัดการฝ่ายก่อสร้าง (คุณ)",
        chatBubbleAiLabel = "🛡️ CAREER SHIELD AI",
        liveVoiceTitle = "สนทนาเสียงสดหน้างาน (Gemini 3.8 Live API)",
        liveVoiceIdleDesc = "กดปุ่มด้านล่างเพื่อพูดปรึกษาปัญหาหน้างานแบบ Hands-Free ขณะเดินตรวจไซต์งาน",
        liveVoiceConnectingDesc = "กำลังเชื่อมต่อเซสชันเสียงสด Gemini 3.8 Live...",
        liveVoiceActiveDesc = "🟢 กำลังสนทนาสด — พูดถามเรื่องค่าปรับ LD สัญญา หรือวิธีรับมือผู้รับเหมาได้เลย",
        liveVoiceStopBtn = "วางสาย / หยุดสนทนาเสียงสด",
        liveVoiceStartBtn = "เริ่มคุยเสียงสดกับ Gemini 3.8 Live",
        liveVoiceUserTranscriptLabel = "🎙️ เสียงของคุณ (Live Input Transcript):",
        liveVoiceModelTranscriptLabel = "🔊 คำตอบจากที่ปรึกษา AI (Live Audio Transcript):",
        highThinkingTitle = "ที่ปรึกษาอาวุโส Hybrid AI (On-Device Preview + Cloud)",
        highThinkingSubtitle = "ตั้งค่าเป้าหมาย OnDeviceModelOption.PREVIEW (PREFER_ON_DEVICE) พร้อมสลับใช้ Cloud Gemini 3.8 Flash / 4 Carbon / 3.8 Live อัตโนมัติ",
        crisisScenariosHeader = "เลือกสถานการณ์วิกฤตหน้างาน หรือพิมพ์ปัญหาของคุณ:",
        crisisInputLabel = "รายละเอียดปัญหาหน้างาน / ข้อพิพาทสัญญา",
        defaultCrisisInput = "ผู้ว่าจ้างสั่งรื้อแก้ไขผนังก่ออิฐมวลเบาชั้น 4 ปากเปล่าโดยไม่ออกเอกสาร VO และเร่งให้เสร็จก่อนตรวจงวดงานวันศุกร์",
        runHighThinkingBtn = "วิเคราะห์เชิงลึก (High Thinking)",
        generateActionPackBtn = "สร้างสคริปต์+เช็กลิสต์",
        highThinkingReasoningHeader = "🧠 กระบวนการคิดเชิงลึก (Internal Reasoning)",
        highThinkingResultHeader = "✅ แผนป้องกันตัวและข้อเสนอแนะทางสัญญา",
        actionPackHeader = "✨ ชุดสคริปต์และเช็กลิสต์พร้อมบันทึกลงฐานข้อมูล Room DB",
        actionPackTasksHeader = "ภารกิจเช็กลิสต์ที่แนะนำ",
        saveActionPackBtn = "บันทึกทั้งหมดลงฐานข้อมูล Room DB ทันที",
        riskReportsTitle = "ฐานข้อมูลรายงานความเสี่ยง (Risk Reports:",
        riskReportsOpenCritical = "⚠️ วิกฤตเปิดอยู่ %d รายการ (Room v4)",
        riskReportsSubDefault = "จัดเก็บถาวรใน SQLite • risk_assessment_reports",
        btnCreateReport = "สร้างรายงาน",
        reportZoneLabel = "โซน / ชั้นหน้างาน (Site Zone)",
        reportHazardLabel = "ความเสี่ยงที่ตรวจพบ (Hazard Summary)",
        reportMitigationLabel = "แผนแก้ไข / มาตรการป้องกัน",
        submitRiskReportBtn = "บันทึกรายงานความเสี่ยงลง Room DB",
        riskReportsEmpty = "ยังไม่มีรายงานประเมินความเสี่ยงที่บันทึกไว้ — กด \"สร้างรายงาน\" หรือบันทึกจากผลวิเคราะห์ Safety AI ด้านบนได้ทันที",
        mitigationPrefix = "แผนแก้ไข:",
        sourcePrefix = "แหล่งข้อมูล:",
        reopenCaseBtn = "เปิดเคสใหม่",
        closeMitigatedBtn = "ปิดเคส (แก้ไขแล้ว)",
        stopWorkBtn = "สั่ง Stop-Work",
        checklistsTitle = "เช็กลิสต์ตรวจรับหน้างาน (Checklists:",
        checklistsSubtitle = "Relational Schema • inspection_checklists -> checklist_items",
        btnCreateChecklist = "สร้างชุดตรวจ",
        checklistTitleLabel = "ชื่อชุดเช็กลิสต์ตรวจรับงาน",
        checklistZoneLabel = "โซน / ชั้นหน้างาน",
        checklistItemsLabel = "รายการตรวจ (1 บรรทัด = 1 หัวข้อ)",
        defaultChecklistItemsText = "ตรวจสายรัดนิรภัย 100%\nตรวจป้าย Tag นั่งร้าน\nตรวจพื้นที่กั้นเขตอันตราย",
        submitChecklistBtn = "บันทึกชุดเช็กลิสต์ลง Room DB",
        passRateLabel = "PASS"
    )

    AppLanguage.EN -> LocalizedStrings(
        headerTitle = "CAREER SHIELD OS",
        headerSubtitle = "Hybrid AI (Nano 4 Preview Target + Cloud) • Room DB",
        switchLanguageContentDesc = "Switch language TH / EN",
        tabChecklist = "Checklist",
        tabSosScripts = "SOS Scripts",
        tabContractScanner = "LD & Photo",
        tabAiShield = "Hybrid AI",
        btnCopy = "Copy",
        btnShare = "Share",
        btnClose = "Close",
        btnCancel = "Cancel",
        btnSave = "Save",
        toastCopied = "Copied to clipboard!",
        inferenceStandbyReady = "Ready",
        inferenceCloudFallbackLabel = "[Cloud Fallback: Gemini]",
        inferenceOnDeviceDefaultLabel = "[On-Device: AICore]",
        inferenceStandbyPill = "Hybrid: PREFER_ON_DEVICE (Preview)",
        stripOfflineLabel = "🚧 Offline Site (ONLY_ON_DEVICE • PREVIEW)",
        stripHybridLabel = "⚡ Hybrid (PREFER_ON_DEVICE • PREVIEW)",
        stripToggleOfflineDesc = "Toggle Offline Site Test Mode",
        offlineToggleText = "Offline",
        appCheckBannerTitle = "How to fix 'Firebase App Check token is invalid' on Debug APK",
        appCheckBannerBody = "Sideloaded Debug APKs generate a unique device Debug UUID. Copy the UUID below and register it in Firebase Console → App Check → Manage debug tokens (or build a Release APK with reCAPTCHA/Play Integrity):",
        appCheckTokenLabel = "Device Debug Token UUID:",
        appCheckTokenUnavailable = "No cached UUID yet. Tap 'Get Debug Token' to generate/read it.",
        appCheckCopyTokenBtn = "Copy Debug Token",
        appCheckRefreshTokenBtn = "Get Debug Token",
        appCheckCopiedToast = "Copied App Check Debug Token!",
        roomDbStatusBadge = "Room DB: Local SQLite & Site Photo Storage",
        resetDayButton = "Reset Day",
        todayProgressLabel = "Today's Risk Shield Progress",
        dailyLogButton = "Daily Log Summary",
        antiBsButton = "Anti-BS Audit",
        redFlagsTitle = "⚠️ 3 Critical Survival Red Flags",
        tapToConsultAi = "Tap to ask AI",
        redFlag1 = "1. Subcontractor abandoning site (manpower drop)",
        redFlag2 = "2. Verbal change order (never work without signed VO)",
        redFlag3 = "3. Liquidated Damages 150k/day (issue EOT notice)",
        redFlag1Prompt = "Our subcontractor is showing signs of abandoning the site: daily crew count dropped over 3 consecutive days and critical path works are delayed. How should we lock contract rights and withhold progress payments safely?",
        redFlag2Prompt = "The client or site representative verbally ordered urgent rework without an approved Variation Order (VO) or budget/time signature. How should we respond and issue a formal written notice immediately?",
        redFlag3Prompt = "The project faces Liquidated Damages (LD) of 150,000 THB/day due to late site handover and severe rain delays. How should we issue an Extension of Time (EOT) reservation notice and preserve timestamped evidence?",
        dailyCheckpointsTitle = "Daily Action Checkpoints",
        taskSearchPlaceholder = "Search tasks or details (e.g., Concrete, Stock, Cashflow)...",
        categoryAll = "ALL",
        categorySafety = "SAFETY (Site/Stock)",
        categorySla = "SLA (4h Lock)",
        categoryFinance = "FINANCE (Cashflow)",
        categoryContract = "CONTRACT (VO/EOT)",
        emptyFilteredTasks = "No checkpoints match your search or category filter",
        emptyNoTasks = "No checkpoints in Room DB yet",
        emptyTasksHint = "Try clearing filters or tap '+ Add Task' below",
        showAllTasksBtn = "Show All Tasks",
        addTaskButton = "Add Task",
        savedInRoomBadge = "✓ Saved in Room DB",
        photoSavedRoomTitle = "Site photo evidence saved in Room DB",
        photoTapToPreview = "Tap to preview full size or retake",
        scanPhotoAiBtn = "AI Scan",
        retakePhotoBtn = "Retake",
        removePhotoBtn = "Remove",
        captureSiteRiskPhotoBtn = "Capture Risk Photo",
        galleryBtn = "Gallery",
        photoPreviewDialogTitle = "📷 Site Risk Photo Evidence",
        deletePhotoBtn = "Delete Photo",
        addTaskDialogTitle = "Add Checkpoint to Room DB",
        editTaskDialogTitle = "Edit Checkpoint in Room DB",
        saveChangesBtn = "Save Changes",
        saveToDeviceBtn = "Save Locally",
        dialogTimeLabel = "Select scheduled site reminder time:",
        dialogCategoryLabel = "Risk category:",
        dialogTaskTitleLabel = "Checkpoint title",
        dialogTaskTitlePlaceholder = "e.g. Inspect pile cut-off elevation before footing pour",
        dialogTaskDescLabel = "Risk mitigation details",
        dialogTaskDescPlaceholder = "e.g. Take timestamped GPS photo and obtain foreman sign-off",
        dialogPhotoSectionLabel = "Site risk evidence photo (stored in Room DB):",
        dialogPhotoReady = "Ready to save photo to Room DB",
        dialogCaptureBtn = "Camera",
        sosScriptsTitle = "One-Tap Survival Scripts (SOS Scripts)",
        sosScriptsSubtitle = "Copy ready-to-send notices or let Hybrid AI (AICore Preview On-Device / Cloud Fallback) tailor formal wording for your situation.",
        variableReplacerTitle = "Auto-Fill Variables & Hybrid AI Context",
        recipientLabel = "Client / Company Name",
        clauseRfiLabel = "Clause / RFI No.",
        reasonZoneLabel = "Cause / Site Zone (e.g. Heavy rain flooding Zone B footing)",
        aiCustomInstructionLabel = "Extra instructions for Hybrid AI (e.g. Reserve 5-day EOT + attach photos)",
        addScriptButton = "Add Script",
        tailorWithAiBtn = "Tailor w/ AI",
        tailoredResultHeader = "✨ Tailored Ready-to-Send Notice:",
        saveToRoomBtn = "Save to Room",
        disableOfflineUseCloudBtn = "Disable Offline Mode to use Cloud Gemini Fallback",
        addScriptDialogTitle = "Add SOS Script to Room DB",
        newScriptTitleLabel = "Script title",
        newScriptContentLabel = "Script body",
        contractScannerTitle = "LD & Site Photo Scanner (Cloud Gemini Multimodal)",
        contractScannerSubtitle = "Scan construction contract clauses for LD exposure and inspect site hazard photos with Cloud Gemini",
        scannerHeaderSubBadge = "Cloud Fallback Route • Multimodal Photo & Legal Search Grounding",
        scannerModeContract = "⚖️ Scan LD Clause",
        scannerModePhoto = "📷 Inspect Site Photo",
        sampleClausesHeader = "High-risk contract clause presets (tap to test):",
        clauseInputLabel = "Paste construction contract clause to audit",
        scanModeQuickSearch = "⚡ Quick Scan + Google Search",
        scanModeHighThinking = "🧠 Deep Reasoning (High Thinking)",
        analyzeClauseBtn = "Scan Legal & LD Exposure Now",
        defaultClauseText = "Clause 14. Liquidated Damages (LD): If the Contractor fails to complete the Works by the Time for Completion, the Contractor shall pay Liquidated Damages at 0.25% of the total Contract Price per day (150,000 THB/day) with no maximum cap, and the Contractor waives all rights to claim any Extension of Time (EOT) arising from inclement weather or late site handover.",
        defaultPhotoFocusNote = "Inspect structural/rebar/concrete quality, OSH fall hazards, and documentation needed for VO claims or LD defense",
        offlineMultimodalNotice = "ℹ️ Multimodal photo inspection automatically routes to Cloud Gemini for high-accuracy visual hazard detection.",
        selectPhotoStepTitle = "1. Select Site Risk Photo (Camera / Gallery / Room DB):",
        captureSitePhotoBtn = "Capture Photo",
        pickFromGalleryBtn = "Pick Gallery",
        roomEvidencePhotosHeader = "Evidence photos stored in Room DB",
        photoFocusInputLabel = "Inspection focus areas for AI",
        runPhotoInspectionBtn = "Inspect Site Photo (Cloud Gemini Multimodal)",
        thoughtsToggleTitle = "🧠 Deep Reasoning Trace (High Thinking)",
        groundingSourcesTitle = "Verified via Google Search Grounding",
        saveToSosScriptsBtn = "Save as SOS Script",
        askInAiChatBtn = "Continue in AI Chat",
        askShortChatBtn = "Ask in Chat",
        subTabHybridOps = "Hybrid Ops (Preview)",
        subTabChat = "AI Chat",
        subTabLiveVoice = "Live Voice",
        subTabHighThinking = "High Thinking",
        hybridCardTitle = "Firebase AI Logic Hybrid Inference",
        deviceStatusPrefix = "Device Status:",
        deviceHardwarePrefix = "Hardware:",
        downloadOnDeviceBtn = "Download On-Device Preview Model",
        nanoDiagnosticTitle = "Nano 4 Full Preview Diagnostic (ONLY_ON_DEVICE)",
        nanoDiagnosticSubtitle = "Target: OnDeviceModelOption.PREVIEW (nano-v4-full / Gemma 4 E4B) • maxOutputTokens=256 • No Cloud Fallback",
        nanoDiagnosticNote = "Google Docs Note: AICore Developer Preview supports registered devices/programs (EN/KO officially supported; TH tested separately for observation).",
        nanoDiagnosticEnBtn = "Test EN (On-Device)",
        nanoDiagnosticThBtn = "Test TH (On-Device)",
        siteOpsRoomCardTitle = "On-Device / Hybrid Site Ops (Live Room DB Data)",
        summarizeDailyLogBtn = "Daily Log Summary",
        antiBsCheckBtn = "Anti-BS Check",
        singleTurnQaTitle = "Quick Site Q&A (Single-Turn Hybrid Q&A):",
        singleTurnQaPlaceholder = "e.g. Pre-pour checklist before foundation concrete?",
        safetyCardTitle = "Site Safety Risk Analyzer (Site Safety AI)",
        safetyCardSubtitle = "Assess OSH hazards, Ministry Regulation B.E. 2564, PPE & Stop-Work controls",
        safetyInputLabel = "Describe site conditions / observed safety hazards",
        analyzeSafetyBtn = "Analyze Site Safety Risk (Hybrid AI)",
        defaultSafetyHazardInput = "Workers erecting 14m Level-5 scaffolding with missing guardrails and no Safety Harness tie-off",
        safetyResultHeader = "🦺 Site Safety Risk Assessment",
        addToChecklistBtn = "Add to Checklist",
        saveToRiskReportBtn = "Save to Risk Report (Room v4)",
        sanityCardTitle = "Material Requisition & Price Sanity Check",
        sanityCardSubtitle = "Compared against Read-Only Certified Baseline • Ephemeral input never mutates norms",
        sanityPreset60Label = "60 sqm Case (40 Adhesive Bags / 95 sqm Tiles)",
        sanityPreset60Area = "Level 1 (60 sqm floor area)",
        sanityPreset60Req = "Requisition for 95 sqm ceramic tiles and 40 bags (20kg) tile adhesive for Level 1",
        sanityPresetLot3Label = "Lot 3 Urgent Cost (+22% Over Standard Budget)",
        sanityPresetLot3Area = "Lot 3 Tempered Glass & Partition",
        sanityPresetLot3Req = "Order 12mm Tempered Glass Lot 3 at +22% above baseline unit rate to meet milestone deadline",
        sanityAreaInputLabel = "Work Zone / Lot Scope (e.g. Level 1 - 60 sqm)",
        sanityRequestInputLabel = "Subordinate's material/cost request (ephemeral check)",
        runSanityCheckBtn = "Run Baseline Sanity Check (Hybrid AI)",
        sanityBaselineRefPrefix = "🔒 Certified Reference:",
        exceptionNoteLabel = "Manager Transparency Audit Note (reason for exception / hold)",
        exceptionNotePlaceholder = "e.g. Lot 3 rate +22% approved due to urgent milestone delivery to avoid LD",
        approveExceptionBtn = "Approve Exception",
        holdAuditBtn = "Hold / Audit",
        auditLogTitlePrefix = "Exception Audit Log (",
        auditLogSubtitle = "Records transparent justification for approved exceptions or holds without altering baseline norms",
        auditLogEmpty = "No exception audits yet — run 'Baseline Sanity Check' above and approve or hold to create a transparent audit record.",
        auditReasonPrefix = "Justification:",
        auditFooterImmutable = "Baseline Unmodified",
        chatHeaderTitle = "💬 Construction Law & Risk Advisor Chat (Cloud Gemini + Search)",
        chatInputPlaceholder = "Ask about construction contracts, LD, EOT, or negotiation...",
        chatBubbleUserLabel = "Construction Manager (You)",
        chatBubbleAiLabel = "🛡️ CAREER SHIELD AI",
        liveVoiceTitle = "Hands-Free Site Live Voice (Gemini 3.8 Live API)",
        liveVoiceIdleDesc = "Tap below to talk hands-free with CAREER SHIELD AI while walking the jobsite.",
        liveVoiceConnectingDesc = "Connecting to Gemini 3.8 Live audio session...",
        liveVoiceActiveDesc = "🟢 Live session active — speak about LD risks, EOT notices, or subcontractor disputes",
        liveVoiceStopBtn = "End Live Voice Session",
        liveVoiceStartBtn = "Start Live Voice w/ Gemini 3.8",
        liveVoiceUserTranscriptLabel = "🎙️ Your Voice (Live Input Transcript):",
        liveVoiceModelTranscriptLabel = "🔊 AI Advisor (Live Audio Transcript):",
        highThinkingTitle = "Senior Hybrid AI Advisor (On-Device Preview + Cloud)",
        highThinkingSubtitle = "Targets OnDeviceModelOption.PREVIEW (PREFER_ON_DEVICE) with seamless Cloud Gemini 3.8 Flash / 4 Carbon / 3.8 Live fallback",
        crisisScenariosHeader = "Select a critical site scenario or type your dispute:",
        crisisInputLabel = "Site dispute / contract crisis details",
        defaultCrisisInput = "Client verbally ordered demolition and rework of Level-4 AAC block walls without issuing a signed VO while demanding completion before Friday's milestone inspection.",
        runHighThinkingBtn = "Deep Analysis (High Thinking)",
        generateActionPackBtn = "Generate Script + Tasks",
        highThinkingReasoningHeader = "🧠 Deep Reasoning Trace (Internal Reasoning)",
        highThinkingResultHeader = "✅ Defense Strategy & Contractual Action Plan",
        actionPackHeader = "✨ Action Pack Ready to Save into Room DB",
        actionPackTasksHeader = "Recommended Daily Checkpoints",
        saveActionPackBtn = "Save Entire Action Pack to Room DB",
        riskReportsTitle = "Risk Assessment Reports (Room v4:",
        riskReportsOpenCritical = "⚠️ %d Open Critical / Stop-Work Report(s)",
        riskReportsSubDefault = "Persisted in SQLite • risk_assessment_reports",
        btnCreateReport = "New Report",
        reportZoneLabel = "Site Zone / Floor",
        reportHazardLabel = "Hazard Summary",
        reportMitigationLabel = "Mitigation Plan / Controls",
        submitRiskReportBtn = "Save Risk Report to Room DB",
        riskReportsEmpty = "No saved risk assessment reports yet — tap 'New Report' or save directly from Site Safety AI above.",
        mitigationPrefix = "Mitigation:",
        sourcePrefix = "Origin:",
        reopenCaseBtn = "Reopen Case",
        closeMitigatedBtn = "Mark Mitigated",
        stopWorkBtn = "Order Stop-Work",
        checklistsTitle = "Site Inspection Checklists (",
        checklistsSubtitle = "Relational Schema • inspection_checklists -> checklist_items",
        btnCreateChecklist = "New Checklist",
        checklistTitleLabel = "Inspection checklist title",
        checklistZoneLabel = "Site Zone / Floor",
        checklistItemsLabel = "Checklist items (1 line = 1 check item)",
        defaultChecklistItemsText = "100% Safety Harness tie-off verified\nScaffolding Green Tag inspected\nPerimeter exclusion zone barricaded",
        submitChecklistBtn = "Save Checklist to Room DB",
        passRateLabel = "PASS"
    )
}

object LocalizedSeedMapper {
    data class DisplayTask(val title: String, val description: String)
    data class DisplayScript(val title: String, val content: String)

    fun mapTask(task: TaskEntity, lang: AppLanguage): DisplayTask {
        if (lang == AppLanguage.TH) return DisplayTask(task.title, task.description)
        return when {
            task.title.startsWith("08:30 ตรวจสอบรูปถ่าย Timestamp Camera") -> DisplayTask(
                title = "08:30 Verify Timestamp Camera rebar/cement stock photos",
                description = "Count site rebar and cement inventory with GPS-stamped photos to prevent theft or over-requisition."
            )
            task.title.startsWith("11:30 ล็อกคอ SLA คอนเซาท์") -> DisplayTask(
                title = "11:30 Lock Consultant concrete pour RFI SLA (max 4 hrs)",
                description = "Submit concrete pour RFI in advance and enforce the 4-hour consultant inspection SLA before ready-mix trucks arrive."
            )
            task.title.startsWith("15:00 สรุปยอดตัดจ่าย Cashflow") -> DisplayTask(
                title = "15:00 Reconcile subcontractor progress vs. Cashflow",
                description = "Cross-check subcontractor interim payment claims against verified physical site progress before weekly disbursement."
            )
            task.title.startsWith("16:30 ยิงลิงก์ Google Form") -> DisplayTask(
                title = "16:30 Send daily delay-tracking report link to foremen",
                description = "Collect daily progress logs from site engineers/foremen and flag any Critical Path bottlenecks immediately."
            )
            task.title.startsWith("17:30 เช็กยอดจำนวนคนงานรายวัน") -> DisplayTask(
                title = "17:30 Audit daily site headcount vs. baseline plan",
                description = "Log subcontractor crew size per trade; if manpower drops >20% for 3 consecutive days, issue formal warning notice."
            )
            else -> DisplayTask(task.title, task.description)
        }
    }

    fun mapScript(script: ScriptEntity, lang: AppLanguage): DisplayScript {
        if (lang == AppLanguage.TH) return DisplayScript(script.title, script.content)
        return when {
            script.title.startsWith("1. ปฏิเสธคำสั่งแก้งานปากเปล่า") -> DisplayScript(
                title = "1. Decline Verbal Change Order (Lock Written VO First)",
                content = "Dear [Client / Project Director], we acknowledge your requested scope modification. To comply with Contract procedures and protect the Master Schedule, please allow us to submit the cost and time impact assessment for your written VO approval prior to commencing work. Thank you."
            )
            script.title.startsWith("2. ขอขยายเวลาก่อสร้าง (EOT)") -> DisplayScript(
                title = "2. Extension of Time (EOT) Notice to Stop LD",
                content = "Due to force majeure / external delay caused by [Reason / Site Zone], the Contractor hereby reserves its contractual right to claim an Extension of Time (EOT) pursuant to Clause [Clause / RFI No.]. Timestamped site photographic evidence is attached for your consideration."
            )
            script.title.startsWith("3. ล็อกเวลาคอนเซาท์ตรวจงานเทปูน") -> DisplayScript(
                title = "3. Lock Consultant Pre-Pour Inspection (4-Hr SLA)",
                content = "Dear Supervision Consultant Team, formwork and rebar installation at [Reason / Site Zone] are ready for inspection under RFI No. [Clause / RFI No.]. Ready-mix concrete delivery is scheduled at [Time]. Kindly inspect within the 4-hour SLA to prevent concrete degradation and schedule impact."
            )
            script.title.startsWith("4. แจ้งเตือนผู้รับเหมาช่วงเมื่อแรงงานลดลง") -> DisplayScript(
                title = "4. Formal Warning: Subcontractor Manpower Shortage",
                content = "Dear Project Manager of [Client / Company Name], our daily site audit shows only [Actual Crew] workers on site versus the planned [Planned Crew], jeopardizing Milestone [Clause / RFI No.]. Please submit a Recovery Plan and mobilize full manpower within 24 hours; otherwise we will deploy supplemental labor and back-charge per Contract terms."
            )
            else -> DisplayScript(script.title, script.content)
        }
    }

    fun mapChecklistTitle(code: String, rawTitle: String, lang: AppLanguage): String {
        if (lang == AppLanguage.TH) return rawTitle
        return when (code) {
            "CHK-SAFETY-001" -> "Scaffolding & Working at Height Inspection (>= 2m)"
            "CHK-POUR-002" -> "Pre-Pour Formwork & Rebar QA/QC Inspection"
            else -> rawTitle
        }
    }

    fun mapChecklistMeta(code: String, siteZone: String, inspectorName: String, lang: AppLanguage): String {
        if (lang == AppLanguage.TH) return "$code • $siteZone • $inspectorName"
        val mappedZone = when (siteZone) {
            "Zone A / ชั้น 4-5" -> "Zone A / Levels 4-5"
            "คาน/พื้นชั้น 2" -> "Level 2 Beams & Slab"
            "พื้นที่หน้างานหลัก" -> "Main Site Zone"
            else -> siteZone
        }
        val mappedInspector = when (inspectorName) {
            "จป. / ผจก.โครงการ" -> "Safety Officer / PM"
            "วิศวกรสนาม" -> "Site Engineer"
            else -> inspectorName
        }
        return "$code • $mappedZone • $mappedInspector"
    }

    fun mapChecklistItemTitle(standardRef: String, seqOrder: Int, rawTitle: String, lang: AppLanguage): String {
        if (lang == AppLanguage.TH) return rawTitle
        return when (standardRef to seqOrder) {
            "STD-SAFETY-2564" to 1 -> "Guardrails (90-110 cm) and Toe Boards properly installed"
            "STD-SAFETY-2564" to 2 -> "Valid Scaffolding Green Tag attached & base plates verified"
            "STD-SAFETY-2564" to 3 -> "100% Safety Harness tie-off enforced for all workers"
            "STD-CONCRETE-MIX" to 1 -> "Rebar bar size, lap splice length & concrete cover spacers verified"
            "STD-CONCRETE-MIX" to 2 -> "Shoring & formwork joints tight with zero grout leakage risk"
            "STD-CONCRETE-MIX" to 3 -> "Slump test & cylinder strength sample compliant (240-280 ksc)"
            else -> rawTitle
        }
    }

    fun mapRiskZone(rawZone: String, lang: AppLanguage): String {
        if (lang == AppLanguage.TH) return rawZone
        return when (rawZone) {
            "พื้นที่หน้างานหลัก" -> "Main Site Zone"
            "พื้นที่หน้างานตามรายงาน Safety AI" -> "Site Zone (Safety AI Report)"
            else -> rawZone
        }
    }

    fun mapRiskMitigation(rawMitigation: String, lang: AppLanguage): String {
        if (lang == AppLanguage.TH) return rawMitigation
        return when (rawMitigation) {
            "ดำเนินการแก้ไขตามมาตรการความปลอดภัยและตรวจสอบ PPE ก่อนอนุญาตให้ทำงานต่อ" ->
                "Implement engineering controls and verify full PPE compliance before resuming work."
            "เฝ้าระวังและตรวจสอบซ้ำตามรอบงาน" ->
                "Monitor and re-inspect at scheduled shift intervals."
            else -> rawMitigation
        }
    }
}
