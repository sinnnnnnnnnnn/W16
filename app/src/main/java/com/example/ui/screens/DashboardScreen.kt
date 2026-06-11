package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AlertLevel
import com.example.models.SeaStation
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    station: SeaStation?,
    alerts: List<String>,
    onRefresh: () -> Unit
) {
    if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val todayStr = remember {
        val sdf = SimpleDateFormat("yyyy年MM月dd日", Locale.TAIWAN)
        sdf.format(Date())
    }

    // Safety Alert specific color variables
    val alertColor = when (station.alertLevel) {
        AlertLevel.SAFE -> SeaAlertSafe
        AlertLevel.CAUTION -> SeaAlertCaution
        AlertLevel.DANGER -> SeaAlertDanger
    }

    val alertBg = alertColor.copy(alpha = 0.15f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header Status Card
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = todayStr,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = station.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick simulation action button
            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    .testTag("refresh_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "同步重新整理",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Active Warning/Notification Banner (警報警示)
        AnimatedVisibility(visible = alerts.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SeaAlertDanger.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, SeaAlertDanger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "海嘯與風暴警戒",
                        tint = SeaAlertDanger,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "國家級巨浪/強風警戒中",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeaAlertDanger
                        )
                        Text(
                            text = alerts.firstOrNull() ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Central Sea Status Badge Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = alertBg),
            border = BorderStroke(2.dp, alertColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "當前觀報海況等級",
                    fontSize = 14.sp,
                    color = alertColor,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = station.alertLevel.label,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = alertColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = "更新時間",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "觀測更新時間：${station.lastUpdated}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid-based detail sensors cards (4 variables)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wave Height Card (波高超過 3M 顯示紅色警告)
                val waveIsDanger = station.waveHeight >= 3.0
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "即時波高",
                    value = " %.1f 米".format(station.waveHeight),
                    subtitle = if (waveIsDanger) "⚠️ 紅色警示浪高 (>=3m)" else "穩定波浪中",
                    icon = Icons.Default.Waves,
                    iconColor = if (waveIsDanger) SeaAlertDanger else OceanPrimary,
                    bgColor = if (waveIsDanger) SeaAlertDanger.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )

                // Wind Speed Card (風速超過 15 m/s 顯示警告)
                val windIsDanger = station.windSpeed >= 15.0
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "即時風速",
                    value = " %.1f m/s".format(station.windSpeed),
                    subtitle = if (windIsDanger) "⚠️ 強風警示 (>=15)" else "微風徐徐",
                    icon = Icons.Default.Air,
                    iconColor = if (windIsDanger) SeaAlertDanger else OceanSecondary,
                    bgColor = if (windIsDanger) SeaAlertDanger.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wind Direction Card
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "即時風向",
                    value = station.windDirection,
                    subtitle = "風系監測",
                    icon = Icons.Default.Explore,
                    iconColor = OceanTertiary,
                    bgColor = MaterialTheme.colorScheme.surface
                )

                // Sea Temperature Card
                InfoCard(
                    modifier = Modifier.weight(1f),
                    title = "當前海溫",
                    value = " %.1f °C".format(station.seaTemp),
                    subtitle = "海洋表面溫度",
                    icon = Icons.Default.Thermostat,
                    iconColor = SeaAlertDanger,
                    bgColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    bgColor: Color
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = iconColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}
