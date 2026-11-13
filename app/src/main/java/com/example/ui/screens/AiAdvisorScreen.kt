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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.CertifiedBaselineData
import com.example.data.ExceptionAuditEntity
import com.example.data.NanoDiagnosticLanguage
import com.example.ui.components.InferenceSourcePill
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
    onToggleLiveVoice: () -> Unit
) {
    // 0 = Hybrid Ops (Preview Target + Cloud), 1 = Chatbot, 2 = Live Voice, 3 = High Thinking & Pack
    var activeSubTab by rememberSaveable { mutableIntStateOf(0) }
    var chatInput by rememberSaveable { mutableStateOf("") }
    var hybridQaInput by rememberSaveable { mutableStateOf("") }
    var materialAreaInput by rememberSaveable { mutableStateOf("ชั้น 1 (พื้นที่ 60 ตร.ม.)") }
    var materialRequestInput by rememberSaveable {
        mutableStateOf("ขอเบิกกระเบื้อง 95 ตร.ม. และปูนกาว 40 ถุง สำหรับปูพื้นชั้น 1")
    }
    var exceptionJustificationInput by rememberSaveable { mutableStateOf("") }
    var safetyHazardInput by rememberSaveable {
        mutableStateOf("ช่างติดตั้งนั่งร้านชั้น 5 สูง 14 เมตร ไม่มีราวกันตกและคนงานไม่สวม Safety Harness")
    }
    var crisisInput by rememberSaveable {
        mutableStateOf(
            "ผู้ว่าจ้างสั่งรื้อแก้ไขผนังก่ออิฐมวลเบาชั้น 4 ปากเปล่าโดยไม่ออกเอกสาร VO และเร่งให้เสร็จก่อนตรวจงวดงานวันศุกร์"
        )
    }
    var showThoughts by rememberSaveable { mutableStateOf(true) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onToggleLiveVoice()
        } else {
            Toast.makeText(
                context,
                "กรุณาอนุญาตการใช้ไมโครโฟนเพื่อสนทนาเสียงสดกับ Gemini 3.8 Live",
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
                label = { Text("Hybrid Ops (Preview)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Hybrid Ops On-Device Preview",
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
                label = { Text("แชทบอท AI", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "แชทบอทที่ปรึกษาสัญญา",
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
                label = { Text("คุยเสียงสด (Live)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "สนทนาเสียงสด Gemini 3.8 Live",
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
                label = { Text("High Thinking", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "โหมดคิดเชิงลึก Gemini 4 Carbon",
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
                onDisableOfflineMode = onDisableOfflineMode
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
    onDisableOfflineMode: () -> Unit
) {
    val auditDateFormatter = remember { SimpleDateFormat("dd/MM HH:mm", Locale.US) }
    val safetyPresets = listOf(
        "บ่อขุดลึก 3.5 เมตร ไม่มีชีทไพล์กันดินพังช่วงฝนตก",
        "ตู้ไฟชั่วคราวหน้างานน้ำขัง ไม่มีสายดินและ ELCB",
        "ยกแผ่นพื้นสำเร็จรูปใกล้สายไฟแรงสูง ไม่กั้นเขตอันตราย",
        "ทำงานบนนั่งร้านสูง 12 เมตร ไม่ติดป้าย Tag ตรวจรับ"
    )
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
                                contentDescription = "สถานะ AICore On-Device Preview",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Firebase AI Logic Hybrid Inference",
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
                                contentDescription = "ตรวจสอบสถานะ AICore ใหม่",
                                tint = Color(0xFF00E5FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "สถานะเครื่อง: $onDeviceStatusLabel",
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB)
                    )
                    if (deviceCompatibilitySummary.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "อุปกรณ์: $deviceCompatibilitySummary • Model Name: ${verifiedOnDeviceModelName ?: "ยังไม่ยืนยันจาก AICore"}",
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
                                contentDescription = "ดาวน์โหลดโมเดล On-Device Preview",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ดาวน์โหลดโมเดล On-Device Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
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
                                text = "Nano 4 Full Preview Diagnostic (ONLY_ON_DEVICE)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = "Target: OnDeviceModelOption.PREVIEW (nano-v4-full / Gemma 4 E4B) • maxOutputTokens=256 • ไม่สลับไป Cloud",
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
                                    contentDescription = "ล้างผลทดสอบ Diagnostic",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "หมายเหตุเอกสาร Google: AICore Developer Preview รองรับอุปกรณ์และโปรแกรมที่ลงทะเบียน (EN/KO ได้รับการรับรองอย่างเป็นทางการ; ทดสอบ TH แยกเพื่อสังเกตผลจริง)",
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
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("nano4_diagnostic_en_button")
                        ) {
                            Text(
                                text = "Test EN (On-Device)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = { onRunNanoDiagnostic(NanoDiagnosticLanguage.THAI) },
                            enabled = nanoDiagnosticState !is NanoDiagnosticUiState.Running,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("nano4_diagnostic_th_button")
                        ) {
                            Text(
                                text = "ทดสอบ TH (On-Device)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
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
                                    text = "กำลังเรียก ONLY_ON_DEVICE (${nanoDiagnosticState.language.name})...",
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
                        text = "งานหน้างานแบบ On-Device / Hybrid (จาก Room DB จริง)",
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
                                contentDescription = "สรุป Daily Remind Log",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("สรุป Daily Log", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
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
                                contentDescription = "ตรวจจับรายงานซ้ำซ้อน Anti-BS",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ตรวจ Anti-BS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ถาม-ตอบด่วนหน้างาน (Single-Turn Hybrid Q&A):",
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
                                    text = "เช่น เช็กอะไรก่อนเทคอนกรีตฐานราก?",
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
                                contentDescription = "ส่งคำถาม Hybrid Q&A"
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
                                text = "กำลังประมวลผล: ${hybridOpsState.taskLabel}...",
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
                                        Toast.makeText(context, "คัดลอกผลวิเคราะห์สำเร็จ!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "คัดลอกผล Hybrid AI",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("คัดลอก", fontSize = 11.sp, color = Color.White)
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
                                        text = "ปิด Offline Mode เพื่อสลับกลับ Cloud Fallback",
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
                            contentDescription = "วิเคราะห์ความเสี่ยงปลอดภัยหน้างาน",
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "วิเคราะห์ความเสี่ยงปลอดภัยหน้างาน (Site Safety AI)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "ประเมินอันตราย กฎกระทรวง 2564, PPE และมาตรการ Stop-Work จากข้อความ",
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
                                text = "ระบุสภาพหน้างาน / จุดเสี่ยงอุบัติเหตุที่พบ",
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
                            contentDescription = "วิเคราะห์ความเสี่ยงปลอดภัยด้วย Gemini AI",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "วิเคราะห์ความเสี่ยงปลอดภัย (Hybrid AI)",
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
                                text = "กำลังประเมินความเสี่ยงความปลอดภัยและมาตรการป้องกันหน้างาน...",
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
                                        contentDescription = "ปิดผลวิเคราะห์ความปลอดภัย",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🦺 ผลการประเมินความเสี่ยงปลอดภัยหน้างาน",
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
                                        Toast.makeText(context, "คัดลอกผลประเมินความปลอดภัยแล้ว!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "คัดลอกผลประเมินความปลอดภัย",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("คัดลอก", fontSize = 11.sp, color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        onSaveSafetyMitigationTask(
                                            siteSafetyRiskState.hazardInput,
                                            siteSafetyRiskState.analysis
                                        )
                                        Toast.makeText(
                                            context,
                                            "เพิ่มภารกิจ SAFETY ลงใน Room DB แล้ว!",
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
                                        contentDescription = "บันทึกเป็นภารกิจ Safety ใน Room DB",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("เพิ่มเข้าเช็กลิสต์", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                }
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
                                        text = "ปิด Offline Mode เพื่อสลับกลับ Cloud Fallback",
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
                            contentDescription = "ตรวจความผิดปกติใบขอเบิกวัสดุ",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ตรวจใบขอเบิกวัสดุ & ราคาผิดปกติ (Sanity Check)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "เทียบเกณฑ์กลางอ่านอย่างเดียว • ข้อมูลชั่วคราวไม่ทับมาตรฐาน",
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
                                contentDescription = "เกณฑ์กลางแบบอ่านอย่างเดียว",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${CertifiedBaselineData.BASELINE_VERSION} • ${CertifiedBaselineData.ROLE_TITLE}",
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
                                    onMaterialAreaInputChange("ชั้น 1 (พื้นที่ 60 ตร.ม.)")
                                    onMaterialRequestInputChange("ขอเบิกกระเบื้อง 95 ตร.ม. และปูนกาว 40 ถุง สำหรับปูพื้นชั้น 1")
                                }
                                .testTag("material_preset_60sqm_button")
                        ) {
                            Text(
                                text = "เคส 60 ตร.ม. (ปูนกาว 40 ถุง / กระเบื้อง 95 ตร.ม.)",
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
                                    onMaterialAreaInputChange("Lot 3 งานกระจกและผนังเบา")
                                    onMaterialRequestInputChange("สั่งซื้อกระจก Tempered 12mm Lot 3 ราคาสูงกว่างบกลาง +22% เพื่อส่งมอบทันงวดงาน")
                                }
                                .testTag("material_preset_lot3_button")
                        ) {
                            Text(
                                text = "เคส Lot 3 ราคาสูงกว่าปกติ (+22% งานเร่งด่วน)",
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
                                text = "พื้นที่ / Lot งานที่ขอเบิก (เช่น ชั้น 1 พื้นที่ 60 ตร.ม.)",
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
                                text = "รายละเอียดใบขอเบิกวัสดุ/ราคาที่ลูกน้องส่งมา (ตรวจชั่วคราว)",
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
                            contentDescription = "ตรวจความผิดปกติเทียบเกณฑ์กลาง",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ตรวจความผิดปกติเทียบเกณฑ์กลาง (Hybrid AI)",
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
                                text = "กำลังเทียบรายการขอเบิกกับ Certified Baseline (ไม่แก้ฐานข้อมูลกลาง)...",
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
                                        contentDescription = "ปิดผลตรวจใบเบิกวัสดุ",
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "🔒 เกณฑ์อ้างอิง: ${checkResult.baselineVersion}",
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
                                        text = "หมายเหตุโปร่งใสของผู้จัดการ (เหตุผลที่อนุมัติแพงขึ้น / ระงับตรวจ)",
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 11.sp
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = "เช่น Lot 3 ราคาสูงกว่าเกณฑ์ +22% แต่ต้องสั่งด่วนเพื่อส่งมอบทันงวด",
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
                                            "บันทึกอนุมัติข้อยกเว้นลง Audit Log แล้ว (เกณฑ์กลางไม่ถูกแก้ไข)",
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
                                        contentDescription = "อนุมัติแบบมีข้อยกเว้นลง Audit Log",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "อนุมัติข้อยกเว้น",
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
                                            "บันทึกคำสั่งระงับ/เรียกชี้แจงลง Audit Log แล้ว",
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
                                        contentDescription = "ระงับและเรียกชี้แจงลง Audit Log",
                                        tint = Color(0xFFFF8A80),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ระงับ/เรียกชี้แจง",
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
                                        text = "ปิด Offline Mode เพื่อสลับกลับ Cloud Fallback",
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
                            contentDescription = "บันทึกหลักฐานข้อยกเว้น Audit Log",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "บันทึกหลักฐานข้อยกเว้น (Exception Audit Log: ${exceptionAudits.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "เก็บหลักฐานเหตุผลที่อนุมัติหรือระงับเฉพาะ Lot/เคส โดยไม่ไปแก้เกณฑ์มาตรฐานกลาง",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    if (exceptionAudits.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ยังไม่มีบันทึกข้อยกเว้น — กด \"ตรวจความผิดปกติเทียบเกณฑ์กลาง\" แล้วเลือกอนุมัติข้อยกเว้นหรือระงับเพื่อเก็บหลักฐานโปร่งใส",
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
                                                    contentDescription = "ลบบันทึกข้อยกเว้น ${audit.lotOrItemTitle}",
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
                                            text = "เหตุผล: ${audit.justificationNote}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFE5E7EB),
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$formattedTime • ${audit.inferenceOrigin} • ไม่ทับเกณฑ์มาตรฐานกลาง",
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
    val quickPrompts = listOf(
        "วิธีลดค่าปรับ LD ตามกฎหมายไทย",
        "ขั้นตอนทำหนังสือขอ EOT เมื่อส่งพื้นที่ช้า",
        "ผู้รับเหมาทิ้งงาน หัก Retention ได้ไหม?"
    )

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
                text = "💬 แชทบอทที่ปรึกษากฎหมายก่อสร้าง (Cloud Gemini + Google Search)",
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
                    contentDescription = "ล้างประวัติแชท",
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
                        text = "ถามข้อกฎหมายก่อสร้าง ค่าปรับ LD หรือวิธีเจรจา...",
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
                    contentDescription = "ส่งข้อความแชท"
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
                        text = if (isUser) "ผู้จัดการฝ่ายก่อสร้าง (คุณ)" else "🛡️ CAREER SHIELD AI",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color.White else Color(0xFF00E5FF)
                    )
                    if (!isUser && !message.isStreaming) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("AI Chat", message.text))
                                Toast.makeText(context, "คัดลอกคำตอบสำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "คัดลอกคำตอบแชท",
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
    liveVoiceState: LiveVoiceUiState,
    onToggleVoice: () -> Unit
) {
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
                            contentDescription = "สถานะไมโครโฟนสนทนาสด",
                            tint = if (isActive) Color(0xFF00E676) else Color(0xFF00E5FF),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "สนทนาเสียงสดหน้างาน (Gemini 3.8 Live API)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (liveVoiceState) {
                        is LiveVoiceUiState.Idle ->
                            "กดปุ่มด้านล่างเพื่อพูดปรึกษาปัญหาหน้างานแบบ Hands-Free ขณะเดินตรวจไซต์งาน"
                        is LiveVoiceUiState.Connecting ->
                            "กำลังเชื่อมต่อเซสชันเสียงสด Gemini 3.8 Live..."
                        is LiveVoiceUiState.Active ->
                            "🟢 กำลังสนทนาสด — พูดถามเรื่องค่าปรับ LD สัญญา หรือวิธีรับมือผู้รับเหมาได้เลย"
                        is LiveVoiceUiState.Error ->
                            "⚠️ ${liveVoiceState.message}"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )

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
                        contentDescription = "สลับการสนทนาเสียงสด"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isActive || isConnecting) "วางสาย / หยุดสนทนาเสียงสด" else "เริ่มคุยเสียงสดกับ Gemini 3.8 Live",
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
                        text = "🎙️ เสียงของคุณ (Live Input Transcript):",
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
                        text = "🔊 คำตอบจากที่ปรึกษา AI (Live Audio Transcript):",
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
    val quickScenarios = listOf(
        "ผู้รับเหมาลดคนงานเสี่ยงทิ้งงาน",
        "สั่งแก้งานปากเปล่าไม่เซ็น VO",
        "เสี่ยงโดนค่าปรับ LD วันละ 1.5 แสน",
        "คอนเซาท์ดึงเวลาตรวจปูนเกิน SLA 4 ชม."
    )

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
                            contentDescription = "โหมดคิดวิเคราะห์เชิงลึก Gemini 4 Carbon",
                            tint = Color(0xFFB388FF),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.ai_thinking_title),
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
                        text = stringResource(R.string.ai_thinking_subtitle),
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
                        text = "เลือกสถานการณ์วิกฤตหน้างาน หรือพิมพ์ปัญหาของคุณ:",
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
                        label = { Text("รายละเอียดปัญหาหน้างาน / ข้อพิพาทสัญญา", color = Color(0xFF9CA3AF)) },
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
                                .testTag("run_high_thinking_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "วิเคราะห์เชิงลึกด้วย High Thinking",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "วิเคราะห์เชิงลึก (High Thinking)",
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
                                .testTag("generate_action_pack_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "สร้างสคริปต์และเช็กลิสต์ลง Room DB",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "สร้างสคริปต์+เช็กลิสต์",
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
                                    text = "Gemini 4 Carbon กำลังใช้โหมดคิดวิเคราะห์เชิงลึก (ThinkingLevel.HIGH)...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "กำลังตรวจสอบเงื่อนไขสัญญา ค่าปรับ LD และแนวทางป้องกันความรับผิดชอบ",
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
                                                text = "🧠 กระบวนการคิดเชิงลึก (Internal Reasoning)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB388FF)
                                            )
                                            Icon(
                                                imageVector = if (showThoughts) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "สลับการแสดงกระบวนการคิด",
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
                                    text = if (isStreaming) "⚡ กำลังร่างแผนรับมือ..." else "✅ แผนป้องกันตัวและข้อเสนอแนะทางสัญญา",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                                Button(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Advice", answer))
                                        Toast.makeText(context, "คัดลอกแผนรับมือสำเร็จ!", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "คัดลอกคำแนะนำ AI",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("คัดลอก", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = answer.ifBlank { "กำลังวิเคราะห์ข้อมูล..." },
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
                        Text(
                            text = "⚠️ ${thinkingState.message}",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
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
                                text = "กำลังสร้างสคริปต์ SOS และรายการเช็กลิสต์สำหรับบันทึกลง Room DB...",
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
                                text = "✨ ชุดสคริปต์และเช็กลิสต์พร้อมบันทึกลงฐานข้อมูล Room DB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "สคริปต์ SOS: ${pack.scriptTitle}",
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
                                text = "ภารกิจเช็กลิสต์ที่แนะนำ (${pack.suggestedTasks.size} ข้อ):",
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
                                        "บันทึกสคริปต์และเช็กลิสต์ลง Room DB เรียบร้อย!",
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
                                    contentDescription = "บันทึกลง Room DB",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "บันทึกทั้งหมดลงฐานข้อมูล Room DB ทันที",
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
                        Text(
                            text = "⚠️ ${generatorState.message}",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}
