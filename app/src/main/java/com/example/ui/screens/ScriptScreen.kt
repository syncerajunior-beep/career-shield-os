package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.R
import com.example.data.ScriptEntity
import com.example.ui.components.InferenceSourcePill
import com.example.viewmodel.ScriptTailorUiState

@Composable
fun ScriptScreen(
    context: Context,
    scripts: List<ScriptEntity>,
    scriptTailorState: ScriptTailorUiState,
    offlineSiteTestMode: Boolean,
    onTailorScript: (
        script: ScriptEntity,
        recipientName: String,
        reasonDetail: String,
        clauseNumber: String,
        customSituation: String
    ) -> Unit,
    onClearTailorResult: () -> Unit,
    onDisableOfflineMode: () -> Unit,
    onAddScript: (title: String, content: String, category: String) -> Unit,
    onDeleteScript: (ScriptEntity) -> Unit
) {
    var recipientName by rememberSaveable { mutableStateOf("") }
    var reasonDetail by rememberSaveable { mutableStateOf("") }
    var clauseNumber by rememberSaveable { mutableStateOf("") }
    var customSituation by rememberSaveable { mutableStateOf("") }
    var showAddScriptDialog by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("script_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.sos_scripts_title),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "คัดลอกข้อความส่ง LINE หรือให้ Hybrid AI (AICore Preview On-Device / Cloud Fallback) ช่วยเกลาภาษาให้รัดกุมก่อนส่งทันที",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            // Quick Variable Replacer & Hybrid AI Context Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "ตัวช่วยเติมข้อมูลอัตโนมัติ",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ตัวช่วยเติมข้อมูล & บริบทสำหรับ Hybrid AI",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = recipientName,
                                onValueChange = { recipientName = it },
                                label = { Text("ชื่อผู้ว่าจ้าง/บริษัท", fontSize = 10.sp, color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF374151)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("script_recipient_input")
                            )
                            OutlinedTextField(
                                value = clauseNumber,
                                onValueChange = { clauseNumber = it },
                                label = { Text("ข้อสัญญา/เลขที่ RFI", fontSize = 10.sp, color = Color(0xFF9CA3AF)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF00E5FF),
                                    unfocusedBorderColor = Color(0xFF374151)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("script_clause_input")
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = reasonDetail,
                            onValueChange = { reasonDetail = it },
                            label = { Text("สาเหตุ/โซนหน้างาน (เช่น ฝนตกหนักน้ำท่วมขังบ่อฐานราก โซน B)", fontSize = 10.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("script_reason_input")
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customSituation,
                            onValueChange = { customSituation = it },
                            label = { Text("คำสั่งเพิ่มเติมให้ Hybrid AI (เช่น ขอสงวนสิทธิ์ EOT 5 วัน + แนบรูปถ่าย)", fontSize = 10.sp, color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E676),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("script_ai_custom_situation_input")
                        )
                    }
                }
            }

            // Hybrid AI Tailored Script Result Card
            if (scriptTailorState !is ScriptTailorUiState.Idle) {
                item {
                    HybridTailoredScriptCard(
                        context = context,
                        state = scriptTailorState,
                        offlineSiteTestMode = offlineSiteTestMode,
                        onSaveAsNewScript = { title, content ->
                            onAddScript(title, content, "EOT_LD")
                            Toast.makeText(
                                context,
                                "บันทึกสคริปต์ที่ปรับโดย AI ลง Room DB แล้ว!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onClear = onClearTailorResult,
                        onDisableOfflineMode = onDisableOfflineMode
                    )
                }
            }

            items(
                items = scripts,
                key = { it.id }
            ) { script ->
                val resolvedContent = remember(script.content, recipientName, reasonDetail, clauseNumber) {
                    var text = script.content
                    if (recipientName.isNotBlank()) {
                        text = text
                            .replace("[ชื่อผู้ว่าจ้าง/เจ้านาย]", recipientName)
                            .replace("[ชื่อผู้รับเหมาช่วง]", recipientName)
                    }
                    if (reasonDetail.isNotBlank()) {
                        text = text
                            .replace("[ระบุเหตุผล]", reasonDetail)
                            .replace("[ระบุโซน/ชั้น]", reasonDetail)
                    }
                    if (clauseNumber.isNotBlank()) {
                        text = text
                            .replace("[X]", clauseNumber)
                            .replace("[เลขที่ RFI]", clauseNumber)
                    }
                    text
                }

                ScriptCard(
                    scriptId = script.id,
                    title = script.title,
                    category = script.category,
                    content = resolvedContent,
                    context = context,
                    isTailoringLoading = scriptTailorState is ScriptTailorUiState.Loading,
                    onTailorWithAi = {
                        onTailorScript(script, recipientName, reasonDetail, clauseNumber, customSituation)
                    },
                    onDelete = { onDeleteScript(script) }
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddScriptDialog = true },
            containerColor = Color(0xFF00E5FF),
            contentColor = Color(0xFF030712),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_script_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_script_button)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_script_button),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showAddScriptDialog) {
        AddScriptDialog(
            onDismiss = { showAddScriptDialog = false },
            onConfirm = { title, content, category ->
                onAddScript(title, content, category)
                showAddScriptDialog = false
            }
        )
    }
}

@Composable
private fun HybridTailoredScriptCard(
    context: Context,
    state: ScriptTailorUiState,
    offlineSiteTestMode: Boolean,
    onSaveAsNewScript: (title: String, content: String) -> Unit,
    onClear: () -> Unit,
    onDisableOfflineMode: () -> Unit
) {
    when (state) {
        is ScriptTailorUiState.Idle -> Unit
        is ScriptTailorUiState.Loading -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                    .testTag("script_tailor_loading_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E676),
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (offlineSiteTestMode) {
                                "กำลังเรียกประมวลผลบนอุปกรณ์ (ONLY_ON_DEVICE • PREVIEW)..."
                            } else {
                                "Hybrid AI กำลังปรับสคริปต์ '${state.scriptTitle}'..."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "โหมด: ${if (offlineSiteTestMode) "ONLY_ON_DEVICE (PREVIEW)" else "PREFER_ON_DEVICE (PREVIEW -> Cloud)"}",
                            fontSize = 10.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
        is ScriptTailorUiState.Success -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF00E676), RoundedCornerShape(12.dp))
                    .testTag("script_tailor_result_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        InferenceSourcePill(
                            source = state.inferenceSource,
                            detailLabel = "SOS Tailor"
                        )
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "ปิดผลปรับสคริปต์",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "✨ สคริปต์ปรับปรุงพร้อมส่ง: ${state.originalTitle}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.tailoredContent,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Tailored SOS", state.tailoredContent))
                                Toast.makeText(context, "คัดลอกสคริปต์ที่ปรับแล้วสำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_tailored_script_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "คัดลอกสคริปต์ที่ปรับแล้ว",
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("คัดลอก", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                onSaveAsNewScript(
                                    "${state.originalTitle} (AI)",
                                    state.tailoredContent
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_tailored_script_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "บันทึกสคริปต์ใหม่ลง Room DB",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("เก็บลง Room", fontSize = 11.sp, color = Color(0xFF00E676))
                        }
                    }
                }
            }
        }
        is ScriptTailorUiState.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                    .testTag("script_tailor_error_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ ${state.message}",
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "ปิดแจ้งเตือน",
                                tint = Color.White
                            )
                        }
                    }
                    if (state.isOnDeviceUnavailable) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onDisableOfflineMode,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier.testTag("switch_cloud_fallback_from_script_button")
                        ) {
                            Text(
                                text = "ปิด Offline Mode เพื่อใช้ Cloud Gemini Fallback",
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptCard(
    scriptId: Int = 0,
    title: String,
    category: String = "CONTRACT",
    content: String,
    context: Context,
    isTailoringLoading: Boolean = false,
    onTailorWithAi: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF374151), RoundedCornerShape(10.dp))
            .testTag("script_card_$scriptId")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_script_button_$scriptId")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ลบสคริปต์ $title",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = content,
                fontSize = 12.sp,
                color = Color(0xFFD1D5DB),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onTailorWithAi != null) {
                    OutlinedButton(
                        onClick = onTailorWithAi,
                        enabled = !isTailoringLoading,
                        modifier = Modifier.testTag("tailor_script_ai_button_$scriptId")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "ปรับแต่งสคริปต์ด้วย Hybrid AI",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ปรับด้วย AI",
                            fontSize = 11.sp,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, title)
                            putExtra(Intent.EXTRA_TEXT, content)
                        }
                        val chooser = Intent.createChooser(sendIntent, "แชร์สคริปต์ SOS ผ่าน...")
                        context.startActivity(chooser)
                    },
                    modifier = Modifier.testTag("share_script_button_$scriptId")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "แชร์สคริปต์",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_share),
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Script", content))
                        Toast.makeText(context, "คัดลอกข้อความสำเร็จ!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF030712)
                    ),
                    modifier = Modifier.testTag("copy_script_button_$scriptId")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "คัดลอกข้อความลงคลิปบอร์ด",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.btn_copy),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddScriptDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("CONTRACT") }
    val categories = listOf("CONTRACT", "EOT_LD", "SLA", "SAFETY")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = "เพิ่มสคริปต์ SOS ลง Room DB",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF00E5FF)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category == cat
                        Surface(
                            color = if (selected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color(0xFF030712) else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("หัวข้อสคริปต์เอาตัวรอด", color = Color(0xFF9CA3AF)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_script_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("เนื้อหาสคริปต์", color = Color(0xFF9CA3AF)) },
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_script_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content, category)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF030712)
                ),
                modifier = Modifier.testTag("confirm_add_script_button")
            ) {
                Text("บันทึกลงเครื่อง", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = Color(0xFFD1D5DB))
            }
        }
    )
}
