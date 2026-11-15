package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.SiteWalkDefectEntity
import com.example.ui.components.InferenceSourcePill
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.InspectorPrefs
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalInspectorStrings
import com.example.util.PdfBlueprintHelper
import com.example.viewmodel.HybridInferenceSource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun ReportDispatchScreen(
    context: Context,
    defects: List<SiteWalkDefectEntity>,
    activePdfUri: String?,
    activePdfPageIndex: Int,
    onToggleDefectStatus: (SiteWalkDefectEntity) -> Unit,
    onDeleteDefect: (SiteWalkDefectEntity) -> Unit
) {
    val s = LocalInspectorStrings.current
    val lang = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var contractorEmail by remember { mutableStateOf(InspectorPrefs.loadContractorEmail(context)) }
    var pmEmail by remember { mutableStateOf(InspectorPrefs.loadPmEmail(context)) }
    var previewPhotoUri by remember { mutableStateOf<String?>(null) }
    var renderedPdfBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(activePdfUri, activePdfPageIndex) {
        if (!activePdfUri.isNullOrBlank()) {
            renderedPdfBitmap = PdfBlueprintHelper.renderPdfPage(context, activePdfUri, activePdfPageIndex)?.bitmap
        } else {
            renderedPdfBitmap = null
        }
    }

    val openCount = remember(defects) { defects.count { it.status == "OPEN" } }
    val filteredDefects = remember(defects, selectedFilter) {
        when (selectedFilter) {
            "CRITICAL" -> defects.filter { it.severityLevel.equals("CRITICAL", ignoreCase = true) }
            "OPEN" -> defects.filter { it.status == "OPEN" }
            "FIXED" -> defects.filter { it.status == "FIXED" }
            else -> defects
        }
    }

    val pinsOnPage = remember(defects, activePdfPageIndex) {
        defects.filter { it.pdfPageIndex == activePdfPageIndex && it.pinXRatio != null && it.pinYRatio != null }
    }

    fun buildReportText(): String {
        val dateStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date())
        val sb = StringBuilder()
        if (lang == AppLanguage.EN) {
            sb.appendLine("NANO 4oK — HOME INSPECTOR & SITE INTELLIGENCE REPORT")
            sb.appendLine("Generated (Immutable Real-Time): $dateStr")
            sb.appendLine("Total Defects: ${defects.size} (Open: $openCount)")
            sb.appendLine("----------------------------------------")
        } else {
            sb.appendLine("NANO 4oK — รายงานตรวจบ้าน & หน้างานอัจฉริยะ (Site Walk Report)")
            sb.appendLine("เวลาออกรายงาน (ล็อกตามเวลาจริง): $dateStr")
            sb.appendLine("รวมทั้งหมด: ${defects.size} รายการ (รอแก้ไข: $openCount)")
            sb.appendLine("----------------------------------------")
        }

        val targetList = filteredDefects.ifEmpty { defects }
        targetList.forEach { d ->
            val time = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.US).format(Date(d.capturedAt))
            sb.appendLine("#${d.pinNumber} [${d.severityLevel}] ${d.floorZone} • ${d.tradeCategory} (${d.status})")
            if (d.openingCode.isNotBlank() || d.measuredInput.isNotBlank()) {
                sb.appendLine("   Code: ${d.openingCode.ifBlank { "-" }} | Measured: ${d.measuredInput.ifBlank { "-" }}")
            }
            sb.appendLine("   Responsible: ${d.responsibleParty} | Locked Time: $time")
            sb.appendLine("   Note: ${d.inspectorNote}")
            if (d.aiAnalysis.isNotBlank()) {
                sb.appendLine("   AI Findings: ${d.aiAnalysis}")
            }
            sb.appendLine()
        }
        return sb.toString().trim()
    }

    fun dispatchEmailReport() {
        scope.launch {
            val subject = if (lang == AppLanguage.EN) {
                "[NANO 4oK Inspection Report] ${defects.firstOrNull()?.floorZone ?: "Site Walk"} ($openCount Open)"
            } else {
                "[NANO 4oK รายงานจุดแก้ไข] ${defects.firstOrNull()?.floorZone ?: "ตรวจบ้าน"} (รอแก้ $openCount จุด)"
            }
            val body = buildReportText()

            val attachmentUris = ArrayList<Uri>()
            val baseBmp = renderedPdfBitmap
            if (baseBmp != null && pinsOnPage.isNotEmpty()) {
                val annotatedUri = PdfBlueprintHelper.renderAnnotatedBlueprintSnapshot(
                    context = context,
                    baseBitmap = baseBmp,
                    pins = pinsOnPage,
                    floorZoneLabel = defects.firstOrNull()?.floorZone ?: "Plan"
                )
                if (annotatedUri != null) {
                    attachmentUris.add(annotatedUri)
                }
            }
            defects.mapNotNull { it.photoUri }
                .distinct()
                .forEach { uriStr ->
                    runCatching { Uri.parse(uriStr) }.getOrNull()?.let { attachmentUris.add(it) }
                }

            val toArray = contractorEmail.split(",", ";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toTypedArray()
            val ccArray = pmEmail.split(",", ";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toTypedArray()

            val emailIntent = if (attachmentUris.isNotEmpty()) {
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } else {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                }
            }.apply {
                if (toArray.isNotEmpty()) putExtra(Intent.EXTRA_EMAIL, toArray)
                if (ccArray.isNotEmpty()) putExtra(Intent.EXTRA_CC, ccArray)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            try {
                context.startActivity(Intent.createChooser(emailIntent, s.sendEmailBtn))
            } catch (_: Exception) {
                Toast.makeText(context, s.noEmailAppToast, Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Filter Chips
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.reportHeaderTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = s.reportHeaderSubtitle,
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    Surface(
                        color = if (openCount > 0) Color(0xFFFF5252).copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            if (openCount > 0) Color(0xFFFF5252) else Color(0xFF00E676)
                        )
                    ) {
                        Text(
                            text = String.format(Locale.US, s.openCountFormat, openCount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (openCount > 0) Color(0xFFFF5252) else Color(0xFF00E676),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to s.filterAll,
                        "CRITICAL" to s.filterCritical,
                        "OPEN" to s.filterOpen,
                        "FIXED" to s.filterFixed
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF111827),
                                labelColor = Color(0xFFD1D5DB)
                            )
                        )
                    }
                }
            }
        }

        // Pinned Blueprint Overview (only shown if a PDF is active and has pins or was loaded)
        val pdfBmp = renderedPdfBitmap
        if (pdfBmp != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = s.blueprintOverviewTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        var boxSize by remember { mutableStateOf(IntSize.Zero) }
                        val aspect = (pdfBmp.width.toFloat() / pdfBmp.height.toFloat()).coerceIn(0.6f, 2.2f)
                        val pinRadiusPx = with(LocalDensity.current) { 12.dp.toPx() }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(aspect)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .onSizeChanged { boxSize = it }
                        ) {
                            Image(
                                bitmap = pdfBmp.asImageBitmap(),
                                contentDescription = s.blueprintOverviewTitle,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                            pinsOnPage.forEach { d ->
                                val px = d.pinXRatio ?: return@forEach
                                val py = d.pinYRatio ?: return@forEach
                                if (boxSize.width > 0 && boxSize.height > 0) {
                                    val xOff = (px * boxSize.width - pinRadiusPx).roundToInt()
                                    val yOff = (py * boxSize.height - pinRadiusPx).roundToInt()
                                    Surface(
                                        color = if (d.status == "FIXED") Color(0xFF00C853) else Color(0xFFEF4444),
                                        shape = CircleShape,
                                        border = BorderStroke(1.5.dp, Color.White),
                                        modifier = Modifier
                                            .offset { IntOffset(xOff, yOff) }
                                            .size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("#${d.pinNumber}", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                        Text(
                            text = String.format(Locale.US, s.blueprintPinsOnPageFormat, pinsOnPage.size, activePdfPageIndex + 1),
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }

        // Email Dispatch Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(s.emailCardTitle, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(s.emailCardSubtitle, fontSize = 10.sp, color = Color(0xFF9CA3AF))
                        }
                    }

                    OutlinedTextField(
                        value = contractorEmail,
                        onValueChange = {
                            contractorEmail = it
                            InspectorPrefs.saveContractorEmail(context, it)
                        },
                        label = { Text(s.contractorEmailLabel, fontSize = 11.sp) },
                        placeholder = { Text("contractor@example.com", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E676),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contractor_email_input")
                    )

                    OutlinedTextField(
                        value = pmEmail,
                        onValueChange = {
                            pmEmail = it
                            InspectorPrefs.savePmEmail(context, it)
                        },
                        label = { Text(s.pmEmailLabel, fontSize = 11.sp) },
                        placeholder = { Text("pm@example.com", fontSize = 11.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pm_email_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { dispatchEmailReport() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF030712)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                                .testTag("send_email_report_button")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = s.sendEmailBtn, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.sendEmailBtn, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NANO 4oK Defect Report", buildReportText()))
                                Toast.makeText(
                                    context,
                                    context.getString(com.example.R.string.toast_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .heightIn(min = 48.dp)
                                .testTag("copy_report_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = s.copyReportBtn, tint = Color(0xFF00E5FF), modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.copyReportBtn, fontSize = 11.sp, color = Color(0xFF00E5FF), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Empty State or Defect Cards
        if (filteredDefects.isEmpty()) {
            item {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(32.dp))
                        Text(s.reportEmptyTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(s.reportEmptySubtitle, fontSize = 11.sp, color = Color(0xFF9CA3AF))
                    }
                }
            }
        } else {
            items(filteredDefects, key = { it.id }) { defect ->
                val isFixed = defect.status == "FIXED"
                val sevColor = when (defect.severityLevel.uppercase()) {
                    "CRITICAL" -> Color(0xFFFF5252)
                    "MINOR" -> Color(0xFF00E5FF)
                    else -> Color(0xFFFFB300)
                }
                val timeLocked = remember(defect.capturedAt) {
                    SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.US).format(Date(defect.capturedAt))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isFixed) Color(0xFF00E676).copy(alpha = 0.45f) else sevColor.copy(alpha = 0.55f),
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("defect_card_${defect.id}")
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
                                    color = if (isFixed) Color(0xFF00C853) else sevColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "#${defect.pinNumber} • ${defect.severityLevel}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF030712),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = defect.floorZone,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            InferenceSourcePill(
                                source = when (defect.inferenceOrigin) {
                                    "ON_DEVICE" -> HybridInferenceSource.ON_DEVICE
                                    "IN_CLOUD" -> HybridInferenceSource.IN_CLOUD
                                    else -> HybridInferenceSource.STANDBY
                                }
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$timeLocked • ${defect.tradeCategory} • ${defect.responsibleParty}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF9CA3AF)
                            )
                        }

                        if (defect.openingCode.isNotBlank() || defect.measuredInput.isNotBlank()) {
                            Text(
                                text = "📐 ${defect.openingCode.ifBlank { "-" }}  |  ${defect.measuredInput.ifBlank { "-" }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                        }

                        Text(
                            text = defect.inspectorNote,
                            fontSize = 12.sp,
                            color = Color.White,
                            lineHeight = 17.sp
                        )

                        if (!defect.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = Uri.parse(defect.photoUri),
                                contentDescription = s.photoPreviewTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { previewPhotoUri = defect.photoUri }
                            )
                        }

                        if (defect.aiAnalysis.isNotBlank()) {
                            Surface(
                                color = Color(0xFF111827),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = defect.aiAnalysis,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD1D5DB),
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onToggleDefectStatus(defect) },
                                border = BorderStroke(1.dp, if (isFixed) Color(0xFFFFB300) else Color(0xFF00E676)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFixed) Icons.Default.Refresh else Icons.Default.CheckCircle,
                                    contentDescription = if (isFixed) s.reopenBtn else s.markFixedBtn,
                                    tint = if (isFixed) Color(0xFFFFB300) else Color(0xFF00E676),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFixed) s.reopenBtn else s.markFixedBtn,
                                    fontSize = 11.sp,
                                    color = if (isFixed) Color(0xFFFFB300) else Color(0xFF00E676)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteDefect(defect) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = s.deleteBtn,
                                    tint = Color(0xFFFF5252)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full-screen photo preview dialog
    val previewUri = previewPhotoUri
    if (previewUri != null) {
        Dialog(onDismissRequest = { previewPhotoUri = null }) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.photoPreviewTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        IconButton(onClick = { previewPhotoUri = null }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Close, contentDescription = s.closeBtn, tint = Color.White)
                        }
                    }
                    AsyncImage(
                        model = Uri.parse(previewUri),
                        contentDescription = s.photoPreviewTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    )
                }
            }
        }
    }
}
