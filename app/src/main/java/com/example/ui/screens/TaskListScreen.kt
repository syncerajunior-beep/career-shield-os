package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.TaskEntity
import com.example.util.SitePhotoStorage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(
    allTasks: List<TaskEntity>,
    filteredTasks: List<TaskEntity>,
    selectedCategory: String,
    searchQuery: String,
    onSelectCategory: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleTask: (TaskEntity) -> Unit,
    onAddTask: (title: String, description: String, category: String, timeSlot: String, photoUri: String?) -> Unit,
    onEditTask: (task: TaskEntity, title: String, description: String, category: String, timeSlot: String, photoUri: String?) -> Unit,
    onUpdateTaskPhoto: (task: TaskEntity, photoUri: String?) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onResetDay: () -> Unit,
    onAnalyzeRedFlagWithAi: (String) -> Unit,
    onInspectPhotoWithAi: (photoUri: String, taskTitle: String) -> Unit,
    onRunDailyLogSummary: () -> Unit,
    onRunAntiBsCheck: () -> Unit
) {
    val context = LocalContext.current

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }
    var previewTask by remember { mutableStateOf<TaskEntity?>(null) }

    // Active photo capture state for existing task cards
    var targetTaskForPhoto by remember { mutableStateOf<TaskEntity?>(null) }
    var pendingCaptureUri by remember { mutableStateOf<Uri?>(null) }

    // Active photo capture state for Add/Edit Task dialog
    var dialogPhotoUri by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogPendingCaptureUri by remember { mutableStateOf<Uri?>(null) }
    var captureTargetMode by remember { mutableStateOf<String>("NONE") } // "CARD" or "DIALOG"

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            if (captureTargetMode == "CARD") {
                val task = targetTaskForPhoto
                val uriStr = pendingCaptureUri?.toString()
                if (task != null && !uriStr.isNullOrBlank()) {
                    SitePhotoStorage.deletePhotoFileIfLocal(context, task.photoUri)
                    onUpdateTaskPhoto(task, uriStr)
                    Toast.makeText(
                        context,
                        "บันทึกรูปถ่ายจุดเสี่ยงหน้างานลง Room DB แล้ว!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (captureTargetMode == "DIALOG") {
                val uriStr = dialogPendingCaptureUri?.toString()
                if (!uriStr.isNullOrBlank()) {
                    dialogPhotoUri = uriStr
                    Toast.makeText(
                        context,
                        "ถ่ายรูปหลักฐานหน้างานสำเร็จ!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            if (captureTargetMode == "CARD") {
                SitePhotoStorage.deletePhotoFileIfLocal(context, pendingCaptureUri?.toString())
            } else if (captureTargetMode == "DIALOG") {
                SitePhotoStorage.deletePhotoFileIfLocal(context, dialogPendingCaptureUri?.toString())
            }
        }
        pendingCaptureUri = null
        dialogPendingCaptureUri = null
        targetTaskForPhoto = null
        captureTargetMode = "NONE"
    }

    val launchCameraForCard: (TaskEntity) -> Unit = { task ->
        val uri = SitePhotoStorage.createSitePhotoUri(context)
        targetTaskForPhoto = task
        pendingCaptureUri = uri
        captureTargetMode = "CARD"
        takePictureLauncher.launch(uri)
    }

    val launchCameraForDialog: () -> Unit = {
        val uri = SitePhotoStorage.createSitePhotoUri(context)
        dialogPendingCaptureUri = uri
        captureTargetMode = "DIALOG"
        takePictureLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            when (captureTargetMode) {
                "CARD" -> targetTaskForPhoto?.let { launchCameraForCard(it) }
                "DIALOG" -> launchCameraForDialog()
            }
        } else {
            Toast.makeText(
                context,
                "กรุณาอนุญาตการใช้กล้องเพื่อถ่ายรูปหลักฐานจุดเสี่ยงหน้างาน",
                Toast.LENGTH_LONG
            ).show()
            targetTaskForPhoto = null
            captureTargetMode = "NONE"
        }
    }

    val requestCameraForTask: (TaskEntity) -> Unit = { task ->
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            launchCameraForCard(task)
        } else {
            targetTaskForPhoto = task
            captureTargetMode = "CARD"
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val requestCameraForDialog: () -> Unit = {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission) {
            launchCameraForDialog()
        } else {
            captureTargetMode = "DIALOG"
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        if (pickedUri != null) {
            val savedUri = SitePhotoStorage.persistPickedPhoto(context, pickedUri)
            if (savedUri != null) {
                if (captureTargetMode == "CARD") {
                    val task = targetTaskForPhoto
                    if (task != null) {
                        SitePhotoStorage.deletePhotoFileIfLocal(context, task.photoUri)
                        onUpdateTaskPhoto(task, savedUri)
                        Toast.makeText(
                            context,
                            "แนบรูปหลักฐานหน้างานลง Room DB สำเร็จ!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else if (captureTargetMode == "DIALOG") {
                    dialogPhotoUri = savedUri
                }
            } else {
                Toast.makeText(context, "ไม่สามารถบันทึกรูปภาพได้", Toast.LENGTH_SHORT).show()
            }
        }
        targetTaskForPhoto = null
        captureTargetMode = "NONE"
    }

    val completedCount = remember(allTasks) { allTasks.count { it.isCompleted } }
    val photoEvidenceCount = remember(allTasks) { allTasks.count { !it.photoUri.isNullOrBlank() } }
    val totalCount = allTasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("task_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Hero Command Status Banner (Room DB Native Status)
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_construction_hero_1794457161643),
                            contentDescription = "ภาพพื้นหลังแดชบอร์ดควบคุมงานก่อสร้าง",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(156.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(156.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x99030712),
                                            Color(0xEB0A0F1D)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF0A0F1D).copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.border(
                                        1.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(20.dp)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = "สถานะฐานข้อมูล Room SQLite",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Room DB: เก็บข้อมูล & ภาพหน้างานในเครื่อง",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onResetDay,
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("reset_day_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "รีเซ็ตเช็กลิสต์ประจำวัน",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.reset_day_button),
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "ความคืบหน้าการป้องกันความเสี่ยงวันนี้",
                                        fontSize = 12.sp,
                                        color = Color(0xFFD1D5DB)
                                    )
                                    Text(
                                        text = "สำเร็จแล้ว $completedCount / $totalCount ภารกิจ • รูปหลักฐาน $photoEvidenceCount ภาพ",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (progress >= 1f) Color(0xFF00E676) else MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            // 1.5 Hybrid AI Quick Operations Strip (On-Device Gemini Nano 4 + Cloud Fallback)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRunDailyLogSummary,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_daily_log_summary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "สรุป Daily Remind Log ด้วย Hybrid AI",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "สรุป Log ประจำวัน",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676)
                        )
                    }

                    OutlinedButton(
                        onClick = onRunAntiBsCheck,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("quick_anti_bs_check_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = "ตรวจงานซ้ำซ้อน Anti-BS ด้วย Hybrid AI",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ตรวจ Anti-BS ซ้ำซ้อน",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                    }
                }
            }

            // 2. 3 จุดตายคอขาดบาดตาย (Critical Survival Red Flags)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                        .testTag("red_flags_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = "คำเตือนจุดตายหน้างาน",
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.red_flags_title),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                            }
                            Text(
                                text = "แตะเพื่อปรึกษา AI",
                                fontSize = 10.sp,
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        RedFlagRow(
                            text = stringResource(R.string.red_flag_1),
                            tag = "red_flag_1_button",
                            onClick = {
                                onAnalyzeRedFlagWithAi(
                                    "ผู้รับเหมาช่วงส่งสัญญาณทิ้งงาน จำนวนช่างลดลงต่อเนื่องเกิน 3 วัน และงานล่าช้ากว่าแผนหลัก ต้องล็อกสัญญาและตัดจ่ายเงินอย่างไรไม่ให้โครงการเสียหาย?"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RedFlagRow(
                            text = stringResource(R.string.red_flag_2),
                            tag = "red_flag_2_button",
                            onClick = {
                                onAnalyzeRedFlagWithAi(
                                    "ผู้ว่าจ้างหรือตัวแทนสั่งแก้งานหน้างานปากเปล่าเร่งด่วนโดยยังไม่มีเอกสาร VO และไม่มีลายเซ็นอนุมัติงบ/เวลาเพิ่ม ต้องรับมือและออกหนังสืออย่างไร?"
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        RedFlagRow(
                            text = stringResource(R.string.red_flag_3),
                            tag = "red_flag_3_button",
                            onClick = {
                                onAnalyzeRedFlagWithAi(
                                    "โครงการเสี่ยงโดนค่าปรับล่าช้า (LD) วันละ 150,000 บาท เนื่องจากส่งมอบพื้นที่ล่าช้าและฝนตกหนัก ต้องทำหนังสือสงวนสิทธิ์ขอขยายเวลา (EOT) และเก็บหลักฐานอย่างไร?"
                                )
                            }
                        )
                    }
                }
            }

            // 3. Search Bar & Category Filter Section
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.daily_checkpoints_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "พบ ${filteredTasks.size} รายการ",
                            fontSize = 11.sp,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "ค้นหาชื่อภารกิจ หรือรายละเอียด (เช่น เทปูน, สต็อก, Cashflow)...",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "ค้นหาภารกิจ",
                                tint = Color(0xFF00E5FF)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "ล้างคำค้นหา",
                                        tint = Color(0xFFD1D5DB)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF111827),
                            unfocusedContainerColor = Color(0xFF111827),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF374151)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_search_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(
                        "ALL" to "ทั้งหมด",
                        "SAFETY" to "SAFETY (สต็อก/หน้างาน)",
                        "SLA" to "SLA (ล็อกเวลา)",
                        "FINANCE" to "FINANCE (การเงิน)",
                        "CONTRACT" to "CONTRACT (สัญญา)"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { (key, label) ->
                            val isSelected = selectedCategory == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectCategory(key) },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(0xFF00E5FF),
                                    containerColor = Color(0xFF111827),
                                    labelColor = Color(0xFFD1D5DB)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color(0xFF374151),
                                    selectedBorderColor = Color(0xFF00E5FF)
                                ),
                                modifier = Modifier.testTag("filter_chip_${key.lowercase()}")
                            )
                        }
                    }
                }
            }

            // 4. Room DB Task Items
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF374151), RoundedCornerShape(12.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "ไม่พบภารกิจที่ตรงกับเงื่อนไข",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategory != "ALL") {
                                    "ไม่พบภารกิจที่ตรงกับการค้นหาหรือหมวดหมู่ที่เลือก"
                                } else {
                                    "ยังไม่มีภารกิจในระบบ"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "ลองเปลี่ยนคำค้นหา หมวดหมู่ หรือกดปุ่ม '+ เพิ่มงานใหม่' ด้านล่าง",
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                            if (searchQuery.isNotBlank() || selectedCategory != "ALL") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        onSelectCategory("ALL")
                                    },
                                    modifier = Modifier.testTag("reset_filters_button")
                                ) {
                                    Text("แสดงภารกิจทั้งหมด", color = Color(0xFF00E5FF), fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredTasks,
                    key = { it.id }
                ) { task ->
                    TaskItemCard(
                        task = task,
                        onToggle = { onToggleTask(task) },
                        onCapturePhoto = { requestCameraForTask(task) },
                        onPickPhoto = {
                            targetTaskForPhoto = task
                            captureTargetMode = "CARD"
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onViewPhoto = { previewTask = task },
                        onInspectPhotoWithAi = {
                            task.photoUri?.let { uri -> onInspectPhotoWithAi(uri, task.title) }
                        },
                        onRemovePhoto = {
                            SitePhotoStorage.deletePhotoFileIfLocal(context, task.photoUri)
                            onUpdateTaskPhoto(task, null)
                            Toast.makeText(context, "ลบรูปหลักฐานออกจากภารกิจแล้ว", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = {
                            dialogPhotoUri = task.photoUri
                            editingTask = task
                        },
                        onDelete = {
                            SitePhotoStorage.deletePhotoFileIfLocal(context, task.photoUri)
                            onDeleteTask(task)
                        }
                    )
                }
            }
        }

        // FAB for adding a new Task into Room DB
        FloatingActionButton(
            onClick = {
                dialogPhotoUri = null
                showAddDialog = true
            },
            containerColor = Color(0xFF00E5FF),
            contentColor = Color(0xFF030712),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_task_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task_button)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.add_task_button),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            dialogTitle = "เพิ่มภารกิจใหม่ลง Room DB",
            confirmButtonText = "บันทึกลงเครื่อง",
            initialTitle = "",
            initialDescription = "",
            initialCategory = "SAFETY",
            initialTimeSlot = "09:00",
            photoUri = dialogPhotoUri,
            onCapturePhoto = requestCameraForDialog,
            onPickPhoto = {
                captureTargetMode = "DIALOG"
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                SitePhotoStorage.deletePhotoFileIfLocal(context, dialogPhotoUri)
                dialogPhotoUri = null
            },
            titleInputTag = "new_task_title_input",
            descInputTag = "new_task_desc_input",
            confirmButtonTag = "confirm_add_task_button",
            cancelButtonTag = "cancel_add_task_button",
            onDismiss = {
                dialogPhotoUri = null
                showAddDialog = false
            },
            onConfirm = { title, description, category, timeSlot ->
                onAddTask(title, description, category, timeSlot, dialogPhotoUri)
                dialogPhotoUri = null
                showAddDialog = false
            }
        )
    }

    editingTask?.let { taskToEdit ->
        val rawTitleWithoutTime = remember(taskToEdit.title) {
            taskToEdit.title.replace(Regex("^\\d{2}:\\d{2}\\s*"), "")
        }
        TaskFormDialog(
            dialogTitle = "แก้ไขภารกิจใน Room DB",
            confirmButtonText = "บันทึกการแก้ไข",
            initialTitle = rawTitleWithoutTime,
            initialDescription = taskToEdit.description,
            initialCategory = taskToEdit.category,
            initialTimeSlot = taskToEdit.timeSlot,
            photoUri = dialogPhotoUri,
            onCapturePhoto = requestCameraForDialog,
            onPickPhoto = {
                captureTargetMode = "DIALOG"
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                dialogPhotoUri = null
            },
            titleInputTag = "edit_task_title_input",
            descInputTag = "edit_task_desc_input",
            confirmButtonTag = "confirm_edit_task_button",
            cancelButtonTag = "cancel_edit_task_button",
            onDismiss = {
                dialogPhotoUri = null
                editingTask = null
            },
            onConfirm = { newTitle, newDesc, newCategory, newTimeSlot ->
                if (taskToEdit.photoUri != null && taskToEdit.photoUri != dialogPhotoUri) {
                    SitePhotoStorage.deletePhotoFileIfLocal(context, taskToEdit.photoUri)
                }
                onEditTask(taskToEdit, newTitle, newDesc, newCategory, newTimeSlot, dialogPhotoUri)
                dialogPhotoUri = null
                editingTask = null
            }
        )
    }

    previewTask?.let { taskWithPhoto ->
        if (!taskWithPhoto.photoUri.isNullOrBlank()) {
            SitePhotoPreviewDialog(
                task = taskWithPhoto,
                onDismiss = { previewTask = null },
                onRetakePhoto = {
                    previewTask = null
                    requestCameraForTask(taskWithPhoto)
                },
                onDeletePhoto = {
                    SitePhotoStorage.deletePhotoFileIfLocal(context, taskWithPhoto.photoUri)
                    onUpdateTaskPhoto(taskWithPhoto, null)
                    previewTask = null
                    Toast.makeText(context, "ลบรูปหลักฐานหน้างานแล้ว", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun RedFlagRow(
    text: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B).copy(alpha = 0.65f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "วิเคราะห์จุดตายนี้ด้วย Gemini AI",
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskItemCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onCapturePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onViewPhoto: () -> Unit,
    onInspectPhotoWithAi: () -> Unit,
    onRemovePhoto: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val badgeColor = when (task.category.uppercase()) {
        "SAFETY" -> Color(0xFF00E676)
        "SLA" -> Color(0xFFFFB300)
        "FINANCE" -> Color(0xFF00E5FF)
        else -> Color(0xFFFF5252)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Color(0xFF0B111E) else Color(0xFF111827)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (task.isCompleted) Color(0xFF1F2937) else Color(0xFF374151),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onToggle)
            .testTag("task_item_card_${task.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF00E5FF),
                        uncheckedColor = Color.White,
                        checkmarkColor = Color(0xFF030712)
                    ),
                    modifier = Modifier.testTag("task_checkbox_${task.id}")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.border(
                                0.5.dp,
                                badgeColor.copy(alpha = 0.6f),
                                RoundedCornerShape(6.dp)
                            )
                        ) {
                            Text(
                                text = task.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (task.isCompleted) {
                            Text(
                                text = "✓ บันทึกสำเร็จใน Room DB",
                                fontSize = 10.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.title,
                        fontSize = 13.sp,
                        color = if (task.isCompleted) Color(0xFF9CA3AF) else Color.White,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    AnimatedVisibility(visible = task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            fontSize = 11.sp,
                            color = if (task.isCompleted) Color(0xFF6B7280) else Color(0xFFD1D5DB),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("edit_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "แก้ไขภารกิจ ${task.title}",
                            tint = Color(0xFF00E5FF)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_task_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ลบภารกิจ ${task.title}",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Site Risk Photo Evidence Section
            if (!task.photoUri.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF00E676).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewPhoto)
                        .testTag("task_photo_preview_${task.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = Uri.parse(task.photoUri),
                            contentDescription = "ภาพถ่ายหลักฐานความเสี่ยงหน้างานของ ${task.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "บันทึกหลักฐานรูปถ่ายแล้ว",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "บันทึกภาพถ่ายหน้างานลง Room DB แล้ว",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                            }
                            Text(
                                text = "แตะเพื่อดูภาพขนาดเต็ม หรือถ่ายใหม่",
                                fontSize = 10.sp,
                                color = Color(0xFF9CA3AF),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .border(0.5.dp, Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                                        .clickable(onClick = onInspectPhotoWithAi)
                                        .testTag("inspect_photo_ai_button_${task.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "สแกนภาพจุดเสี่ยงด้วย Cloud Gemini",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("สแกนรูป AI", fontSize = 10.sp, color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable(onClick = onCapturePhoto)
                                        .testTag("retake_photo_button_${task.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "ถ่ายรูปใหม่",
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ถ่ายใหม่", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    color = Color(0xFF3B1219),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .clickable(onClick = onRemovePhoto)
                                        .testTag("remove_photo_button_${task.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "ลบรูปหลักฐาน",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ลบรูป", fontSize = 10.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCapturePhoto,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("capture_photo_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "ถ่ายรูปจุดเสี่ยงหน้างาน",
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ถ่ายรูปจุดเสี่ยงหน้างาน",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF)
                        )
                    }

                    OutlinedButton(
                        onClick = onPickPhoto,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("pick_photo_button_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "เลือกรูปจากคลัง",
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "คลังภาพ",
                            fontSize = 11.sp,
                            color = Color(0xFFD1D5DB)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SitePhotoPreviewDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onRetakePhoto: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📷 หลักฐานภาพถ่ายจุดเสี่ยงหน้างาน",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ปิดหน้าต่างภาพถ่าย",
                            tint = Color.White
                        )
                    }
                }

                Text(
                    text = task.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                AsyncImage(
                    model = Uri.parse(task.photoUri),
                    contentDescription = "ภาพถ่ายหลักฐานเต็มจอ",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 380.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF030712))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRetakePhoto,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E5FF),
                            contentColor = Color(0xFF030712)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "ถ่ายรูปใหม่",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ถ่ายรูปใหม่", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDeletePhoto,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "ลบรูปภาพ",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ลบรูปภาพ", fontSize = 12.sp, color = Color(0xFFFF5252))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskFormDialog(
    dialogTitle: String,
    confirmButtonText: String,
    initialTitle: String,
    initialDescription: String,
    initialCategory: String,
    initialTimeSlot: String,
    photoUri: String?,
    onCapturePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    titleInputTag: String,
    descInputTag: String,
    confirmButtonTag: String,
    cancelButtonTag: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, timeSlot: String) -> Unit
) {
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }
    var description by rememberSaveable(initialDescription) { mutableStateOf(initialDescription) }
    var category by rememberSaveable(initialCategory) { mutableStateOf(initialCategory) }
    var timeSlot by rememberSaveable(initialTimeSlot) { mutableStateOf(initialTimeSlot) }

    val categories = listOf("SAFETY", "SLA", "FINANCE", "CONTRACT")
    val timePresets = listOf("08:30", "10:00", "11:30", "13:30", "15:00", "16:30", "17:30")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F172A),
        titleContentColor = Color.White,
        textContentColor = Color.White,
        title = {
            Text(
                text = dialogTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF00E5FF)
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "เลือกเวลาแจ้งเตือนหน้างาน:",
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePresets.forEach { preset ->
                        val selected = timeSlot == preset
                        Surface(
                            color = if (selected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable { timeSlot = preset }
                                .testTag("dialog_time_chip_$preset")
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color(0xFF030712) else Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "หมวดหมู่ความเสี่ยง:",
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category.equals(cat, ignoreCase = true)
                        Surface(
                            color = if (selected) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable { category = cat }
                                .testTag("dialog_category_chip_${cat.lowercase()}")
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
                    label = { Text("ชื่อภารกิจเช็กลิสต์", color = Color(0xFF9CA3AF)) },
                    placeholder = { Text("เช่น ตรวจระดับหัวเสาเข็มก่อนเทฟุตติ้ง", color = Color(0xFF6B7280)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(titleInputTag)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("รายละเอียดการป้องกันปัญหา", color = Color(0xFF9CA3AF)) },
                    placeholder = { Text("เช่น ถ่ายรูป Timestamp Camera พร้อมให้โฟร์แมนเซ็นรับทราบ", color = Color(0xFF6B7280)) },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00E5FF),
                        unfocusedBorderColor = Color(0xFF374151)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(descInputTag)
                )

                // Optional Site Photo Attachment inside Dialog
                Text(
                    text = "รูปถ่ายหลักฐานความเสี่ยงหน้างาน (บันทึกลง Room DB):",
                    fontSize = 12.sp,
                    color = Color(0xFFD1D5DB)
                )

                if (!photoUri.isNullOrBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = Uri.parse(photoUri),
                            contentDescription = "รูปถ่ายจุดเสี่ยงที่จะบันทึก",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "พร้อมบันทึกรูปลง Room DB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = onCapturePhoto,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("ถ่ายใหม่", fontSize = 11.sp, color = Color(0xFF00E5FF))
                                }
                                TextButton(
                                    onClick = onRemovePhoto,
                                    contentPadding = PaddingValues(horizontal = 4.dp)
                                ) {
                                    Text("ลบรูป", fontSize = 11.sp, color = Color(0xFFFF5252))
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCapturePhoto,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_capture_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "ถ่ายรูปจุดเสี่ยง",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ถ่ายรูป", fontSize = 11.sp, color = Color(0xFF00E5FF))
                        }
                        OutlinedButton(
                            onClick = onPickPhoto,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dialog_pick_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "เลือกจากคลัง",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("คลังภาพ", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, description, category, timeSlot)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color(0xFF030712)
                ),
                modifier = Modifier.testTag(confirmButtonTag)
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(cancelButtonTag)
            ) {
                Text("ยกเลิก", color = Color(0xFFD1D5DB))
            }
        }
    )
}
