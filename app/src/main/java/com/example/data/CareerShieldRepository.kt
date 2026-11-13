package com.example.data

import kotlinx.coroutines.flow.Flow

class CareerShieldRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allScripts: Flow<List<ScriptEntity>> = taskDao.getAllScripts()
    val allExceptionAudits: Flow<List<ExceptionAuditEntity>> = taskDao.getAllExceptionAudits()

    fun getFilteredTasks(category: String, searchQuery: String): Flow<List<TaskEntity>> =
        taskDao.getFilteredTasks(category, searchQuery.trim())

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun resetAllTasks() = taskDao.resetAllTasks()

    suspend fun insertScript(script: ScriptEntity) = taskDao.insertScript(script)

    suspend fun updateScript(script: ScriptEntity) = taskDao.updateScript(script)

    suspend fun deleteScript(script: ScriptEntity) = taskDao.deleteScript(script)

    suspend fun insertExceptionAudit(audit: ExceptionAuditEntity) = taskDao.insertExceptionAudit(audit)

    suspend fun deleteExceptionAudit(audit: ExceptionAuditEntity) = taskDao.deleteExceptionAudit(audit)

    suspend fun ensureSeedData() {
        if (taskDao.getTaskCount() == 0) {
            val defaultTasks = listOf(
                TaskEntity(
                    title = "08:30 ตรวจสอบรูปถ่าย Timestamp Camera สต็อกเหล็ก/ปูน",
                    description = "ตรวจนับจำนวนเหล็กเส้นและปูนซีเมนต์หน้างาน พร้อมถ่ายรูปติดพิกัด GPS และเวลาเพื่อป้องกันของหายหรือเบิกเกิน",
                    category = "SAFETY",
                    timeSlot = "08:30",
                    isCompleted = false
                ),
                TaskEntity(
                    title = "11:30 ล็อกคอ SLA คอนเซาท์ตรวจงานเทปูน (ห้ามเกิน 4 ชม.)",
                    description = "ส่งใบขออนุญาตเทคอนกรีต (Request for Inspection) ล่วงหน้า และติดตามผลตรวจรับภายใน SLA 4 ชั่วโมงก่อนรถปูนมาถึงหน้างาน",
                    category = "SLA",
                    timeSlot = "11:30",
                    isCompleted = false
                ),
                TaskEntity(
                    title = "15:00 สรุปยอดตัดจ่าย Cashflow กับ ผจก.การเงิน",
                    description = "กระทบยอดเบิกงวดงานผู้รับเหมาช่วง เทียบกับเปอร์เซ็นต์งานจริงหน้างานก่อนอนุมัติจ่ายเงินสดประจำสัปดาห์",
                    category = "FINANCE",
                    timeSlot = "15:00",
                    isCompleted = false
                ),
                TaskEntity(
                    title = "16:30 ยิงลิงก์ Google Form ดักคอไซต์งานที่ช้ากว่าแผน",
                    description = "ให้โฟร์แมนและวิศวกรสนามส่งรายงานความคืบหน้ารายวัน พร้อมระบุอุปสรรคที่กระทบสายงานหลัก (Critical Path)",
                    category = "SLA",
                    timeSlot = "16:30",
                    isCompleted = false
                ),
                TaskEntity(
                    title = "17:30 เช็กยอดจำนวนคนงานรายวันเทียบแผน (ป้องกันทิ้งงาน)",
                    description = "บันทึกจำนวนช่างและแรงงานของแต่ละผู้รับเหมาช่วง หากลดลงเกิน 20% ติดต่อกัน 3 วัน ต้องออกหนังสือเตือนทันที",
                    category = "CONTRACT",
                    timeSlot = "17:30",
                    isCompleted = false
                )
            )
            taskDao.insertAllTasks(defaultTasks)
        }

        if (taskDao.getScriptCount() == 0) {
            val defaultScripts = listOf(
                ScriptEntity(
                    title = "1. ปฏิเสธคำสั่งแก้งานปากเปล่า (ล็อก VO ก่อนทำ)",
                    content = "เรียน คุณ [ชื่อผู้ว่าจ้าง/เจ้านาย] ทางโครงการได้รับทราบคำสั่งเปลี่ยนแปลงงานแล้วครับ ทั้งนี้ เพื่อให้เป็นไปตามระเบียบสัญญาและไม่กระทบต่อกำหนดส่งมอบงานใหญ่ ขออนุญาตส่งสรุปผลกระทบด้านงบประมาณและเวลา เพื่อขออนุมัติเป็นลายลักษณ์อักษรก่อนเริ่มดำเนินการ ขอบคุณครับ",
                    category = "CONTRACT",
                    riskLevel = "CRITICAL"
                ),
                ScriptEntity(
                    title = "2. ขอขยายเวลาก่อสร้าง (EOT) หยุดค่าปรับ LD",
                    content = "ตามที่โครงการเกิดเหตุสุดวิสัยอันเนื่องมาจาก [ระบุเหตุผล] ทางผู้รับจ้างขอแจ้งสงวนสิทธิ์ในการขอขยายระยะเวลาก่อสร้าง (EOT) ตามสัญญาข้อ [X] โดยแนบภาพถ่าย Timestamp มาเพื่อโปรดพิจารณาครับ",
                    category = "EOT_LD",
                    riskLevel = "CRITICAL"
                ),
                ScriptEntity(
                    title = "3. ล็อกเวลาคอนเซาท์ตรวจงานเทปูน (SLA 4 ชม.)",
                    content = "เรียน ทีมที่ปรึกษาควบคุมงาน (Consult) ทางโครงการได้จัดเตรียมแบบหล่อและเหล็กเสริมบริเวณ [ระบุโซน/ชั้น] พร้อมสำหรับการตรวจรับแล้วตามใบแจ้งตรวจเลขที่ [เลขที่ RFI] โดยมีกำหนดรถคอนกรีตเข้าหน้างานเวลา [ระบุเวลา] จึงขอความอนุเคราะห์เข้าตรวจภายในกรอบ SLA 4 ชม. เพื่อป้องกันคอนกรีตเสื่อมสภาพและผลกระทบต่อแผนงานหลักครับ",
                    category = "SLA",
                    riskLevel = "HIGH"
                ),
                ScriptEntity(
                    title = "4. แจ้งเตือนผู้รับเหมาช่วงเมื่อแรงงานลดลงผิดปกติ",
                    content = "เรียน ผู้จัดการโครงการ บริษัท [ชื่อผู้รับเหมาช่วง] จากการตรวจสอบยอดแรงงานประจำวันที่ [ระบุวันที่] พบว่ามีช่างเข้าปฏิบัติงานเพียง [จำนวนคนจริง] คน จากแผนที่ตกลงไว้ [จำนวนคนตามแผน] คน ซึ่งส่งผลกระทบต่อความก้าวหน้าของงานงวดที่ [X] ขอให้จัดส่งแผนเร่งรัดงาน (Recovery Plan) และเพิ่มกำลังคนภายใน 24 ชม. มิฉะนั้นทางโครงการจำเป็นต้องดำเนินการจัดหาทีมเสริมและหักค่าใช้จ่ายตามเงื่อนไขสัญญาครับ",
                    category = "CONTRACT",
                    riskLevel = "CRITICAL"
                )
            )
            taskDao.insertAllScripts(defaultScripts)
        }
    }
}
