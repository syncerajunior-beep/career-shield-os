package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.CareerShieldRepository
import com.example.data.GeminiAiRepository
import com.example.ui.components.HybridStatusStrip
import com.example.ui.screens.AiAdvisorScreen
import com.example.ui.screens.ContractRiskScreen
import com.example.ui.screens.ScriptScreen
import com.example.ui.screens.TaskListScreen
import com.example.ui.theme.CareerShieldAndroidTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.CareerShieldViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CareerShieldRepository(database.taskDao())
        val aiRepository = GeminiAiRepository()

        setContent {
            CareerShieldAndroidTheme {
                val careerViewModel: CareerShieldViewModel = viewModel(
                    factory = CareerShieldViewModel.provideFactory(repository, aiRepository)
                )
                MainAppScreen(viewModel = careerViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: CareerShieldViewModel = viewModel(
        factory = CareerShieldViewModel.provideFactory(
            CareerShieldRepository(AppDatabase.getDatabase(LocalContext.current).taskDao()),
            GeminiAiRepository()
        )
    )
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var pendingAiCrisisPrompt by rememberSaveable { mutableStateOf("") }
    var pendingChatDraft by rememberSaveable { mutableStateOf("") }
    var pendingHybridOp by rememberSaveable { mutableStateOf("") }
    var pendingInspectPhotoUri by rememberSaveable { mutableStateOf("") }
    var pendingInspectTaskTitle by rememberSaveable { mutableStateOf("") }

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val scripts by viewModel.scripts.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val offlineSiteTestMode by viewModel.offlineSiteTestMode.collectAsStateWithLifecycle()
    val lastInferenceState by viewModel.lastInferenceState.collectAsStateWithLifecycle()
    val onDeviceStatusLabel by viewModel.onDeviceStatusLabel.collectAsStateWithLifecycle()
    val isOnDeviceDownloadable by viewModel.isOnDeviceDownloadable.collectAsStateWithLifecycle()
    val scriptTailorState by viewModel.scriptTailorState.collectAsStateWithLifecycle()
    val hybridOpsState by viewModel.hybridOpsState.collectAsStateWithLifecycle()
    val siteSafetyRiskState by viewModel.siteSafetyRiskState.collectAsStateWithLifecycle()
    val sitePhotoInspectState by viewModel.sitePhotoInspectState.collectAsStateWithLifecycle()

    val contractRiskState by viewModel.contractRiskState.collectAsStateWithLifecycle()
    val thinkingState by viewModel.thinkingState.collectAsStateWithLifecycle()
    val generatorState by viewModel.generatorState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val liveVoiceState by viewModel.liveVoiceState.collectAsStateWithLifecycle()

    val remainingTasksCount = remember(tasks) { tasks.count { !it.isCompleted } }
    val tasksWithPhotos = remember(tasks) { tasks.filter { !it.photoUri.isNullOrBlank() } }

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "โลโก้ Career Shield OS",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.header_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.header_subtitle),
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0F1D)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A0F1D)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_checklist),
                            fontSize = 10.sp,
                            color = if (selectedTab == 0) Color(0xFF00E5FF) else Color.White,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (remainingTasksCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFFF5252),
                                        contentColor = Color.White
                                    ) {
                                        Text("$remainingTasksCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = stringResource(R.string.tab_checklist)
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("nav_tab_checklist")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_sos_scripts),
                            fontSize = 10.sp,
                            color = if (selectedTab == 1) Color(0xFF00E5FF) else Color.White,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = stringResource(R.string.tab_sos_scripts)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E5FF),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("nav_tab_scripts")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_contract_scanner),
                            fontSize = 10.sp,
                            color = if (selectedTab == 2) Color(0xFFFFB300) else Color.White,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = stringResource(R.string.tab_contract_scanner)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("nav_tab_contract_scanner")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = {
                        Text(
                            text = stringResource(R.string.tab_ai_shield),
                            fontSize = 10.sp,
                            color = if (selectedTab == 3) Color(0xFF00E676) else Color.White,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = stringResource(R.string.tab_ai_shield)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00E676),
                        unselectedIconColor = Color(0xFF9CA3AF),
                        indicatorColor = Color(0xFF1E293B)
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_advisor")
                )
            }
        },
        containerColor = Color(0xFF030712)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            HybridStatusStrip(
                lastInference = lastInferenceState,
                offlineSiteTestMode = offlineSiteTestMode,
                onDeviceStatusLabel = onDeviceStatusLabel,
                onToggleOfflineSiteTestMode = viewModel::toggleOfflineSiteTestMode
            )

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> TaskListScreen(
                        allTasks = tasks,
                        filteredTasks = filteredTasks,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onSelectCategory = viewModel::selectCategory,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onToggleTask = viewModel::toggleTaskCompleted,
                        onAddTask = viewModel::addTask,
                        onEditTask = viewModel::updateTaskDetails,
                        onUpdateTaskPhoto = viewModel::updateTaskPhoto,
                        onDeleteTask = viewModel::deleteTask,
                        onResetDay = viewModel::resetDailyChecklist,
                        onAnalyzeRedFlagWithAi = { prompt ->
                            pendingAiCrisisPrompt = prompt
                            selectedTab = 3
                        },
                        onInspectPhotoWithAi = { photoUri, taskTitle ->
                            pendingInspectPhotoUri = photoUri
                            pendingInspectTaskTitle = taskTitle
                            selectedTab = 2
                        },
                        onRunDailyLogSummary = {
                            pendingHybridOp = "DAILY_LOG"
                            selectedTab = 3
                        },
                        onRunAntiBsCheck = {
                            pendingHybridOp = "ANTI_BS"
                            selectedTab = 3
                        }
                    )
                    1 -> ScriptScreen(
                        context = context,
                        scripts = scripts,
                        scriptTailorState = scriptTailorState,
                        offlineSiteTestMode = offlineSiteTestMode,
                        onTailorScript = viewModel::tailorSosScript,
                        onClearTailorResult = viewModel::clearScriptTailorResult,
                        onDisableOfflineMode = { viewModel.setOfflineSiteTestMode(false) },
                        onAddScript = viewModel::addScript,
                        onDeleteScript = viewModel::deleteScript
                    )
                    2 -> ContractRiskScreen(
                        context = context,
                        tasksWithPhotos = tasksWithPhotos,
                        initialInspectPhotoUri = pendingInspectPhotoUri,
                        initialInspectTaskTitle = pendingInspectTaskTitle,
                        onConsumeInitialInspectPhoto = {
                            pendingInspectPhotoUri = ""
                            pendingInspectTaskTitle = ""
                        },
                        contractRiskState = contractRiskState,
                        sitePhotoInspectState = sitePhotoInspectState,
                        offlineSiteTestMode = offlineSiteTestMode,
                        onAnalyzeClause = viewModel::analyzeContractClause,
                        onInspectSitePhoto = { uri, focus ->
                            viewModel.inspectSitePhoto(context, uri, focus)
                        },
                        onSaveAnalysisAsSosScript = { title, content ->
                            viewModel.addScript(
                                title = title,
                                content = content,
                                category = "EOT_LD",
                                riskLevel = "CRITICAL"
                            )
                        },
                        onDiscussInChat = { draft ->
                            pendingChatDraft = draft
                            selectedTab = 3
                        }
                    )
                    3 -> AiAdvisorScreen(
                        context = context,
                        initialCrisisPrompt = pendingAiCrisisPrompt,
                        initialChatDraft = pendingChatDraft,
                        initialHybridOp = pendingHybridOp,
                        onConsumeInitialPrompt = { pendingAiCrisisPrompt = "" },
                        onConsumeInitialChatDraft = { pendingChatDraft = "" },
                        onConsumeInitialHybridOp = { pendingHybridOp = "" },
                        offlineSiteTestMode = offlineSiteTestMode,
                        onDeviceStatusLabel = onDeviceStatusLabel,
                        isOnDeviceDownloadable = isOnDeviceDownloadable,
                        hybridOpsState = hybridOpsState,
                        siteSafetyRiskState = siteSafetyRiskState,
                        thinkingState = thinkingState,
                        generatorState = generatorState,
                        chatMessages = chatMessages,
                        isChatLoading = isChatLoading,
                        liveVoiceState = liveVoiceState,
                        onDisableOfflineMode = { viewModel.setOfflineSiteTestMode(false) },
                        onRefreshOnDeviceStatus = viewModel::refreshOnDeviceStatus,
                        onDownloadOnDeviceModel = viewModel::downloadOnDeviceModel,
                        onSummarizeDailyLog = viewModel::summarizeDailyRemindLog,
                        onDetectDuplicateProgress = viewModel::detectDuplicateProgress,
                        onAskHybridSiteQa = viewModel::askHybridSiteQa,
                        onAnalyzeSiteSafetyRisk = viewModel::analyzeSiteSafetyRisk,
                        onClearSiteSafetyRisk = viewModel::clearSiteSafetyRisk,
                        onSaveSafetyMitigationTask = viewModel::saveSafetyMitigationToRoom,
                        onAnalyzeCrisis = viewModel::analyzeConstructionCrisisWithHighThinking,
                        onGenerateActionPack = viewModel::generateActionPackForRoom,
                        onSaveActionPackToRoom = viewModel::saveGeneratedPackToRoom,
                        onSendChatMessage = viewModel::sendChatMessage,
                        onClearChat = viewModel::clearChatHistory,
                        onToggleLiveVoice = viewModel::toggleLiveVoiceSession
                    )
                }
            }
        }
    }
}
