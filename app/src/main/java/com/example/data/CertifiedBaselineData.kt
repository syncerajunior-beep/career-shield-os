package com.example.data

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
        val varianceTrigger: String
    )

    val constructionStandards: List<BaselineStandard> = listOf(
        BaselineStandard(
            code = "STD-FLOOR-60",
            title = "งานปูกระเบื้อง/ปูนกาว & งานพื้น (อ้างอิงต่อ 60 ตร.ม.)",
            safeReferenceRule = "พื้นที่ 60 ตร.ม. ปกติใช้กระเบื้องเผื่อตัดเสีย 5-10% (ประมาณ 63-66 ตร.ม.) และปูนกาวถุง 20 กก. อัตรา ~4-5 กก./ตร.ม. (ประมาณ 12-16 ถุง ขึ้นกับความหนาเกรียงหวี)",
            varianceTrigger = "หากขอเบิกกระเบื้อง > 70 ตร.ม. (เกิน +15%) หรือปูนกาว > 20 ถุง สำหรับพื้นที่ 60 ตร.ม. ให้ระงับและขอตรวจสอบแบบตัดกระเบื้อง/สภาพพื้น"
        ),
        BaselineStandard(
            code = "STD-CONCRETE-MIX",
            title = "กำลังอัดคอนกรีตโครงสร้าง (Strength) & ปริมาตรเท",
            safeReferenceRule = "งานโครงสร้างทั่วไปใช้ fc' 240-280 ksc (ทรงกระบอก) / Slump ตามแบบวิศวกร เผื่อสูญเสียหน้างานมาตรฐาน 3-5% จากปริมาตรแบบ (กxยxส)",
            varianceTrigger = "หากปริมาตรสั่งเทเกินแบบ > 8% หรือลดสเปกกำลังอัดต่ำกว่าแบบ ให้สั่ง Hold งานเทและตรวจแบบหล่อ/ใบสั่งปูนทันที"
        ),
        BaselineStandard(
            code = "STD-GLASS-FACADE",
            title = "มาตรฐานติดตั้งกระจก/ผนังเบา (Safety Margin)",
            safeReferenceRule = "บานกระจกพื้นที่เสี่ยง/ความสูงต้องใช้ Tempered หรือ Laminated ตามข้อกำหนดแรงลม ตรวจสอบระยะเผื่อหุ้มขอบและซิลิโคนกันรั่วซึมก่อนส่งมอบ",
            varianceTrigger = "ห้ามลดความหนาหรือเปลี่ยนชนิดกระจกนิรภัยโดยไม่มีหนังสืออนุมัติจากผู้ออกแบบ/คอนเซาท์"
        ),
        BaselineStandard(
            code = "STD-SAFETY-2564",
            title = "กฎหมายความปลอดภัยงานก่อสร้าง (พ.ศ. 2564)",
            safeReferenceRule = "ทำงานสูง >= 2 เมตร ต้องมีราวกันตก นั่งร้านตรวจป้าย Tag และเข็มขัดนิรภัย (Safety Harness) ตู้ไฟหน้างานต้องมีสายดินและ ELCB",
            varianceTrigger = "พบการละเมิดความปลอดภัยที่เสี่ยงตกจากที่สูงหรือไฟดูด ให้ใช้คำสั่ง Stop-Work ทันที"
        ),
        BaselineStandard(
            code = "STD-CONTRACT-LD",
            title = "ค่าปรับล่าช้า (LD), VO และ EOT",
            safeReferenceRule = "ห้ามทำคำสั่งแก้งานปากเปล่า (VO) โดยไม่มีลายลักษณ์อักษร หากมีเหตุล่าช้าจากผู้ว่าจ้าง/ภัยธรรมชาติ ต้องส่งหนังสือสงวนสิทธิ์ EOT ตามรอบเวลาสัญญา",
            varianceTrigger = "งานเพิ่ม/แก้ปากเปล่าหรือยอดคนงานผู้รับเหมาช่วงลด > 20% เกิน 3 วัน ต้องออกหนังสือแจ้งเตือนเป็นหลักฐาน"
        )
    )

    fun findRelevantBaselineText(query: String): String {
        val lower = query.lowercase()
        val matched = constructionStandards.filter { std ->
            when (std.code) {
                "STD-FLOOR-60" -> lower.contains("กระเบื้อง") || lower.contains("ปูน") ||
                    lower.contains("60") || lower.contains("ตร.ม") || lower.contains("เบิก") ||
                    lower.contains("วัสดุ") || lower.contains("พื้น")
                "STD-CONCRETE-MIX" -> lower.contains("คอนกรีต") || lower.contains("ปูน") ||
                    lower.contains("strength") || lower.contains("คิว") || lower.contains("เท")
                "STD-GLASS-FACADE" -> lower.contains("กระจก") || lower.contains("facade") ||
                    lower.contains("หน้าต่าง")
                "STD-SAFETY-2564" -> lower.contains("นั่งร้าน") || lower.contains("สูง") ||
                    lower.contains("ไฟ") || lower.contains("ปลอดภัย") || lower.contains("safety")
                "STD-CONTRACT-LD" -> lower.contains("สัญญา") || lower.contains("ld") ||
                    lower.contains("vo") || lower.contains("eot") || lower.contains("ปรับ")
                else -> false
            }
        }
        val effectiveList = matched.ifEmpty { listOf(constructionStandards.first(), constructionStandards.last()) }
        return buildString {
            appendLine("[CERTIFIED BASELINE: $BASELINE_VERSION]")
            effectiveList.forEach { std ->
                appendLine("- ${std.code} | ${std.title}")
                appendLine("  เกณฑ์ปลอดภัย: ${std.safeReferenceRule}")
                appendLine("  จุดสังเกตผิดปกติ: ${std.varianceTrigger}")
            }
        }.trim()
    }
}
