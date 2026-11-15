package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChecklistWithItems
import com.example.data.InspectionChecklistEntity
import com.example.data.RiskAssessmentReportEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.localization.LocalizedSeedMapper
import com.example.ui.localization.appStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RiskInspectionRoomPanel(
    riskReports: List<RiskAssessmentReportEntity>,
    checklistsWithItems: List<ChecklistWithItems>,
    openCriticalReportCount: Int,
    onCreateRiskReport: (
        siteZone: String,
        reportType: String,
        severityLevel: String,
        hazardSummary: String,
        assessmentFindings: String,
        mitigationPlan: String
    ) -> Unit,
    onUpdateRiskReportStatus: (reportId: Long, newStatus: String) -> Unit,
    onDeleteRiskReport: (RiskAssessmentReportEntity) -> Unit,
    onCreateInspectionChecklist: (
        title: String,
        tradeCategory: String,
        siteZone: String,
        itemTitles: List<String>,
        linkedReportId: Long?
    ) -> Unit,
    onSetInspectionItemResult: (checklistId: Long, itemId: Long, resultStatus: String) -> Unit,
    onDeleteInspectionChecklist: (InspectionChecklistEntity) -> Unit
) {
    val lang = LocalAppLanguage.current
    val s = LocalAppStrings.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM HH:mm", Locale.US) }

    var showNewReportForm by rememberSaveable { mutableStateOf(false) }
    var newReportZone by rememberSaveable { mutableStateOf("") }
    var newReportSeverity by rememberSaveable { mutableStateOf("HIGH") }
    var newReportHazard by rememberSaveable { mutableStateOf("") }
    var newReportMitigation by rememberSaveable { mutableStateOf("") }

    var showNewChecklistForm by rememberSaveable { mutableStateOf(false) }
    var newChecklistTitle by rememberSaveable { mutableStateOf("") }
    var newChecklistZone by rememberSaveable { mutableStateOf("") }
    var newChecklistTrade by rememberSaveable { mutableStateOf("SAFETY") }
    var newChecklistItemsText by rememberSaveable {
        mutableStateOf(s.defaultChecklistItemsText)
    }

    LaunchedEffect(lang) {
        val otherLang = if (lang == AppLanguage.TH) AppLanguage.EN else AppLanguage.TH
        if (newChecklistItemsText == appStrings(otherLang).defaultChecklistItemsText) {
            newChecklistItemsText = s.defaultChecklistItemsText
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("risk_inspection_room_panel")
    ) {
        // 1. Construction Risk Assessment Reports (Room v4)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFFF7043), RoundedCornerShape(12.dp))
                .testTag("risk_reports_room_section")
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
                            imageVector = Icons.Default.ReportProblem,
                            contentDescription = s.riskReportsTitle,
                            tint = Color(0xFFFF7043),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${s.riskReportsTitle} ${riskReports.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (openCriticalReportCount > 0) {
                                    String.format(Locale.US, s.riskReportsOpenCritical, openCriticalReportCount)
                                } else {
                                    s.riskReportsSubDefault
                                },
                                fontSize = 10.sp,
                                color = if (openCriticalReportCount > 0) Color(0xFFFF8A80) else Color(0xFF9CA3AF)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showNewReportForm = !showNewReportForm },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("toggle_new_risk_report_form_button")
                    ) {
                        Icon(
                            imageVector = if (showNewReportForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showNewReportForm) s.btnClose else s.btnCreateReport,
                            tint = Color(0xFFFFAB91),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showNewReportForm) s.btnClose else s.btnCreateReport,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFAB91)
                        )
                    }
                }

                AnimatedVisibility(visible = showNewReportForm) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = newReportZone,
                            onValueChange = { newReportZone = it },
                            label = { Text(s.reportZoneLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF7043),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_report_zone_input")
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { level ->
                                FilterChip(
                                    selected = newReportSeverity == level,
                                    onClick = { newReportSeverity = level },
                                    label = { Text(level, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFF7043).copy(alpha = 0.25f),
                                        selectedLabelColor = Color(0xFFFFAB91),
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFFD1D5DB)
                                    ),
                                    modifier = Modifier.testTag("severity_chip_$level")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newReportHazard,
                            onValueChange = { newReportHazard = it },
                            label = { Text(s.reportHazardLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFF7043),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_report_hazard_input")
                        )

                        OutlinedTextField(
                            value = newReportMitigation,
                            onValueChange = { newReportMitigation = it },
                            label = { Text(s.reportMitigationLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_report_mitigation_input")
                        )

                        Button(
                            onClick = {
                                onCreateRiskReport(
                                    newReportZone,
                                    "SITE_SAFETY",
                                    newReportSeverity,
                                    newReportHazard,
                                    newReportHazard,
                                    newReportMitigation
                                )
                                newReportZone = ""
                                newReportHazard = ""
                                newReportMitigation = ""
                                showNewReportForm = false
                            },
                            enabled = newReportHazard.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF7043),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_new_risk_report_button")
                        ) {
                            Text(s.submitRiskReportBtn, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (riskReports.isEmpty()) {
                    Text(
                        text = s.riskReportsEmpty,
                        fontSize = 11.sp,
                        color = Color(0xFFD1D5DB),
                        lineHeight = 16.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        riskReports.take(10).forEach { report ->
                            val severityColor = when (report.severityLevel) {
                                "CRITICAL" -> Color(0xFFFF5252)
                                "HIGH" -> Color(0xFFFFB300)
                                "MEDIUM" -> Color(0xFF00E5FF)
                                else -> Color(0xFF00E676)
                            }
                            val statusColor = when (report.status) {
                                "MITIGATED" -> Color(0xFF00E676)
                                "STOP_WORK" -> Color(0xFFFF5252)
                                else -> Color(0xFFFFB300)
                            }
                            val displayZone = LocalizedSeedMapper.mapRiskZone(report.siteZone, lang)
                            val displayMitigation = LocalizedSeedMapper.mapRiskMitigation(report.mitigationPlan, lang)

                            Surface(
                                color = Color(0xFF0B1324),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.75.dp, severityColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                    .testTag("risk_report_item_${report.id}")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = severityColor.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = report.severityLevel,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = severityColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Surface(
                                                color = statusColor.copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = report.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = report.reportCode,
                                                fontSize = 10.sp,
                                                color = Color(0xFF9CA3AF)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onDeleteRiskReport(report) },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("delete_risk_report_${report.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = report.reportCode,
                                                tint = Color(0xFF9CA3AF),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$displayZone • ${report.hazardSummary}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${s.mitigationPrefix} $displayMitigation",
                                        fontSize = 11.sp,
                                        color = Color(0xFFD1D5DB),
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${dateFormatter.format(Date(report.createdAt))} • ${s.sourcePrefix} ${report.inferenceOrigin}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF9CA3AF)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val isMitigated = report.status == "MITIGATED"
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateRiskReportStatus(
                                                    report.id,
                                                    if (isMitigated) "OPEN" else "MITIGATED"
                                                )
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(48.dp)
                                                .testTag("toggle_report_mitigated_${report.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = s.closeMitigatedBtn,
                                                tint = if (isMitigated) Color(0xFFFFB300) else Color(0xFF00E676),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (isMitigated) s.reopenCaseBtn else s.closeMitigatedBtn,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMitigated) Color(0xFFFFB300) else Color(0xFF00E676)
                                            )
                                        }

                                        if (report.status != "STOP_WORK") {
                                            OutlinedButton(
                                                onClick = { onUpdateRiskReportStatus(report.id, "STOP_WORK") },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .height(48.dp)
                                                    .testTag("mark_report_stop_work_${report.id}")
                                            ) {
                                                Text(
                                                    text = s.stopWorkBtn,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF5252)
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

        // 2. Relational Site Inspection Checklists (inspection_checklists + inspection_checklist_items)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                .testTag("inspection_checklists_room_section")
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
                            imageVector = Icons.AutoMirrored.Filled.FactCheck,
                            contentDescription = s.checklistsTitle,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${s.checklistsTitle}${checklistsWithItems.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = s.checklistsSubtitle,
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { showNewChecklistForm = !showNewChecklistForm },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("toggle_new_checklist_form_button")
                    ) {
                        Icon(
                            imageVector = if (showNewChecklistForm) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = if (showNewChecklistForm) s.btnClose else s.btnCreateChecklist,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showNewChecklistForm) s.btnClose else s.btnCreateChecklist,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }
                }

                AnimatedVisibility(visible = showNewChecklistForm) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        OutlinedTextField(
                            value = newChecklistTitle,
                            onValueChange = { newChecklistTitle = it },
                            label = { Text(s.checklistTitleLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_checklist_title_input")
                        )

                        OutlinedTextField(
                            value = newChecklistZone,
                            onValueChange = { newChecklistZone = it },
                            label = { Text(s.checklistZoneLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_checklist_zone_input")
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("SAFETY", "STRUCTURAL", "MEP", "FINISHING").forEach { trade ->
                                FilterChip(
                                    selected = newChecklistTrade == trade,
                                    onClick = { newChecklistTrade = trade },
                                    label = { Text(trade, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.22f),
                                        selectedLabelColor = Color(0xFF00E5FF),
                                        containerColor = Color(0xFF1E293B),
                                        labelColor = Color(0xFFD1D5DB)
                                    ),
                                    modifier = Modifier.testTag("trade_chip_$trade")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newChecklistItemsText,
                            onValueChange = { newChecklistItemsText = it },
                            label = { Text(s.checklistItemsLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF)) },
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_checklist_items_input")
                        )

                        Button(
                            onClick = {
                                val parsedItems = newChecklistItemsText.lines()
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                onCreateInspectionChecklist(
                                    newChecklistTitle,
                                    newChecklistTrade,
                                    newChecklistZone,
                                    parsedItems,
                                    riskReports.firstOrNull()?.id
                                )
                                newChecklistTitle = ""
                                newChecklistZone = ""
                                showNewChecklistForm = false
                            },
                            enabled = newChecklistTitle.isNotBlank() && newChecklistItemsText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_new_checklist_button")
                        ) {
                            Text(s.submitChecklistBtn, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    checklistsWithItems.forEach { entry ->
                        val checklist = entry.checklist
                        val sortedItems = entry.items.sortedBy { it.sequenceOrder }
                        val passedCount = sortedItems.count { it.resultStatus == "PASS" }
                        val overallColor = when (checklist.overallStatus) {
                            "PASSED" -> Color(0xFF00E676)
                            "FAILED_HOLD" -> Color(0xFFFF5252)
                            else -> Color(0xFFFFB300)
                        }
                        val displayTitle = LocalizedSeedMapper.mapChecklistTitle(
                            checklist.checklistCode,
                            checklist.title,
                            lang
                        )
                        val displayMeta = LocalizedSeedMapper.mapChecklistMeta(
                            checklist.checklistCode,
                            checklist.siteZone,
                            checklist.inspectorName,
                            lang
                        )

                        Surface(
                            color = Color(0xFF0B1324),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.75.dp, overallColor.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                .testTag("inspection_checklist_card_${checklist.id}")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = checklist.checklistCode,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Surface(
                                            color = overallColor.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = checklist.overallStatus,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = overallColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = "${checklist.tradeCategory} • $passedCount/${sortedItems.size} ${s.passRateLabel}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF9CA3AF)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteInspectionChecklist(checklist) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("delete_checklist_${checklist.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = displayTitle,
                                            tint = Color(0xFF9CA3AF),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = displayTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = displayMeta,
                                    fontSize = 10.sp,
                                    color = Color(0xFF9CA3AF)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    sortedItems.forEach { item ->
                                        val itemStatusColor = when (item.resultStatus) {
                                            "PASS" -> Color(0xFF00E676)
                                            "FAIL" -> Color(0xFFFF5252)
                                            else -> Color(0xFF9CA3AF)
                                        }
                                        val displayItemTitle = LocalizedSeedMapper.mapChecklistItemTitle(
                                            item.standardReference,
                                            item.sequenceOrder,
                                            item.itemTitle,
                                            lang
                                        )
                                        Surface(
                                            color = Color(0xFF111827),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(0.5.dp, itemStatusColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .testTag("checklist_item_row_${item.id}")
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "${item.sequenceOrder}. $displayItemTitle",
                                                    fontSize = 11.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val isPass = item.resultStatus == "PASS"
                                                    val isFail = item.resultStatus == "FAIL"

                                                    OutlinedButton(
                                                        onClick = {
                                                            onSetInspectionItemResult(
                                                                checklist.id,
                                                                item.id,
                                                                if (isPass) "PENDING" else "PASS"
                                                            )
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            containerColor = if (isPass) Color(0xFF00E676).copy(alpha = 0.2f) else Color.Transparent
                                                        ),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(48.dp)
                                                            .testTag("checklist_item_pass_${item.id}")
                                                    ) {
                                                        Text(
                                                            text = "PASS",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (isPass) Color(0xFF00E676) else Color(0xFFD1D5DB)
                                                        )
                                                    }

                                                    OutlinedButton(
                                                        onClick = {
                                                            onSetInspectionItemResult(
                                                                checklist.id,
                                                                item.id,
                                                                if (isFail) "PENDING" else "FAIL"
                                                            )
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                            containerColor = if (isFail) Color(0xFFFF5252).copy(alpha = 0.2f) else Color.Transparent
                                                        ),
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .height(48.dp)
                                                            .testTag("checklist_item_fail_${item.id}")
                                                    ) {
                                                        Text(
                                                            text = "FAIL",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = if (isFail) Color(0xFFFF5252) else Color(0xFFD1D5DB)
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
            }
        }
    }
}
