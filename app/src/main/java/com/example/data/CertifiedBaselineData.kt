package com.example.data

import com.example.ui.localization.AppLanguage

/**
 * Immutable Certified Role Baseline ("CareerShield-RoleCore-v2026.1").
 *
 * Architectural Rule:
 * - Read-only reference norms curated and verified before release.
 * - Ephemeral user inputs and project Exception Audit Logs NEVER mutate or overwrite
 *   these baseline values (Non-Mutating Baseline / Zero Data Poisoning).
 */
object CertifiedBaselineData {

    const val BASELINE_VERSION = "CareerShield-RoleCore-v2026.1 (Verified Read-Only)"
    const val ROLE_TITLE = "Construction Project & Site Risk Manager"

    data class BaselineStandard(
        val code: String,
        val title: String,
        val safeReferenceRule: String,
        val varianceTrigger: String,
        val titleEn: String = title,
        val safeReferenceRuleEn: String = safeReferenceRule,
        val varianceTriggerEn: String = varianceTrigger
    ) {
        fun localizedTitle(lang: AppLanguage): String =
            if (lang == AppLanguage.EN) titleEn else title

        fun localizedSafeRule(lang: AppLanguage): String =
            if (lang == AppLanguage.EN) safeReferenceRuleEn else safeReferenceRule

        fun localizedVarianceTrigger(lang: AppLanguage): String =
            if (lang == AppLanguage.EN) varianceTriggerEn else varianceTrigger
    }

    val constructionStandards: List<BaselineStandard> = listOf(
        BaselineStandard(
            code = "STD-FLOOR-60",
            title = "งานปูกระเบื้อง/ปูนกาว & งานพื้น (อ้างอิงต่อ 60 ตร.ม.)",
            safeReferenceRule = "พื้นที่ 60 ตร.ม. ปกติใช้กระเบื้องเผื่อตัดเสีย 5-10% (ประมาณ 63-66 ตร.ม.) และปูนกาวถุง 20 กก. อัตรา ~4-5 กก./ตร.ม. (ประมาณ 12-16 ถุง ขึ้นกับความหนาเกรียงหวี)",
            varianceTrigger = "หากขอเบิกกระเบื้อง > 70 ตร.ม. (เกิน +15%) หรือปูนกาว > 20 ถุง สำหรับพื้นที่ 60 ตร.ม. ให้ระงับและขอตรวจสอบแบบตัดกระเบื้อง/สภาพพื้น",
            titleEn = "Floor Tiling & Adhesive Norm (Per 60 sqm Reference)",
            safeReferenceRuleEn = "For 60 sqm, standard tile quantity includes 5-10% cutting waste (~63-66 sqm) and 20kg adhesive bags at ~4-5 kg/sqm (~12-16 bags depending on trowel notch).",
            varianceTriggerEn = "Flag and hold if requisition exceeds 70 sqm tiles (>+15%) or >20 adhesive bags for 60 sqm; inspect cutting layout and slab level."
        ),
        BaselineStandard(
            code = "STD-CONCRETE-MIX",
            title = "กำลังอัดคอนกรีตโครงสร้าง (Strength) & ปริมาตรเท",
            safeReferenceRule = "งานโครงสร้างทั่วไปใช้ fc' 240-280 ksc (ทรงกระบอก) / Slump ตามแบบวิศวกร เผื่อสูญเสียหน้างานมาตรฐาน 3-5% จากปริมาตรแบบ (กxยxส)",
            varianceTrigger = "หากปริมาตรสั่งเทเกินแบบ > 8% หรือลดสเปกกำลังอัดต่ำกว่าแบบ ให้สั่ง Hold งานเทและตรวจแบบหล่อ/ใบสั่งปูนทันที",
            titleEn = "Structural Concrete Strength & Pour Volume",
            safeReferenceRuleEn = "Standard structural concrete fc' 240-280 ksc (cylinder) / specified slump, with 3-5% normal site wastage allowance over design volume (L x W x H).",
            varianceTriggerEn = "Hold pour immediately if ordered volume exceeds drawing >8% or compressive strength grade is downgraded."
        ),
        BaselineStandard(
            code = "STD-GLASS-FACADE",
            title = "มาตรฐานติดตั้งกระจก/ผนังเบา (Safety Margin)",
            safeReferenceRule = "บานกระจกพื้นที่เสี่ยง/ความสูงต้องใช้ Tempered หรือ Laminated ตามข้อกำหนดแรงลม ตรวจสอบระยะเผื่อหุ้มขอบและซิลิโคนกันรั่วซึมก่อนส่งมอบ",
            varianceTrigger = "ห้ามลดความหนาหรือเปลี่ยนชนิดกระจกนิรภัยโดยไม่มีหนังสืออนุมัติจากผู้ออกแบบ/คอนเซาท์",
            titleEn = "Tempered Glass & Facade Safety Margin",
            safeReferenceRuleEn = "High-risk / elevated glazing must use Tempered or Laminated safety glass per wind-load specs; verify edge bite & weatherproof sealant before handover.",
            varianceTriggerEn = "Never downgrade glass thickness or safety type without written approval from the Structural Designer/Consultant."
        ),
        BaselineStandard(
            code = "STD-SAFETY-2564",
            title = "กฎหมายความปลอดภัยงานก่อสร้าง (พ.ศ. 2564)",
            safeReferenceRule = "ทำงานสูง >= 2 เมตร ต้องมีราวกันตก นั่งร้านตรวจป้าย Tag และเข็มขัดนิรภัย (Safety Harness) ตู้ไฟหน้างานต้องมีสายดินและ ELCB",
            varianceTrigger = "พบการละเมิดความปลอดภัยที่เสี่ยงตกจากที่สูงหรือไฟดูด ให้ใช้คำสั่ง Stop-Work ทันที",
            titleEn = "Construction OSH Ministerial Regulation (B.E. 2564)",
            safeReferenceRuleEn = "Work >= 2m height requires guardrails, inspected scaffold Green Tags, and 100% Safety Harness tie-off; temp power boards must have grounding and ELCB.",
            varianceTriggerEn = "Issue immediate Stop-Work Order upon any fall-from-height or electrocution hazard violation."
        ),
        BaselineStandard(
            code = "STD-CONTRACT-LD",
            title = "ค่าปรับล่าช้า (LD), VO และ EOT",
            safeReferenceRule = "ห้ามทำคำสั่งแก้งานปากเปล่า (VO) โดยไม่มีลายลักษณ์อักษร หากมีเหตุล่าช้าจากผู้ว่าจ้าง/ภัยธรรมชาติ ต้องส่งหนังสือสงวนสิทธิ์ EOT ตามรอบเวลาสัญญา",
            varianceTrigger = "งานเพิ่ม/แก้ปากเปล่าหรือยอดคนงานผู้รับเหมาช่วงลด > 20% เกิน 3 วัน ต้องออกหนังสือแจ้งเตือนเป็นหลักฐาน",
            titleEn = "Liquidated Damages (LD), VO & EOT Controls",
            safeReferenceRuleEn = "Never execute verbal change orders without written VO sign-off. Submit timely EOT reservation notices for employer-caused or force majeure delays.",
            varianceTriggerEn = "Issue formal written notice if verbal rework is demanded or subcontractor manpower drops >20% for over 3 consecutive days."
        )
    )

    fun findRelevantBaselineText(
        query: String,
        language: AppLanguage = AppLanguage.TH
    ): String {
        val lower = query.lowercase()
        val matched = constructionStandards.filter { std ->
            when (std.code) {
                "STD-FLOOR-60" -> lower.contains("กระเบื้อง") || lower.contains("ปูน") ||
                    lower.contains("60") || lower.contains("ตร.ม") || lower.contains("เบิก") ||
                    lower.contains("วัสดุ") || lower.contains("พื้น") ||
                    lower.contains("tile") || lower.contains("adhesive") || lower.contains("sqm") ||
                    lower.contains("floor")
                "STD-CONCRETE-MIX" -> lower.contains("คอนกรีต") || lower.contains("ปูน") ||
                    lower.contains("strength") || lower.contains("คิว") || lower.contains("เท") ||
                    lower.contains("concrete") || lower.contains("pour") || lower.contains("slump")
                "STD-GLASS-FACADE" -> lower.contains("กระจก") || lower.contains("facade") ||
                    lower.contains("หน้าต่าง") || lower.contains("glass") || lower.contains("tempered") ||
                    lower.contains("lot 3")
                "STD-SAFETY-2564" -> lower.contains("นั่งร้าน") || lower.contains("สูง") ||
                    lower.contains("ไฟ") || lower.contains("ปลอดภัย") || lower.contains("safety") ||
                    lower.contains("scaffold") || lower.contains("harness") || lower.contains("elcb")
                "STD-CONTRACT-LD" -> lower.contains("สัญญา") || lower.contains("ld") ||
                    lower.contains("vo") || lower.contains("eot") || lower.contains("ปรับ") ||
                    lower.contains("contract") || lower.contains("liquidated") || lower.contains("delay")
                else -> false
            }
        }
        val effectiveList = matched.ifEmpty { listOf(constructionStandards.first(), constructionStandards.last()) }
        return buildString {
            appendLine("[CERTIFIED BASELINE: $BASELINE_VERSION]")
            effectiveList.forEach { std ->
                appendLine("- ${std.code} | ${std.localizedTitle(language)}")
                if (language == AppLanguage.EN) {
                    appendLine("  Safe Norm: ${std.localizedSafeRule(language)}")
                    appendLine("  Variance Trigger: ${std.localizedVarianceTrigger(language)}")
                } else {
                    appendLine("  เกณฑ์ปลอดภัย: ${std.localizedSafeRule(language)}")
                    appendLine("  จุดสังเกตผิดปกติ: ${std.localizedVarianceTrigger(language)}")
                }
            }
        }.trim()
    }
}
