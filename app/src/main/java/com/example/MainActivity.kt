package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.CareerShieldRepository
import com.example.data.GeminiAiRepository
import com.example.data.NanoDiagnosticLanguage
import com.example.ui.components.AppCheckDebugTokenBanner
import com.example.ui.components.HybridStatusStrip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppLanguagePrefs
import com.example.ui.localization.InspectorPrefs
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalInspectorStrings
import com.example.ui.localization.appStrings
import com.example.ui.localization.inspectorStrings
import com.example.ui.screens.ReportDispatchScreen
import com.example.ui.screens.SiteWalkScreen
import com.example.ui.theme.CareerShieldAndroidTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.CareerShieldViewModel
import com.example.viewmodel.NanoDiagnosticUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = CareerShieldRepository(
            taskDao = database.taskDao(),
            inspectionDao = database.inspectionDao(),
            siteWalkDefectDao = database.siteWalkDefectDao()
        )
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
            AppDatabase.getDatabase(LocalContext.current).let { db ->
                CareerShieldRepository(db.taskDao(), db.inspectionDao(), db.siteWalkDefectDao())
            },
            GeminiAiRepository()
        )
    )
) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDiagnosticsDialog by rememberSaveable { mutableStateOf(false) }

    var activePdfUri by rememberSaveable { mutableStateOf(InspectorPrefs.loadActivePdfUri(context)) }
    var activePdfPageIndex by rememberSaveable { mutableIntStateOf(InspectorPrefs.loadActivePdfPage(context)) }

    LaunchedEffect(Unit) {
        val savedLanguage = AppLanguagePrefs.loadLanguage(context)
        viewModel.setAppLanguage(savedLanguage)
    }

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val appStringsValue = remember(appLanguage) { appStrings(appLanguage) }
    val inspStrings = remember(appLanguage) { inspectorStrings(appLanguage) }

    val defects by viewModel.siteWalkDefects.collectAsStateWithLifecycle()
    val openDefectCount by viewModel.openSiteWalkDefectCount.collectAsStateWithLifecycle()
    val defectAiState by viewModel.defectAiState.collectAsStateWithLifecycle()

    val offlineSiteTestMode by viewModel.offlineSiteTestMode.collectAsStateWithLifecycle()
    val lastInferenceState by viewModel.lastInferenceState.collectAsStateWithLifecycle()
    val onDeviceStatusLabel by viewModel.onDeviceStatusLabel.collectAsStateWithLifecycle()
    val isOnDeviceDownloadable by viewModel.isOnDeviceDownloadable.collectAsStateWithLifecycle()
    val deviceCompatibilitySummary by viewModel.deviceCompatibilitySummary.collectAsStateWithLifecycle()
    val nanoDiagnosticState by viewModel.nanoDiagnosticState.collectAsStateWithLifecycle()

    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides appLanguage,
        LocalAppStrings provides appStringsValue,
        LocalInspectorStrings provides inspStrings
    ) {
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
                                    imageVector = Icons.Default.Apartment,
                                    contentDescription = inspStrings.appHeaderTitle,
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = inspStrings.appHeaderTitle,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = inspStrings.appHeaderSubtitle,
                                    fontSize = 10.sp,
                                    color = Color(0xFF00E5FF),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showDiagnosticsDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("open_diagnostics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = inspStrings.diagnosticsDesc,
                                tint = Color(0xFF9CA3AF)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val nextLanguage = viewModel.toggleAppLanguage()
                                AppLanguagePrefs.saveLanguage(context, nextLanguage)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (appLanguage == AppLanguage.EN) Color(0xFF00E676) else Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .heightIn(min = 48.dp)
                                .testTag("language_toggle_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = appStringsValue.switchLanguageContentDesc,
                                tint = if (appLanguage == AppLanguage.EN) Color(0xFF00E676) else Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (appLanguage == AppLanguage.TH) "ไทย | EN" else "EN | ไทย",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
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
                                text = inspStrings.tabSiteWalk,
                                fontSize = 11.sp,
                                color = if (selectedTab == 0) Color(0xFF00E5FF) else Color.White,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = inspStrings.tabSiteWalk
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E5FF),
                            unselectedIconColor = Color(0xFF9CA3AF),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_tab_site_walk")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = {
                            Text(
                                text = inspStrings.tabReportEmail,
                                fontSize = 11.sp,
                                color = if (selectedTab == 1) Color(0xFF00E676) else Color.White,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (openDefectCount > 0) {
                                        Badge(
                                            containerColor = Color(0xFFFF5252),
                                            contentColor = Color.White
                                        ) {
                                            Text("$openDefectCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = inspStrings.tabReportEmail
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF00E676),
                            unselectedIconColor = Color(0xFF9CA3AF),
                            indicatorColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.testTag("nav_tab_report_email")
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
                        0 -> SiteWalkScreen(
                            context = context,
                            defects = defects,
                            activePdfUri = activePdfUri,
                            activePdfPageIndex = activePdfPageIndex,
                            onPdfChanged = { uri, pageIdx ->
                                activePdfUri = uri
                                activePdfPageIndex = pageIdx
                                InspectorPrefs.saveActivePdfUri(context, uri)
                                InspectorPrefs.saveActivePdfPage(context, pageIdx)
                            },
                            defectAiState = defectAiState,
                            offlineSiteTestMode = offlineSiteTestMode,
                            onDisableOfflineMode = { viewModel.setOfflineSiteTestMode(false) },
                            onRunHybridTriage = viewModel::analyzeSiteWalkDefectHybrid,
                            onComparePhotoVsPdf = { photoUri, pdfUri, pageIdx, zone, trade, code, measured, note ->
                                viewModel.comparePhotoVsPdfPlan(
                                    context = context,
                                    photoUriString = photoUri,
                                    pdfUriString = pdfUri,
                                    pdfPageIndex = pageIdx,
                                    floorZone = zone,
                                    tradeCategory = trade,
                                    openingCode = code,
                                    measuredInput = measured,
                                    inspectorNote = note
                                )
                            },
                            onClearAiState = viewModel::clearDefectAiState,
                            onSaveDefect = { floorZone, tradeCategory, openingCode, measuredInput, inspectorNote,
                                             aiAnalysis, severityLevel, responsibleParty, photoUri,
                                             pdfPlanUri, pdfPageIndex, pinXRatio, pinYRatio, inferenceOrigin ->
                                viewModel.saveSiteWalkDefect(
                                    floorZone = floorZone,
                                    tradeCategory = tradeCategory,
                                    openingCode = openingCode,
                                    measuredInput = measuredInput,
                                    inspectorNote = inspectorNote,
                                    aiAnalysis = aiAnalysis,
                                    severityLevel = severityLevel,
                                    responsibleParty = responsibleParty,
                                    photoUri = photoUri,
                                    pdfPlanUri = pdfPlanUri,
                                    pdfPageIndex = pdfPageIndex,
                                    pinXRatio = pinXRatio,
                                    pinYRatio = pinYRatio,
                                    inferenceOrigin = inferenceOrigin
                                )
                            }
                        )

                        1 -> ReportDispatchScreen(
                            context = context,
                            defects = defects,
                            activePdfUri = activePdfUri,
                            activePdfPageIndex = activePdfPageIndex,
                            onToggleDefectStatus = viewModel::toggleSiteWalkDefectStatus,
                            onDeleteDefect = { defect -> viewModel.deleteSiteWalkDefect(context, defect) }
                        )
                    }
                }
            }
        }

        if (showDiagnosticsDialog) {
            Dialog(onDismissRequest = { showDiagnosticsDialog = false }) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diagnostics_dialog")
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = inspStrings.diagTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = inspStrings.diagSubtitle,
                                    fontSize = 11.sp,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                            IconButton(
                                onClick = { showDiagnosticsDialog = false },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = inspStrings.closeBtn, tint = Color.White)
                            }
                        }

                        HorizontalDivider(color = Color(0xFF1E293B))

                        // Hardware & On-Device status card
                        Surface(
                            color = Color(0xFF111827),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${inspStrings.diagDeviceLabel} $onDeviceStatusLabel",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                                if (deviceCompatibilitySummary.isNotBlank()) {
                                    Text(
                                        text = "${inspStrings.diagHardwareLabel} $deviceCompatibilitySummary",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF9CA3AF)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = viewModel::refreshOnDeviceStatus,
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 44.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00E5FF))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (appLanguage == AppLanguage.EN) "Refresh" else "รีเฟรช",
                                            fontSize = 11.sp,
                                            color = Color(0xFF00E5FF)
                                        )
                                    }
                                    if (isOnDeviceDownloadable) {
                                        Button(
                                            onClick = viewModel::downloadOnDeviceModel,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF00E676),
                                                contentColor = Color(0xFF030712)
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 44.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (appLanguage == AppLanguage.EN) "Download Nano" else "ดาวน์โหลด Nano",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Firebase App Check Debug Token banner (always visible in Debug builds)
                        AppCheckDebugTokenBanner(
                            context = context,
                            errorMessage = null,
                            alwaysShowInDebug = true
                        )

                        // Isolated On-Device Preview Diagnostic check (strict ONLY_ON_DEVICE)
                        Surface(
                            color = Color(0xFF111827),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = inspStrings.diagNanoTestTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF00E5FF)
                                )
                                Text(
                                    text = inspStrings.diagNanoTestNote,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9CA3AF),
                                    lineHeight = 14.sp
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.runNano4PreviewDiagnostic(NanoDiagnosticLanguage.ENGLISH) },
                                        enabled = nanoDiagnosticState !is NanoDiagnosticUiState.Running,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00E5FF),
                                            contentColor = Color(0xFF030712)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 44.dp)
                                    ) {
                                        Text(inspStrings.diagTestEnBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.runNano4PreviewDiagnostic(NanoDiagnosticLanguage.THAI) },
                                        enabled = nanoDiagnosticState !is NanoDiagnosticUiState.Running,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 44.dp)
                                    ) {
                                        Text(inspStrings.diagTestThBtn, fontSize = 11.sp, color = Color.White)
                                    }
                                }

                                when (val diag = nanoDiagnosticState) {
                                    is NanoDiagnosticUiState.Idle -> Unit
                                    is NanoDiagnosticUiState.Running -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF00E5FF))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Running ONLY_ON_DEVICE (${diag.language.name})...", fontSize = 11.sp, color = Color(0xFF00E5FF))
                                        }
                                    }
                                    is NanoDiagnosticUiState.Success -> {
                                        Surface(
                                            color = Color(0xFF062C1B),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = "✓ ${diag.actualSource} | model=${diag.result.detectedModelName ?: "preview"}",
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF00E676)
                                                )
                                                Text(
                                                    text = diag.result.responseText,
                                                    fontSize = 11.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    is NanoDiagnosticUiState.UnavailableOrError -> {
                                        Surface(
                                            color = Color(0xFF2B1218),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(8.dp))
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                Text(
                                                    text = "Pre-call AICore: ${diag.result.preCallStatus}",
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFFFCDD2)
                                                )
                                                Text(
                                                    text = "${diag.result.errorType}: ${diag.result.errorMessage}",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFFFF8A80)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showDiagnosticsDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text(inspStrings.closeBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
