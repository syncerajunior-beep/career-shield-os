package com.example.ui.localization

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

object InspectorPrefs {
    private const val PREF_NAME = "nano4_inspector_prefs"
    private const val KEY_CONTRACTOR_EMAIL = "contractor_email"
    private const val KEY_PM_EMAIL = "pm_email"
    private const val KEY_ACTIVE_PDF_URI = "active_pdf_uri"
    private const val KEY_ACTIVE_PDF_PAGE = "active_pdf_page"

    fun loadContractorEmail(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CONTRACTOR_EMAIL, "") ?: ""

    fun saveContractorEmail(context: Context, email: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_CONTRACTOR_EMAIL, email.trim()).apply()
    }

    fun loadPmEmail(context: Context): String =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PM_EMAIL, "") ?: ""

    fun savePmEmail(context: Context, email: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PM_EMAIL, email.trim()).apply()
    }

    fun loadActivePdfUri(context: Context): String? =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACTIVE_PDF_URI, null)?.takeIf { it.isNotBlank() }

    fun saveActivePdfUri(context: Context, uri: String?) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_ACTIVE_PDF_URI, uri).apply()
    }

    fun loadActivePdfPage(context: Context): Int =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_ACTIVE_PDF_PAGE, 0)

    fun saveActivePdfPage(context: Context, page: Int) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_ACTIVE_PDF_PAGE, page).apply()
    }
}

data class InspectorStrings(
    val appHeaderTitle: String,
    val appHeaderSubtitle: String,
    val tabSiteWalk: String,
    val tabReportEmail: String,
    val diagnosticsDesc: String,
    // 4 Pillars of Excellence ("All-OK")
    val pillarsBadge: String,
    val pillarsHeadline: String,
    val pillarsIntro: String,
    val pillar1Title: String,
    val pillar1Desc: String,
    val pillar2Title: String,
    val pillar2Desc: String,
    val pillar3Title: String,
    val pillar3Desc: String,
    val pillar4Title: String,
    val pillar4Desc: String,
    val pillarsClosing: String,
    // Step 1: Inspection Context
    val walkSetupTitle: String,
    val timestampLockedPrefix: String,
    val floorZoneLabel: String,
    val floorZonePlaceholder: String,
    val quickZones: List<String>,
    val tradeOpenings: String,
    val tradeMasonry: String,
    val tradeStructural: String,
    val tradeMepSafety: String,
    // Step 2: PDF Blueprint & Pin
    val pdfCardTitle: String,
    val pdfCardSubtitle: String,
    val uploadPdfBtn: String,
    val samplePdfBtn: String,
    val pdfEmptyHint: String,
    val pdfPageIndicator: String,
    val pdfTapToPinHint: String,
    val pdfPinPlacedFormat: String,
    val pdfClearPinBtn: String,
    // Step 3: Defect Capture & AI
    val captureCardTitle: String,
    val captureCardSubtitle: String,
    val takePhotoBtn: String,
    val pickPhotoBtn: String,
    val retakePhotoBtn: String,
    val clearPhotoBtn: String,
    val photoReadyBadge: String,
    val openingCodeLabel: String,
    val openingCodePlaceholder: String,
    val measuredInputLabel: String,
    val measuredInputPlaceholder: String,
    val noteLabel: String,
    val notePlaceholder: String,
    val quickPresetCrack: String,
    val quickPresetWindowW1: String,
    val quickPresetDoorD1: String,
    val btnComparePdfCloud: String,
    val btnQuickHybridTriage: String,
    val aiResultTitle: String,
    val btnSaveDefectToRoom: String,
    val savedDefectToast: String,
    val btnDisableOfflineRetry: String,
    // Tab 2: Report & Email
    val reportHeaderTitle: String,
    val reportHeaderSubtitle: String,
    val openCountFormat: String,
    val filterAll: String,
    val filterCritical: String,
    val filterOpen: String,
    val filterFixed: String,
    val blueprintOverviewTitle: String,
    val blueprintPinsOnPageFormat: String,
    val emailCardTitle: String,
    val emailCardSubtitle: String,
    val contractorEmailLabel: String,
    val pmEmailLabel: String,
    val sendEmailBtn: String,
    val copyReportBtn: String,
    val noEmailAppToast: String,
    val reportEmptyTitle: String,
    val reportEmptySubtitle: String,
    val markFixedBtn: String,
    val reopenBtn: String,
    val deleteBtn: String,
    val statusOpen: String,
    val statusFixed: String,
    val photoPreviewTitle: String,
    // Diagnostics Dialog
    val diagTitle: String,
    val diagSubtitle: String,
    val diagDeviceLabel: String,
    val diagHardwareLabel: String,
    val diagNanoTestTitle: String,
    val diagNanoTestNote: String,
    val diagTestEnBtn: String,
    val diagTestThBtn: String,
    val closeBtn: String
)

// English is the primary default experience for NANO 4oK
val LocalInspectorStrings = compositionLocalOf { inspectorStrings(AppLanguage.EN) }

fun inspectorStrings(lang: AppLanguage): InspectorStrings = if (lang == AppLanguage.TH) {
    InspectorStrings(
        appHeaderTitle = "NANO 4oK",
        appHeaderSubtitle = "AI ตรวจบ้าน & หน้างานอัจฉริยะ",
        tabSiteWalk = "ตรวจหน้างาน",
        tabReportEmail = "รายงาน & ส่งเมล",
        diagnosticsDesc = "สถานะ Hybrid AI & Debug Token",
        pillarsBadge = "All-OK • 4 แกนหลัก",
        pillarsHeadline = "NANO 4oK: 4 Pillars of Excellence",
        pillarsIntro = "ผู้ช่วย AI ยุคใหม่บนขุมพลัง Gemini Nano 4 สำหรับสถาปนิกอิสระ ผู้ตรวจบ้าน และโฟร์แมนคุมงาน เพื่อให้งานทุกโครงการ \"All-OK\" ครบ 4 ด้าน:",
        pillar1Title = "1. Precision Inspection",
        pillar1Desc = "ตรวจจับจุด Defect บ้านและคอนโดด้วย AI ทันทีและแม่นยำ",
        pillar2Title = "2. Site Management",
        pillar2Desc = "รองรับโฟร์แมนคุมหน้างานโครงการขนาดใหญ่และซับซ้อน",
        pillar3Title = "3. Evidence Vault",
        pillar3Desc = "บันทึกรูปภาพ ปักหมุดแบบ และออกรายงาน PDF มืออาชีพ",
        pillar4Title = "4. Smart Consultant",
        pillar4Desc = "วิเคราะห์ต้นตอปัญหาหน้างานพร้อมชี้แนวทางแก้ไขทันที",
        pillarsClosing = "ตั้งแต่บ้านเดี่ยวถึงอาคารสูง ตรวจไวขึ้น เอกสารครบ และสร้างงานอย่างมั่นใจ",
        walkSetupTitle = "1. Site Management • กำหนดโซนเดินตรวจประจำวัน",
        timestampLockedPrefix = "🔒 เวลาจริง (ล็อกอัตโนมัติ แก้ไม่ได้):",
        floorZoneLabel = "ชั้น / ห้อง / โซนที่เดินตรวจ",
        floorZonePlaceholder = "เช่น ชั้น 2 ห้องนอนใหญ่ / หน้าต่าง W1",
        quickZones = listOf("ชั้น 1 / โถงรับแขก", "ชั้น 2 / ห้องนอนใหญ่", "ห้องน้ำชั้น 2", "ประตู-หน้าต่างรอบบ้าน"),
        tradeOpenings = "ประตู-หน้าต่าง",
        tradeMasonry = "ปูน/ฉาบ/กระเบื้อง",
        tradeStructural = "โครงสร้าง/รอยร้าว",
        tradeMepSafety = "ไฟฟ้า/Safety",
        pdfCardTitle = "2. Evidence Vault • แนบแบบ PDF & แตะปักหมุด",
        pdfCardSubtitle = "ไม่ต้องหอบแบบกระดาษ • เปิดหน้าแบบขยายแล้วแตะปักหมุดจุดเสียได้เลย",
        uploadPdfBtn = "อัปโหลด PDF แบบ",
        samplePdfBtn = "ลองใช้แบบ PDF ตัวอย่าง",
        pdfEmptyHint = "อัปโหลดไฟล์แบบแปลน PDF ของบ้าน/โครงการ หรือกด 'ลองใช้แบบ PDF ตัวอย่าง' เพื่อดูแปลนชั้น 2 และตารางประตู-หน้าต่างทันที",
        pdfPageIndicator = "หน้า %d / %d",
        pdfTapToPinHint = "👆 แตะตรงจุดที่พบปัญหาบนแบบแปลน PDF เพื่อปักหมุด Defect #%d",
        pdfPinPlacedFormat = "📍 ปักหมุด #%d บนแบบแล้ว (หน้า %d)",
        pdfClearPinBtn = "ล้างหมุด",
        captureCardTitle = "3. Precision Inspection & Smart Consultant",
        captureCardSubtitle = "ถ่ายรูปเฉพาะจุดที่มี Defect หรือกรอกค่าวัด ให้ AI วิเคราะห์ต้นตอและเทียบแบบ PDF",
        takePhotoBtn = "ถ่ายรูป Defect / ตลับเมตร",
        pickPhotoBtn = "เลือกจากคลังภาพ",
        retakePhotoBtn = "ถ่ายใหม่",
        clearPhotoBtn = "ลบรูป",
        photoReadyBadge = "✓ แนบรูปถ่ายหลักฐานแล้ว",
        openingCodeLabel = "รหัสช่อง / จุด (เช่น W1, D1)",
        openingCodePlaceholder = "W1 / D2 / เสา B3",
        measuredInputLabel = "ระยะที่วัดได้จริง (กันแสงสะท้อน)",
        measuredInputPlaceholder = "เช่น 117.5 x 199 ซม.",
        noteLabel = "อาการ / หมายเหตุสั้นๆ",
        notePlaceholder = "เช่น ช่องเปิดปูนแคบกว่าแบบ หรือ ปูนฉาบมีรอยร้าวแนวเฉียง",
        quickPresetCrack = "ปูนมีรอยร้าว",
        quickPresetWindowW1 = "หน้าต่าง W1 แคบกว่าแบบ",
        quickPresetDoorD1 = "ประตู D1 ไม่ได้ดิ่ง",
        btnComparePdfCloud = "เทียบภาพถ่ายกับแบบ PDF (Cloud AI)",
        btnQuickHybridTriage = "วิเคราะห์ด่วน (Hybrid On-Device)",
        aiResultTitle = "Smart Consultant • ผลวิเคราะห์และวิธีแก้ไข",
        btnSaveDefectToRoom = "บันทึกจุด Defect #%d ลงคลังหลักฐาน (Room DB)",
        savedDefectToast = "บันทึกจุด Defect #%d พร้อมเวลาจริงแล้ว!",
        btnDisableOfflineRetry = "ปิดโหมด Offline เพื่อใช้ Cloud AI",
        reportHeaderTitle = "Evidence Vault • รายงาน Defect & ส่งอีเมล",
        reportHeaderSubtitle = "รวมรูปถ่ายหน้างาน + แบบแปลนปักหมุด + เวลาจริงที่แก้ไขไม่ได้",
        openCountFormat = "รอแก้ไข %d รายการ",
        filterAll = "ทั้งหมด",
        filterCritical = "วิกฤต",
        filterOpen = "รอแก้",
        filterFixed = "แก้แล้ว",
        blueprintOverviewTitle = "แบบแปลนปักหมุดจุด Defect บน PDF",
        blueprintPinsOnPageFormat = "แสดง %d หมุดบนหน้า %d ของแบบแปลน",
        emailCardTitle = "ส่งรายงานครบชุดทาง Email (1 แตะ)",
        emailCardSubtitle = "แนบทั้งภาพแบบแปลนปักหมุด + รูปถ่ายหน้างานทุกจุดให้อัตโนมัติ",
        contractorEmailLabel = "To: อีเมลผู้รับเหมา / ช่าง",
        pmEmailLabel = "Cc: อีเมล Project Manager / เจ้าของบ้าน",
        sendEmailBtn = "ส่ง Report ทาง Email (แนบรูป+แปลน)",
        copyReportBtn = "คัดลอกข้อความ",
        noEmailAppToast = "ไม่พบแอปอีเมลในเครื่อง กรุณากด 'คัดลอกข้อความ' แทน",
        reportEmptyTitle = "ยังไม่มีจุด Defect ที่บันทึกไว้",
        reportEmptySubtitle = "ไปที่แท็บ 'ตรวจหน้างาน' ถ่ายรูปเฉพาะจุดที่มีปัญหา แล้วบันทึกเพื่อสร้างรายงานส่งอีเมล",
        markFixedBtn = "ปิดงาน (แก้แล้ว)",
        reopenBtn = "เปิดซ้ำ",
        deleteBtn = "ลบ",
        statusOpen = "รอแก้ไข",
        statusFixed = "แก้ไขแล้ว",
        photoPreviewTitle = "ภาพถ่ายหลักฐานหน้างาน (Real-Time Locked)",
        diagTitle = "NANO 4oK • สถานะ Hybrid AI & App Check",
        diagSubtitle = "PREFER_ON_DEVICE (Gemini Nano 4 Preview) พร้อมสลับ Cloud อัตโนมัติ",
        diagDeviceLabel = "สถานะ On-Device:",
        diagHardwareLabel = "ฮาร์ดแวร์เครื่อง:",
        diagNanoTestTitle = "ทดสอบ Nano 4 On-Device โดยตรง (ONLY_ON_DEVICE)",
        diagNanoTestNote = "หมายเหตุ: มือถือทั่วไปที่ไม่มี Google AICore จะแสดง UNAVAILABLE และระบบหลักจะสลับใช้ Cloud Gemini ให้อัตโนมัติ",
        diagTestEnBtn = "Test EN (Nano)",
        diagTestThBtn = "ทดสอบ TH (Nano)",
        closeBtn = "ปิด"
    )
} else {
    InspectorStrings(
        appHeaderTitle = "NANO 4oK",
        appHeaderSubtitle = "Home Inspector & Site Intelligence",
        tabSiteWalk = "Site Walk",
        tabReportEmail = "Report & Email",
        diagnosticsDesc = "Hybrid AI Status & 4 Pillars",
        pillarsBadge = "All-OK • 4 Pillars",
        pillarsHeadline = "NANO 4oK: The 4 Pillars of Excellence",
        pillarsIntro = "Elevate your construction and property inspection workflow with NANO 4oK—the next-generation AI assistant powered by Gemini Nano 4. Designed for freelance architects, home inspectors, and site foremen so every project is \"All-OK\":",
        pillar1Title = "1. Precision Inspection",
        pillar1Desc = "Instant AI-assisted defect detection for residential homes and condos.",
        pillar2Title = "2. Site Management",
        pillar2Desc = "Scalable support for foremen handling complex, large-scale building projects.",
        pillar3Title = "3. Evidence Vault",
        pillar3Desc = "Effortless photo logging and automated, professional PDF report generation.",
        pillar4Title = "4. Smart Consultant",
        pillar4Desc = "On-the-spot root-cause analysis and practical repair solutions.",
        pillarsClosing = "From single-family homes to high-rise developments, inspect smarter, document faster, and build with confidence.",
        walkSetupTitle = "1. Site Management • Daily Zone & Lock",
        timestampLockedPrefix = "🔒 Real-Time Locked (Immutable):",
        floorZoneLabel = "Floor / Unit / Inspection Zone",
        floorZonePlaceholder = "e.g. Level 2 Master Bedroom / Window W1",
        quickZones = listOf("Level 1 / Living", "Level 2 / Master Bed", "Bathroom 2", "Doors & Windows"),
        tradeOpenings = "Doors & Windows",
        tradeMasonry = "Masonry & Tile",
        tradeStructural = "Structure / Cracks",
        tradeMepSafety = "MEP / Safety",
        pdfCardTitle = "2. Evidence Vault • PDF Plan & Tap-to-Pin",
        pdfCardSubtitle = "Leave paper blueprints behind • Flip to drawing page & tap to drop pin",
        uploadPdfBtn = "Upload PDF Plan",
        samplePdfBtn = "Sample Arch PDF",
        pdfEmptyHint = "Upload your architectural PDF floorplan/schedule or tap 'Sample Arch PDF' to load a 2-page Level 2 Plan & Window Schedule.",
        pdfPageIndicator = "Page %d / %d",
        pdfTapToPinHint = "👆 Tap directly on the PDF drawing to place Defect Pin #%d",
        pdfPinPlacedFormat = "📍 Pin #%d placed on Page %d",
        pdfClearPinBtn = "Clear Pin",
        captureCardTitle = "3. Precision Inspection & Smart Consultant",
        captureCardSubtitle = "Snap defect / tape-measure photo for root-cause analysis & instant PDF comparison",
        takePhotoBtn = "Snap Defect / Tape Photo",
        pickPhotoBtn = "Pick from Gallery",
        retakePhotoBtn = "Retake",
        clearPhotoBtn = "Remove",
        photoReadyBadge = "✓ Site Evidence Photo Attached",
        openingCodeLabel = "Opening / Code (e.g. W1, D1)",
        openingCodePlaceholder = "W1 / D2 / Col B3",
        measuredInputLabel = "Actual Measured Size",
        measuredInputPlaceholder = "e.g. 117.5 x 199 cm",
        noteLabel = "Defect Observation Note",
        notePlaceholder = "e.g. Masonry opening narrower than W1 spec or plaster crack",
        quickPresetCrack = "Concrete / Plaster Crack",
        quickPresetWindowW1 = "W1 Narrow Opening",
        quickPresetDoorD1 = "D1 Out-of-Plumb",
        btnComparePdfCloud = "Compare Photo vs PDF (Cloud AI)",
        btnQuickHybridTriage = "Quick Triage (Hybrid On-Device)",
        aiResultTitle = "Smart Consultant • Root Cause & Fix",
        btnSaveDefectToRoom = "Save Defect #%d to Evidence Vault (Room DB)",
        savedDefectToast = "Saved Defect #%d with locked real-time timestamp!",
        btnDisableOfflineRetry = "Disable Offline Mode to Use Cloud AI",
        reportHeaderTitle = "Evidence Vault • Report & Email",
        reportHeaderSubtitle = "Bundles annotated PDF pins + defect photos + immutable timestamps",
        openCountFormat = "%d Open",
        filterAll = "All",
        filterCritical = "Critical",
        filterOpen = "Open",
        filterFixed = "Fixed",
        blueprintOverviewTitle = "Annotated PDF Blueprint Pins",
        blueprintPinsOnPageFormat = "Showing %d pin(s) on PDF Page %d",
        emailCardTitle = "1-Tap Email Dispatch",
        emailCardSubtitle = "Automatically attaches annotated PDF snapshot + all defect site photos",
        contractorEmailLabel = "To: Contractor / Sub-contractor Email",
        pmEmailLabel = "Cc: Project Manager / Homeowner Email",
        sendEmailBtn = "Send Defect Report (Email)",
        copyReportBtn = "Copy Summary",
        noEmailAppToast = "No email app found on device. Use 'Copy Summary' instead.",
        reportEmptyTitle = "No defects logged yet",
        reportEmptySubtitle = "Switch to 'Site Walk' to snap defects against your PDF plan and build an instant report.",
        markFixedBtn = "Mark Fixed",
        reopenBtn = "Reopen",
        deleteBtn = "Delete",
        statusOpen = "OPEN",
        statusFixed = "FIXED",
        photoPreviewTitle = "Site Evidence Photo (Real-Time Locked)",
        diagTitle = "NANO 4oK • Hybrid AI & App Check",
        diagSubtitle = "PREFER_ON_DEVICE (Gemini Nano 4 Preview) with automatic Cloud Fallback",
        diagDeviceLabel = "On-Device Status:",
        diagHardwareLabel = "Device Hardware:",
        diagNanoTestTitle = "Direct On-Device Nano 4 Check (ONLY_ON_DEVICE)",
        diagNanoTestNote = "Devices without Google AICore report UNAVAILABLE here and automatically route through Cloud Gemini in normal mode.",
        diagTestEnBtn = "Test EN (Nano)",
        diagTestThBtn = "Test TH (Nano)",
        closeBtn = "Close"
    )
}
