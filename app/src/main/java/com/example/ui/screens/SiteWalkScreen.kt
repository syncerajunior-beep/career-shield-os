package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.SiteWalkDefectEntity
import com.example.ui.components.AppCheckDebugTokenBanner
import com.example.ui.components.InferenceSourcePill
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalInspectorStrings
import com.example.util.PdfBlueprintHelper
import com.example.util.SitePhotoStorage
import com.example.viewmodel.DefectAiUiState
import com.example.viewmodel.HybridInferenceSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun SiteWalkScreen(
    context: Context,
    defects: List<SiteWalkDefectEntity>,
    activePdfUri: String?,
    activePdfPageIndex: Int,
    onPdfChanged: (String?, Int) -> Unit,
    defectAiState: DefectAiUiState,
    offlineSiteTestMode: Boolean,
    onDisableOfflineMode: () -> Unit,
    onRunHybridTriage: (floorZone: String, tradeCategory: String, openingCode: String, measuredInput: String, inspectorNote: String) -> Unit,
    onComparePhotoVsPdf: (photoUri: String?, pdfUri: String?, pageIndex: Int, floorZone: String, tradeCategory: String, openingCode: String, measuredInput: String, inspectorNote: String) -> Unit,
    onClearAiState: () -> Unit,
    onSaveDefect: (
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
    ) -> Unit
) {
    val s = LocalInspectorStrings.current
    val lang = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var floorZone by rememberSaveable { mutableStateOf(s.quickZones.first()) }
    var tradeCategory by rememberSaveable { mutableStateOf("OPENINGS") }
    var openingCode by rememberSaveable { mutableStateOf("W1") }
    var measuredInput by rememberSaveable { mutableStateOf("") }
    var inspectorNote by rememberSaveable { mutableStateOf("") }
    var capturedPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    var draftPinX by rememberSaveable { mutableStateOf<Float?>(null) }
    var draftPinY by rememberSaveable { mutableStateOf<Float?>(null) }
    var renderedPdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pdfPageCount by remember { mutableIntStateOf(1) }
    var isRenderingPdf by remember { mutableStateOf(false) }

    // Real-time locked clock (read-only immutable evidence timestamp)
    var currentClockFormatted by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale.US).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            currentClockFormatted = SimpleDateFormat("dd MMM yyyy • HH:mm:ss", Locale.US).format(Date())
            delay(1000L)
        }
    }

    // Keep default floor zone synced if language toggled while using a default chip
    LaunchedEffect(lang) {
        val otherLangDefaults = if (lang == AppLanguage.EN) {
            listOf("ชั้น 1 / โถงรับแขก", "ชั้น 2 / ห้องนอนใหญ่", "ห้องน้ำชั้น 2", "ประตู-หน้าต่างรอบบ้าน")
        } else {
            listOf("Level 1 / Living", "Level 2 / Master Bed", "Bathroom 2", "Doors & Windows")
        }
        val idx = otherLangDefaults.indexOf(floorZone)
        if (idx in s.quickZones.indices) {
            floorZone = s.quickZones[idx]
        }
    }

    // Re-render PDF page whenever activePdfUri or activePdfPageIndex changes
    LaunchedEffect(activePdfUri, activePdfPageIndex) {
        if (activePdfUri.isNullOrBlank()) {
            renderedPdfBitmap = null
            pdfPageCount = 1
        } else {
            isRenderingPdf = true
            val res = PdfBlueprintHelper.renderPdfPage(context, activePdfUri, activePdfPageIndex)
            if (res != null) {
                renderedPdfBitmap = res.bitmap
                pdfPageCount = res.pageCount
                if (res.pageIndex != activePdfPageIndex) {
                    onPdfChanged(activePdfUri, res.pageIndex)
                }
            }
            isRenderingPdf = false
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingCameraUri != null) {
            capturedPhotoUri = pendingCameraUri.toString()
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = SitePhotoStorage.createSitePhotoUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            Toast.makeText(
                context,
                if (lang == AppLanguage.EN) "Camera permission is needed to capture site defects." else "กรุณาอนุญาตการใช้กล้องเพื่อถ่ายรูป Defect",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = SitePhotoStorage.persistPickedPhoto(context, uri)
            if (saved != null) capturedPhotoUri = saved
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val persisted = PdfBlueprintHelper.persistPickedPdf(context, uri)
                if (persisted != null) {
                    onPdfChanged(persisted, 0)
                    draftPinX = null
                    draftPinY = null
                }
            }
        }
    }

    val nextPinNumber = remember(defects) { (defects.maxOfOrNull { it.pinNumber } ?: 0) + 1 }
    val pinsOnCurrentPage = remember(defects, activePdfPageIndex) {
        defects.filter { it.pdfPageIndex == activePdfPageIndex && it.pinXRatio != null && it.pinYRatio != null }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = SitePhotoStorage.createSitePhotoUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var showPillarsDetail by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 0. NANO 4oK — The 4 Pillars of Excellence ("All-OK") Collapsible Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1628)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .clickable { showPillarsDetail = !showPillarsDetail }
                    .testTag("pillars_overview_card")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Color(0xFF00E676).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = s.pillarsBadge,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = s.pillarsBadge,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF00E676)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.pillarsHeadline,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (showPillarsDetail) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = s.pillarsHeadline,
                            tint = Color(0xFF00E5FF)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            s.pillar1Title to Color(0xFF00E5FF),
                            s.pillar2Title to Color(0xFF00E676),
                            s.pillar3Title to Color(0xFFFFB300),
                            s.pillar4Title to Color(0xFFA78BFA)
                        ).forEach { (title, color) ->
                            Surface(
                                color = color.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = showPillarsDetail) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = s.pillarsIntro,
                                fontSize = 11.sp,
                                color = Color(0xFFD1D5DB),
                                lineHeight = 16.sp
                            )
                            listOf(
                                s.pillar1Title to s.pillar1Desc,
                                s.pillar2Title to s.pillar2Desc,
                                s.pillar3Title to s.pillar3Desc,
                                s.pillar4Title to s.pillar4Desc
                            ).forEach { (title, desc) ->
                                Surface(
                                    color = Color(0xFF111827),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00E5FF)
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = Color(0xFFE5E7EB),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = s.pillarsClosing,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00E676),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // 1. Setup Zone, Trade & Immutable Real-Time Timestamp
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.walkSetupTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    // Read-Only Immutable Real-Time Timestamp Bar
                    Surface(
                        color = Color(0xFF020617),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("locked_realtime_timestamp")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = s.timestampLockedPrefix,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${s.timestampLockedPrefix} $currentClockFormatted",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Quick Floor / Zone chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        s.quickZones.forEach { preset ->
                            FilterChip(
                                selected = floorZone == preset,
                                onClick = { floorZone = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF00E5FF),
                                    containerColor = Color(0xFF111827),
                                    labelColor = Color(0xFFD1D5DB)
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = floorZone,
                        onValueChange = { floorZone = it },
                        label = { Text(s.floorZoneLabel, fontSize = 12.sp) },
                        placeholder = { Text(s.floorZonePlaceholder, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("floor_zone_input")
                    )

                    // Trade Category Chips
                    val trades = listOf(
                        "OPENINGS" to s.tradeOpenings,
                        "MASONRY" to s.tradeMasonry,
                        "STRUCTURAL" to s.tradeStructural,
                        "MEP_SAFETY" to s.tradeMepSafety
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        trades.forEach { (key, label) ->
                            FilterChip(
                                selected = tradeCategory == key,
                                onClick = { tradeCategory = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF00E676),
                                    containerColor = Color(0xFF111827),
                                    labelColor = Color(0xFFD1D5DB)
                                ),
                                modifier = Modifier.testTag("trade_chip_$key")
                            )
                        }
                    }
                }
            }
        }

        // 2. PDF Blueprint Viewer & Interactive Tap-to-Pin
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = s.pdfCardTitle,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.pdfCardTitle,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = s.pdfCardSubtitle,
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                contentColor = Color(0xFF030712)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("upload_pdf_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = s.uploadPdfBtn, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.uploadPdfBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val sampleUri = PdfBlueprintHelper.createSampleArchitecturalPdf(context, lang)
                                    if (sampleUri != null) {
                                        onPdfChanged(sampleUri, 0)
                                        draftPinX = null
                                        draftPinY = null
                                    }
                                }
                            },
                            border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("load_sample_pdf_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = s.samplePdfBtn, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.samplePdfBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    val pdfBmp = renderedPdfBitmap
                    if (isRenderingPdf) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF00E5FF))
                        }
                    } else if (pdfBmp == null) {
                        Surface(
                            color = Color(0xFF111827),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = s.pdfEmptyHint,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        // Page Controls & Pin Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (activePdfPageIndex > 0) {
                                            onPdfChanged(activePdfUri, activePdfPageIndex - 1)
                                        }
                                    },
                                    enabled = activePdfPageIndex > 0,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous PDF Page",
                                        tint = if (activePdfPageIndex > 0) Color.White else Color(0xFF475569)
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, s.pdfPageIndicator, activePdfPageIndex + 1, pdfPageCount),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                                IconButton(
                                    onClick = {
                                        if (activePdfPageIndex < pdfPageCount - 1) {
                                            onPdfChanged(activePdfUri, activePdfPageIndex + 1)
                                        }
                                    },
                                    enabled = activePdfPageIndex < pdfPageCount - 1,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Next PDF Page",
                                        tint = if (activePdfPageIndex < pdfPageCount - 1) Color.White else Color(0xFF475569)
                                    )
                                }
                            }

                            if (draftPinX != null && draftPinY != null) {
                                OutlinedButton(
                                    onClick = {
                                        draftPinX = null
                                        draftPinY = null
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.heightIn(min = 40.dp)
                                ) {
                                    Text(s.pdfClearPinBtn, fontSize = 11.sp, color = Color(0xFFFF5252))
                                }
                            }
                        }

                        // Interactive PDF Drawing Canvas with Pin-Drop Overlay
                        var canvasSize by remember { mutableStateOf(IntSize.Zero) }
                        val aspect = (pdfBmp.width.toFloat() / pdfBmp.height.toFloat()).coerceIn(0.6f, 2.2f)
                        val density = LocalDensity.current
                        val pinRadiusPx = with(density) { 14.dp.toPx() }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspect)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, Color(0xFF00E5FF).copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                                .onSizeChanged { canvasSize = it }
                                .pointerInput(activePdfUri, activePdfPageIndex) {
                                    detectTapGestures { offset ->
                                        if (canvasSize.width > 0 && canvasSize.height > 0) {
                                            draftPinX = (offset.x / canvasSize.width.toFloat()).coerceIn(0.03f, 0.97f)
                                            draftPinY = (offset.y / canvasSize.height.toFloat()).coerceIn(0.03f, 0.97f)
                                        }
                                    }
                                }
                                .testTag("interactive_pdf_canvas")
                        ) {
                            Image(
                                bitmap = pdfBmp.asImageBitmap(),
                                contentDescription = s.pdfCardTitle,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Already saved pins on this PDF page
                            pinsOnCurrentPage.forEach { savedDefect ->
                                val px = savedDefect.pinXRatio ?: return@forEach
                                val py = savedDefect.pinYRatio ?: return@forEach
                                if (canvasSize.width > 0 && canvasSize.height > 0) {
                                    val xOff = (px * canvasSize.width - pinRadiusPx).roundToInt()
                                    val yOff = (py * canvasSize.height - pinRadiusPx).roundToInt()
                                    Surface(
                                        color = if (savedDefect.status == "FIXED") Color(0xFF00C853) else Color(0xFFFF9100),
                                        shape = CircleShape,
                                        border = BorderStroke(2.dp, Color.White),
                                        modifier = Modifier
                                            .offset { IntOffset(xOff, yOff) }
                                            .size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "#${savedDefect.pinNumber}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            // Current Draft Pin
                            val dx = draftPinX
                            val dy = draftPinY
                            if (dx != null && dy != null && canvasSize.width > 0 && canvasSize.height > 0) {
                                val xOff = (dx * canvasSize.width - pinRadiusPx).roundToInt()
                                val yOff = (dy * canvasSize.height - pinRadiusPx).roundToInt()
                                Surface(
                                    color = Color(0xFFEF4444),
                                    shape = CircleShape,
                                    border = BorderStroke(2.dp, Color.White),
                                    modifier = Modifier
                                        .offset { IntOffset(xOff, yOff) }
                                        .size(30.dp)
                                        .testTag("draft_pdf_pin_badge")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#$nextPinNumber",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = if (draftPinX != null) Color(0xFF00E676) else Color(0xFFFFB300),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (draftPinX != null) {
                                    String.format(Locale.US, s.pdfPinPlacedFormat, nextPinNumber, activePdfPageIndex + 1)
                                } else {
                                    String.format(Locale.US, s.pdfTapToPinHint, nextPinNumber)
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (draftPinX != null) Color(0xFF00E676) else Color(0xFFFFD54F)
                            )
                        }
                    }
                }
            }
        }

        // 3. Defect Photo Capture & AI PDF Comparison
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.captureCardTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = s.captureCardSubtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF)
                    )

                    // Photo buttons / Preview
                    if (capturedPhotoUri.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { launchCamera() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF),
                                    contentColor = Color(0xFF030712)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .heightIn(min = 48.dp)
                                    .testTag("capture_defect_photo_button")
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = s.takePhotoBtn, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(s.takePhotoBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("pick_defect_photo_button")
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = s.pickPhotoBtn, tint = Color(0xFFD1D5DB), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(s.pickPhotoBtn, fontSize = 11.sp, color = Color(0xFFD1D5DB), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFF111827),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = Uri.parse(capturedPhotoUri),
                                    contentDescription = s.photoReadyBadge,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = s.photoReadyBadge,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { launchCamera() },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.heightIn(min = 40.dp)
                                        ) {
                                            Text(s.retakePhotoBtn, fontSize = 11.sp, color = Color(0xFF00E5FF))
                                        }
                                        OutlinedButton(
                                            onClick = { capturedPhotoUri = null },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.heightIn(min = 40.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = s.clearPhotoBtn, tint = Color(0xFFFF5252), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(s.clearPhotoBtn, fontSize = 11.sp, color = Color(0xFFFF5252))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Quick 1-Tap Defect Presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                tradeCategory = "OPENINGS"
                                openingCode = "W1"
                                measuredInput = "117.5 x 199 cm"
                                inspectorNote = if (lang == AppLanguage.EN) {
                                    "Masonry rough opening plastered narrower than W1 spec; aluminum frame cannot fit."
                                } else {
                                    "ช่องเปิดหน้าต่าง W1 ช่างปูนฉาบแล้วแคบกว่าแบบ ช่างอลูมิเนียมเข้ากรอบไม่ได้"
                                }
                            },
                            label = { Text(s.quickPresetWindowW1, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                tradeCategory = "STRUCTURAL"
                                openingCode = "B3"
                                measuredInput = "0.5 mm"
                                inspectorNote = if (lang == AppLanguage.EN) {
                                    "Diagonal hairline crack observed near beam-column joint B3."
                                } else {
                                    "ปูนมีรอยร้าวแนวเฉียงบริเวณรอยต่อคาน-เสา B3 ต้องการตรวจระดับความเสี่ยง"
                                }
                            },
                            label = { Text(s.quickPresetCrack, fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                tradeCategory = "OPENINGS"
                                openingCode = "D1"
                                measuredInput = "89 x 209 cm"
                                inspectorNote = if (lang == AppLanguage.EN) {
                                    "D1 doorway jamb out-of-plumb by 8mm at bottom."
                                } else {
                                    "วงกบประตู D1 ฉาบไม่ได้ดิ่งเอียง 8 มม. ด้านล่าง"
                                }
                            },
                            label = { Text(s.quickPresetDoorD1, fontSize = 11.sp) }
                        )
                    }

                    // Compact Fields: Code + Measured Size
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = openingCode,
                            onValueChange = { openingCode = it },
                            label = { Text(s.openingCodeLabel, fontSize = 10.sp) },
                            placeholder = { Text(s.openingCodePlaceholder, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(0.45f)
                        )
                        OutlinedTextField(
                            value = measuredInput,
                            onValueChange = { measuredInput = it },
                            label = { Text(s.measuredInputLabel, fontSize = 10.sp) },
                            placeholder = { Text(s.measuredInputPlaceholder, fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(0.55f)
                        )
                    }

                    OutlinedTextField(
                        value = inspectorNote,
                        onValueChange = { inspectorNote = it },
                        label = { Text(s.noteLabel, fontSize = 11.sp) },
                        placeholder = { Text(s.notePlaceholder, fontSize = 11.sp) },
                        minLines = 2,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("defect_note_input")
                    )

                    // Two Clean AI Action Buttons
                    Button(
                        onClick = {
                            onComparePhotoVsPdf(
                                capturedPhotoUri,
                                activePdfUri,
                                activePdfPageIndex,
                                floorZone,
                                tradeCategory,
                                openingCode,
                                measuredInput,
                                inspectorNote
                            )
                        },
                        enabled = defectAiState !is DefectAiUiState.Loading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF030712)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("compare_photo_pdf_ai_button")
                    ) {
                        Icon(Icons.Default.Compare, contentDescription = s.btnComparePdfCloud, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(s.btnComparePdfCloud, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = {
                            onRunHybridTriage(
                                floorZone,
                                tradeCategory,
                                openingCode,
                                measuredInput,
                                inspectorNote
                            )
                        },
                        enabled = defectAiState !is DefectAiUiState.Loading,
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("quick_hybrid_triage_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = s.btnQuickHybridTriage, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(s.btnQuickHybridTriage, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    }

                    // AI State Output Box
                    when (defectAiState) {
                        is DefectAiUiState.Idle -> Unit
                        is DefectAiUiState.Loading -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF00E5FF), modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(defectAiState.modeLabel, fontSize = 12.sp, color = Color(0xFF00E5FF))
                            }
                        }
                        is DefectAiUiState.Error -> {
                            Surface(
                                color = Color(0xFF3B1219),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFF5252)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "⚠️ ${defectAiState.message}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFFFCDD2)
                                    )
                                    if (defectAiState.isOnDeviceUnavailable && offlineSiteTestMode) {
                                        Button(
                                            onClick = onDisableOfflineMode,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFB300),
                                                contentColor = Color(0xFF030712)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(s.btnDisableOfflineRetry, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    AppCheckDebugTokenBanner(
                                        context = context,
                                        errorMessage = defectAiState.message
                                    )
                                }
                            }
                        }
                        is DefectAiUiState.Success -> {
                            Surface(
                                color = Color(0xFF111827),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("defect_ai_result_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(s.aiResultTitle, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00E676))
                                        }
                                        InferenceSourcePill(source = defectAiState.inferenceSource)
                                    }

                                    Text(
                                        text = defectAiState.analysis,
                                        fontSize = 12.sp,
                                        color = Color(0xFFF3F4F6),
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // Direct 1-Tap Save Defect Button (works both with or without AI)
                    val aiSuccess = defectAiState as? DefectAiUiState.Success
                    Button(
                        onClick = {
                            onSaveDefect(
                                floorZone,
                                tradeCategory,
                                openingCode,
                                measuredInput,
                                inspectorNote,
                                aiSuccess?.analysis ?: "",
                                aiSuccess?.suggestedSeverity ?: "MAJOR",
                                aiSuccess?.suggestedParty ?: "CONTRACTOR",
                                capturedPhotoUri,
                                activePdfUri,
                                activePdfPageIndex,
                                draftPinX,
                                draftPinY,
                                when (aiSuccess?.inferenceSource) {
                                    HybridInferenceSource.ON_DEVICE -> "ON_DEVICE"
                                    HybridInferenceSource.IN_CLOUD -> "IN_CLOUD"
                                    else -> "MANUAL"
                                }
                            )
                            Toast.makeText(
                                context,
                                String.format(Locale.US, s.savedDefectToast, nextPinNumber),
                                Toast.LENGTH_SHORT
                            ).show()
                            // Reset per-defect draft fields while keeping active PDF & Floor Zone ready for next pin!
                            capturedPhotoUri = null
                            draftPinX = null
                            draftPinY = null
                            inspectorNote = ""
                            measuredInput = ""
                            onClearAiState()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color(0xFF030712)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("save_defect_pin_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format(Locale.US, s.btnSaveDefectToRoom, nextPinNumber),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
