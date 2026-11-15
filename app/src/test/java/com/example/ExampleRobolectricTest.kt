package com.example

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiInferenceOrigin
import com.example.data.AppDatabase
import com.example.data.CareerShieldRepository
import com.example.data.CertifiedBaselineData
import com.example.data.ExceptionAuditEntity
import com.example.data.GeminiAiRepository
import com.example.data.InspectionChecklistEntity
import com.example.data.InspectionChecklistItemEntity
import com.example.data.InspectionDao
import com.example.data.NanoDiagnosticLanguage
import com.example.data.RiskAssessmentReportEntity
import com.example.data.ScriptEntity
import com.example.data.SiteWalkDefectEntity
import com.example.data.TaskDao
import com.example.data.TaskEntity
import com.example.data.toOrigin
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppLanguagePrefs
import com.example.ui.localization.LocalizedSeedMapper
import com.example.util.FirebaseManager
import com.example.viewmodel.CareerShieldViewModel
import com.example.viewmodel.DefectAiUiState
import com.example.viewmodel.HybridInferenceSource
import com.example.viewmodel.MaterialSanityCheckUiState
import com.example.viewmodel.NanoDiagnosticUiState
import com.example.viewmodel.ScriptTailorUiState
import com.example.viewmodel.SiteSafetyRiskUiState
import com.example.viewmodel.toHybridSource
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.OnDeviceModelOption
import com.google.firebase.ai.type.PublicPreviewAPI
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao
    private lateinit var inspectionDao: InspectionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor { it.run() }
            .setTransactionExecutor { it.run() }
            .build()
        dao = db.taskDao()
        inspectionDao = db.inspectionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NANO 4oK", appName)
    }

    @OptIn(PublicPreviewAPI::class)
    @Test
    fun `test hybrid inference source mapping and offline site mode toggle`() {
        assertEquals(HybridInferenceSource.ON_DEVICE, InferenceSource.ON_DEVICE.toHybridSource())
        assertEquals(HybridInferenceSource.IN_CLOUD, InferenceSource.IN_CLOUD.toHybridSource())
        assertEquals(HybridInferenceSource.IN_CLOUD, null.toHybridSource())
        assertEquals(AiInferenceOrigin.ON_DEVICE, InferenceSource.ON_DEVICE.toOrigin())
        assertEquals(AiInferenceOrigin.IN_CLOUD, InferenceSource.IN_CLOUD.toOrigin())

        val repository = CareerShieldRepository(dao)
        val aiRepository = GeminiAiRepository()
        val viewModel = CareerShieldViewModel(repository, aiRepository)
        assertFalse(viewModel.offlineSiteTestMode.value)
        viewModel.toggleOfflineSiteTestMode()
        assertTrue(viewModel.offlineSiteTestMode.value)
        viewModel.setOfflineSiteTestMode(false)
        assertFalse(viewModel.offlineSiteTestMode.value)
    }

    @Test
    fun `test safety risk analyzer validation and prompt builder in GeminiAiRepository`() = runTest {
        val repository = CareerShieldRepository(dao)
        val aiRepository = GeminiAiRepository()
        val viewModel = CareerShieldViewModel(repository, aiRepository)

        viewModel.analyzeSiteSafetyRisk("   ")
        val state = viewModel.siteSafetyRiskState.value
        assertTrue(state is SiteSafetyRiskUiState.Error)

        viewModel.clearSiteSafetyRisk()
        assertTrue(viewModel.siteSafetyRiskState.value is SiteSafetyRiskUiState.Idle)

        val prompt = aiRepository.buildContractRiskPrompt("ค่าปรับวันละ 0.25%")
        assertTrue(prompt.contains("ค่าปรับวันละ 0.25%"))
    }

    @Test
    fun `test FirebaseManager initializes App Check and persists debug token`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testToken = "test-appcheck-debug-token-1234"
        val intent = Intent().apply {
            putExtra("FIREBASE_APPCHECK_DEBUG_TOKEN", testToken)
        }

        FirebaseManager.initializeAppCheck(context, intent)

        assertTrue(FirebaseManager.isAppCheckInitialized())
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
            "com.google.firebase.appcheck.debug.store.$persistenceKey",
            Context.MODE_PRIVATE
        )
        assertEquals(
            testToken,
            prefs.getString("com.google.firebase.appcheck.debug.DEBUG_SECRET", null)
        )
    }

    @Test
    fun `test insert edit delete and filter search tasks in Room`() = runTest {
        val task1 = TaskEntity(
            title = "08:30 ตรวจสอบสต็อกเหล็ก",
            description = "ถ่ายรูป Timestamp Camera",
            category = "SAFETY",
            timeSlot = "08:30"
        )
        val task2 = TaskEntity(
            title = "11:30 ล็อกคอ SLA เทปูน",
            description = "ตรวจรับภายใน 4 ชม.",
            category = "SLA",
            timeSlot = "11:30"
        )
        dao.insertTask(task1)
        dao.insertTask(task2)

        val all = dao.getFilteredTasks("ALL", "").first()
        assertEquals(2, all.size)

        // Filter by category
        val slaOnly = dao.getFilteredTasks("SLA", "").first()
        assertEquals(1, slaOnly.size)
        assertEquals("11:30 ล็อกคอ SLA เทปูน", slaOnly.first().title)

        // Search by keyword in description
        val searchResult = dao.getFilteredTasks("ALL", "Timestamp").first()
        assertEquals(1, searchResult.size)
        assertEquals("SAFETY", searchResult.first().category)

        // Edit task title, description, and category
        val insertedTask1 = searchResult.first()
        val updatedTask1 = insertedTask1.copy(
            title = "09:00 ตรวจรับเหล็กเส้นและปูน",
            description = "อัปเดตยอดสต็อก",
            category = "FINANCE",
            timeSlot = "09:00"
        )
        dao.updateTask(updatedTask1)

        val financeTasks = dao.getFilteredTasks("FINANCE", "อัปเดต").first()
        assertEquals(1, financeTasks.size)
        assertEquals("09:00 ตรวจรับเหล็กเส้นและปูน", financeTasks.first().title)

        // Attach site risk photo URI to task and verify persistence
        val photoUriString = "content://com.aistudio.careershield.rkmvqz.fileprovider/site_photos/SITE_20261112_165000.jpg"
        val taskWithPhoto = updatedTask1.copy(photoUri = photoUriString)
        dao.updateTask(taskWithPhoto)

        val fetchedWithPhoto = dao.getFilteredTasks("FINANCE", "อัปเดต").first().first()
        assertEquals(photoUriString, fetchedWithPhoto.photoUri)

        // Delete task
        dao.deleteTask(taskWithPhoto)
        val remaining = dao.getFilteredTasks("ALL", "").first()
        assertEquals(1, remaining.size)
        assertTrue(remaining.none { it.id == insertedTask1.id })
    }

    @OptIn(PublicPreviewAPI::class, ExperimentalCoroutinesApi::class)
    @Test
    fun `test Nano 4 Preview diagnostic prompts and strict non-cloud fallback on JVM`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(
                    context,
                    FirebaseOptions.Builder()
                        .setApplicationId("1:000000000000:android:0000000000000000")
                        .setApiKey("test-api-key-placeholder")
                        .setProjectId("test-project-id")
                        .build()
                )
            }

            val aiRepository = GeminiAiRepository()

            // Verify OnDeviceModelOption.PREVIEW enum/constant is linked and prompts for EN/TH are distinct
            assertEquals("PREVIEW", OnDeviceModelOption.PREVIEW.toString().uppercase().let {
                if (it.contains("PREVIEW")) "PREVIEW" else it
            })
            val enPrompt = aiRepository.buildDiagnosticPrompt(NanoDiagnosticLanguage.ENGLISH)
            val thPrompt = aiRepository.buildDiagnosticPrompt(NanoDiagnosticLanguage.THAI)
            assertTrue(enPrompt.contains("scaffolding"))
            assertTrue(thPrompt.contains("นั่งร้าน"))

            // On Robolectric JVM without AICore/Play Services, ONLY_ON_DEVICE diagnostic must NOT fall back to Cloud
            val enResult = aiRepository.runNano4PreviewDiagnostic(NanoDiagnosticLanguage.ENGLISH)
            assertFalse(enResult.isSuccess)
            assertEquals(null, enResult.inferenceSource)
            assertTrue(!enResult.errorType.isNullOrBlank())
            assertTrue(!enResult.errorMessage.isNullOrBlank())

            // Verify ViewModel updates nanoDiagnosticState to UnavailableOrError and keeps lastInferenceState STANDBY
            val repository = CareerShieldRepository(dao)
            val viewModel = CareerShieldViewModel(repository, aiRepository)
            viewModel.runNano4PreviewDiagnostic(NanoDiagnosticLanguage.THAI)
            advanceUntilIdle()

            val diagState = viewModel.nanoDiagnosticState.value
            assertTrue(diagState is NanoDiagnosticUiState.UnavailableOrError)
            assertEquals(HybridInferenceSource.STANDBY, viewModel.lastInferenceState.value.source)

            viewModel.clearNanoDiagnostic()
            assertTrue(viewModel.nanoDiagnosticState.value is NanoDiagnosticUiState.Idle)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test immutable CertifiedBaselineData lookup, sanity check prompt guardrails, and ExceptionAuditEntity Room CRUD`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            // 1. Verify read-only baseline lookup matches Floor 60 sqm & concrete/material rules
            val baselineExcerpt = CertifiedBaselineData.findRelevantBaselineText(
                "ชั้น 1 พื้นที่ 60 ตร.ม. ขอเบิกกระเบื้อง 95 ตร.ม. และปูนกาว 40 ถุง"
            )
            assertTrue(baselineExcerpt.contains(CertifiedBaselineData.BASELINE_VERSION))
            assertTrue(baselineExcerpt.contains("STD-FLOOR-60"))

            // 2. Verify sanity check prompt embeds baseline, ephemeral input, no-web-search & non-mutating rules
            val aiRepository = GeminiAiRepository()
            val prompt = aiRepository.buildMaterialSanityCheckPrompt(
                areaScope = "ชั้น 1 (60 ตร.ม.)",
                requestDescription = "ขอเบิกกระเบื้อง 95 ตร.ม. และปูนกาว 40 ถุง",
                baselineExcerpt = baselineExcerpt
            )
            assertTrue(prompt.contains("ห้ามค้นเว็บภายนอก"))
            assertTrue(prompt.contains("ไม่ถูกนำไปแก้ทับมาตรฐานกลาง"))
            assertTrue(prompt.contains("STD-FLOOR-60"))
            assertTrue(prompt.contains("ขอเบิกกระเบื้อง 95 ตร.ม."))

            // 3. Verify ViewModel validation and Room v3 ExceptionAuditEntity insert/query/delete
            val repository = CareerShieldRepository(dao)
            val viewModel = CareerShieldViewModel(repository, aiRepository)

            viewModel.runMaterialSanityCheck("ชั้น 1", "   ")
            assertTrue(viewModel.materialSanityCheckState.value is MaterialSanityCheckUiState.Error)
            viewModel.clearMaterialSanityCheck()
            assertTrue(viewModel.materialSanityCheckState.value is MaterialSanityCheckUiState.Idle)

            viewModel.recordExceptionAudit(
                lotOrItemTitle = "สั่งซื้อกระจก Tempered Lot 3",
                areaScope = "Lot 3 งานกระจกและผนังเบา",
                baselineVersion = CertifiedBaselineData.BASELINE_VERSION,
                aiFlagSummary = "ราคาสูงกว่าเกณฑ์กลาง +22%",
                decisionStatus = "APPROVED_WITH_EXCEPTION",
                justificationNote = "เร่งสั่งมอบทันงวดงานป้องกันค่าปรับ LD",
                inferenceOrigin = "IN_CLOUD"
            )
            advanceUntilIdle()

            val auditsAfterInsert = dao.getAllExceptionAudits().first()
            assertEquals(1, auditsAfterInsert.size)
            val savedAudit = auditsAfterInsert.first()
            assertEquals("APPROVED_WITH_EXCEPTION", savedAudit.decisionStatus)
            assertEquals("Lot 3 งานกระจกและผนังเบา", savedAudit.areaOrScope)
            assertEquals(CertifiedBaselineData.BASELINE_VERSION, savedAudit.baselineVersion)
            assertTrue(savedAudit.justificationNote.contains("LD"))

            // Baseline remains completely unmodified after recording an exception
            assertEquals(5, CertifiedBaselineData.constructionStandards.size)
            assertEquals("CareerShield-RoleCore-v2026.1 (Verified Read-Only)", CertifiedBaselineData.BASELINE_VERSION)

            viewModel.deleteExceptionAudit(savedAudit)
            advanceUntilIdle()
            val auditsAfterDelete = dao.getAllExceptionAudits().first()
            assertTrue(auditsAfterDelete.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `test Room v4 RiskAssessmentReport and relational InspectionChecklist foreign keys and status recomputation`() = runTest {
        val reportId = inspectionDao.insertRiskReport(
            RiskAssessmentReportEntity(
                reportCode = "RPT-SAFE-001",
                siteZone = "Zone B / ชั้น 5",
                reportType = "SITE_SAFETY",
                severityLevel = "CRITICAL",
                hazardSummary = "นั่งร้านสูง 14 เมตร ไม่มีราวกันตก",
                assessmentFindings = "พบความเสี่ยงตกจากที่สูงระดับวิกฤต",
                mitigationPlan = "สั่งหยุดงานชั่วคราวและติดตั้ง Guardrail",
                inferenceOrigin = "IN_CLOUD",
                status = "STOP_WORK"
            )
        )
        assertTrue(reportId > 0L)
        assertEquals(1, inspectionDao.observeOpenCriticalReportCount().first())

        val filteredCritical = inspectionDao.getFilteredRiskReports(
            reportType = "SITE_SAFETY",
            severityLevel = "CRITICAL",
            status = "STOP_WORK",
            searchQuery = "นั่งร้าน"
        ).first()
        assertEquals(1, filteredCritical.size)

        val checklistId = inspectionDao.insertChecklistWithItems(
            checklist = InspectionChecklistEntity(
                checklistCode = "CHK-SAFE-900",
                title = "ตรวจนั่งร้านหลังแก้ไข Zone B",
                tradeCategory = "SAFETY",
                siteZone = "Zone B / ชั้น 5",
                linkedReportId = reportId
            ),
            items = listOf(
                InspectionChecklistItemEntity(
                    checklistId = 0L,
                    sequenceOrder = 1,
                    itemTitle = "ติดตั้งราวกันตกครบทุกด้าน",
                    isCriticalStopWork = true
                ),
                InspectionChecklistItemEntity(
                    checklistId = 0L,
                    sequenceOrder = 2,
                    itemTitle = "ติดป้าย Green Tag",
                    isCriticalStopWork = true
                )
            )
        )

        val reportWithChecklists = inspectionDao.getReportsWithChecklists().first()
        assertEquals(1, reportWithChecklists.size)
        assertEquals(1, reportWithChecklists.first().checklists.size)

        val repository = CareerShieldRepository(dao, inspectionDao)
        val initialChecklist = inspectionDao.getChecklistWithItemsById(checklistId).first()!!
        assertEquals(2, initialChecklist.items.size)
        assertEquals("PENDING", initialChecklist.checklist.overallStatus)

        // Marking one item FAIL sets overallStatus to FAILED_HOLD
        val firstItemId = initialChecklist.items[0].id
        val secondItemId = initialChecklist.items[1].id
        repository.updateChecklistItemResult(checklistId, firstItemId, "FAIL", "ขาด Toe board")
        val afterFail = inspectionDao.getChecklistWithItemsById(checklistId).first()!!
        assertEquals("FAILED_HOLD", afterFail.checklist.overallStatus)

        // Marking all items PASS sets overallStatus to PASSED
        repository.updateChecklistItemResult(checklistId, firstItemId, "PASS", "แก้ไขแล้ว")
        repository.updateChecklistItemResult(checklistId, secondItemId, "PASS", "ผ่านเกณฑ์")
        val afterAllPass = inspectionDao.getChecklistWithItemsById(checklistId).first()!!
        assertEquals("PASSED", afterAllPass.checklist.overallStatus)

        // Deleting parent RiskAssessmentReport sets linkedReportId to NULL (SET_NULL foreign key)
        inspectionDao.deleteRiskReport(filteredCritical.first())
        assertEquals(0, inspectionDao.observeOpenCriticalReportCount().first())
        val checklistAfterReportDelete = inspectionDao.getChecklistWithItemsById(checklistId).first()!!
        assertEquals(null, checklistAfterReportDelete.checklist.linkedReportId)

        // Deleting parent InspectionChecklist cascades and removes child items (CASCADE foreign key)
        inspectionDao.deleteChecklist(checklistAfterReportDelete.checklist)
        val orphanItems = inspectionDao.getItemsForChecklistOnce(checklistId)
        assertTrue(orphanItems.isEmpty())
    }

    @Test
    fun `test AppDatabase MIGRATION_3_4 upgrades v3 SQLite schema to v4 cleanly`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration_3_4.db"
        context.deleteDatabase(dbName)

        // Create a version 3 SQLite database on disk manually
        val v3Helper = object : SQLiteOpenHelper(context, dbName, null, 3) {
            override fun onCreate(sqliteDb: SQLiteDatabase) {
                sqliteDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS career_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        timeSlot TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        photoUri TEXT
                    )
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sos_scripts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        category TEXT NOT NULL,
                        riskLevel TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exception_audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        lotOrItemTitle TEXT NOT NULL,
                        areaOrScope TEXT NOT NULL,
                        baselineVersion TEXT NOT NULL,
                        aiFlagSummary TEXT NOT NULL,
                        decisionStatus TEXT NOT NULL,
                        justificationNote TEXT NOT NULL,
                        inferenceOrigin TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                sqliteDb.execSQL(
                    """
                    INSERT INTO career_tasks (title, description, category, timeSlot, isCompleted, timestamp)
                    VALUES ('09:00 Legacy Task', 'Existing v3 row', 'SAFETY', '09:00', 0, 1000)
                    """.trimIndent()
                )
            }

            override fun onUpgrade(sqliteDb: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        v3Helper.writableDatabase.close()
        v3Helper.close()

        // Open with Room v5 + MIGRATION_1_2..4_5 (without destructive migration fallback) to validate exact SQL schema
        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .allowMainThreadQueries()
            .build()
        try {
            val migratedTasks = migratedDb.taskDao().getAllTasks().first()
            assertEquals(1, migratedTasks.size)
            assertEquals("09:00 Legacy Task", migratedTasks.first().title)

            val insertedId = migratedDb.inspectionDao().insertRiskReport(
                RiskAssessmentReportEntity(
                    reportCode = "RPT-MIG-01",
                    siteZone = "Basement B1",
                    reportType = "SITE_SAFETY",
                    severityLevel = "HIGH",
                    hazardSummary = "น้ำซึมบ่อลิฟต์",
                    assessmentFindings = "พบน้ำขังใกล้ตู้ไฟ",
                    mitigationPlan = "สูบน้ำและตัดไฟชั่วคราว"
                )
            )
            assertTrue(insertedId > 0L)
            val reports = migratedDb.inspectionDao().getAllRiskReports().first()
            assertEquals(1, reports.size)
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test CareerShieldViewModel seeds inspection checklists and manages Room v4 reports`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val repository = CareerShieldRepository(dao, inspectionDao)
            val aiRepository = GeminiAiRepository()
            val viewModel = CareerShieldViewModel(repository, aiRepository)
            advanceUntilIdle()

            // Seeded checklists (CHK-SAFETY-001 & CHK-POUR-002)
            val seededChecklists = inspectionDao.getAllChecklistsWithItems().first { it.size == 2 }
            assertEquals(2, seededChecklists.size)
            assertEquals(3, seededChecklists[0].items.size)

            viewModel.saveSiteSafetyRiskReport(
                siteZone = "ชั้น 5 ด้านทิศเหนือ",
                hazardInput = "คนงานไม่สวม Safety Harness",
                analysis = "CRITICAL: ต้องสั่ง STOP WORK ทันทีจนกว่าจะสวมอุปกรณ์ครบ",
                severityLevel = "HIGH",
                inferenceSource = HybridInferenceSource.IN_CLOUD
            )
            advanceUntilIdle()

            val reports = inspectionDao.getAllRiskReports().first()
            assertEquals(1, reports.size)
            assertEquals("CRITICAL", reports.first().severityLevel)
            assertEquals("STOP_WORK", reports.first().status)

            viewModel.updateRiskReportStatus(reports.first().id, "MITIGATED")
            advanceUntilIdle()
            assertEquals("MITIGATED", inspectionDao.getAllRiskReports().first().first().status)

            viewModel.createInspectionChecklist(
                title = "ตรวจไฟฟ้าชั่วคราวหน้างาน",
                tradeCategory = "MEP",
                siteZone = "MDB Room",
                itemTitles = listOf("ตรวจสายดิน", "ตรวจ ELCB 30mA"),
                linkedReportId = reports.first().id
            )
            advanceUntilIdle()

            val allChecklists = inspectionDao.getAllChecklistsWithItems().first()
            assertEquals(3, allChecklists.size)
            val mepChecklist = allChecklists.first { it.checklist.tradeCategory == "MEP" }
            assertEquals(2, mepChecklist.items.size)

            viewModel.setInspectionItemResult(mepChecklist.checklist.id, mepChecklist.items[0].id, "PASS")
            viewModel.setInspectionItemResult(mepChecklist.checklist.id, mepChecklist.items[1].id, "PASS")
            advanceUntilIdle()

            val updatedMep = inspectionDao.getChecklistWithItemsById(mepChecklist.checklist.id).first()!!
            assertEquals("PASSED", updatedMep.checklist.overallStatus)

            viewModel.deleteInspectionChecklist(updatedMep.checklist)
            advanceUntilIdle()
            assertEquals(2, inspectionDao.getAllChecklistsWithItems().first().size)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test bilingual toggle updates ViewModel state, chat greeting, and clears stale offline error`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(
                    context,
                    FirebaseOptions.Builder()
                        .setApplicationId("1:000000000000:android:0000000000000000")
                        .setApiKey("test-api-key-placeholder")
                        .setProjectId("test-project-id")
                        .build()
                )
            }

            // Verify SharedPreferences persistence helper & values-en / values-th strings.xml
            AppLanguagePrefs.saveLanguage(context, AppLanguage.TH)
            assertEquals(AppLanguage.TH, AppLanguagePrefs.loadLanguage(context))
            AppLanguagePrefs.saveLanguage(context, AppLanguage.EN)
            assertEquals(AppLanguage.EN, AppLanguagePrefs.loadLanguage(context))

            val enConfig = Configuration(context.resources.configuration).apply {
                setLocale(Locale.ENGLISH)
            }
            val enContext = context.createConfigurationContext(enConfig)
            assertEquals("NANO 4oK", enContext.getString(R.string.app_name))

            val repository = CareerShieldRepository(dao, inspectionDao)
            val aiRepository = GeminiAiRepository()
            val viewModel = CareerShieldViewModel(repository, aiRepository)
            advanceUntilIdle()

            // Primary default is English
            assertEquals(AppLanguage.EN, viewModel.appLanguage.value)
            assertTrue(viewModel.chatMessages.value.first().text.contains("Hello!"))

            // Toggle to Thai
            val nextLang = viewModel.toggleAppLanguage()
            assertEquals(AppLanguage.TH, nextLang)
            assertEquals(AppLanguage.TH, viewModel.appLanguage.value)
            assertTrue(viewModel.chatMessages.value.first().text.contains("สวัสดีครับ"))
            assertEquals("พร้อมทำงาน", viewModel.lastInferenceState.value.featureLabel)

            // Trigger Offline Site Mode failure (OnDevice unavailable on JVM), then disable Offline mode
            viewModel.setOfflineSiteTestMode(true)
            val sampleScript = ScriptEntity(
                id = 1,
                title = "1. Decline Verbal Change Order",
                content = "Sample content",
                category = "CONTRACT"
            )
            viewModel.tailorSosScript(
                script = sampleScript,
                recipientName = "Project Director",
                reasonDetail = "Level 4",
                clauseNumber = "VO-12",
                customSituation = "Make it firm"
            )
            advanceUntilIdle()

            val errorState = viewModel.scriptTailorState.value
            assertTrue(errorState is ScriptTailorUiState.Error)
            assertTrue((errorState as ScriptTailorUiState.Error).isOnDeviceUnavailable)

            // Turning Offline Mode OFF must clear stale isOnDeviceUnavailable errors immediately
            viewModel.setOfflineSiteTestMode(false)
            assertTrue(viewModel.scriptTailorState.value is ScriptTailorUiState.Idle)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `test English CertifiedBaselineData lookup and GeminiAiRepository EN prompt builders`() {
        val enBaseline = CertifiedBaselineData.findRelevantBaselineText(
            query = "Level 1 floor 60 sqm ceramic tiles and adhesive",
            language = AppLanguage.EN
        )
        assertTrue(enBaseline.contains(CertifiedBaselineData.BASELINE_VERSION))
        assertTrue(enBaseline.contains("STD-FLOOR-60"))
        assertTrue(enBaseline.contains("Safe Norm:"))

        val aiRepository = GeminiAiRepository()
        val contractPromptEn = aiRepository.buildContractRiskPrompt(
            clauseText = "Liquidated Damages at 0.25% per day with zero EOT entitlement.",
            language = AppLanguage.EN
        )
        assertTrue(contractPromptEn.contains("Analyze the legal risk and Liquidated Damages (LD)"))
        assertTrue(contractPromptEn.contains("Liquidated Damages at 0.25% per day"))

        val sanityPromptEn = aiRepository.buildMaterialSanityCheckPrompt(
            areaScope = "Level 1 (60 sqm)",
            requestDescription = "Request 95 sqm tiles and 40 adhesive bags",
            baselineExcerpt = enBaseline,
            language = AppLanguage.EN
        )
        assertTrue(sanityPromptEn.contains("Respond concisely in English"))
        assertTrue(sanityPromptEn.contains("Anomaly Signal"))
        assertTrue(sanityPromptEn.contains("STD-FLOOR-60"))
    }

    @Test
    fun `test LocalizedSeedMapper translates seeded records to EN and leaves user entries untouched`() {
        val seededTask = TaskEntity(
            title = "08:30 ตรวจสอบรูปถ่าย Timestamp Camera (สตีล/ปูน)",
            description = "Original Thai desc",
            category = "FINANCE",
            timeSlot = "08:30"
        )
        val enTask = LocalizedSeedMapper.mapTask(seededTask, AppLanguage.EN)
        assertTrue(enTask.title.contains("Verify Timestamp Camera"))
        val thTask = LocalizedSeedMapper.mapTask(seededTask, AppLanguage.TH)
        assertEquals(seededTask.title, thTask.title)

        val userTask = TaskEntity(
            title = "Custom Steel Delivery Check",
            description = "User entered note",
            category = "SAFETY",
            timeSlot = "10:00"
        )
        val mappedUserTask = LocalizedSeedMapper.mapTask(userTask, AppLanguage.EN)
        assertEquals("Custom Steel Delivery Check", mappedUserTask.title)
        assertEquals("User entered note", mappedUserTask.description)

        val seededScript = ScriptEntity(
            title = "2. ขอขยายเวลาก่อสร้าง (EOT) เพื่อหยุดค่าปรับ LD",
            content = "Original Thai script",
            category = "EOT_LD"
        )
        val enScript = LocalizedSeedMapper.mapScript(seededScript, AppLanguage.EN)
        assertTrue(enScript.title.contains("Extension of Time (EOT)"))
        assertTrue(enScript.content.contains("[Reason / Site Zone]"))

        assertEquals(
            "Scaffolding & Working at Height Inspection (>= 2m)",
            LocalizedSeedMapper.mapChecklistTitle("CHK-SAFETY-001", "ตรวจนั่งร้าน", AppLanguage.EN)
        )
        assertEquals(
            "100% Safety Harness tie-off enforced for all workers",
            LocalizedSeedMapper.mapChecklistItemTitle("STD-SAFETY-2564", 3, "สายรัด", AppLanguage.EN)
        )
        assertEquals(
            "Main Site Zone",
            LocalizedSeedMapper.mapRiskZone("พื้นที่หน้างานหลัก", AppLanguage.EN)
        )
    }

    @Test
    fun `test FirebaseManager getDebugSecret and isAppCheckError helpers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedUuid = "aaaaaaaa-bbbb-4ccc-dddd-eeeeeeeeeeee"
        val intent = Intent().apply {
            putExtra("FIREBASE_APPCHECK_DEBUG_TOKEN", expectedUuid)
        }
        FirebaseManager.initializeAppCheck(context, intent)

        assertEquals(expectedUuid, FirebaseManager.getDebugSecret(context))
        assertTrue(FirebaseManager.isAppCheckError("Firebase App Check token is invalid."))
        assertTrue(FirebaseManager.isAppCheckError("HTTP 403 Forbidden"))
        assertTrue(FirebaseManager.isAppCheckError("App attestation failed"))
        assertFalse(FirebaseManager.isAppCheckError("On-device model is not available"))
        assertFalse(FirebaseManager.isAppCheckError(null))
    }

    @Test
    fun `test AppDatabase MIGRATION_4_5 upgrades v4 SQLite schema to v5 and supports SiteWalkDefectDao CRUD`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration_4_5.db"
        context.deleteDatabase(dbName)

        // Build v4 schema directly using Room's v4 migration SQL
        val rawDb = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(dbName).apply {
            parentFile?.mkdirs()
        }, null)
        rawDb.version = 4
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS career_tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                timeSlot TEXT NOT NULL,
                isCompleted INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                photoUri TEXT
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sos_scripts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                content TEXT NOT NULL,
                category TEXT NOT NULL,
                riskLevel TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS exception_audit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                lotOrItemTitle TEXT NOT NULL,
                areaOrScope TEXT NOT NULL,
                baselineVersion TEXT NOT NULL,
                aiFlagSummary TEXT NOT NULL,
                decisionStatus TEXT NOT NULL,
                justificationNote TEXT NOT NULL,
                inferenceOrigin TEXT NOT NULL,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS risk_assessment_reports (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reportCode TEXT NOT NULL,
                projectName TEXT NOT NULL,
                siteZone TEXT NOT NULL,
                reportType TEXT NOT NULL,
                severityLevel TEXT NOT NULL,
                hazardSummary TEXT NOT NULL,
                assessmentFindings TEXT NOT NULL,
                mitigationPlan TEXT NOT NULL,
                inspectorName TEXT NOT NULL,
                photoUri TEXT,
                inferenceOrigin TEXT NOT NULL,
                status TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_reportType ON risk_assessment_reports (reportType)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_severityLevel ON risk_assessment_reports (severityLevel)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_status ON risk_assessment_reports (status)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_risk_assessment_reports_createdAt ON risk_assessment_reports (createdAt)")
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspection_checklists (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                checklistCode TEXT NOT NULL,
                title TEXT NOT NULL,
                tradeCategory TEXT NOT NULL,
                siteZone TEXT NOT NULL,
                inspectorName TEXT NOT NULL,
                linkedReportId INTEGER,
                overallStatus TEXT NOT NULL,
                notes TEXT NOT NULL,
                inspectedAt INTEGER NOT NULL,
                FOREIGN KEY(linkedReportId) REFERENCES risk_assessment_reports(id) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_linkedReportId ON inspection_checklists (linkedReportId)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_tradeCategory ON inspection_checklists (tradeCategory)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklists_overallStatus ON inspection_checklists (overallStatus)")
        rawDb.execSQL(
            """
            CREATE TABLE IF NOT EXISTS inspection_checklist_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                checklistId INTEGER NOT NULL,
                sequenceOrder INTEGER NOT NULL,
                itemTitle TEXT NOT NULL,
                standardReference TEXT NOT NULL,
                resultStatus TEXT NOT NULL,
                isCriticalStopWork INTEGER NOT NULL,
                remarks TEXT NOT NULL,
                photoUri TEXT,
                checkedAt INTEGER,
                FOREIGN KEY(checklistId) REFERENCES inspection_checklists(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklist_items_checklistId ON inspection_checklist_items (checklistId)")
        rawDb.execSQL("CREATE INDEX IF NOT EXISTS index_inspection_checklist_items_resultStatus ON inspection_checklist_items (resultStatus)")
        rawDb.close()

        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5
            )
            .allowMainThreadQueries()
            .build()
        try {
            val defectDao = migratedDb.siteWalkDefectDao()
            val id = defectDao.insertDefect(
                SiteWalkDefectEntity(
                    pinNumber = 1,
                    floorZone = "Level 2 Master Bedroom",
                    tradeCategory = "OPENINGS",
                    openingCode = "W1",
                    measuredInput = "117.5 x 199 cm",
                    inspectorNote = "Narrower than W1 opening schedule by 25 mm",
                    severityLevel = "MAJOR",
                    responsibleParty = "MASONRY",
                    pinXRatio = 0.42f,
                    pinYRatio = 0.65f,
                    inferenceOrigin = "IN_CLOUD"
                )
            )
            assertTrue(id > 0L)
            assertEquals(1, defectDao.observeOpenDefectCount().first())
            assertEquals(1, defectDao.getMaxPinNumber())

            defectDao.updateDefectStatus(id, "FIXED")
            assertEquals(0, defectDao.observeOpenDefectCount().first())

            val saved = defectDao.getAllDefects().first().first()
            assertEquals("FIXED", saved.status)
            assertEquals("W1", saved.openingCode)

            defectDao.deleteDefect(saved)
            assertTrue(defectDao.getAllDefects().first().isEmpty())
        } finally {
            migratedDb.close()
            context.deleteDatabase(dbName)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `test Nano 4 Site Walk ViewModel defect saving, prompt builder, and offline error reset`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        try {
            val context = ApplicationProvider.getApplicationContext<Context>()
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(
                    context,
                    FirebaseOptions.Builder()
                        .setApplicationId("1:000000000000:android:0000000000000000")
                        .setApiKey("test-api-key-placeholder")
                        .setProjectId("test-project-id")
                        .build()
                )
            }

            val aiRepository = GeminiAiRepository()
            val thPrompt = aiRepository.buildHybridDefectTriagePrompt(
                floorZone = "ชั้น 2 ห้องนอนใหญ่",
                tradeCategory = "OPENINGS",
                openingCode = "W1",
                measuredInput = "117.5 x 199 ซม.",
                inspectorNote = "ช่องเปิดปูนแคบกว่าแบบ",
                language = AppLanguage.TH
            )
            assertTrue(thPrompt.contains("NANO 4oK"))
            assertTrue(thPrompt.contains("W1"))
            assertTrue(thPrompt.contains("117.5 x 199"))

            val enPrompt = aiRepository.buildHybridDefectTriagePrompt(
                floorZone = "Level 2 Master Bed",
                tradeCategory = "OPENINGS",
                openingCode = "W1",
                measuredInput = "117.5 x 199 cm",
                inspectorNote = "Rough opening narrow",
                language = AppLanguage.EN
            )
            assertTrue(enPrompt.contains("MASONRY / ALUMINUM"))

            val repository = CareerShieldRepository(dao, inspectionDao, db.siteWalkDefectDao())
            val viewModel = CareerShieldViewModel(repository, aiRepository)
            advanceUntilIdle()

            // Save a defect pin directly into Room DB v5
            viewModel.saveSiteWalkDefect(
                floorZone = "Level 2 Master Bedroom",
                tradeCategory = "OPENINGS",
                openingCode = "W1",
                measuredInput = "117.5 x 199 cm",
                inspectorNote = "Masonry opening narrower than W1 spec",
                aiAnalysis = "Masonry to chisel 25 mm clearance.",
                severityLevel = "CRITICAL",
                responsibleParty = "MASONRY",
                photoUri = null,
                pdfPlanUri = null,
                pdfPageIndex = 0,
                pinXRatio = 0.5f,
                pinYRatio = 0.5f,
                inferenceOrigin = "IN_CLOUD"
            )
            advanceUntilIdle()

            val defects = db.siteWalkDefectDao().getAllDefects().first()
            assertEquals(1, defects.size)
            assertEquals(1, defects.first().pinNumber)
            assertEquals("OPEN", defects.first().status)

            viewModel.toggleSiteWalkDefectStatus(defects.first())
            advanceUntilIdle()
            assertEquals("FIXED", db.siteWalkDefectDao().getAllDefects().first().first().status)

            // Offline triage fails with OnDeviceUnavailable on JVM, then clears when offline mode disabled
            viewModel.setOfflineSiteTestMode(true)
            viewModel.analyzeSiteWalkDefectHybrid(
                floorZone = "Level 2",
                tradeCategory = "OPENINGS",
                openingCode = "W1",
                measuredInput = "117.5 x 199 cm",
                inspectorNote = "Check opening tolerance"
            )
            advanceUntilIdle()

            val defectErr = viewModel.defectAiState.value
            assertTrue(defectErr is DefectAiUiState.Error)
            assertTrue((defectErr as DefectAiUiState.Error).isOnDeviceUnavailable)

            viewModel.setOfflineSiteTestMode(false)
            assertTrue(viewModel.defectAiState.value is DefectAiUiState.Idle)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
