package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.CertifiedBaselineData
import com.example.data.ChecklistWithItems
import com.example.data.ExceptionAuditEntity
import com.example.data.InspectionChecklistEntity
import com.example.data.NanoDiagnosticLanguage
import com.example.data.RiskAssessmentReportEntity
import com.example.ui.components.AppCheckDebugTokenBanner
import com.example.ui.components.InferenceSourcePill
import com.example.ui.components.RiskInspectionRoomPanel
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.appStrings
import com.example.viewmodel.AiGeneratorUiState
import com.example.viewmodel.AiThinkingUiState
import com.example.viewmodel.ChatMessage
import com.example.viewmodel.GeneratedActionPack
import com.example.viewmodel.HybridInferenceSource
import com.example.viewmodel.HybridOpsUiState
import com.example.viewmodel.LiveVoiceUiState
import com.example.viewmodel.MaterialSanityCheckUiState
import com.example.viewmodel.NanoDiagnosticUiState
import com.example.viewmodel.SiteSafetyRiskUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAdvisorScreen(
    context: Context,
    initialCrisisPrompt: String,
    initialChatDraft: String,
    initialHybridOp: String,
    onConsumeInitialPrompt: () -> Unit,
    onConsumeInitialChatDraft: () -> Unit,
    onConsumeInitialHybridOp: () -> Unit,
    offlineSiteTestMode: Boolean,
    onDeviceStatusLabel: String,
    isOnDeviceDownloadable: Boolean,
    verifiedOnDeviceModelName: String?,
    deviceCompatibilitySummary: String,
    nanoDiagnosticState: NanoDiagnosticUiState,
    materialSanityCheckState: MaterialSanityCheckUiState,
    exceptionAudits: List<ExceptionAuditEntity>,
    hybridOpsState: HybridOpsUiState,
    siteSafetyRiskState: SiteSafetyRiskUiState,
    thinkingState: AiThinkingUiState,
    generatorState: AiGeneratorUiState,
    chatMessages: List<ChatMessage>,
    isChatLoading: Boolean,
    liveVoiceState: LiveVoiceUiState,
    onDisableOfflineMode: () -> Unit,
    onRefreshOnDeviceStatus: () -> Unit,
    onDownloadOnDeviceModel: () -> Unit,
    onRunNanoDiagnostic: (NanoDiagnosticLanguage) -> Unit,
    onClearNanoDiagnostic: () -> Unit,
    onRunMaterialSanityCheck: (String, String) -> Unit,
    onClearMaterialSanityCheck: () -> Unit,
    onRecordExceptionAudit: (String, String, String, String, String, String, String) -> Unit,
    onDeleteExceptionAudit: (ExceptionAuditEntity) -> Unit,
    onSummarizeDailyLog: () -> Unit,
    onDetectDuplicateProgress: () -> Unit,
    onAskHybridSiteQa: (String) -> Unit,
    onAnalyzeSiteSafetyRisk: (String) -> Unit,
    onClearSiteSafetyRisk: () -> Unit,
    onSaveSafetyMitigationTask: (String, String) -> Unit,
    onAnalyzeCrisis: (String) -> Unit,
    onGenerateActionPack: (String) -> Unit,
    onSaveActionPackToRoom: (GeneratedActionPack) -> Unit,
    onSendChatMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onToggleLiveVoice: () -> Unit,
    riskReports: List<RiskAssessmentReportEntity> = emptyList(),
    checklistsWithItems: List<ChecklistWithItems> = emptyList(),
    openCriticalReportCount: Int = 0,
    onSaveSiteSafetyRiskReport: (String, String, String, String, HybridInferenceSource) -> Unit = { _, _, _, _, _ -> },
    onCreateRiskReport: (String, String, String, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
    onUpdateRiskReportStatus: (Long, String) -> Unit = { _, _ -> },
    onDeleteRiskReport: (RiskAssessmentReportEntity) -> Unit = {},
    onCreateInspectionChecklist: (String, String, String, List<String>, Long?) -> Unit = { _, _, _, _, _ -> },
    onSetInspectionItemResult: (Long, Long, String) -> Unit = { _, _, _ -> },
    onDeleteInspectionChecklist: (InspectionChecklistEntity) -> Unit = {}
) {
    val lang = LocalAppLanguage.current
    val s = LocalAppStrings.current

    // 0 = Hybrid Ops (Preview Target + Cloud), 1 = Chatbot, 2 = Live Voice, 3 = High Thinking & Pack
    var activeSubTab by rememberSaveable { mutableIntStateOf(0) }
    var chatInput by rememberSaveable { mutableStateOf("") }
    var hybridQaInput by rememberSaveable { mutableStateOf("") }
    var materialAreaInput by rememberSaveable { mutableStateOf(s.sanityPreset60Area) }
    var materialRequestInput by rememberSaveable { mutableStateOf(s.sanityPreset60Req) }
    var exceptionJustificationInput by rememberSaveable { mutableStateOf("") }
    var safetyHazardInput by rememberSaveable { mutableStateOf(s.defaultSafetyHazardInput) }
    var crisisInput by rememberSaveable { mutableStateOf(s.defaultCrisisInput) }
    var showThoughts by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(lang) {
        val otherLang = if (lang == AppLanguage.TH) AppLanguage.EN else AppLanguage.TH
        val otherStrings = appStrings(otherLang)
        if (materialAreaInput == otherStrings.sanityPreset60Area) {
            materialAreaInput = s.sanityPreset60Area
        } else if (materialAreaInput == otherStrings.sanityPresetLot3Area) {
            materialAreaInput = s.sanityPresetLot3Area
        }
        if (materialRequestInput == otherStrings.sanityPreset60Req) {
            materialRequestInput = s.sanityPreset60Req
        } else if (materialRequestInput == otherStrings.sanityPresetLot3Req) {
            materialRequestInput = s.sanityPresetLot3Req
        }
        if (safetyHazardInput == otherStrings.defaultSafetyHazardInput) {
            safetyHazardInput = s.defaultSafetyHazardInput
        }
        if (crisisInput == otherStrings.defaultCrisisInput) {
            crisisInput = s.defaultCrisisInput
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleLiveVoice()
        } else {
            Toast.makeText(
                context,
                if (lang == AppLanguage.EN) {
                    "Please allow microphone permission to talk live with Gemini 3.8 Live"
                } else {
                    "กรุณาอนุญาตการใช้ไมโครโฟนเพื่อสนทนาเสียงสดกับ Gemini 3.8 Live"
                },
                Toast.LENGTH_LONG
            ).show()
        }
    }

    LaunchedEffect(initialCrisisPrompt) {
        if (initialCrisisPrompt.isNotBlank()) {
            activeSubTab = 3
            crisisInput = initialCrisisPrompt
            onAnalyzeCrisis(initialCrisisPrompt)
            onConsumeInitialPrompt()
        }
    }

    LaunchedEffect(initialChatDraft) {
        if (initialChatDraft.isNotBlank()) {
            activeSubTab = 1
            chatInput = initialChatDraft
            onSendChatMessage(initialChatDraft)
            chatInput = ""
            onConsumeInitialChatDraft()
        }
    }

    LaunchedEffect(initialHybridOp) {
        if (initialHybridOp.isNotBlank()) {
            activeSubTab = 0
            when (initialHybridOp) {
                "DAILY_LOG" -> onSummarizeDailyLog()
                "ANTI_BS" -> onDetectDuplicateProgress()
            }
            onConsumeInitialHybridOp()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally scrollable Sub-Tab Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                label = { Text(s.subTabHybridOps, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = s.subTabHybridOps,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF00E676),
                    selectedLeadingIconColor = Color(0xFF00E676),
                    containerColor = Color(0xFF111827),
                    labelColor = Color(0xFFD1D5DB),
                    iconColor = Color(0xFF9CA3AF)
                ),
                modifier = Modifier.testTag("ai_subtab_hybrid_ops")
            )

            FilterChip(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                label = { Text(s.subTabChat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = s.subTabChat,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFF00E5FF),
                    selectedLeadingIconColor = Color(0xFF00E5FF),
                    containerColor = Color(0xFF111827),
                    labelColor = Color(0xFFD1D5DB),
                    iconColor = Color(0xFF9CA3AF)
                ),
                modifier = Modifier.testTag("ai_subtab_chat")
            )

            FilterChip(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                label = { Text(s.subTabLiveVoice, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = s.subTabLiveVoice,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFB300).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFFFB300),
                    selectedLeadingIconColor = Color(0xFFFFB300),
                    containerColor = Color(0xFF111827),
                    labelColor = Color(0xFFD1D5DB),
                    iconColor = Color(0xFF9CA3AF)
                ),
                modifier = Modifier.testTag("ai_subtab_live_voice")
            )

            FilterChip(
                selected = activeSubTab == 3,
                onClick = { activeSubTab = 3 },
                label = { Text(s.subTabHighThinking, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = s.subTabHighThinking,
                        modifier = Modifier.size(15.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFB388FF).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFB388FF),
                    selectedLeadingIconColor = Color(0xFFB388FF),
                    containerColor = Color(0xFF111827),
                    labelColor = Color(0xFFD1D5DB),
                    iconColor = Color(0xFF9CA3AF)
                ),
                modifier = Modifier.testTag("ai_subtab_high_thinking")
            )
        }

        when (activeSubTab) {
            0 -> HybridOpsSubScreen(
                context = context,
                offlineSiteTestMode = offlineSiteTestMode,
                onDeviceStatusLabel = onDeviceStatusLabel,
                isOnDeviceDownloadable = isOnDeviceDownloadable,
                verifiedOnDeviceModelName = verifiedOnDeviceModelName,
                deviceCompatibilitySummary = deviceCompatibilitySummary,
                nanoDiagnosticState = nanoDiagnosticState,
                materialSanityCheckState = materialSanityCheckState,
                exceptionAudits = exceptionAudits,
                materialAreaInput = materialAreaInput,
                onMaterialAreaInputChange = { materialAreaInput = it },
                materialRequestInput = materialRequestInput,
                onMaterialRequestInputChange = { materialRequestInput = it },
                exceptionJustificationInput = exceptionJustificationInput,
                onExceptionJustificationInputChange = { exceptionJustificationInput = it },
                onRunMaterialSanityCheck = {
                    onRunMaterialSanityCheck(materialAreaInput, materialRequestInput)
                },
                onClearMaterialSanityCheck = onClearMaterialSanityCheck,
                onRecordExceptionAudit = onRecordExceptionAudit,
                onDeleteExceptionAudit = onDeleteExceptionAudit,
                hybridOpsState = hybridOpsState,
                siteSafetyRiskState = siteSafetyRiskState,
                safetyHazardInput = safetyHazardInput,
                onSafetyHazardInputChange = { safetyHazardInput = it },
                onAnalyzeSiteSafetyRisk = { onAnalyzeSiteSafetyRisk(safetyHazardInput) },
                onClearSiteSafetyRisk = onClearSiteSafetyRisk,
                onSaveSafetyMitigationTask = onSaveSafetyMitigationTask,
                qaInput = hybridQaInput,
                onQaInputChange = { hybridQaInput = it },
                onRefreshStatus = onRefreshOnDeviceStatus,
                onDownloadModel = onDownloadOnDeviceModel,
                onRunNanoDiagnostic = onRunNanoDiagnostic,
                onClearNanoDiagnostic = onClearNanoDiagnostic,
                onSummarizeDailyLog = onSummarizeDailyLog,
                onDetectDuplicateProgress = onDetectDuplicateProgress,
                onAskHybridQa = {
                    if (hybridQaInput.isNotBlank()) {
                        onAskHybridSiteQa(hybridQaInput)
                        hybridQaInput = ""
                    }
                },
                onDisableOfflineMode = onDisableOfflineMode,
                riskReports = riskReports,
                checklistsWithItems = checklistsWithItems,
                openCriticalReportCount = openCriticalReportCount,
                onSaveSiteSafetyRiskReport = onSaveSiteSafetyRiskReport,
                onCreateRiskReport = onCreateRiskReport,
                onUpdateRiskReportStatus = onUpdateRiskReportStatus,
                onDeleteRiskReport = onDeleteRiskReport,
                onCreateInspectionChecklist = onCreateInspectionChecklist,
                onSetInspectionItemResult = onSetInspectionItemResult,
                onDeleteInspectionChecklist = onDeleteInspectionChecklist
            )
            1 -> MultiTurnChatSubScreen(
                context = context,
                chatMessages = chatMessages,
                isChatLoading = isChatLoading,
                chatInput = chatInput,
                onChatInputChange = { chatInput = it },
                onSend = {
                    if (chatInput.isNotBlank()) {
                        onSendChatMessage(chatInput)
                        chatInput = ""
                    }
                },
                onQuickPrompt = { prompt ->
                    onSendChatMessage(prompt)
                },
                onClearChat = onClearChat
            )
            2 -> LiveVoiceSubScreen(
                context = context,
                liveVoiceState = liveVoiceState,
                onToggleVoice = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        onToggleLiveVoice()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
            3 -> HighThinkingSubScreen(
                context = context,
                crisisInput = crisisInput,
                onCrisisInputChange = { crisisInput = it },
                showThoughts = showThoughts,
                onToggleThoughts = { showThoughts = !showThoughts },
                thinkingState = thinkingState,
                generatorState = generatorState,
                onAnalyzeCrisis = onAnalyzeCrisis,
                onGenerateActionPack = onGenerateActionPack,
                onSaveActionPackToRoom = onSaveActionPackToRoom
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HybridOpsSubScreen(
    context: Context,
    offlineSiteTestMode: Boolean,
    onDeviceStatusLabel: String,
    isOnDeviceDownloadable: Boolean,
    verifiedOnDeviceModelName: String?,
    deviceCompatibilitySummary: String,
    nanoDiagnosticState: NanoDiagnosticUiState,
    materialSanityCheckState: MaterialSanityCheckUiState,
    exceptionAudits: List<ExceptionAuditEntity>,
    materialAreaInput: String,
    onMaterialAreaInputChange: (String) -> Unit,
    materialRequestInput: String,
    onMaterialRequestInputChange: (String) -> Unit,
    exceptionJustificationInput: String,
    onExceptionJustificationInputChange: (String) -> Unit,
    onRunMaterialSanityCheck: () -> Unit,
    onClearMaterialSanityCheck: () -> Unit,
    onRecordExceptionAudit: (String, String, String, String, String, String, String) -> Unit,
    onDeleteExceptionAudit: (ExceptionAuditEntity) -> Unit,
    hybridOpsState: HybridOpsUiState,
    siteSafetyRiskState: SiteSafetyRiskUiState,
    safetyHazardInput: String,
    onSafetyHazardInputChange: (String) -> Unit,
    onAnalyzeSiteSafetyRisk: () -> Unit,
    onClearSiteSafetyRisk: () -> Unit,
    onSaveSafetyMitigationTask: (String, String) -> Unit,
    qaInput: String,
    onQaInputChange: (String) -> Unit,
    onRefreshStatus: () -> Unit,
    onDownloadModel: () -> Unit,
    onRunNanoDiagnostic: (NanoDiagnosticLanguage) -> Unit,
    onClearNanoDiagnostic: () -> Unit,
    onSummarizeDailyLog: () -> Unit,
    onDetectDuplicateProgress: () -> Unit,
    onAskHybridQa: () -> Unit,
    onDisableOfflineMode: () -> Unit,
    riskReports: List<RiskAssessmentReportEntity>,
    checklistsWithItems: List<ChecklistWithItems>,
    openCriticalReportCount: Int,
    onSaveSiteSafetyRiskReport: (String, String, String, String, HybridInferenceSource) -> Unit,
    onCreateRiskReport: (String, String, String, String, String, String) -> Unit,
    onUpdateRiskReportStatus: (Long, String) -> Unit,
    onDeleteRiskReport: (RiskAssessmentReportEntity) -> Unit,
    onCreateInspectionChecklist: (String, String, String, List<String>, Long?) -> Unit,
    onSetInspectionItemResult: (Long, Long, String) -> Unit,
    onDeleteInspectionChecklist: (InspectionChecklistEntity) -> Unit
) {
    val lang = LocalAppLanguage.current
    val s = LocalAppStrings.current
    val auditDateFormatter = remember { SimpleDateFormat("dd/MM HH:mm", Locale.US) }
    val safetyPresets = remember(lang) {
        if (lang == AppLanguage.EN) {
            listOf(
                "3.5m deep excavation without sheet piles during heavy rain",
                "Temporary site DB panel near standing water without earthing or ELCB",
                "Craning precast slabs near energized overhead HV power lines without exclusion zone",
                "Working on 12m scaffolding missing inspection Green Tag"
            )
        } else {
            listOf(
                "บ่อขุดลึก 3.5 เมตร ไม่มีชีทไพล์กันดินพังช่วงฝนตก",
                "ตู้ไฟชั่วคราวหน้างานน้ำขัง ไม่มีสายดินและ ELCB",
                "ยกแผ่นพื้นสำเร็จรูปใกล้สายไฟแรงสูง ไม่กั้นเขตอันตราย",
                "ทำงานบนนั่งร้านสูง 12 เมตร ไม่ติดป้าย Tag ตรวจรับ"
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hybrid_ops_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. AICore / OnDeviceModelOption.PREVIEW Hardware Status & Mode Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Memory,
                                contentDescription = s.hybridCardTitle,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = s.hybridCardTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (offlineSiteTestMode) {
                                        "ONLY_ON_DEVICE • OnDeviceModelOption.PREVIEW"
                                    } else {
                                        "PREFER_ON_DEVICE (PREVIEW) -> Cloud gemini-3.8-flash"
                                    },
                                    fontSize = 11.sp,
                                    color = if (offlineSiteTestMode) Color(0xFFFFB300) else Color(0xFF00E676),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshStatus,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("refresh_ondevice_status_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = s.deviceStatusPrefix,
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${s.deviceStatusPrefix} $onDeviceStatusLabel",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                    if (deviceCompatibilitySummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        val unconfirmedLabel = if (lang == AppLanguage.EN) "Not confirmed by AICore" else "ยังไม่ยืนยันจาก AICore"
                        Text(
                            text = "${s.deviceHardwarePrefix} $deviceCompatibilitySummary • Model Name: ${verifiedOnDeviceModelName ?: unconfirmedLabel}",
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    if (isOnDeviceDownloadable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onDownloadModel,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier.testTag("download_gemini_nano_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = s.downloadOnDeviceBtn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.downloadOnDeviceBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    AppCheckDebugTokenBanner(
                        context = context,
                        errorMessage = null,
                        alwaysShowInDebug = true,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        // 1.5 Dedicated Nano 4 Full Preview Diagnostic Card (ONLY_ON_DEVICE, NO Cloud Fallback)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                    .testTag("nano4_diagnostic_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.nanoDiagnosticTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = s.nanoDiagnosticSubtitle,
                                fontSize = 10.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        if (nanoDiagnosticState !is NanoDiagnosticUiState.Idle) {
                            IconButton(
                                onClick = onClearNanoDiagnostic,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.btnClose,
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = s.nanoDiagnosticNote,
                        fontSize = 10.sp,
                        color = Color(0xFFFFD54F),
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onRunNanoDiagnostic(NanoDiagnosticLanguage.ENGLISH) },
                            enabled = nanoDiagnosticState !is NanoDiagnosticUiState.Running,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("nano4_diagnostic_en_button")
                        ) {
                            Text(
                                text = s.nanoDiagnosticEnBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { onRunNanoDiagnostic(NanoDiagnosticLanguage.THAI) },
                            enabled = nanoDiagnosticState !is NanoDiagnosticUiState.Running,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("nano4_diagnostic_th_button")
                        ) {
                            Text(
                                text = s.nanoDiagnosticThBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    when (nanoDiagnosticState) {
                        is NanoDiagnosticUiState.Idle -> Unit
                        is NanoDiagnosticUiState.Running -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == AppLanguage.EN) {
                                        "Running ONLY_ON_DEVICE (${nanoDiagnosticState.language.name})..."
                                    } else {
                                        "กำลังเรียก ONLY_ON_DEVICE (${nanoDiagnosticState.language.name})..."
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }
                        is NanoDiagnosticUiState.Success -> {
                            val res = nanoDiagnosticState.result
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                                    .testTag("nano4_diagnostic_result_card")
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    InferenceSourcePill(
                                        source = nanoDiagnosticState.actualSource,
                                        detailLabel = "Diagnostic (${res.language.name})",
                                        verifiedModelName = res.detectedModelName
                                    )
                                    Text(
                                        text = "Pre-call AICore: ${res.preCallStatus} | getModelName(): ${res.detectedModelName ?: "null"}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Text(
                                        text = "Prompt: ${res.prompt}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFD1D5DB)
                                    )
                                    Text(
                                        text = res.responseText,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                        is NanoDiagnosticUiState.UnavailableOrError -> {
                            val res = nanoDiagnosticState.result
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF3B1219),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                                    .testTag("nano4_diagnostic_error_card")
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "❌ On-Device Diagnostic Unavailable (No Cloud Fallback)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80)
                                    )
                                    Text(
                                        text = "Device: ${res.deviceInfo}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFD1D5DB)
                                    )
                                    Text(
                                        text = "Pre-call AICore: ${res.preCallStatus} | getModelName(): ${res.detectedModelName ?: "null"}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFD1D5DB)
                                    )
                                    Text(
                                        text = "Exception: ${res.errorType ?: "Unknown"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = res.errorMessage ?: "Unavailable",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFCDD2),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Daily Remind Log & Anti-BS Duplicate Check Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = s.siteOpsRoomCardTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onSummarizeDailyLog,
                            enabled = hybridOpsState !is HybridOpsUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hybrid_summarize_daily_log_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Summarize,
                                contentDescription = s.summarizeDailyLogBtn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.summarizeDailyLogBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = onDetectDuplicateProgress,
                            enabled = hybridOpsState !is HybridOpsUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("hybrid_anti_bs_check_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = s.antiBsCheckBtn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.antiBsCheckBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = s.singleTurnQaTitle,
                        fontSize = 11.sp,
                        color = Color(0xFFD1D5DB),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = qaInput,
                            onValueChange = onQaInputChange,
                            placeholder = {
                                Text(
                                    text = s.singleTurnQaPlaceholder,
                                    fontSize = 12.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("hybrid_qa_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAskHybridQa,
                            enabled = qaInput.isNotBlank() && hybridOpsState !is HybridOpsUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("hybrid_qa_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = s.singleTurnQaTitle
                            )
                        }
                    }
                }
            }
        }

        // 3. Hybrid Ops Result Card
        item {
            when (hybridOpsState) {
                is HybridOpsUiState.Idle -> Unit
                is HybridOpsUiState.Loading -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hybrid_ops_loading_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00E676),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${if (lang == AppLanguage.EN) "Processing:" else "กำลังประมวลผล:"} ${hybridOpsState.taskLabel}...",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                is HybridOpsUiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                            .testTag("hybrid_ops_result_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            InferenceSourcePill(
                                source = hybridOpsState.inferenceSource,
                                detailLabel = "Hybrid Ops",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✅ ${hybridOpsState.taskTitle}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Hybrid AI Result", hybridOpsState.resultText))
                                        Toast.makeText(context, s.toastCopied, Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = s.btnCopy,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(s.btnCopy, fontSize = 11.sp, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hybridOpsState.resultText,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
                is HybridOpsUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                            .testTag("hybrid_ops_error_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ ${hybridOpsState.message}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            AppCheckDebugTokenBanner(
                                context = context,
                                errorMessage = hybridOpsState.message,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (hybridOpsState.isOnDeviceUnavailable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onDisableOfflineMode,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF030712)
                                    ),
                                    modifier = Modifier.testTag("switch_cloud_fallback_from_ops_button")
                                ) {
                                    Text(
                                        text = s.disableOfflineUseCloudBtn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Construction Site Safety Risk Analyzer (Firebase AI Repository)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFFF7043), RoundedCornerShape(12.dp))
                    .testTag("safety_risk_analyzer_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = s.safetyCardTitle,
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.safetyCardTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = s.safetyCardSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFFFFAB91)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        safetyPresets.forEachIndexed { idx, preset ->
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(
                                        0.5.dp,
                                        Color(0xFFFF7043).copy(alpha = 0.6f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSafetyHazardInputChange(preset) }
                                    .testTag("safety_preset_chip_$idx")
                            ) {
                                Text(
                                    text = preset,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = safetyHazardInput,
                        onValueChange = onSafetyHazardInputChange,
                        label = {
                            Text(
                                text = s.safetyInputLabel,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF7043),
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("safety_risk_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAnalyzeSiteSafetyRisk,
                        enabled = safetyHazardInput.isNotBlank() && siteSafetyRiskState !is SiteSafetyRiskUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7043),
                            contentColor = Color(0xFF030712)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("analyze_safety_risk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = s.analyzeSafetyBtn,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.analyzeSafetyBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 5. Site Safety Risk Result Card
        item {
            when (siteSafetyRiskState) {
                is SiteSafetyRiskUiState.Idle -> Unit
                is SiteSafetyRiskUiState.Loading -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("safety_risk_loading_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFF7043),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (lang == AppLanguage.EN) {
                                    "Evaluating OSH safety hazards and site controls..."
                                } else {
                                    "กำลังประเมินความเสี่ยงความปลอดภัยและมาตรการป้องกันหน้างาน..."
                                },
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                is SiteSafetyRiskUiState.Success -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFF7043), RoundedCornerShape(12.dp))
                            .testTag("safety_risk_result_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InferenceSourcePill(
                                    source = siteSafetyRiskState.inferenceSource,
                                    detailLabel = "Safety AI"
                                )
                                IconButton(
                                    onClick = onClearSiteSafetyRisk,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = s.btnClose,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = s.safetyResultHeader,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFAB91)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = siteSafetyRiskState.analysis,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(
                                            ClipData.newPlainText("Safety Risk Analysis", siteSafetyRiskState.analysis)
                                        )
                                        Toast.makeText(context, s.toastCopied, Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = s.btnCopy,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(s.btnCopy, fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        onSaveSafetyMitigationTask(
                                            siteSafetyRiskState.hazardInput,
                                            siteSafetyRiskState.analysis
                                        )
                                        Toast.makeText(
                                            context,
                                            if (lang == AppLanguage.EN) "Added SAFETY checkpoint to Room DB!" else "เพิ่มภารกิจ SAFETY ลงใน Room DB แล้ว!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E676),
                                        contentColor = Color(0xFF030712)
                                    ),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(48.dp)
                                        .testTag("save_safety_task_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddTask,
                                        contentDescription = s.addToChecklistBtn,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(s.addToChecklistBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onSaveSiteSafetyRiskReport(
                                        if (lang == AppLanguage.EN) "Site Zone (Safety AI Report)" else "พื้นที่หน้างานตามรายงาน Safety AI",
                                        siteSafetyRiskState.hazardInput,
                                        siteSafetyRiskState.analysis,
                                        "HIGH",
                                        siteSafetyRiskState.inferenceSource
                                    )
                                    Toast.makeText(
                                        context,
                                        if (lang == AppLanguage.EN) "Saved to Risk Assessment Report (Room DB)!" else "บันทึกลงฐานข้อมูล Risk Report (Room DB) แล้ว!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("save_safety_risk_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = s.saveToRiskReportBtn,
                                    tint = Color(0xFFFFAB91),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.saveToRiskReportBtn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFAB91)
                                )
                            }
                        }
                    }
                }
                is SiteSafetyRiskUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                            .testTag("safety_risk_error_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ ${siteSafetyRiskState.message}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            AppCheckDebugTokenBanner(
                                context = context,
                                errorMessage = siteSafetyRiskState.message,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (siteSafetyRiskState.isOnDeviceUnavailable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onDisableOfflineMode,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF030712)
                                    ),
                                    modifier = Modifier.testTag("switch_cloud_fallback_from_safety_button")
                                ) {
                                    Text(
                                        text = s.disableOfflineUseCloudBtn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Role-Specific Material Budget Sanity Check (Non-Mutating Certified Baseline)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                    .testTag("material_sanity_check_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FactCheck,
                            contentDescription = s.sanityCardTitle,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.sanityCardTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = s.sanityCardSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = s.sanityBaselineRefPrefix,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val roleLabel = if (lang == AppLanguage.EN) {
                                "Construction Site Risk & Budget Shield Role"
                            } else {
                                CertifiedBaselineData.ROLE_TITLE
                            }
                            Text(
                                text = "${CertifiedBaselineData.BASELINE_VERSION} • $roleLabel",
                                fontSize = 10.sp,
                                color = Color(0xFFE0F2FE),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(0.5.dp, Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onMaterialAreaInputChange(s.sanityPreset60Area)
                                    onMaterialRequestInputChange(s.sanityPreset60Req)
                                }
                                .testTag("material_preset_60sqm_button")
                        ) {
                            Text(
                                text = s.sanityPreset60Label,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .border(0.5.dp, Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable {
                                    onMaterialAreaInputChange(s.sanityPresetLot3Area)
                                    onMaterialRequestInputChange(s.sanityPresetLot3Req)
                                }
                                .testTag("material_preset_lot3_button")
                        ) {
                            Text(
                                text = s.sanityPresetLot3Label,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = materialAreaInput,
                        onValueChange = onMaterialAreaInputChange,
                        label = {
                            Text(
                                text = s.sanityAreaInputLabel,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_area_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = materialRequestInput,
                        onValueChange = onMaterialRequestInputChange,
                        label = {
                            Text(
                                text = s.sanityRequestInputLabel,
                                color = Color(0xFF9CA3AF),
                                fontSize = 11.sp
                            )
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFB300),
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_request_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onRunMaterialSanityCheck,
                        enabled = materialRequestInput.isNotBlank() && materialSanityCheckState !is MaterialSanityCheckUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB300),
                            contentColor = Color(0xFF030712)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_material_sanity_check_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FactCheck,
                            contentDescription = s.runSanityCheckBtn,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = s.runSanityCheckBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 7. Material Sanity Check Result & Human Final Approval / Audit Note Panel
        item {
            when (materialSanityCheckState) {
                is MaterialSanityCheckUiState.Idle -> Unit
                is MaterialSanityCheckUiState.Loading -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_sanity_loading_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFFB300),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (lang == AppLanguage.EN) {
                                    "Auditing requisition against Certified Baseline (norms remain read-only)..."
                                } else {
                                    "กำลังเทียบรายการขอเบิกกับ Certified Baseline (ไม่แก้ฐานข้อมูลกลาง)..."
                                },
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                is MaterialSanityCheckUiState.Success -> {
                    val checkResult = materialSanityCheckState.result
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1324)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                            .testTag("material_sanity_result_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InferenceSourcePill(
                                    source = materialSanityCheckState.inferenceSource,
                                    detailLabel = "Sanity Check"
                                )
                                IconButton(
                                    onClick = onClearMaterialSanityCheck,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("clear_material_sanity_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = s.btnClose,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${s.sanityBaselineRefPrefix} ${checkResult.baselineVersion}",
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = checkResult.assessment,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = exceptionJustificationInput,
                                onValueChange = onExceptionJustificationInputChange,
                                label = {
                                    Text(
                                        text = s.exceptionNoteLabel,
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = s.exceptionNotePlaceholder,
                                        color = Color(0xFF6B7280),
                                        fontSize = 11.sp
                                    )
                                },
                                minLines = 2,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E676),
                                    unfocusedBorderColor = Color(0xFF374151)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("exception_justification_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onRecordExceptionAudit(
                                            checkResult.requestInput,
                                            checkResult.areaScope,
                                            checkResult.baselineVersion,
                                            checkResult.assessment,
                                            "APPROVED_WITH_EXCEPTION",
                                            exceptionJustificationInput,
                                            checkResult.origin.name
                                        )
                                        onExceptionJustificationInputChange("")
                                        Toast.makeText(
                                            context,
                                            if (lang == AppLanguage.EN) {
                                                "Recorded Approved Exception to Audit Log (baseline unmodified)"
                                            } else {
                                                "บันทึกอนุมัติข้อยกเว้นลง Audit Log แล้ว (เกณฑ์กลางไม่ถูกแก้ไข)"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E676),
                                        contentColor = Color(0xFF030712)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("approve_exception_audit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = s.approveExceptionBtn,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = s.approveExceptionBtn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        onRecordExceptionAudit(
                                            checkResult.requestInput,
                                            checkResult.areaScope,
                                            checkResult.baselineVersion,
                                            checkResult.assessment,
                                            "HOLD_FOR_INVESTIGATION",
                                            exceptionJustificationInput,
                                            checkResult.origin.name
                                        )
                                        onExceptionJustificationInputChange("")
                                        Toast.makeText(
                                            context,
                                            if (lang == AppLanguage.EN) {
                                                "Recorded Hold / Investigation order to Audit Log"
                                            } else {
                                                "บันทึกคำสั่งระงับ/เรียกชี้แจงลง Audit Log แล้ว"
                                            },
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("hold_exception_audit_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PauseCircle,
                                        contentDescription = s.holdAuditBtn,
                                        tint = Color(0xFFFF8A80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = s.holdAuditBtn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80)
                                    )
                                }
                            }
                        }
                    }
                }
                is MaterialSanityCheckUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                            .testTag("material_sanity_error_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ ${materialSanityCheckState.message}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            AppCheckDebugTokenBanner(
                                context = context,
                                errorMessage = materialSanityCheckState.message,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            if (materialSanityCheckState.isOnDeviceUnavailable) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onDisableOfflineMode,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color(0xFF030712)
                                    ),
                                    modifier = Modifier.testTag("switch_cloud_fallback_from_sanity_button")
                                ) {
                                    Text(
                                        text = s.disableOfflineUseCloudBtn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Saved Exception Audit Log Trail (Room DB v3 - Non-Mutating Transparency Record)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
                    .testTag("exception_audit_log_section")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = s.auditLogTitlePrefix,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${s.auditLogTitlePrefix} ${exceptionAudits.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.auditLogSubtitle,
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    if (exceptionAudits.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = s.auditLogEmpty,
                            fontSize = 11.sp,
                            color = Color(0xFFD1D5DB),
                            lineHeight = 16.sp
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            exceptionAudits.take(10).forEach { audit ->
                                val isApproved = audit.decisionStatus == "APPROVED_WITH_EXCEPTION"
                                val badgeColor = if (isApproved) Color(0xFF00E676) else Color(0xFFFF5252)
                                val badgeText = if (isApproved) "APPROVED EXCEPTION" else "HOLD / AUDIT"
                                val formattedTime = auditDateFormatter.format(Date(audit.timestamp))

                                Surface(
                                    color = Color(0xFF0B1324),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(0.75.dp, badgeColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                        .testTag("exception_audit_item_${audit.id}")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                color = badgeColor.copy(alpha = 0.16f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = badgeText,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = badgeColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteExceptionAudit(audit) },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("delete_exception_audit_${audit.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = audit.lotOrItemTitle,
                                                    tint = Color(0xFF9CA3AF),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${audit.areaOrScope} • ${audit.lotOrItemTitle}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${s.auditReasonPrefix} ${audit.justificationNote}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE5E7EB),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$formattedTime • ${audit.inferenceOrigin} • ${s.auditFooterImmutable}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 9. Room v4 Construction Risk Reports & Relational Inspection Checklists
        item {
            RiskInspectionRoomPanel(
                riskReports = riskReports,
                checklistsWithItems = checklistsWithItems,
                openCriticalReportCount = openCriticalReportCount,
                onCreateRiskReport = onCreateRiskReport,
                onUpdateRiskReportStatus = onUpdateRiskReportStatus,
                onDeleteRiskReport = onDeleteRiskReport,
                onCreateInspectionChecklist = onCreateInspectionChecklist,
                onSetInspectionItemResult = onSetInspectionItemResult,
                onDeleteInspectionChecklist = onDeleteInspectionChecklist
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MultiTurnChatSubScreen(
    context: Context,
    chatMessages: List<ChatMessage>,
    isChatLoading: Boolean,
    chatInput: String,
    onChatInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onQuickPrompt: (String) -> Unit,
    onClearChat: () -> Unit
) {
    val lang = LocalAppLanguage.current
    val s = LocalAppStrings.current
    val quickPrompts = remember(lang) {
        if (lang == AppLanguage.EN) {
            listOf(
                "How to reduce excessive LD penalties under Thai law?",
                "Steps to issue formal EOT notice for late handover",
                "Subcontractor walked off site: can we withhold Retention?"
            )
        } else {
            listOf(
                "วิธีลดค่าปรับ LD ตามกฎหมายไทย",
                "ขั้นตอนทำหนังสือขอ EOT เมื่อส่งพื้นที่ช้า",
                "ผู้รับเหมาทิ้งงาน หัก Retention ได้ไหม?"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = s.chatHeaderTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = s.chatHeaderTitle,
                    tint = Color(0xFF9CA3AF)
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            quickPrompts.forEachIndexed { idx, prompt ->
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !isChatLoading) { onQuickPrompt(prompt) }
                        .testTag("chat_quick_prompt_$idx")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = chatMessages,
                key = { it.id }
            ) { message ->
                ChatBubbleItem(
                    context = context,
                    message = message
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = onChatInputChange,
                placeholder = {
                    Text(
                        text = s.chatInputPlaceholder,
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF00E5FF),
                    unfocusedBorderColor = Color(0xFF374151)
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSend,
                enabled = chatInput.isNotBlank() && !isChatLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF030712)
                ),
                modifier = Modifier
                    .height(52.dp)
                    .testTag("send_chat_message_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = s.chatInputPlaceholder
                )
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    context: Context,
    message: ChatMessage
) {
    val s = LocalAppStrings.current
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF00838F) else Color(0xFF111827)
            ),
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 12.dp
            ),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .border(
                    1.dp,
                    if (isUser) Color(0xFF00E5FF) else Color(0xFF374151),
                    RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) s.chatBubbleUserLabel else s.chatBubbleAiLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else Color(0xFF00E5FF)
                    )
                    if (!isUser && !message.isStreaming) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Chat", message.text))
                                Toast.makeText(context, s.toastCopied, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = s.btnCopy,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                if (!isUser && message.inferenceSource != null && !message.isStreaming) {
                    Spacer(modifier = Modifier.height(4.dp))
                    InferenceSourcePill(source = message.inferenceSource)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )

                if (!isUser && message.text.contains("App Check", ignoreCase = true)) {
                    AppCheckDebugTokenBanner(
                        context = context,
                        errorMessage = message.text,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GroundingSourcesSection(
                        context = context,
                        searchQueries = emptyList(),
                        sources = message.sources
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveVoiceSubScreen(
    context: Context,
    liveVoiceState: LiveVoiceUiState,
    onToggleVoice: () -> Unit
) {
    val s = LocalAppStrings.current
    val isActive = liveVoiceState is LiveVoiceUiState.Active
    val isConnecting = liveVoiceState is LiveVoiceUiState.Connecting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.5.dp,
                    if (isActive) Color(0xFF00E676) else Color(0xFF00E5FF),
                    RoundedCornerShape(14.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                InferenceSourcePill(
                    source = HybridInferenceSource.IN_CLOUD,
                    detailLabel = "Gemini 3.8 Live",
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(84.dp)
                        .background(
                            color = if (isActive) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFF1E293B),
                            shape = CircleShape
                        )
                        .border(
                            2.dp,
                            if (isActive) Color(0xFF00E676) else Color(0xFF00E5FF),
                            CircleShape
                        )
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isActive) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = s.liveVoiceTitle,
                            tint = if (isActive) Color(0xFF00E676) else Color(0xFF00E5FF),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = s.liveVoiceTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (liveVoiceState) {
                        is LiveVoiceUiState.Idle -> s.liveVoiceIdleDesc
                        is LiveVoiceUiState.Connecting -> s.liveVoiceConnectingDesc
                        is LiveVoiceUiState.Active -> s.liveVoiceActiveDesc
                        is LiveVoiceUiState.Error -> "⚠️ ${liveVoiceState.message}"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )

                if (liveVoiceState is LiveVoiceUiState.Error) {
                    AppCheckDebugTokenBanner(
                        context = context,
                        errorMessage = liveVoiceState.message,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onToggleVoice,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive || isConnecting) Color(0xFFFF5252) else Color(0xFF00E676),
                        contentColor = if (isActive || isConnecting) Color.White else Color(0xFF030712)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("toggle_live_voice_button")
                ) {
                    Icon(
                        imageVector = if (isActive || isConnecting) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = if (isActive || isConnecting) s.liveVoiceStopBtn else s.liveVoiceStartBtn
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive || isConnecting) s.liveVoiceStopBtn else s.liveVoiceStartBtn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (liveVoiceState is LiveVoiceUiState.Active) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = s.liveVoiceUserTranscriptLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Text(
                        text = liveVoiceState.userTranscript,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = s.liveVoiceModelTranscriptLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                    Text(
                        text = liveVoiceState.modelTranscript,
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HighThinkingSubScreen(
    context: Context,
    crisisInput: String,
    onCrisisInputChange: (String) -> Unit,
    showThoughts: Boolean,
    onToggleThoughts: () -> Unit,
    thinkingState: AiThinkingUiState,
    generatorState: AiGeneratorUiState,
    onAnalyzeCrisis: (String) -> Unit,
    onGenerateActionPack: (String) -> Unit,
    onSaveActionPackToRoom: (GeneratedActionPack) -> Unit
) {
    val lang = LocalAppLanguage.current
    val s = LocalAppStrings.current
    val quickScenarios = remember(lang) {
        if (lang == AppLanguage.EN) {
            listOf(
                "Subcontractor cutting manpower — abandonment risk",
                "Verbal rework order with no signed VO",
                "LD exposure 150,000 THB/day — EOT needed",
                "Consultant delaying concrete pour RFI past 4-hr SLA"
            )
        } else {
            listOf(
                "ผู้รับเหมาลดคนงานเสี่ยงทิ้งงาน",
                "สั่งแก้งานปากเปล่าไม่เซ็น VO",
                "เสี่ยงโดนค่าปรับ LD วันละ 1.5 แสน",
                "คอนเซาท์ดึงเวลาตรวจปูนเกิน SLA 4 ชม."
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ai_advisor_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFB388FF), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = s.highThinkingTitle,
                            tint = Color(0xFFB388FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = s.highThinkingTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Gemini 4 Carbon (ThinkingLevel.HIGH) + Gemini 3.8 Flash",
                                fontSize = 11.sp,
                                color = Color(0xFFB388FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = s.highThinkingSubtitle,
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = s.crisisScenariosHeader,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickScenarios.forEachIndexed { index, label ->
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .border(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable { onCrisisInputChange(label) }
                                    .testTag("quick_scenario_chip_$index")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = crisisInput,
                        onValueChange = onCrisisInputChange,
                        label = { Text(s.crisisInputLabel, color = Color(0xFF9CA3AF)) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_crisis_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAnalyzeCrisis(crisisInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("run_high_thinking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = s.runHighThinkingBtn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = s.runHighThinkingBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = { onGenerateActionPack(crisisInput) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("generate_action_pack_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = s.generateActionPackBtn,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = s.generateActionPackBtn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // High Thinking Output Section
        item {
            when (thinkingState) {
                is AiThinkingUiState.Idle -> Unit
                is AiThinkingUiState.Loading -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFB388FF),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (lang == AppLanguage.EN) {
                                        "Gemini 4 Carbon analyzing in Deep Thinking mode (ThinkingLevel.HIGH)..."
                                    } else {
                                        "Gemini 4 Carbon กำลังใช้โหมดคิดวิเคราะห์เชิงลึก (ThinkingLevel.HIGH)..."
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (lang == AppLanguage.EN) {
                                        "Evaluating contract clauses, LD exposure, and defense strategy"
                                    } else {
                                        "กำลังตรวจสอบเงื่อนไขสัญญา ค่าปรับ LD และแนวทางป้องกันความรับผิดชอบ"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }
                is AiThinkingUiState.Streaming, is AiThinkingUiState.Success -> {
                    val thoughts = when (thinkingState) {
                        is AiThinkingUiState.Streaming -> thinkingState.thoughts
                        is AiThinkingUiState.Success -> thinkingState.thoughts
                        else -> ""
                    }
                    val answer = when (thinkingState) {
                        is AiThinkingUiState.Streaming -> thinkingState.answer
                        is AiThinkingUiState.Success -> thinkingState.answer
                        else -> ""
                    }
                    val successState = thinkingState as? AiThinkingUiState.Success
                    val isStreaming = thinkingState is AiThinkingUiState.Streaming

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFB388FF), RoundedCornerShape(12.dp))
                            .testTag("high_thinking_result_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (successState != null) {
                                InferenceSourcePill(
                                    source = successState.inferenceSource,
                                    detailLabel = "Gemini 4 Carbon",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            if (thoughts.isNotBlank()) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleThoughts() }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = s.highThinkingReasoningHeader,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB388FF)
                                            )
                                            Icon(
                                                imageVector = if (showThoughts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = s.highThinkingReasoningHeader,
                                                tint = Color(0xFFB388FF)
                                            )
                                        }
                                        AnimatedVisibility(visible = showThoughts) {
                                            Text(
                                                text = thoughts,
                                                fontSize = 11.sp,
                                                color = Color(0xFF9CA3AF),
                                                lineHeight = 16.sp,
                                                modifier = Modifier.padding(top = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isStreaming) {
                                        if (lang == AppLanguage.EN) "⚡ Drafting defense strategy..." else "⚡ กำลังร่างแผนรับมือ..."
                                    } else {
                                        s.highThinkingResultHeader
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Advice", answer))
                                        Toast.makeText(context, s.toastCopied, Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = s.btnCopy,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(s.btnCopy, fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = answer.ifBlank {
                                    if (lang == AppLanguage.EN) "Analyzing dispute..." else "กำลังวิเคราะห์ข้อมูล..."
                                },
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }
                is AiThinkingUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                            .testTag("high_thinking_error_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ ${thinkingState.message}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            AppCheckDebugTokenBanner(
                                context = context,
                                errorMessage = thinkingState.message,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Structured Action Pack Output Section (Save directly to Room DB)
        item {
            when (generatorState) {
                is AiGeneratorUiState.Idle -> Unit
                is AiGeneratorUiState.Loading -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (lang == AppLanguage.EN) {
                                    "Generating structured SOS script & action checklist for Room DB..."
                                } else {
                                    "กำลังสร้างสคริปต์ SOS และรายการเช็กลิสต์สำหรับบันทึกลง Room DB..."
                                },
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
                is AiGeneratorUiState.Success -> {
                    val pack = generatorState.pack
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                            .testTag("generated_pack_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            InferenceSourcePill(
                                source = pack.inferenceSource,
                                detailLabel = "JSON Pack",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = s.actionPackHeader,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SOS Script: ${pack.scriptTitle}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = pack.scriptContent,
                                fontSize = 11.sp,
                                color = Color(0xFFD1D5DB),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${s.actionPackTasksHeader} (${pack.suggestedTasks.size}):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            pack.suggestedTasks.forEach { task ->
                                Text(
                                    text = "• [${task.category}] ${task.title}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD1D5DB),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onSaveActionPackToRoom(pack)
                                    Toast.makeText(
                                        context,
                                        if (lang == AppLanguage.EN) {
                                            "Saved SOS script and checkpoints to Room DB!"
                                        } else {
                                            "บันทึกสคริปต์และเช็กลิสต์ลง Room DB เรียบร้อย!"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E676),
                                    contentColor = Color(0xFF030712)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_pack_to_room_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = s.saveActionPackBtn,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.saveActionPackBtn,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
                is AiGeneratorUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "⚠️ ${generatorState.message}",
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            AppCheckDebugTokenBanner(
                                context = context,
                                errorMessage = generatorState.message,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
