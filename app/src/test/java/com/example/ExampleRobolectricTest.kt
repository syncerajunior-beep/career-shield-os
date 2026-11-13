package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiInferenceOrigin
import com.example.data.AppDatabase
import com.example.data.CareerShieldRepository
import com.example.data.GeminiAiRepository
import com.example.data.TaskDao
import com.example.data.TaskEntity
import com.example.data.toOrigin
import com.example.util.FirebaseManager
import com.example.viewmodel.CareerShieldViewModel
import com.example.viewmodel.HybridInferenceSource
import com.example.viewmodel.SiteSafetyRiskUiState
import com.example.viewmodel.toHybridSource
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Career Shield", appName)
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
}
