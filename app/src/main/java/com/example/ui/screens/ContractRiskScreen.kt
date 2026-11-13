package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TravelExplore
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.TaskEntity
import com.example.ui.components.InferenceSourcePill
import com.example.util.SitePhotoStorage
import com.example.viewmodel.ContractRiskUiState
import com.example.viewmodel.GroundingSource
import com.example.viewmodel.SitePhotoInspectUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContractRiskScreen(
    context: Context,
    tasksWithPhotos: List<TaskEntity>,
    initialInspectPhotoUri: String,
    initialInspectTaskTitle: String,
    onConsumeInitialInspectPhoto: () -> Unit,
    contractRiskState: ContractRiskUiState,
    sitePhotoInspectState: SitePhotoInspectUiState,
    offlineSiteTestMode: Boolean,
    onAnalyzeClause: (clauseText: String, useDeepThinking: Boolean) -> Unit,
    onInspectSitePhoto: (photoUri: String, focusNote: String) -> Unit,
    onSaveAnalysisAsSosScript: (title: String, content: String) -> Unit,
    onDiscussInChat: (String) -> Unit
) {
    // 0 = Contract Clause LD Scanner, 1 = Site Photo Risk Inspection (Cloud Multimodal)
    var activeScannerMode by rememberSaveable { mutableIntStateOf(0) }

    var clauseText by rememberSaveable {
        mutableStateOf(
            "ข้อ 14. ค่าปรับกรณีงานล่าช้า (Liquidated Damages): หากผู้รับจ้างส่งมอบงานล่าช้ากว่ากำหนดในสัญญา ผู้รับจ้างยินยอมให้ผู้ว่าจ้างปรับเป็นรายวันในอัตราวันละ 0.25% ของมูลค่างานตามสัญญาทั้งหมด (คิดเป็นวันละ 150,000 บาท) โดยไม่มีเพดานสูงสุด และผู้รับจ้างตกลงสละสิทธิ์การขอขยายเวลาก่อสร้าง (EOT) จากเหตุฝนตกหรือการส่งมอบพื้นที่ล่าช้าทุกกรณี"
        )
    }
    var useDeepThinking by rememberSaveable { mutableStateOf(false) }
    var showThoughts by rememberSaveable { mutableStateOf(true) }

    var selectedPhotoUri by rememberSaveable { mutableStateOf("") }
    var photoFocusNote by rememberSaveable {
        mutableStateOf("ตรวจความเสี่ยงความปลอดภัย โครงสร้างเหล็ก/คอนกรีต และหลักฐานเพื่อขอ VO หรือป้องกันค่าปรับ LD")
    }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(initialInspectPhotoUri) {
        if (initialInspectPhotoUri.isNotBlank()) {
            activeScannerMode = 1
            selectedPhotoUri = initialInspectPhotoUri
            if (initialInspectTaskTitle.isNotBlank()) {
                photoFocusNote = "ตรวจสอบจุดเสี่ยงจากภารกิจ: $initialInspectTaskTitle"
            }
            onInspectSitePhoto(initialInspectPhotoUri, photoFocusNote)
            onConsumeInitialInspectPhoto()
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val captured = pendingCaptureUri?.toString().orEmpty()
            if (captured.isNotBlank()) {
                selectedPhotoUri = captured
            }
        } else {
            SitePhotoStorage.deletePhotoFileIfLocal(context, pendingCaptureUri?.toString())
        }
        pendingCaptureUri = null
    }

    val launchCamera: () -> Unit = {
        val uri = SitePhotoStorage.createSitePhotoUri(context)
        pendingCaptureUri = uri
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(
                context,
                "กรุณาอนุญาตกล้องเพื่อถ่ายภาพจุดเสี่ยงหน้างานส่งให้ AI ตรวจสอบ",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        if (pickedUri != null) {
            val saved = SitePhotoStorage.persistPickedPhoto(context, pickedUri)
            if (!saved.isNullOrBlank()) {
                selectedPhotoUri = saved
            } else {
                Toast.makeText(context, "ไม่สามารถอ่านรูปภาพที่เลือกได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val sampleClauses = listOf(
        "ค่าปรับ LD วันละ 1.5 แสน ไม่จำกัดเพดาน" to
            "ข้อ 14. ค่าปรับกรณีงานล่าช้า (Liquidated Damages): หากผู้รับจ้างส่งมอบงานล่าช้ากว่ากำหนดในสัญญา ผู้รับจ้างยินยอมให้ผู้ว่าจ้างปรับเป็นรายวันในอัตราวันละ 0.25% ของมูลค่างานตามสัญญาทั้งหมด (คิดเป็นวันละ 150,000 บาท) โดยไม่มีเพดานสูงสุด และผู้รับจ้างตกลงสละสิทธิ์การขอขยายเวลาก่อสร้าง (EOT) จากเหตุฝนตกหรือการส่งมอบพื้นที่ล่าช้าทุกกรณี",
        "สั่งแก้งานปากเปล่าก่อนค่อยเบิก VO" to
            "ข้อ 9. การแก้ไขเปลี่ยนแปลงงาน (Variation Order): ผู้ว่าจ้างหรือวิศวกรผู้ควบคุมงานมีสิทธิ์สั่งแก้ไขเปลี่ยนแปลงรายละเอียดงานหน้างานด้วยวาจาได้ทันที และผู้รับจ้างต้องดำเนินการให้เสร็จสิ้นก่อนจึงจะมีสิทธิ์ยื่นขออนุมัติงบประมาณและระยะเวลาเพิ่มเติมภายหลัง",
        "แจ้ง EOT ภายใน 3 วัน มิฉะนั้นสละสิทธิ์" to
            "ข้อ 18. การขอขยายระยะเวลาก่อสร้าง (EOT): ผู้รับจ้างมีสิทธิ์ขอขยายเวลาก่อสร้างเฉพาะเหตุภัยพิบัติที่ทางราชการประกาศเท่านั้น ทั้งนี้ต้องทำหนังสือแจ้งภายใน 3 วันนับแต่วันเกิดเหตุ มิฉะนั้นถือว่าผู้รับจ้างสละสิทธิ์เรียกร้องเวลาและค่าใช้จ่ายใดๆ ทั้งสิ้น",
        "Pay-when-Paid จ่ายเมื่อเจ้าของจ่าย" to
            "ข้อ 7. เงื่อนไขการชำระเงินงวดงาน: ผู้รับเหมาหลักจะชำระค่างวดงานให้แก่ผู้รับเหมาช่วงภายใน 45 วันนับตั้งแต่วันที่ผู้รับเหมาหลักได้รับเงินงวดนั้นจากเจ้าของโครงการแล้วเท่านั้น พร้อมหักเงินประกันผลงาน (Retention) ร้อยละ 10 ของทุกงวด"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("contract_risk_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Card + Mode Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "LD และ Site Photo Scanner",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.contract_scanner_title),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Cloud Fallback Route • Multimodal Photo & Legal Search Grounding",
                                fontSize = 11.sp,
                                color = Color(0xFFFFB300),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.contract_scanner_subtitle),
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB),
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeScannerMode == 0,
                            onClick = { activeScannerMode = 0 },
                            label = {
                                Text(
                                    text = "⚖️ สแกนสัญญา LD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFB300).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFFFB300),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scanner_tab_contract_ld")
                        )
                        FilterChip(
                            selected = activeScannerMode == 1,
                            onClick = { activeScannerMode = 1 },
                            label = {
                                Text(
                                    text = "📷 ตรวจรูปหน้างาน",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF00E5FF),
                                containerColor = Color(0xFF1E293B),
                                labelColor = Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scanner_tab_site_photo")
                        )
                    }
                }
            }
        }

        if (activeScannerMode == 0) {
            // Mode 0: Contract Clause & LD Risk Scanner
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
                            text = "ตัวอย่างข้อสัญญาที่มีความเสี่ยงสูง (แตะเพื่อทดสอบ):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sampleClauses.forEachIndexed { index, (label, sampleText) ->
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .border(0.5.dp, Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .clickable { clauseText = sampleText }
                                        .testTag("sample_clause_chip_$index")
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
                            value = clauseText,
                            onValueChange = { clauseText = it },
                            label = {
                                Text(
                                    text = "วางข้อความข้อสัญญาก่อสร้าง (Contract Clause) ที่ต้องการตรวจสอบ",
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            trailingIcon = {
                                if (clauseText.isNotEmpty()) {
                                    IconButton(
                                        onClick = { clauseText = "" },
                                        modifier = Modifier.testTag("clear_clause_input_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "ล้างข้อความสัญญา",
                                            tint = Color(0xFF9CA3AF)
                                        )
                                    }
                                }
                            },
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFB300),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("contract_clause_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !useDeepThinking,
                                onClick = { useDeepThinking = false },
                                label = {
                                    Text(
                                        text = "⚡ สแกนด่วน + Google Search",
                                        fontSize = 11.sp,
                                        fontWeight = if (!useDeepThinking) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TravelExplore,
                                        contentDescription = "Google Search Grounding",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF00E5FF),
                                    selectedLeadingIconColor = Color(0xFF00E5FF),
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFD1D5DB),
                                    iconColor = Color(0xFF9CA3AF)
                                ),
                                modifier = Modifier.testTag("mode_search_grounding_chip")
                            )

                            FilterChip(
                                selected = useDeepThinking,
                                onClick = { useDeepThinking = true },
                                label = {
                                    Text(
                                        text = "🧠 คิดเชิงลึก (High Thinking)",
                                        fontSize = 11.sp,
                                        fontWeight = if (useDeepThinking) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = "Gemini 4 Carbon High Thinking",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFB388FF).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFFB388FF),
                                    selectedLeadingIconColor = Color(0xFFB388FF),
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFD1D5DB),
                                    iconColor = Color(0xFF9CA3AF)
                                ),
                                modifier = Modifier.testTag("mode_deep_thinking_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onAnalyzeClause(clauseText, useDeepThinking) },
                            enabled = clauseText.isNotBlank() && contractRiskState !is ContractRiskUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB300),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("analyze_contract_clause_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = "วิเคราะห์ความเสี่ยงข้อสัญญาและค่าปรับ LD"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "สแกนความเสี่ยงกฎหมาย & ค่าปรับ LD ทันที",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item {
                ContractRiskResultSection(
                    context = context,
                    clauseText = clauseText,
                    useDeepThinking = useDeepThinking,
                    showThoughts = showThoughts,
                    onToggleThoughts = { showThoughts = !showThoughts },
                    contractRiskState = contractRiskState,
                    onSaveAnalysisAsSosScript = onSaveAnalysisAsSosScript,
                    onDiscussInChat = onDiscussInChat
                )
            }
        } else {
            // Mode 1: Site Risk Photo Inspection (Cloud Gemini Multimodal)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (offlineSiteTestMode) {
                            Surface(
                                color = Color(0xFF2B210B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = "ℹ️ งานวิเคราะห์ภาพถ่าย Multimodal ใช้ Cloud Fallback: Gemini (อัตโนมัติ) เพื่อความแม่นยำในการตรวจจับความเสี่ยงหน้างาน",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFB300),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Text(
                            text = "1. เลือกภาพถ่ายจุดเสี่ยงหน้างาน (ถ่ายใหม่ / คลังภาพ / จาก Room DB):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val granted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (granted) launchCamera() else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scanner_capture_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "ถ่ายรูปจุดเสี่ยงหน้างาน",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ถ่ายรูปหน้างาน", fontSize = 11.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scanner_pick_photo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = "เลือกรูปจากคลังภาพ",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("เลือกจากคลัง", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (tasksWithPhotos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "รูปภาพหลักฐานที่บันทึกไว้ใน Room DB (${tasksWithPhotos.size} รายการ):",
                                fontSize = 11.sp,
                                color = Color(0xFFD1D5DB)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(tasksWithPhotos, key = { it.id }) { task ->
                                    val uri = task.photoUri.orEmpty()
                                    val isSelected = selectedPhotoUri == uri
                                    Surface(
                                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.16f) else Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .width(140.dp)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) Color(0xFF00E5FF) else Color(0xFF374151),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedPhotoUri = uri
                                                photoFocusNote = "ตรวจสอบจุดเสี่ยงจากภารกิจ: ${task.title}"
                                            }
                                            .testTag("room_photo_preset_${task.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            AsyncImage(
                                                model = Uri.parse(uri),
                                                contentDescription = task.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(64.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = task.title,
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (selectedPhotoUri.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AsyncImage(
                                model = Uri.parse(selectedPhotoUri),
                                contentDescription = "ภาพถ่ายหน้างานที่จะส่งตรวจ AI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 160.dp, max = 260.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF030712))
                                    .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp))
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = photoFocusNote,
                            onValueChange = { photoFocusNote = it },
                            label = { Text("จุดที่ต้องการให้ AI ตรวจสอบในภาพ", color = Color(0xFF9CA3AF)) },
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color(0xFF374151)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("photo_focus_note_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onInspectSitePhoto(selectedPhotoUri, photoFocusNote) },
                            enabled = selectedPhotoUri.isNotBlank() && sitePhotoInspectState !is SitePhotoInspectUiState.Loading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E5FF),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_site_photo_inspection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "วิเคราะห์ภาพถ่ายด้วย Cloud Gemini Multimodal"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "วิเคราะห์ภาพจุดเสี่ยง (Cloud Gemini Multimodal)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            item {
                SitePhotoInspectionResultSection(
                    context = context,
                    state = sitePhotoInspectState,
                    onSaveAnalysisAsSosScript = onSaveAnalysisAsSosScript,
                    onDiscussInChat = onDiscussInChat
                )
            }
        }
    }
}

@Composable
private fun ContractRiskResultSection(
    context: Context,
    clauseText: String,
    useDeepThinking: Boolean,
    showThoughts: Boolean,
    onToggleThoughts: () -> Unit,
    contractRiskState: ContractRiskUiState,
    onSaveAnalysisAsSosScript: (title: String, content: String) -> Unit,
    onDiscussInChat: (String) -> Unit
) {
    when (contractRiskState) {
        is ContractRiskUiState.Idle -> Unit
        is ContractRiskUiState.Loading -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contract_risk_loading_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFB300),
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (useDeepThinking) {
                                "Gemini 4 Carbon กำลังวิเคราะห์ข้อสัญญาในโหมด High Thinking..."
                            } else {
                                "Cloud Gemini 3.8 Flash กำลังตรวจสอบข้อกฎหมายและค้นหาข้อมูลผ่าน Google Search..."
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "กำลังประเมินเพดานค่าปรับ LD เงื่อนไข EOT และร่างข้อสัญญาโต้กลับ (Counter-Clause)",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
        is ContractRiskUiState.Streaming, is ContractRiskUiState.Success -> {
            val analysisText = when (contractRiskState) {
                is ContractRiskUiState.Streaming -> contractRiskState.partialAnalysis
                is ContractRiskUiState.Success -> contractRiskState.analysis
                else -> ""
            }
            val thoughts = when (contractRiskState) {
                is ContractRiskUiState.Streaming -> contractRiskState.thoughts
                is ContractRiskUiState.Success -> contractRiskState.thoughts
                else -> ""
            }
            val successState = contractRiskState as? ContractRiskUiState.Success
            val sources = successState?.sources.orEmpty()
            val searchQueries = successState?.searchQueries.orEmpty()
            val isStreaming = contractRiskState is ContractRiskUiState.Streaming

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFFF5252), RoundedCornerShape(12.dp))
                    .testTag("contract_risk_result_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (successState != null) {
                        InferenceSourcePill(
                            source = successState.inferenceSource,
                            detailLabel = "LD Scanner",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

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
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "ผลการวิเคราะห์ความเสี่ยงสัญญา",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isStreaming) {
                                    "⚡ กำลังสแกนจุดเสี่ยงข้อสัญญา..."
                                } else {
                                    "🚨 ผลการวิเคราะห์ความเสี่ยงกฎหมาย & ค่าปรับ LD"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFF5252)
                            )
                        }

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Contract Risk Analysis", analysisText)
                                )
                                Toast.makeText(context, "คัดลอกผลวิเคราะห์สำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("copy_contract_analysis_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "คัดลอกผลวิเคราะห์",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("คัดลอก", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    if (thoughts.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onToggleThoughts)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🧠 กระบวนการคิดเชิงลึก (High Thinking Reasoning)",
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = analysisText.ifBlank { "กำลังประมวลผลข้อกฎหมาย..." },
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 19.sp
                    )

                    if (searchQueries.isNotEmpty() || sources.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GroundingSourcesSection(
                            context = context,
                            searchQueries = searchQueries,
                            sources = sources
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val shortTitle = "ข้อโต้แย้งสัญญา/ลดเสี่ยง LD (${clauseText.take(24)}...)"
                                onSaveAnalysisAsSosScript(shortTitle, analysisText)
                                Toast.makeText(
                                    context,
                                    "บันทึกลงหน้าสคริปต์ SOS (Room DB) แล้ว!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_clause_analysis_to_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "บันทึกเป็นสคริปต์ SOS",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "เก็บลงสคริปต์ SOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onDiscussInChat(
                                    "ช่วยแนะนำวิธีเจรจาต่อรองกับผู้ว่าจ้างสำหรับข้อสัญญานี้เพิ่มเติม: \"$clauseText\""
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discuss_clause_in_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "ปรึกษาต่อในแชท AI",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ถามต่อในแชท AI",
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        is ContractRiskUiState.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                    .testTag("contract_risk_error_card")
            ) {
                Text(
                    text = "⚠️ ${contractRiskState.message}",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SitePhotoInspectionResultSection(
    context: Context,
    state: SitePhotoInspectUiState,
    onSaveAnalysisAsSosScript: (title: String, content: String) -> Unit,
    onDiscussInChat: (String) -> Unit
) {
    when (state) {
        is SitePhotoInspectUiState.Idle -> Unit
        is SitePhotoInspectUiState.Loading -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("site_photo_inspect_loading_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Cloud Gemini 3.8 Flash (Multimodal) กำลังตรวจภาพหน้างาน...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "กำลังประเมินความเสี่ยงด้าน Safety, โครงสร้าง, ค่าปรับ LD และหลักฐาน VO/EOT",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
        is SitePhotoInspectUiState.Success -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                    .testTag("site_photo_inspect_result_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    InferenceSourcePill(
                        source = state.inferenceSource,
                        detailLabel = "Multimodal Photo",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📷 ผลตรวจสอบภาพถ่ายจุดเสี่ยงหน้างาน",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Site Photo Inspection", state.analysis))
                                Toast.makeText(context, "คัดลอกผลตรวจภาพถ่ายสำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("copy_photo_inspection_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "คัดลอกผลตรวจภาพถ่าย",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("คัดลอก", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.analysis,
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveAnalysisAsSosScript("รายงานตรวจภาพหน้างาน (Site Photo)", state.analysis)
                                Toast.makeText(
                                    context,
                                    "บันทึกลงหน้าสคริปต์ SOS (Room DB) แล้ว!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                contentColor = Color(0xFF030712)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_photo_inspection_to_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "บันทึกเป็นสคริปต์ SOS",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("เก็บลงสคริปต์", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = {
                                onDiscussInChat(
                                    "จากผลตรวจภาพหน้างานนี้ ช่วยแนะนำวิธีเขียนหนังสือสงวนสิทธิ์ EOT หรือแจ้งเตือนผู้รับเหมา: ${state.analysis.take(300)}"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discuss_photo_in_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "ถามต่อในแชท AI",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ถามต่อในแชท", fontSize = 11.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        is SitePhotoInspectUiState.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(10.dp))
                    .testTag("site_photo_inspect_error_card")
            ) {
                Text(
                    text = "⚠️ ${state.message}",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
fun GroundingSourcesSection(
    context: Context,
    searchQueries: List<String>,
    sources: List<GroundingSource>
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "ข้อมูลอ้างอิงจาก Google Search",
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "อ้างอิงข้อมูลล่าสุดผ่าน Google Search Grounding",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF)
                )
            }

            if (searchQueries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "คำค้นหา: ${searchQueries.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            if (sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                sources.take(4).forEach { source ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (source.uri.isNotBlank()) {
                                    try {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(source.uri))
                                        )
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• ${source.title}",
                            fontSize = 11.sp,
                            color = Color(0xFFD1D5DB),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "เปิดลิงก์อ้างอิง",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
