package com.example.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AiInferenceOrigin
import com.example.data.CareerShieldRepository
import com.example.data.ChecklistWithItems
import com.example.data.ExceptionAuditEntity
import com.example.data.GeminiAiRepository
import com.example.data.InspectionChecklistEntity
import com.example.data.InspectionChecklistItemEntity
import com.example.data.MaterialSanityCheckResult
import com.example.data.Nano4DiagnosticResult
import com.example.data.NanoDiagnosticLanguage
import com.example.data.RiskAssessmentReportEntity
import com.example.data.ScriptEntity
import com.example.data.SiteWalkDefectEntity
import com.example.data.TaskEntity
import com.example.ui.localization.AppLanguage
import com.example.util.PdfBlueprintHelper
import com.example.util.SitePhotoStorage
import com.google.firebase.ai.Chat
import com.google.firebase.ai.DownloadStatus
import com.google.firebase.ai.InferenceSource
import com.google.firebase.ai.type.FirebaseAIOnDeviceNotAvailableException
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "CareerShieldVM"

enum class HybridInferenceSource {
    ON_DEVICE,
    IN_CLOUD,
    STANDBY
}

@OptIn(PublicPreviewAPI::class)
fun InferenceSource?.toHybridSource(): HybridInferenceSource =
    if (this == InferenceSource.ON_DEVICE) {
        HybridInferenceSource.ON_DEVICE
    } else {
        HybridInferenceSource.IN_CLOUD
    }

fun AiInferenceOrigin.toHybridSource(): HybridInferenceSource =
    if (this == AiInferenceOrigin.ON_DEVICE) {
        HybridInferenceSource.ON_DEVICE
    } else {
        HybridInferenceSource.IN_CLOUD
    }

data class LastInferenceInfo(
    val source: HybridInferenceSource = HybridInferenceSource.STANDBY,
    val featureLabel: String = "Ready",
    val verifiedModelName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class GroundingSource(
    val title: String,
    val uri: String,
    val domain: String
)

sealed interface ContractRiskUiState {
    data object Idle : ContractRiskUiState
    data object Loading : ContractRiskUiState
    data class Streaming(
        val partialAnalysis: String,
        val thoughts: String = ""
    ) : ContractRiskUiState
    data class Success(
        val analysis: String,
        val thoughts: String = "",
        val searchQueries: List<String> = emptyList(),
        val sources: List<GroundingSource> = emptyList(),
        val inferenceSource: HybridInferenceSource = HybridInferenceSource.IN_CLOUD
    ) : ContractRiskUiState
    data class Error(val message: String) : ContractRiskUiState
}

sealed interface SitePhotoInspectUiState {
    data object Idle : SitePhotoInspectUiState
    data object Loading : SitePhotoInspectUiState
    data class Success(
        val photoUri: String,
        val analysis: String,
        val inferenceSource: HybridInferenceSource = HybridInferenceSource.IN_CLOUD
    ) : SitePhotoInspectUiState
    data class Error(val message: String) : SitePhotoInspectUiState
}

sealed interface SiteSafetyRiskUiState {
    data object Idle : SiteSafetyRiskUiState
    data object Loading : SiteSafetyRiskUiState
    data class Success(
        val hazardInput: String,
        val analysis: String,
        val inferenceSource: HybridInferenceSource
    ) : SiteSafetyRiskUiState
    data class Error(
        val message: String,
        val isOnDeviceUnavailable: Boolean = false
    ) : SiteSafetyRiskUiState
}

sealed interface ScriptTailorUiState {
    data object Idle : ScriptTailorUiState
    data class Loading(val scriptTitle: String) : ScriptTailorUiState
    data class Success(
        val originalTitle: String,
        val tailoredContent: String,
        val inferenceSource: HybridInferenceSource
    ) : ScriptTailorUiState
    data class Error(
        val message: String,
        val isOnDeviceUnavailable: Boolean = false
    ) : ScriptTailorUiState
}

sealed interface HybridOpsUiState {
    data object Idle : HybridOpsUiState
    data class Loading(val taskLabel: String) : HybridOpsUiState
    data class Success(
        val taskTitle: String,
        val resultText: String,
        val inferenceSource: HybridInferenceSource
    ) : HybridOpsUiState
    data class Error(
        val message: String,
        val isOnDeviceUnavailable: Boolean = false
    ) : HybridOpsUiState
}

sealed interface AiThinkingUiState {
    data object Idle : AiThinkingUiState
    data object Loading : AiThinkingUiState
    data class Streaming(val thoughts: String, val answer: String) : AiThinkingUiState
    data class Success(
        val thoughts: String,
        val answer: String,
        val inferenceSource: HybridInferenceSource = HybridInferenceSource.IN_CLOUD
    ) : AiThinkingUiState
    data class Error(val message: String) : AiThinkingUiState
}

data class GeneratedActionPack(
    val scriptTitle: String,
    val scriptContent: String,
    val scriptCategory: String,
    val suggestedTasks: List<TaskEntity>,
    val inferenceSource: HybridInferenceSource = HybridInferenceSource.IN_CLOUD
)

sealed interface AiGeneratorUiState {
    data object Idle : AiGeneratorUiState
    data object Loading : AiGeneratorUiState
    data class Success(val pack: GeneratedActionPack) : AiGeneratorUiState
    data class Error(val message: String) : AiGeneratorUiState
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val sources: List<GroundingSource> = emptyList(),
    val isStreaming: Boolean = false,
    val inferenceSource: HybridInferenceSource? = null
)

sealed interface LiveVoiceUiState {
    data object Idle : LiveVoiceUiState
    data object Connecting : LiveVoiceUiState
    data class Active(
        val userTranscript: String = "",
        val modelTranscript: String = ""
    ) : LiveVoiceUiState
    data class Error(val message: String) : LiveVoiceUiState
}

sealed interface NanoDiagnosticUiState {
    data object Idle : NanoDiagnosticUiState
    data class Running(val language: NanoDiagnosticLanguage) : NanoDiagnosticUiState
    data class Success(
        val result: Nano4DiagnosticResult,
        val actualSource: HybridInferenceSource
    ) : NanoDiagnosticUiState
    data class UnavailableOrError(
        val result: Nano4DiagnosticResult
    ) : NanoDiagnosticUiState
}

sealed interface MaterialSanityCheckUiState {
    data object Idle : MaterialSanityCheckUiState
    data object Loading : MaterialSanityCheckUiState
    data class Success(
        val result: MaterialSanityCheckResult,
        val inferenceSource: HybridInferenceSource
    ) : MaterialSanityCheckUiState
    data class Error(
        val message: String,
        val isOnDeviceUnavailable: Boolean = false
    ) : MaterialSanityCheckUiState
}

sealed interface DefectAiUiState {
    data object Idle : DefectAiUiState
    data class Loading(val modeLabel: String) : DefectAiUiState
    data class Success(
        val analysis: String,
        val inferenceSource: HybridInferenceSource,
        val suggestedSeverity: String = "MAJOR",
        val suggestedParty: String = "CONTRACTOR",
        val usedPdfPlan: Boolean = false
    ) : DefectAiUiState
    data class Error(
        val message: String,
        val isOnDeviceUnavailable: Boolean = false
    ) : DefectAiUiState
}

@OptIn(ExperimentalCoroutinesApi::class, PublicPreviewAPI::class)
class CareerShieldViewModel(
    private val repository: CareerShieldRepository,
    private val aiRepository: GeminiAiRepository = GeminiAiRepository()
) : ViewModel() {

    private val _appLanguage = MutableStateFlow(AppLanguage.EN)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        category to query
    }.flatMapLatest { (category, query) ->
        repository.getFilteredTasks(category = category, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val scripts: StateFlow<List<ScriptEntity>> = repository.allScripts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val exceptionAudits: StateFlow<List<ExceptionAuditEntity>> = repository.allExceptionAudits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val riskReports: StateFlow<List<RiskAssessmentReportEntity>> = repository.allRiskReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val checklistsWithItems: StateFlow<List<ChecklistWithItems>> = repository.allChecklistsWithItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val openCriticalReportCount: StateFlow<Int> = repository.openCriticalReportCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val siteWalkDefects: StateFlow<List<SiteWalkDefectEntity>> = repository.allSiteWalkDefects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val openSiteWalkDefectCount: StateFlow<Int> = repository.openSiteWalkDefectCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _offlineSiteTestMode = MutableStateFlow(false)
    val offlineSiteTestMode: StateFlow<Boolean> = _offlineSiteTestMode.asStateFlow()

    private val _lastInferenceState = MutableStateFlow(LastInferenceInfo())
    val lastInferenceState: StateFlow<LastInferenceInfo> = _lastInferenceState.asStateFlow()

    private val _onDeviceStatusLabel = MutableStateFlow("Checking AICore / OnDeviceModelOption.PREVIEW...")
    val onDeviceStatusLabel: StateFlow<String> = _onDeviceStatusLabel.asStateFlow()

    private val _isOnDeviceDownloadable = MutableStateFlow(false)
    val isOnDeviceDownloadable: StateFlow<Boolean> = _isOnDeviceDownloadable.asStateFlow()

    private val _verifiedOnDeviceModelName = MutableStateFlow<String?>(null)
    val verifiedOnDeviceModelName: StateFlow<String?> = _verifiedOnDeviceModelName.asStateFlow()

    private val _deviceCompatibilitySummary = MutableStateFlow("")
    val deviceCompatibilitySummary: StateFlow<String> = _deviceCompatibilitySummary.asStateFlow()

    private val _nanoDiagnosticState = MutableStateFlow<NanoDiagnosticUiState>(NanoDiagnosticUiState.Idle)
    val nanoDiagnosticState: StateFlow<NanoDiagnosticUiState> = _nanoDiagnosticState.asStateFlow()

    private val _materialSanityCheckState = MutableStateFlow<MaterialSanityCheckUiState>(MaterialSanityCheckUiState.Idle)
    val materialSanityCheckState: StateFlow<MaterialSanityCheckUiState> = _materialSanityCheckState.asStateFlow()

    private val _defectAiState = MutableStateFlow<DefectAiUiState>(DefectAiUiState.Idle)
    val defectAiState: StateFlow<DefectAiUiState> = _defectAiState.asStateFlow()

    private val _siteSafetyRiskState = MutableStateFlow<SiteSafetyRiskUiState>(SiteSafetyRiskUiState.Idle)
    val siteSafetyRiskState: StateFlow<SiteSafetyRiskUiState> = _siteSafetyRiskState.asStateFlow()

    private val _scriptTailorState = MutableStateFlow<ScriptTailorUiState>(ScriptTailorUiState.Idle)
    val scriptTailorState: StateFlow<ScriptTailorUiState> = _scriptTailorState.asStateFlow()

    private val _hybridOpsState = MutableStateFlow<HybridOpsUiState>(HybridOpsUiState.Idle)
    val hybridOpsState: StateFlow<HybridOpsUiState> = _hybridOpsState.asStateFlow()

    private val _sitePhotoInspectState = MutableStateFlow<SitePhotoInspectUiState>(SitePhotoInspectUiState.Idle)
    val sitePhotoInspectState: StateFlow<SitePhotoInspectUiState> = _sitePhotoInspectState.asStateFlow()

    private val _contractRiskState = MutableStateFlow<ContractRiskUiState>(ContractRiskUiState.Idle)
    val contractRiskState: StateFlow<ContractRiskUiState> = _contractRiskState.asStateFlow()

    private val _thinkingState = MutableStateFlow<AiThinkingUiState>(AiThinkingUiState.Idle)
    val thinkingState: StateFlow<AiThinkingUiState> = _thinkingState.asStateFlow()

    private val _generatorState = MutableStateFlow<AiGeneratorUiState>(AiGeneratorUiState.Idle)
    val generatorState: StateFlow<AiGeneratorUiState> = _generatorState.asStateFlow()

    private fun initialGreetingFor(language: AppLanguage): String = when (language) {
        AppLanguage.TH -> "สวัสดีครับ ผมคือที่ปรึกษากฎหมายสัญญาก่อสร้างและผู้เชี่ยวชาญการบริหารความเสี่ยงหน้างาน (CAREER SHIELD AI) คุณสามารถส่งข้อสัญญาที่กังวล ปัญหาค่าปรับ LD หรือปรึกษาการเจรจากับผู้ว่าจ้าง/ผู้รับเหมาช่วงได้เลยครับ"
        AppLanguage.EN -> "Hello! I am CAREER SHIELD AI — your Construction Contract Law & Site Risk Advisor. Share any risky contract clause, LD exposure, EOT dispute, or subcontractor issue to get actionable protection immediately."
    }

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = 1L,
                isUser = false,
                text = initialGreetingFor(AppLanguage.EN)
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _liveVoiceState = MutableStateFlow<LiveVoiceUiState>(LiveVoiceUiState.Idle)
    val liveVoiceState: StateFlow<LiveVoiceUiState> = _liveVoiceState.asStateFlow()

    private var activeLiveSession: LiveSession? = null
    private var chatSession: Chat? = null

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
        refreshOnDeviceStatus()
    }

    fun setAppLanguage(language: AppLanguage) {
        if (_appLanguage.value == language) return
        _appLanguage.value = language
        chatSession = null
        val currentMsgs = _chatMessages.value
        if (currentMsgs.size == 1 && !currentMsgs.first().isUser) {
            _chatMessages.value = listOf(
                currentMsgs.first().copy(text = initialGreetingFor(language))
            )
        }
        if (_lastInferenceState.value.source == HybridInferenceSource.STANDBY) {
            _lastInferenceState.value = _lastInferenceState.value.copy(
                featureLabel = if (language == AppLanguage.EN) "Ready" else "พร้อมทำงาน"
            )
        }
        refreshOnDeviceStatus()
    }

    fun toggleAppLanguage(): AppLanguage {
        val next = if (_appLanguage.value == AppLanguage.TH) AppLanguage.EN else AppLanguage.TH
        setAppLanguage(next)
        return next
    }

    private fun isEnglish(): Boolean = _appLanguage.value == AppLanguage.EN

    private fun recordInferenceSource(
        source: HybridInferenceSource,
        featureLabel: String,
        verifiedModelName: String? = _verifiedOnDeviceModelName.value
    ) {
        _lastInferenceState.value = LastInferenceInfo(
            source = source,
            featureLabel = featureLabel,
            verifiedModelName = if (source == HybridInferenceSource.ON_DEVICE) verifiedModelName else null,
            timestamp = System.currentTimeMillis()
        )
    }

    fun toggleOfflineSiteTestMode() {
        setOfflineSiteTestMode(!_offlineSiteTestMode.value)
    }

    private fun isOfflineOnDeviceFailure(e: Throwable): Boolean =
        _offlineSiteTestMode.value ||
            e is FirebaseAIOnDeviceNotAvailableException ||
            e.cause is FirebaseAIOnDeviceNotAvailableException ||
            e.message?.contains("on-device", ignoreCase = true) == true ||
            e.message?.contains("aicore", ignoreCase = true) == true

    fun setOfflineSiteTestMode(enabled: Boolean) {
        _offlineSiteTestMode.value = enabled
        if (!enabled) {
            if ((_scriptTailorState.value as? ScriptTailorUiState.Error)?.isOnDeviceUnavailable == true) {
                _scriptTailorState.value = ScriptTailorUiState.Idle
            }
            if ((_hybridOpsState.value as? HybridOpsUiState.Error)?.isOnDeviceUnavailable == true) {
                _hybridOpsState.value = HybridOpsUiState.Idle
            }
            if ((_siteSafetyRiskState.value as? SiteSafetyRiskUiState.Error)?.isOnDeviceUnavailable == true) {
                _siteSafetyRiskState.value = SiteSafetyRiskUiState.Idle
            }
            if ((_materialSanityCheckState.value as? MaterialSanityCheckUiState.Error)?.isOnDeviceUnavailable == true) {
                _materialSanityCheckState.value = MaterialSanityCheckUiState.Idle
            }
            if ((_defectAiState.value as? DefectAiUiState.Error)?.isOnDeviceUnavailable == true) {
                _defectAiState.value = DefectAiUiState.Idle
            }
        }
    }

    fun refreshOnDeviceStatus() {
        viewModelScope.launch {
            val capability = aiRepository.checkOnDeviceCapability(_appLanguage.value)
            _onDeviceStatusLabel.value = capability.statusLabel
            _isOnDeviceDownloadable.value = capability.isDownloadable
            _verifiedOnDeviceModelName.value = capability.detectedModelName
            _deviceCompatibilitySummary.value = capability.deviceSummary
        }
    }

    fun runNano4PreviewDiagnostic(language: NanoDiagnosticLanguage) {
        viewModelScope.launch {
            _nanoDiagnosticState.value = NanoDiagnosticUiState.Running(language)
            val result = aiRepository.runNano4PreviewDiagnostic(language)
            if (result.detectedModelName != null) {
                _verifiedOnDeviceModelName.value = result.detectedModelName
            }
            if (result.isSuccess && result.inferenceSource != null) {
                val actualSource = result.inferenceSource.toHybridSource()
                recordInferenceSource(
                    source = actualSource,
                    featureLabel = "Diagnostic (${language.name})",
                    verifiedModelName = result.detectedModelName
                )
                _nanoDiagnosticState.value = NanoDiagnosticUiState.Success(
                    result = result,
                    actualSource = actualSource
                )
            } else {
                _nanoDiagnosticState.value = NanoDiagnosticUiState.UnavailableOrError(result)
            }
        }
    }

    fun clearNanoDiagnostic() {
        _nanoDiagnosticState.value = NanoDiagnosticUiState.Idle
    }

    fun downloadOnDeviceModel() {
        viewModelScope.launch {
            val en = isEnglish()
            try {
                val downloadFlow = aiRepository.downloadOnDeviceModel() ?: run {
                    _onDeviceStatusLabel.value = if (en) {
                        "Device does not support AICore Preview (Using Cloud Fallback)"
                    } else {
                        "อุปกรณ์ไม่รองรับ AICore Preview (ใช้ Cloud Fallback)"
                    }
                    return@launch
                }
                _onDeviceStatusLabel.value = if (en) {
                    "Starting On-Device Preview model download..."
                } else {
                    "กำลังเริ่มดาวน์โหลดโมเดล On-Device Preview..."
                }
                downloadFlow.collect { downloadStatus ->
                    when (downloadStatus) {
                        is DownloadStatus.DownloadStarted -> {
                            _onDeviceStatusLabel.value = if (en) "Model download started..." else "เริ่มดาวน์โหลดโมเดล..."
                        }
                        is DownloadStatus.DownloadInProgress -> {
                            val mb = downloadStatus.totalBytesDownloaded / (1024 * 1024)
                            _onDeviceStatusLabel.value = if (en) {
                                "Downloading On-Device model ($mb MB)..."
                            } else {
                                "กำลังดาวน์โหลดโมเดล On-Device ($mb MB)..."
                            }
                        }
                        is DownloadStatus.DownloadCompleted -> {
                            _isOnDeviceDownloadable.value = false
                            refreshOnDeviceStatus()
                        }
                        is DownloadStatus.DownloadFailed -> {
                            _onDeviceStatusLabel.value = if (en) {
                                "On-Device download failed: ${downloadStatus.exception.localizedMessage ?: "error"}"
                            } else {
                                "ดาวน์โหลด On-Device ไม่สำเร็จ: ${downloadStatus.exception.localizedMessage ?: "error"}"
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download on-device model", e)
                _onDeviceStatusLabel.value = if (en) {
                    "On-Device download failed (Using Cloud Fallback)"
                } else {
                    "ดาวน์โหลด On-Device ไม่สำเร็จ (ใช้ Cloud Fallback)"
                }
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun addTask(
        title: String,
        description: String,
        category: String,
        timeSlot: String,
        photoUri: String? = null
    ) {
        if (title.isBlank()) return
        val defaultDesc = if (isEnglish()) {
            "Site risk verification checkpoint per Career Shield standard"
        } else {
            "ภารกิจตรวจสอบหน้างานตามมาตรฐาน Career Shield"
        }
        viewModelScope.launch {
            val formattedTitle = if (title.matches(Regex("^\\d{2}:\\d{2}.*"))) {
                title.trim()
            } else {
                "$timeSlot ${title.trim()}"
            }
            repository.insertTask(
                TaskEntity(
                    title = formattedTitle,
                    description = description.ifBlank { defaultDesc },
                    category = category,
                    timeSlot = timeSlot,
                    isCompleted = false,
                    photoUri = photoUri
                )
            )
        }
    }

    fun updateTaskDetails(
        task: TaskEntity,
        newTitle: String,
        newDescription: String,
        newCategory: String,
        newTimeSlot: String,
        newPhotoUri: String? = task.photoUri
    ) {
        if (newTitle.isBlank()) return
        val defaultDesc = if (isEnglish()) {
            "Site risk verification checkpoint per Career Shield standard"
        } else {
            "ภารกิจตรวจสอบหน้างานตามมาตรฐาน Career Shield"
        }
        viewModelScope.launch {
            val cleanTitle = newTitle.trim().replace(Regex("^\\d{2}:\\d{2}\\s*"), "")
            val formattedTitle = "$newTimeSlot $cleanTitle".trim()
            repository.updateTask(
                task.copy(
                    title = formattedTitle,
                    description = newDescription.trim().ifBlank { defaultDesc },
                    category = newCategory,
                    timeSlot = newTimeSlot,
                    photoUri = newPhotoUri
                )
            )
        }
    }

    fun updateTaskPhoto(task: TaskEntity, photoUri: String?) {
        viewModelScope.launch {
            repository.updateTask(task.copy(photoUri = photoUri))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun resetDailyChecklist() {
        viewModelScope.launch {
            repository.resetAllTasks()
        }
    }

    fun addScript(title: String, content: String, category: String, riskLevel: String = "CRITICAL") {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.insertScript(
                ScriptEntity(
                    title = title.trim(),
                    content = content.trim(),
                    category = category,
                    riskLevel = riskLevel
                )
            )
        }
    }

    fun deleteScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.deleteScript(script)
        }
    }

    fun saveGeneratedPackToRoom(pack: GeneratedActionPack) {
        viewModelScope.launch {
            repository.insertScript(
                ScriptEntity(
                    title = pack.scriptTitle,
                    content = pack.scriptContent,
                    category = pack.scriptCategory,
                    riskLevel = "CRITICAL"
                )
            )
            pack.suggestedTasks.forEach { task ->
                repository.insertTask(task)
            }
            _generatorState.value = AiGeneratorUiState.Idle
        }
    }

    private fun onDeviceUnavailableMsg(lang: AppLanguage, detail: String?): String =
        if (lang == AppLanguage.EN) {
            "Offline Site Mode (ONLY_ON_DEVICE) requires AICore / Gemini Nano on a supported device, but this device has no offline model (${detail ?: "Unavailable"}). Disable Offline Mode to use Cloud Gemini Fallback."
        } else {
            "โหมด Offline Site (ONLY_ON_DEVICE) ต้องการ AICore / Gemini Nano บนอุปกรณ์ที่รองรับ แต่เครื่องนี้ไม่มีโมเดลออฟไลน์ในระบบ (${detail ?: "Unavailable"}) กดปิด Offline Mode เพื่อสลับกลับ Cloud Fallback"
        }

    // Construction Site Safety Risk Analysis via GeminiAiRepository
    fun analyzeSiteSafetyRisk(hazardInput: String) {
        val trimmed = hazardInput.trim()
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        if (trimmed.isEmpty()) {
            _siteSafetyRiskState.value = SiteSafetyRiskUiState.Error(
                message = if (en) {
                    "Please enter site conditions or observed safety hazards first."
                } else {
                    "กรุณาพิมพ์รายละเอียดสภาพหน้างานหรือความเสี่ยงที่พบก่อนวิเคราะห์"
                }
            )
            return
        }

        viewModelScope.launch {
            _siteSafetyRiskState.value = SiteSafetyRiskUiState.Loading
            try {
                val result = aiRepository.analyzeSiteSafetyRisks(
                    siteHazardText = trimmed,
                    offlineOnly = _offlineSiteTestMode.value,
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "Site Safety AI" else "วิเคราะห์ Safety หน้างาน")
                _siteSafetyRiskState.value = SiteSafetyRiskUiState.Success(
                    hazardInput = result.hazardInput,
                    analysis = result.analysis,
                    inferenceSource = source
                )
            } catch (e: FirebaseAIOnDeviceNotAvailableException) {
                Log.w(TAG, "On-device Gemini Nano unavailable for safety risk analysis", e)
                _siteSafetyRiskState.value = SiteSafetyRiskUiState.Error(
                    message = onDeviceUnavailableMsg(lang, e.localizedMessage),
                    isOnDeviceUnavailable = true
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Safety risk analysis timed out", e)
                _siteSafetyRiskState.value = SiteSafetyRiskUiState.Error(
                    message = if (en) {
                        "Safety risk analysis timed out (60s). Please try again."
                    } else {
                        "การวิเคราะห์ความเสี่ยงความปลอดภัยหมดเวลา (60 วินาที) กรุณาลองใหม่"
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Safety risk analysis failed", e)
                val isUnavailable = isOfflineOnDeviceFailure(e)
                _siteSafetyRiskState.value = SiteSafetyRiskUiState.Error(
                    message = if (isUnavailable) {
                        onDeviceUnavailableMsg(lang, e.localizedMessage)
                    } else {
                        e.localizedMessage ?: if (en) "Failed to analyze safety risk" else "ไม่สามารถวิเคราะห์ความเสี่ยงความปลอดภัยได้"
                    },
                    isOnDeviceUnavailable = isUnavailable
                )
            }
        }
    }

    fun clearSiteSafetyRisk() {
        _siteSafetyRiskState.value = SiteSafetyRiskUiState.Idle
    }

    fun saveSafetyMitigationToRoom(hazardInput: String, analysis: String) {
        val trimmedHazard = hazardInput.trim()
        if (trimmedHazard.isEmpty()) return
        val currentSlot = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        val prefix = if (isEnglish()) "Fix Hazard:" else "แก้ไขจุดเสี่ยง:"
        val shortTitle = if (trimmedHazard.length > 42) {
            "$prefix ${trimmedHazard.take(42)}..."
        } else {
            "$prefix $trimmedHazard"
        }
        addTask(
            title = shortTitle,
            description = analysis.take(400),
            category = "SAFETY",
            timeSlot = currentSlot
        )
    }

    fun saveSiteSafetyRiskReport(
        siteZone: String,
        hazardInput: String,
        analysis: String,
        severityLevel: String = "HIGH",
        inferenceSource: HybridInferenceSource = HybridInferenceSource.IN_CLOUD
    ) {
        val trimmedHazard = hazardInput.trim()
        if (trimmedHazard.isEmpty()) return
        val en = isEnglish()
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val codeSuffix = SimpleDateFormat("MMddHHmmss", Locale.US).format(Date(timestamp))
            val origin = when (inferenceSource) {
                HybridInferenceSource.ON_DEVICE -> "ON_DEVICE"
                HybridInferenceSource.IN_CLOUD -> "IN_CLOUD"
                HybridInferenceSource.STANDBY -> "MANUAL"
            }
            val inferredSeverity = when {
                analysis.contains("CRITICAL", ignoreCase = true) ||
                    analysis.contains("หยุดงาน", ignoreCase = true) ||
                    analysis.contains("STOP", ignoreCase = true) -> "CRITICAL"
                else -> severityLevel
            }
            repository.insertRiskReport(
                RiskAssessmentReportEntity(
                    reportCode = "RPT-SAFE-$codeSuffix",
                    siteZone = siteZone.trim().ifBlank {
                        if (en) "Main Site Zone" else "พื้นที่หน้างานหลัก"
                    },
                    reportType = "SITE_SAFETY",
                    severityLevel = inferredSeverity,
                    hazardSummary = trimmedHazard.take(180),
                    assessmentFindings = analysis.trim(),
                    mitigationPlan = if (en) {
                        "Implement engineering controls and verify full PPE compliance before resuming work."
                    } else {
                        "ดำเนินการแก้ไขตามมาตรการความปลอดภัยและตรวจสอบ PPE ก่อนอนุญาตให้ทำงานต่อ"
                    },
                    inferenceOrigin = origin,
                    status = if (inferredSeverity == "CRITICAL") "STOP_WORK" else "OPEN",
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            )
        }
    }

    fun createRiskAssessmentReport(
        siteZone: String,
        reportType: String,
        severityLevel: String,
        hazardSummary: String,
        assessmentFindings: String,
        mitigationPlan: String,
        inspectorName: String = "Site Risk Manager"
    ) {
        if (hazardSummary.isBlank()) return
        val en = isEnglish()
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val codeSuffix = SimpleDateFormat("MMddHHmmss", Locale.US).format(Date(timestamp))
            repository.insertRiskReport(
                RiskAssessmentReportEntity(
                    reportCode = "RPT-${reportType.take(4).uppercase(Locale.US)}-$codeSuffix",
                    siteZone = siteZone.trim().ifBlank { if (en) "Main Site Zone" else "พื้นที่หน้างานหลัก" },
                    reportType = reportType,
                    severityLevel = severityLevel,
                    hazardSummary = hazardSummary.trim(),
                    assessmentFindings = assessmentFindings.trim().ifBlank { hazardSummary.trim() },
                    mitigationPlan = mitigationPlan.trim().ifBlank {
                        if (en) "Monitor and re-inspect at scheduled shift intervals." else "เฝ้าระวังและตรวจสอบซ้ำตามรอบงาน"
                    },
                    inspectorName = inspectorName.trim().ifBlank { "Site Risk Manager" },
                    inferenceOrigin = "MANUAL",
                    status = if (severityLevel == "CRITICAL") "STOP_WORK" else "OPEN",
                    createdAt = timestamp,
                    updatedAt = timestamp
                )
            )
        }
    }

    fun updateRiskReportStatus(reportId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateRiskReportStatus(reportId, newStatus)
        }
    }

    fun deleteRiskReport(report: RiskAssessmentReportEntity) {
        viewModelScope.launch {
            repository.deleteRiskReport(report)
        }
    }

    fun createInspectionChecklist(
        title: String,
        tradeCategory: String,
        siteZone: String,
        itemTitles: List<String>,
        linkedReportId: Long? = null,
        inspectorName: String = "Site Engineer"
    ) {
        val cleanItems = itemTitles.map { it.trim() }.filter { it.isNotEmpty() }
        if (title.isBlank() || cleanItems.isEmpty()) return
        val en = isEnglish()
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val codeSuffix = SimpleDateFormat("MMddHHmmss", Locale.US).format(Date(timestamp))
            val checklist = InspectionChecklistEntity(
                checklistCode = "CHK-${tradeCategory.take(4).uppercase(Locale.US)}-$codeSuffix",
                title = title.trim(),
                tradeCategory = tradeCategory,
                siteZone = siteZone.trim().ifBlank { if (en) "Main Site Zone" else "พื้นที่หน้างานหลัก" },
                inspectorName = inspectorName.trim().ifBlank { "Site Engineer" },
                linkedReportId = linkedReportId,
                overallStatus = "PENDING",
                inspectedAt = timestamp
            )
            val itemEntities = cleanItems.mapIndexed { index, itemTitle ->
                InspectionChecklistItemEntity(
                    checklistId = 0L,
                    sequenceOrder = index + 1,
                    itemTitle = itemTitle,
                    standardReference = "SITE-QAQC",
                    resultStatus = "PENDING",
                    isCriticalStopWork = tradeCategory == "SAFETY" || tradeCategory == "STRUCTURAL"
                )
            }
            repository.insertChecklistWithItems(checklist, itemEntities)
        }
    }

    fun setInspectionItemResult(
        checklistId: Long,
        itemId: Long,
        resultStatus: String,
        remarks: String = ""
    ) {
        viewModelScope.launch {
            repository.updateChecklistItemResult(
                checklistId = checklistId,
                itemId = itemId,
                resultStatus = resultStatus,
                remarks = remarks
            )
        }
    }

    fun deleteInspectionChecklist(checklist: InspectionChecklistEntity) {
        viewModelScope.launch {
            repository.deleteChecklist(checklist)
        }
    }

    // Hybrid Use Case 1: Fast On-Device / Hybrid SOS Script Tailoring
    fun tailorSosScript(
        script: ScriptEntity,
        recipientName: String,
        reasonDetail: String,
        clauseNumber: String,
        customSituation: String
    ) {
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        viewModelScope.launch {
            _scriptTailorState.value = ScriptTailorUiState.Loading(script.title)
            val prompt = if (en) {
                """
                Tailor this construction SOS notice into concise, formal, contract-safe English ready to send immediately via email or messaging:
                - Script Topic: ${script.title} (${script.category})
                - Recipient / Company: ${recipientName.ifBlank { "Client / Supervision Consultant" }}
                - Clause / RFI No.: ${clauseNumber.ifBlank { "Per Main Contract" }}
                - Cause / Site Zone: ${reasonDetail.ifBlank { "Per Daily Site Report" }}
                - Additional Context: ${customSituation.ifBlank { "Reserve EOT rights and protect against Liquidated Damages (LD)" }}

                Original Reference Script:
                ${script.content}

                Return ONLY the tailored notice text in English ready to copy and send.
                """.trimIndent()
            } else {
                """
                ช่วยปรับแต่งสคริปต์แจ้งเตือนหน้างานก่อสร้างนี้ให้สุภาพ รัดกุม เป็นทางการ พร้อมส่ง LINE หรืออีเมลทันที:
                - หัวข้อสคริปต์: ${script.title} (${script.category})
                - ผู้รับ/บริษัท: ${recipientName.ifBlank { "ผู้ว่าจ้าง / คอนเซาท์" }}
                - ข้อสัญญา/เลขที่ RFI: ${clauseNumber.ifBlank { "ตามสัญญาหลัก" }}
                - โซน/สาเหตุหน้างาน: ${reasonDetail.ifBlank { "ตามรายงานประจำวัน" }}
                - บริบทเพิ่มเติม: ${customSituation.ifBlank { "ต้องการสงวนสิทธิ์ระยะเวลา (EOT) และป้องกันค่าปรับ LD" }}
                
                สคริปต์ต้นฉบับ:
                ${script.content}
                
                ส่งคืนเฉพาะข้อความสคริปต์ที่ปรับปรุงแล้ว พร้อมใช้งานทันที
                """.trimIndent()
            }

            try {
                val result = aiRepository.generateHybridText(
                    prompt = prompt,
                    offlineOnly = _offlineSiteTestMode.value,
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "SOS Tailor" else "ปรับสคริปต์ SOS")
                _scriptTailorState.value = ScriptTailorUiState.Success(
                    originalTitle = script.title,
                    tailoredContent = result.text,
                    inferenceSource = source
                )
            } catch (e: FirebaseAIOnDeviceNotAvailableException) {
                Log.w(TAG, "On-device Gemini Nano unavailable during Offline Site Mode", e)
                _scriptTailorState.value = ScriptTailorUiState.Error(
                    message = onDeviceUnavailableMsg(lang, e.localizedMessage),
                    isOnDeviceUnavailable = true
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "SOS script tailoring timed out", e)
                _scriptTailorState.value = ScriptTailorUiState.Error(
                    if (en) "SOS script tailoring timed out (60s). Please try again."
                    else "การปรับสคริปต์หมดเวลา (60 วินาที) กรุณาลองใหม่"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "SOS script tailoring failed", e)
                val isUnavailable = isOfflineOnDeviceFailure(e)
                _scriptTailorState.value = ScriptTailorUiState.Error(
                    message = if (isUnavailable) {
                        onDeviceUnavailableMsg(lang, e.localizedMessage)
                    } else {
                        e.localizedMessage ?: if (en) "Unable to tailor script" else "ไม่สามารถปรับแต่งสคริปต์ได้"
                    },
                    isOnDeviceUnavailable = isUnavailable
                )
            }
        }
    }

    fun clearScriptTailorResult() {
        _scriptTailorState.value = ScriptTailorUiState.Idle
    }

    // Hybrid Use Case 2: Daily Remind Log Summarization
    fun summarizeDailyRemindLog() {
        val currentTasks = tasks.value
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        if (currentTasks.isEmpty()) {
            _hybridOpsState.value = HybridOpsUiState.Error(
                if (en) "No checkpoints in Room DB yet for daily summary."
                else "ยังไม่มีรายการภารกิจใน Room DB สำหรับสรุปประจำวัน"
            )
            return
        }

        viewModelScope.launch {
            _hybridOpsState.value = HybridOpsUiState.Loading(
                if (en) "Daily Remind Log Summary (Hybrid AI)" else "สรุป Daily Remind Log (Hybrid AI)"
            )
            val taskLines = currentTasks.take(15).joinToString("\n") { task ->
                val status = if (task.isCompleted) "DONE" else "PENDING"
                val hasPhoto = if (!task.photoUri.isNullOrBlank()) {
                    if (en) "PHOTO_ATTACHED" else "มีรูปหลักฐาน"
                } else {
                    if (en) "NO_PHOTO" else "ยังไม่มีรูป"
                }
                "- [$status][${task.category}][${task.timeSlot}] ${task.title} ($hasPhoto)"
            }
            val completed = currentTasks.count { it.isCompleted }
            val photoCount = currentTasks.count { !it.photoUri.isNullOrBlank() }

            val prompt = if (en) {
                """
                Summarize today's construction site Daily Remind Log for shift handover & management from live Room DB data (respond concisely in English):
                Stats: Completed $completed/${currentTasks.size} checkpoints, $photoCount photo evidence attached.
                Checkpoints:
                $taskLines

                Summarize in 3 short points:
                1) Readiness overview & critical pending items
                2) SLA / LD / VO risks to close before end of shift
                3) Short handover message ready to send to the Project Director
                """.trimIndent()
            } else {
                """
                สรุปรายงานความคืบหน้าหน้างานประจำวัน (Daily Remind Log Summary) สำหรับส่งมอบกะและผู้บริหาร จากข้อมูล Room DB:
                สถิติรวม: สำเร็จ $completed/${currentTasks.size} รายการ, แนบรูปหลักฐานแล้ว $photoCount ภาพ
                รายการภารกิจ:
                $taskLines
                
                สรุปสั้นๆ 3 หัวข้อ:
                1) ภาพรวมความพร้อม & งานที่ค้างวิกฤต
                2) จุดเสี่ยง SLA/LD/VO ที่ต้องเร่งปิดก่อนเลิกงาน
                3) ข้อความสั้นสำหรับส่งไลน์รายงานหัวหน้างาน
                """.trimIndent()
            }

            runHybridOpsPrompt(
                cardTitle = if (en) "Daily Remind Log Summary" else "สรุป Daily Remind Log ประจำวัน",
                featureLabel = if (en) "Daily Log" else "สรุป Log หน้างาน",
                prompt = prompt
            )
        }
    }

    // Hybrid Use Case 3: Anti-BS Duplicate Progress Detection
    fun detectDuplicateProgress() {
        val currentTasks = tasks.value
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        if (currentTasks.isEmpty()) {
            _hybridOpsState.value = HybridOpsUiState.Error(
                if (en) "No checkpoints in Room DB yet for Anti-BS audit."
                else "ยังไม่มีรายการภารกิจใน Room DB สำหรับตรวจสอบ Anti-BS"
            )
            return
        }

        viewModelScope.launch {
            _hybridOpsState.value = HybridOpsUiState.Loading(
                if (en) "Anti-BS Duplicate Progress Audit (Hybrid AI)" else "ตรวจจับรายงานซ้ำซ้อน Anti-BS (Hybrid AI)"
            )
            val timeFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.US)
            val taskLines = currentTasks.take(15).joinToString("\n") { task ->
                val status = if (task.isCompleted) "DONE" else "PENDING"
                val timeStr = timeFormatter.format(Date(task.timestamp))
                val hasPhoto = if (!task.photoUri.isNullOrBlank()) "PHOTO_OK" else "NO_PHOTO"
                "- #$status | ${task.category} | $timeStr | $hasPhoto | ${task.title} :: ${task.description.take(90)}"
            }

            val prompt = if (en) {
                """
                Perform an Anti-BS Duplicate & Authenticity Progress Audit on these Room DB site tasks (respond concisely in English):
                $taskLines

                Analyze in 3 concise points:
                1) Are there duplicate entries, copy-pasted logs, or stalled items without progress? (Cite titles)
                2) Tasks marked DONE with NO_PHOTO evidence that could be challenged during interim payment / EOT claims
                3) Immediate field verification actions to guarantee genuine site proof
                """.trimIndent()
            } else {
                """
                ตรวจสอบความน่าเชื่อถือของรายการความคืบหน้าหน้างาน (Anti-BS Duplicate Progress Audit):
                $taskLines
                
                ช่วยวิเคราะห์แบบกระชับ 3 ข้อ:
                1) พบรายการที่ซ้ำซ้อน / คัดลอกข้อความเดิม / ค้างนานโดยไม่คืบหน้าหรือไม่ (ระบุชื่อข้อ)
                2) งานที่ติ๊ก DONE แต่ไม่มีรูปหลักฐาน (NO_PHOTO) ซึ่งอาจเสี่ยงถูกโต้แย้งเวลาตรวจงวด/EOT
                3) คำแนะนำด่วนเพื่อยืนยันหลักฐานหน้างานจริง
                """.trimIndent()
            }

            runHybridOpsPrompt(
                cardTitle = if (en) "Anti-BS Progress Audit" else "ตรวจจับความซ้ำซ้อน (Anti-BS Progress Check)",
                featureLabel = if (en) "Anti-BS Check" else "ตรวจ Anti-BS",
                prompt = prompt
            )
        }
    }

    // Hybrid Use Case 4: Fast Single-Turn On-Device / Hybrid Site Checklist Q&A
    fun askHybridSiteQa(question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) return
        val en = isEnglish()
        viewModelScope.launch {
            _hybridOpsState.value = HybridOpsUiState.Loading(
                if (en) "Answering Hybrid Site Q&A (PREFER_ON_DEVICE)" else "ตอบคำถามหน้างานแบบ Hybrid (PREFER_ON_DEVICE)"
            )
            val prompt = if (en) {
                """
                Urgent question from Construction Project Manager on site:
                "$trimmed"

                Provide 3 concise, immediate action points in English to prevent LD penalties, protect SLAs, and preserve EOT/VO evidence.
                """.trimIndent()
            } else {
                """
                คำถามด่วนจากผู้จัดการฝ่ายก่อสร้างหน้างาน:
                "$trimmed"
                
                ตอบแนวทางปฏิบัติทันที 3 ข้อสั้นๆ เพื่อป้องกันค่าปรับ LD, รักษา SLA และเก็บหลักฐาน EOT/VO
                """.trimIndent()
            }

            runHybridOpsPrompt(
                cardTitle = if (en) "Hybrid Site Q&A Answer" else "คำตอบด่วนหน้างาน (Hybrid Site Q&A)",
                featureLabel = if (en) "Hybrid Q&A" else "ถาม-ตอบ Hybrid",
                prompt = prompt
            )
        }
    }

    private suspend fun runHybridOpsPrompt(
        cardTitle: String,
        featureLabel: String,
        prompt: String
    ) {
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        try {
            val result = aiRepository.generateHybridText(
                prompt = prompt,
                offlineOnly = _offlineSiteTestMode.value,
                language = lang
            )
            val source = result.origin.toHybridSource()
            recordInferenceSource(source, featureLabel)
            _hybridOpsState.value = HybridOpsUiState.Success(
                taskTitle = cardTitle,
                resultText = result.text,
                inferenceSource = source
            )
        } catch (e: FirebaseAIOnDeviceNotAvailableException) {
            Log.w(TAG, "On-device Gemini Nano unavailable during Offline Site Mode", e)
            _hybridOpsState.value = HybridOpsUiState.Error(
                message = onDeviceUnavailableMsg(lang, e.localizedMessage),
                isOnDeviceUnavailable = true
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Hybrid ops timed out", e)
            _hybridOpsState.value = HybridOpsUiState.Error(
                if (en) "Hybrid AI request timed out (60s)." else "การประมวลผล Hybrid AI หมดเวลา (60 วินาที)"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Hybrid ops failed", e)
            val isUnavailable = isOfflineOnDeviceFailure(e)
            _hybridOpsState.value = HybridOpsUiState.Error(
                message = if (isUnavailable) {
                    onDeviceUnavailableMsg(lang, e.localizedMessage)
                } else {
                    e.localizedMessage ?: if (en) "Error calling Hybrid AI" else "เกิดข้อผิดพลาดในการเรียก Hybrid AI"
                },
                isOnDeviceUnavailable = isUnavailable
            )
        }
    }

    // Hybrid Use Case 5: Material Budget Sanity Check (Immutable Baseline + Audit-Logged Exception)
    fun runMaterialSanityCheck(areaScope: String, requestDescription: String) {
        val trimmed = requestDescription.trim()
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        if (trimmed.isEmpty()) {
            _materialSanityCheckState.value = MaterialSanityCheckUiState.Error(
                message = if (en) {
                    "Please enter the material requisition items and quantities to verify."
                } else {
                    "กรุณาระบุรายการขอเบิกวัสดุและจำนวนที่ลูกน้องส่งมาให้ตรวจสอบ"
                }
            )
            return
        }
        viewModelScope.launch {
            _materialSanityCheckState.value = MaterialSanityCheckUiState.Loading
            try {
                val result = aiRepository.auditMaterialRequisition(
                    areaScope = areaScope,
                    requestDescription = trimmed,
                    offlineOnly = _offlineSiteTestMode.value,
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "Sanity Check" else "ตรวจใบเบิกวัสดุ")
                _materialSanityCheckState.value = MaterialSanityCheckUiState.Success(
                    result = result,
                    inferenceSource = source
                )
            } catch (e: FirebaseAIOnDeviceNotAvailableException) {
                Log.w(TAG, "On-device unavailable during Material Sanity Check", e)
                _materialSanityCheckState.value = MaterialSanityCheckUiState.Error(
                    message = onDeviceUnavailableMsg(lang, e.localizedMessage),
                    isOnDeviceUnavailable = true
                )
            } catch (e: TimeoutCancellationException) {
                _materialSanityCheckState.value = MaterialSanityCheckUiState.Error(
                    message = if (en) {
                        "Material sanity check timed out (60s). Please try again."
                    } else {
                        "การตรวจสอบใบเบิกวัสดุหมดเวลา (60 วินาที) กรุณาลองใหม่"
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Material sanity check failed", e)
                val isUnavailable = isOfflineOnDeviceFailure(e)
                _materialSanityCheckState.value = MaterialSanityCheckUiState.Error(
                    message = if (isUnavailable) {
                        onDeviceUnavailableMsg(lang, e.localizedMessage)
                    } else {
                        e.localizedMessage ?: if (en) "Unable to audit material requisition" else "ไม่สามารถตรวจสอบใบขอเบิกวัสดุได้"
                    },
                    isOnDeviceUnavailable = isUnavailable
                )
            }
        }
    }

    fun clearMaterialSanityCheck() {
        _materialSanityCheckState.value = MaterialSanityCheckUiState.Idle
    }

    fun recordExceptionAudit(
        lotOrItemTitle: String,
        areaScope: String,
        baselineVersion: String,
        aiFlagSummary: String,
        decisionStatus: String,
        justificationNote: String,
        inferenceOrigin: String
    ) {
        val en = isEnglish()
        viewModelScope.launch {
            repository.insertExceptionAudit(
                ExceptionAuditEntity(
                    lotOrItemTitle = lotOrItemTitle.trim().take(120),
                    areaOrScope = areaScope.trim().ifBlank { if (en) "Unspecified Zone" else "ไม่ระบุพื้นที่" },
                    baselineVersion = baselineVersion,
                    aiFlagSummary = aiFlagSummary.trim().take(400),
                    decisionStatus = decisionStatus,
                    justificationNote = justificationNote.trim().ifBlank {
                        if (decisionStatus == "APPROVED_WITH_EXCEPTION") {
                            if (en) "Manager approved exception for this lot only (central baseline unchanged)"
                            else "ผู้จัดการอนุมัติข้อยกเว้นเฉพาะเคสนี้ (ไม่ทับเกณฑ์มาตรฐานกลาง)"
                        } else {
                            if (en) "Placed on hold pending drawing check and subordinate clarification"
                            else "ระงับไว้ตรวจสอบแบบและเรียกผู้ขอเบิกชี้แจงเพิ่มเติม"
                        }
                    },
                    inferenceOrigin = inferenceOrigin
                )
            )
        }
    }

    fun deleteExceptionAudit(audit: ExceptionAuditEntity) {
        viewModelScope.launch {
            repository.deleteExceptionAudit(audit)
        }
    }

    // Cloud Fallback Multimodal: Site Risk Photo Inspection
    fun inspectSitePhoto(context: Context, photoUri: String, focusNote: String) {
        if (photoUri.isBlank()) return
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        viewModelScope.launch {
            _sitePhotoInspectState.value = SitePhotoInspectUiState.Loading
            try {
                val bitmap = SitePhotoStorage.loadScaledBitmap(context, photoUri)
                if (bitmap == null) {
                    _sitePhotoInspectState.value = SitePhotoInspectUiState.Error(
                        if (en) "Unable to read photo from this URI. Please retake or pick another photo."
                        else "ไม่สามารถอ่านไฟล์ภาพหน้างานจาก URI นี้ได้ กรุณาถ่ายหรือเลือกรูปใหม่"
                    )
                    return@launch
                }

                val result = aiRepository.inspectSitePhoto(
                    bitmap = bitmap,
                    focusNote = focusNote,
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "Photo Inspect" else "ตรวจรูปหน้างาน")
                _sitePhotoInspectState.value = SitePhotoInspectUiState.Success(
                    photoUri = photoUri,
                    analysis = result.text,
                    inferenceSource = source
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Site photo inspection timed out", e)
                _sitePhotoInspectState.value = SitePhotoInspectUiState.Error(
                    if (en) "Site photo inspection timed out (60s)."
                    else "การวิเคราะห์ภาพถ่ายหน้างานหมดเวลา (60 วินาที)"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Site photo inspection failed", e)
                _sitePhotoInspectState.value = SitePhotoInspectUiState.Error(
                    e.localizedMessage ?: if (en) {
                        "Error inspecting site photo via Cloud Gemini."
                    } else {
                        "เกิดข้อผิดพลาดในการวิเคราะห์ภาพถ่ายหน้างานผ่าน Cloud Gemini"
                    }
                )
            }
        }
    }

    // Instant Contract Clause & LD Risk Analysis
    fun analyzeContractClause(clauseText: String, useDeepThinking: Boolean = false) {
        if (clauseText.isBlank()) return
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        viewModelScope.launch {
            _contractRiskState.value = ContractRiskUiState.Loading
            try {
                if (useDeepThinking) {
                    val prompt = aiRepository.buildContractRiskPrompt(clauseText, lang)
                    val accumulatedThoughts = StringBuilder()
                    val accumulatedAnalysis = StringBuilder()
                    var detectedSource = HybridInferenceSource.IN_CLOUD
                    withTimeout(60_000L) {
                        aiRepository.streamHighThinking(prompt).collect { chunk ->
                            detectedSource = chunk.origin.toHybridSource()
                            if (chunk.thoughtChunk.isNotEmpty()) accumulatedThoughts.append(chunk.thoughtChunk)
                            if (chunk.textChunk.isNotEmpty()) accumulatedAnalysis.append(chunk.textChunk)
                            _contractRiskState.value = ContractRiskUiState.Streaming(
                                partialAnalysis = accumulatedAnalysis.toString(),
                                thoughts = accumulatedThoughts.toString()
                            )
                        }
                    }
                    if (accumulatedAnalysis.isNotBlank()) {
                        recordInferenceSource(detectedSource, if (en) "LD Scanner" else "สแกนสัญญา LD")
                        _contractRiskState.value = ContractRiskUiState.Success(
                            analysis = accumulatedAnalysis.toString(),
                            thoughts = accumulatedThoughts.toString(),
                            inferenceSource = detectedSource
                        )
                    } else {
                        _contractRiskState.value = ContractRiskUiState.Error(
                            if (en) "No analysis returned from AI model. Please retry."
                            else "ไม่ได้รับผลการวิเคราะห์จากระบบ AI กรุณาลองอีกครั้ง"
                        )
                    }
                } else {
                    val result = aiRepository.analyzeContractClauseWithSearch(clauseText, lang)
                    val source = result.origin.toHybridSource()
                    recordInferenceSource(source, if (en) "LD Scanner" else "สแกนสัญญา LD")
                    _contractRiskState.value = ContractRiskUiState.Success(
                        analysis = result.analysis,
                        searchQueries = result.searchQueries,
                        sources = result.sources.map {
                            GroundingSource(title = it.title, uri = it.uri, domain = it.domain)
                        },
                        inferenceSource = source
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Contract clause analysis timed out", e)
                _contractRiskState.value = ContractRiskUiState.Error(
                    if (en) "Contract analysis timed out (60s). Please try again."
                    else "การวิเคราะห์ข้อสัญญาหมดเวลา (60 วินาที) กรุณาลองใหม่อีกครั้ง"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Contract clause analysis failed", e)
                _contractRiskState.value = ContractRiskUiState.Error(
                    e.localizedMessage ?: if (en) "Error analyzing contract clause" else "เกิดข้อผิดพลาดในการวิเคราะห์ข้อสัญญา"
                )
            }
        }
    }

    fun analyzeConstructionCrisisWithHighThinking(crisisPrompt: String) {
        if (crisisPrompt.isBlank()) return
        val en = isEnglish()
        viewModelScope.launch {
            _thinkingState.value = AiThinkingUiState.Loading
            val accumulatedThoughts = StringBuilder()
            val accumulatedAnswer = StringBuilder()
            var detectedSource = HybridInferenceSource.IN_CLOUD

            val fullPrompt = if (en) {
                """
                You are a Senior Project Director and Construction Contract Counsel (FIDIC / Thai Civil Code) in CAREER SHIELD OS.
                Analyze the following jobsite crisis thoroughly in English to shield the Construction Project Manager from personal liability and Liquidated Damages (LD):

                Site Situation: $crisisPrompt

                Provide:
                1. Critical legal & contractual traps to lock down immediately
                2. Evidence preservation protocol (Timestamp Camera, RFI, Daily Report, EOT/VO Notice)
                3. Negotiation strategy with Client, Supervision Consultant, or Subcontractor
                4. Ready-to-send formal reservation notice wording
                """.trimIndent()
            } else {
                """
                คุณคือผู้อำนวยการโครงการอาวุโสและที่ปรึกษากฎหมายสัญญาก่อสร้าง (FIDIC / สัญญาก่อสร้างไทย) ในระบบ CAREER SHIELD OS
                จงวิเคราะห์ปัญหาหน้างานต่อไปนี้อย่างละเอียดรอบคอบเพื่อปกป้องผู้จัดการฝ่ายก่อสร้างไม่ให้ต้องรับผิดชอบค่าเสียหายหรือค่าปรับ LD:
                
                สถานการณ์หน้างาน: $crisisPrompt
                
                ให้ระบุ:
                1. จุดตายและข้อกฎหมาย/เงื่อนไขสัญญาที่ต้องระวังทันที
                2. ขั้นตอนการเก็บหลักฐาน (Timestamp Camera, RFI, Daily Report, หนังสือสงวนสิทธิ์)
                3. แนวทางเจรจาต่อรองกับผู้ว่าจ้าง คอนเซาท์ หรือผู้รับเหมาช่วง
                4. ร่างข้อความแจ้งเตือนที่รัดกุมพร้อมนำไปใช้งานทันที
                """.trimIndent()
            }

            try {
                withTimeout(60_000L) {
                    aiRepository.streamHighThinking(fullPrompt).collect { chunk ->
                        detectedSource = chunk.origin.toHybridSource()
                        if (chunk.thoughtChunk.isNotEmpty()) {
                            accumulatedThoughts.append(chunk.thoughtChunk)
                        }
                        if (chunk.textChunk.isNotEmpty()) {
                            accumulatedAnswer.append(chunk.textChunk)
                        }
                        _thinkingState.value = AiThinkingUiState.Streaming(
                            thoughts = accumulatedThoughts.toString(),
                            answer = accumulatedAnswer.toString()
                        )
                    }
                }
                if (accumulatedAnswer.isNotBlank()) {
                    recordInferenceSource(detectedSource, "High Thinking")
                    _thinkingState.value = AiThinkingUiState.Success(
                        thoughts = accumulatedThoughts.toString(),
                        answer = accumulatedAnswer.toString(),
                        inferenceSource = detectedSource
                    )
                } else {
                    _thinkingState.value = AiThinkingUiState.Error(
                        if (en) "AI returned an empty answer. Please try again."
                        else "ระบบ AI ไม่ได้ส่งคำตอบกลับมา กรุณาลองใหม่อีกครั้ง"
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "High thinking analysis timed out", e)
                _thinkingState.value = AiThinkingUiState.Error(
                    if (en) "Deep reasoning timed out (60s). Please try again."
                    else "การวิเคราะห์เชิงลึกใช้เวลานานเกิน 60 วินาที กรุณาลองใหม่อีกครั้ง"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "High thinking analysis failed", e)
                _thinkingState.value = AiThinkingUiState.Error(
                    e.localizedMessage ?: if (en) "Error connecting to Gemini AI" else "เกิดข้อผิดพลาดในการเชื่อมต่อ Gemini AI"
                )
            }
        }
    }

    fun generateActionPackForRoom(situation: String) {
        if (situation.isBlank()) return
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        viewModelScope.launch {
            _generatorState.value = AiGeneratorUiState.Loading
            try {
                val result = aiRepository.generateActionPack(situation, lang)
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "Action Pack" else "สร้าง Action Pack")
                _generatorState.value = AiGeneratorUiState.Success(
                    GeneratedActionPack(
                        scriptTitle = result.scriptTitle,
                        scriptContent = result.scriptContent,
                        scriptCategory = result.scriptCategory,
                        suggestedTasks = result.suggestedTasks,
                        inferenceSource = source
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Action pack generation timed out", e)
                _generatorState.value = AiGeneratorUiState.Error(
                    if (en) "Action pack generation timed out (60s)."
                    else "การสร้างสคริปต์และเช็กลิสต์หมดเวลา (60 วินาที)"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to generate structured action pack", e)
                _generatorState.value = AiGeneratorUiState.Error(
                    e.localizedMessage ?: if (en) "Unable to generate action pack" else "ไม่สามารถสร้างชุดสคริปต์อัตโนมัติได้"
                )
            }
        }
    }

    fun sendChatMessage(messageText: String) {
        val trimmed = messageText.trim()
        if (trimmed.isEmpty() || _isChatLoading.value) return
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN

        val userMsg = ChatMessage(
            id = System.currentTimeMillis(),
            isUser = true,
            text = trimmed
        )
        val placeholderId = System.currentTimeMillis() + 1L
        val placeholderMsg = ChatMessage(
            id = placeholderId,
            isUser = false,
            text = if (en) "Analyzing and verifying references..." else "กำลังวิเคราะห์และค้นหาข้อมูลอ้างอิง...",
            isStreaming = true
        )

        _chatMessages.value = _chatMessages.value + userMsg + placeholderMsg
        _isChatLoading.value = true

        viewModelScope.launch {
            try {
                val chat = chatSession ?: aiRepository.createChatSession(lang).also { chatSession = it }
                val result = aiRepository.sendChatMessage(chat, trimmed, lang)
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "AI Chat" else "แชทที่ปรึกษา")
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == placeholderId) {
                        msg.copy(
                            text = result.text,
                            sources = result.sources.map {
                                GroundingSource(title = it.title, uri = it.uri, domain = it.domain)
                            },
                            isStreaming = false,
                            inferenceSource = source
                        )
                    } else {
                        msg
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat message timed out", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == placeholderId) {
                        msg.copy(
                            text = if (en) {
                                "⚠️ Request timed out after 60 seconds. Please send again."
                            } else {
                                "⚠️ การเชื่อมต่อใช้เวลานานเกิน 60 วินาที กรุณาลองส่งข้อความใหม่อีกครั้ง"
                            },
                            isStreaming = false
                        )
                    } else {
                        msg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat message failed", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == placeholderId) {
                        msg.copy(
                            text = "⚠️ ${if (en) "Error" else "เกิดข้อผิดพลาด"}: ${e.localizedMessage ?: if (en) "Unable to connect to AI" else "ไม่สามารถเชื่อมต่อ AI ได้"}",
                            isStreaming = false
                        )
                    } else {
                        msg
                    }
                }
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        chatSession = null
        val en = isEnglish()
        _chatMessages.value = listOf(
            ChatMessage(
                id = System.currentTimeMillis(),
                isUser = false,
                text = if (en) {
                    "New conversation started. Type any contract clause, LD question, or site issue to consult."
                } else {
                    "เริ่มบทสนทนาใหม่แล้วครับ พิมพ์ข้อสัญญาหรือปัญหาหน้างานที่ต้องการปรึกษาได้เลยครับ"
                }
            )
        )
    }

    fun toggleLiveVoiceSession() {
        val currentState = _liveVoiceState.value
        if (currentState is LiveVoiceUiState.Active || currentState is LiveVoiceUiState.Connecting) {
            stopLiveVoiceSession()
        } else {
            startLiveVoiceSession()
        }
    }

    fun startLiveVoiceSession() {
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        viewModelScope.launch {
            _liveVoiceState.value = LiveVoiceUiState.Connecting
            try {
                val session = aiRepository.connectLiveVoice(lang)
                activeLiveSession = session
                recordInferenceSource(HybridInferenceSource.IN_CLOUD, "Live Voice")
                _liveVoiceState.value = LiveVoiceUiState.Active(
                    userTranscript = if (en) "Listening to your microphone..." else "กำลังรับฟังเสียงของคุณผ่านไมโครโฟน...",
                    modelTranscript = if (en) {
                        "Connected to Gemini 3.8 Live. Speak about any urgent jobsite risk."
                    } else {
                        "เชื่อมต่อ Gemini 3.8 Live สำเร็จ พูดปรึกษาปัญหาหน้างานได้เลยครับ"
                    }
                )
                session.startAudioConversation(
                    functionCallHandler = {
                        throw UnsupportedOperationException("No function declarations")
                    },
                    transcriptHandler = { inputTranscript, outputTranscript ->
                        val current = _liveVoiceState.value as? LiveVoiceUiState.Active
                        val updatedUser = inputTranscript?.text?.takeIf { it.isNotBlank() }
                            ?: current?.userTranscript.orEmpty()
                        val updatedModel = outputTranscript?.text?.takeIf { it.isNotBlank() }
                            ?: current?.modelTranscript.orEmpty()
                        _liveVoiceState.value = LiveVoiceUiState.Active(
                            userTranscript = updatedUser,
                            modelTranscript = updatedModel
                        )
                    },
                    enableInterruptions = true
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start Gemini Live voice session", e)
                activeLiveSession = null
                _liveVoiceState.value = LiveVoiceUiState.Error(
                    e.localizedMessage ?: if (en) {
                        "Unable to start Gemini 3.8 Live audio session"
                    } else {
                        "ไม่สามารถเริ่มการสนทนาเสียงสด (Live API) ได้"
                    }
                )
            }
        }
    }

    fun stopLiveVoiceSession() {
        viewModelScope.launch {
            try {
                activeLiveSession?.stopAudioConversation()
                activeLiveSession?.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing live session", e)
            } finally {
                activeLiveSession = null
                _liveVoiceState.value = LiveVoiceUiState.Idle
            }
        }
    }

    // --- NANO 4 Site Walk Inspector & PDF-vs-Photo Comparison ---

    private fun inferSeverityFromText(text: String): String {
        val upper = text.uppercase()
        return when {
            upper.contains("CRITICAL") || upper.contains("วิกฤต") || upper.contains("STOP-WORK") || upper.contains("โครงสร้าง") -> "CRITICAL"
            upper.contains("MINOR") || upper.contains("เล็กน้อย") || upper.contains("ผิวฉาบ") -> "MINOR"
            else -> "MAJOR"
        }
    }

    private fun inferResponsibleParty(text: String, tradeCategory: String): String {
        val upper = text.uppercase()
        return when {
            upper.contains("MASONRY") || upper.contains("งานปูน") || upper.contains("ฝั่งปูน") || upper.contains("ฉาบ") -> "MASONRY"
            upper.contains("ALUMINUM") || upper.contains("อลูมิเนียม") -> "ALUMINUM"
            upper.contains("STRUCTURAL") || upper.contains("โครงสร้าง") -> "STRUCTURAL"
            upper.contains("MEP") || upper.contains("ไฟฟ้า") || upper.contains("ประปา") -> "MEP"
            tradeCategory == "OPENINGS" -> "MASONRY"
            else -> "CONTRACTOR"
        }
    }

    fun analyzeSiteWalkDefectHybrid(
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String
    ) {
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        val cleanNote = inspectorNote.trim()
        val cleanMeasured = measuredInput.trim()
        val cleanCode = openingCode.trim()
        if (cleanNote.isEmpty() && cleanMeasured.isEmpty() && cleanCode.isEmpty()) {
            _defectAiState.value = DefectAiUiState.Error(
                if (en) "Please enter a defect note, opening code, or measurement first."
                else "กรุณาระบุรายละเอียดจุดเสีย รหัสช่องเปิด หรือค่าวัดก่อนวิเคราะห์"
            )
            return
        }

        viewModelScope.launch {
            _defectAiState.value = DefectAiUiState.Loading(
                if (en) "Hybrid AI Triage..." else "กำลังวิเคราะห์จุดเสีย (Hybrid AI)..."
            )
            try {
                val result = aiRepository.triageSiteWalkDefectHybrid(
                    floorZone = floorZone.trim(),
                    tradeCategory = tradeCategory,
                    openingCode = cleanCode,
                    measuredInput = cleanMeasured,
                    inspectorNote = cleanNote.ifBlank { "$cleanCode $cleanMeasured".trim() },
                    offlineOnly = _offlineSiteTestMode.value,
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "Defect Triage" else "วิเคราะห์จุดเสีย")
                _defectAiState.value = DefectAiUiState.Success(
                    analysis = result.text,
                    inferenceSource = source,
                    suggestedSeverity = inferSeverityFromText(result.text),
                    suggestedParty = inferResponsibleParty(result.text, tradeCategory),
                    usedPdfPlan = false
                )
            } catch (e: FirebaseAIOnDeviceNotAvailableException) {
                Log.w(TAG, "On-device Gemini Nano unavailable during Offline Site Mode", e)
                _defectAiState.value = DefectAiUiState.Error(
                    message = onDeviceUnavailableMsg(lang, e.localizedMessage),
                    isOnDeviceUnavailable = true
                )
            } catch (e: TimeoutCancellationException) {
                _defectAiState.value = DefectAiUiState.Error(
                    if (en) "Hybrid AI triage timed out (60s). Please try again."
                    else "การวิเคราะห์หมดเวลา (60 วินาที) กรุณาลองใหม่"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Hybrid defect triage failed", e)
                val isUnavailable = isOfflineOnDeviceFailure(e)
                _defectAiState.value = DefectAiUiState.Error(
                    message = if (isUnavailable) {
                        onDeviceUnavailableMsg(lang, e.localizedMessage)
                    } else {
                        e.localizedMessage ?: if (en) "Unable to analyze defect" else "ไม่สามารถวิเคราะห์จุดเสียได้"
                    },
                    isOnDeviceUnavailable = isUnavailable
                )
            }
        }
    }

    fun comparePhotoVsPdfPlan(
        context: Context,
        photoUriString: String?,
        pdfUriString: String?,
        pdfPageIndex: Int,
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String
    ) {
        val lang = _appLanguage.value
        val en = lang == AppLanguage.EN
        if (photoUriString.isNullOrBlank() && pdfUriString.isNullOrBlank()) {
            _defectAiState.value = DefectAiUiState.Error(
                if (en) "Capture a defect photo or load a PDF plan first."
                else "กรุณาถ่ายภาพหน้างานหรืออัปโหลดแบบแปลน PDF ก่อนเทียบแบบ"
            )
            return
        }
        if (_offlineSiteTestMode.value) {
            _defectAiState.value = DefectAiUiState.Error(
                message = if (en) {
                    "PDF & Photo Cloud comparison requires Cloud Gemini. Disable Offline Mode to compare against architectural PDF."
                } else {
                    "การเปรียบเทียบภาพถ่ายกับแบบ PDF ใช้ Cloud Gemini Multimodal กรุณาปิดโหมด Offline เพื่อเทียบแบบ"
                },
                isOnDeviceUnavailable = true
            )
            return
        }

        viewModelScope.launch {
            _defectAiState.value = DefectAiUiState.Loading(
                if (en) "Comparing Photo vs PDF Spec..." else "กำลังเทียบภาพถ่ายกับแบบ PDF..."
            )
            try {
                val siteBitmap = photoUriString?.takeIf { it.isNotBlank() }?.let {
                    SitePhotoStorage.loadScaledBitmap(context, it, maxDimension = 1280)
                }
                val pdfRender = pdfUriString?.takeIf { it.isNotBlank() }?.let {
                    PdfBlueprintHelper.renderPdfPage(context, it, pdfPageIndex, maxWidth = 1400)
                }
                val result = aiRepository.comparePhotoWithPdfBlueprint(
                    sitePhotoBitmap = siteBitmap,
                    pdfPageBitmap = pdfRender?.bitmap,
                    floorZone = floorZone.trim(),
                    tradeCategory = tradeCategory,
                    openingCode = openingCode.trim(),
                    measuredInput = measuredInput.trim(),
                    inspectorNote = inspectorNote.trim(),
                    language = lang
                )
                val source = result.origin.toHybridSource()
                recordInferenceSource(source, if (en) "PDF vs Photo" else "เทียบแบบ PDF")
                _defectAiState.value = DefectAiUiState.Success(
                    analysis = result.text,
                    inferenceSource = source,
                    suggestedSeverity = inferSeverityFromText(result.text),
                    suggestedParty = inferResponsibleParty(result.text, tradeCategory),
                    usedPdfPlan = pdfRender != null
                )
            } catch (e: TimeoutCancellationException) {
                _defectAiState.value = DefectAiUiState.Error(
                    if (en) "PDF comparison timed out (60s). Please try again."
                    else "การเทียบแบบ PDF หมดเวลา (60 วินาที) กรุณาลองใหม่"
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "PDF vs Photo comparison failed", e)
                _defectAiState.value = DefectAiUiState.Error(
                    e.localizedMessage ?: if (en) {
                        "Unable to compare photo with PDF blueprint"
                    } else {
                        "ไม่สามารถเปรียบเทียบภาพกับแบบแปลน PDF ได้"
                    }
                )
            }
        }
    }

    fun clearDefectAiState() {
        _defectAiState.value = DefectAiUiState.Idle
    }

    fun saveSiteWalkDefect(
        floorZone: String,
        tradeCategory: String,
        openingCode: String,
        measuredInput: String,
        inspectorNote: String,
        aiAnalysis: String,
        severityLevel: String,
        responsibleParty: String,
        photoUri: String?,
        pdfPlanUri: String?,
        pdfPageIndex: Int,
        pinXRatio: Float?,
        pinYRatio: Float?,
        inferenceOrigin: String
    ) {
        val en = isEnglish()
        val cleanNote = inspectorNote.trim().ifBlank {
            val codePart = openingCode.trim()
            val measuredPart = measuredInput.trim()
            when {
                codePart.isNotBlank() && measuredPart.isNotBlank() -> "$codePart ($measuredPart)"
                codePart.isNotBlank() -> codePart
                measuredPart.isNotBlank() -> measuredPart
                else -> if (en) "Site Walk Defect Observation" else "พบจุดแก้ไขหน้างาน (Site Walk Defect)"
            }
        }
        viewModelScope.launch {
            val nextPin = repository.getNextDefectPinNumber()
            repository.insertSiteWalkDefect(
                SiteWalkDefectEntity(
                    pinNumber = nextPin,
                    floorZone = floorZone.trim().ifBlank { if (en) "Main Inspection Zone" else "พื้นที่ตรวจหลัก" },
                    tradeCategory = tradeCategory,
                    openingCode = openingCode.trim(),
                    measuredInput = measuredInput.trim(),
                    inspectorNote = cleanNote,
                    aiAnalysis = aiAnalysis.trim(),
                    severityLevel = severityLevel,
                    responsibleParty = responsibleParty,
                    photoUri = photoUri?.takeIf { it.isNotBlank() },
                    pdfPlanUri = pdfPlanUri?.takeIf { it.isNotBlank() },
                    pdfPageIndex = pdfPageIndex,
                    pinXRatio = pinXRatio,
                    pinYRatio = pinYRatio,
                    inferenceOrigin = inferenceOrigin,
                    status = "OPEN",
                    capturedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleSiteWalkDefectStatus(defect: SiteWalkDefectEntity) {
        viewModelScope.launch {
            val nextStatus = if (defect.status == "OPEN") "FIXED" else "OPEN"
            repository.updateSiteWalkDefectStatus(defect.id, nextStatus)
        }
    }

    fun deleteSiteWalkDefect(context: Context, defect: SiteWalkDefectEntity) {
        viewModelScope.launch {
            SitePhotoStorage.deletePhotoFileIfLocal(context, defect.photoUri)
            repository.deleteSiteWalkDefect(defect)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            activeLiveSession?.stopAudioConversation()
        } catch (_: Exception) {
        }
    }

    companion object {
        fun provideFactory(
            repository: CareerShieldRepository,
            aiRepository: GeminiAiRepository = GeminiAiRepository()
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CareerShieldViewModel(repository, aiRepository) as T
                }
            }
    }
}
