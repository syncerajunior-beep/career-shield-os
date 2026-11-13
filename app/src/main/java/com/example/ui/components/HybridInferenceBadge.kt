package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.HybridInferenceSource
import com.example.viewmodel.LastInferenceInfo

@Composable
fun InferenceSourcePill(
    source: HybridInferenceSource,
    detailLabel: String = "",
    verifiedModelName: String? = null,
    modifier: Modifier = Modifier
) {
    val onDeviceLabel = if (!verifiedModelName.isNullOrBlank()) {
        "[On-Device: $verifiedModelName]"
    } else {
        "[On-Device: AICore]"
    }

    val (bgColor, borderColor, textColor, icon, label) = when (source) {
        HybridInferenceSource.ON_DEVICE -> Quintuple(
            Color(0xFF00E676).copy(alpha = 0.16f),
            Color(0xFF00E676),
            Color(0xFF00E676),
            Icons.Default.Memory,
            onDeviceLabel
        )
        HybridInferenceSource.IN_CLOUD -> Quintuple(
            Color(0xFF00E5FF).copy(alpha = 0.15f),
            Color(0xFF00E5FF),
            Color(0xFF00E5FF),
            Icons.Default.CloudDone,
            "[Cloud Fallback: Gemini]"
        )
        HybridInferenceSource.STANDBY -> Quintuple(
            Color(0xFF1E293B),
            Color(0xFF475569),
            Color(0xFFD1D5DB),
            Icons.Default.Memory,
            "Hybrid: PREFER_ON_DEVICE (Preview)"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.75f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (detailLabel.isNotBlank()) "$label • $detailLabel" else label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HybridStatusStrip(
    lastInference: LastInferenceInfo,
    offlineSiteTestMode: Boolean,
    onDeviceStatusLabel: String,
    onToggleOfflineSiteTestMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (offlineSiteTestMode) Color(0xFF1F160A) else Color(0xFF0D1426),
        border = BorderStroke(
            width = 1.dp,
            color = if (offlineSiteTestMode) Color(0xFFFFB300) else Color(0xFF1E293B)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                InferenceSourcePill(
                    source = lastInference.source,
                    detailLabel = lastInference.featureLabel,
                    verifiedModelName = lastInference.verifiedModelName,
                    modifier = Modifier.testTag("global_inference_source_pill")
                )
                Text(
                    text = if (offlineSiteTestMode) {
                        "🚧 Offline Site (ONLY_ON_DEVICE • PREVIEW) • $onDeviceStatusLabel"
                    } else {
                        "⚡ Hybrid (PREFER_ON_DEVICE • PREVIEW) • $onDeviceStatusLabel"
                    },
                    fontSize = 10.sp,
                    color = if (offlineSiteTestMode) Color(0xFFFFB300) else Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleOfflineSiteTestMode() }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "สลับโหมดทดสอบออฟไลน์หน้างาน",
                    tint = if (offlineSiteTestMode) Color(0xFFFFB300) else Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Offline",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (offlineSiteTestMode) Color(0xFFFFB300) else Color(0xFFD1D5DB)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = offlineSiteTestMode,
                    onCheckedChange = { onToggleOfflineSiteTestMode() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF030712),
                        checkedTrackColor = Color(0xFFFFB300),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.testTag("offline_site_mode_toggle")
                )
            }
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
