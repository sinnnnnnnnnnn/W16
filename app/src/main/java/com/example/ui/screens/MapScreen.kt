package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AlertLevel
import com.example.models.SeaStation
import com.example.ui.theme.*

@Composable
fun MapScreen(
    stations: List<SeaStation>,
    selectedStationId: String,
    onStationSelect: (String) -> Unit
) {
    val selectedStation = stations.find { it.id == selectedStationId } ?: stations.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MarineBackground)
    ) {
        // High-fidelity Nautical Coordinate Map Background (Canvas-based)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(stations) {
                    detectTapGestures { offset ->
                        // Match taps to close station markers
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        
                        // Map coordinates (lat: 20 -> 26, lng: 116 -> 123)
                        val minLat = 20.0
                        val maxLat = 26.5
                        val minLng = 116.0
                        val maxLng = 123.0

                        for (st in stations) {
                            // Find percentage on screen
                            val xPct = (st.lng - minLng) / (maxLng - minLng)
                            val yPct = 1.0 - ((st.lat - minLat) / (maxLat - minLat))

                            val x = (xPct * canvasWidth).toFloat()
                            val y = (yPct * canvasHeight).toFloat()

                            // Hitbox threshold radius (48dp target equivalent)
                            val distance = kotlin.math.hypot(offset.x - x, offset.y - y)
                            if (distance < 50f) {
                                onStationSelect(st.id)
                                break
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw latitude & longitude grid lines (海圖經緯度網格)
            val gridColor = Color(0xFF1E293B).copy(alpha = 0.5f)
            val latLines = 6
            val lngLines = 8

            // Horizontal latitude lines
            for (i in 0..latLines) {
                val y = height * i / latLines
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1f
                )
            }

            // Vertical longitude lines
            for (i in 0..lngLines) {
                val x = width * i / lngLines
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1f
                )
            }

            // 2. Beautifulized Taiwan Island Abstract Vector silhouette (海圖抽象台灣陸地)
            val islandPath = Path().apply {
                // Starting north point near Keelung
                moveTo(width * 0.70f, height * 0.25f)
                // East coast Swells
                quadraticTo(width * 0.76f, height * 0.40f, width * 0.72f, height * 0.55f)
                lineTo(width * 0.61f, height * 0.75f) // Kenting South tip
                // West Coast
                quadraticTo(width * 0.53f, height * 0.55f, width * 0.58f, height * 0.35f)
                close()
            }
            
            // Draw island body with premium marine dark green/gray
            drawPath(
                path = islandPath,
                color = Color(0xFF1E293B).copy(alpha = 0.8f)
            )
            drawPath(
                path = islandPath,
                color = Color(0xFF334155).copy(alpha = 0.6f),
                style = Stroke(width = 3f)
            )

            // 3. Compass Rose indicators
            drawCircle(
                color = Color(0xFF334155).copy(alpha = 0.4f),
                radius = 120f,
                center = Offset(width * 0.25f, height * 0.25f),
                style = Stroke(width = 2f)
            )
            drawLine(
                color = Color(0xFF475569).copy(alpha = 0.5f),
                start = Offset(width * 0.25f - 150f, height * 0.25f),
                end = Offset(width * 0.25f + 150f, height * 0.25f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0xFF475569).copy(alpha = 0.5f),
                start = Offset(width * 0.25f, height * 0.25f - 150f),
                end = Offset(width * 0.25f, height * 0.25f + 150f),
                strokeWidth = 1.5f
            )

            // 4. Highlighted Sea stations nodes with glowing sonar indicators (Marker)
            val minLat = 20.0
            val maxLat = 26.5
            val minLng = 116.0
            val maxLng = 123.0

            stations.forEach { st ->
                val xPct = (st.lng - minLng) / (maxLng - minLng)
                val yPct = 1.0 - ((st.lat - minLat) / (maxLat - minLat))

                val markerX = (xPct * width).toFloat()
                val markerY = (yPct * height).toFloat()

                val isSelected = st.id == selectedStationId
                val baseColor = when (st.alertLevel) {
                    AlertLevel.SAFE -> SeaAlertSafe
                    AlertLevel.CAUTION -> SeaAlertCaution
                    AlertLevel.DANGER -> SeaAlertDanger
                }

                // If selected, draw expanding wave ripple ring (模擬海浪聲納)
                if (isSelected) {
                    drawCircle(
                        color = baseColor.copy(alpha = 0.25f),
                        radius = 45f,
                        center = Offset(markerX, markerY)
                    )
                    drawCircle(
                        color = baseColor.copy(alpha = 0.6f),
                        radius = 25f,
                        center = Offset(markerX, markerY),
                        style = Stroke(width = 3f)
                    )
                }

                // Center node core
                drawCircle(
                    color = baseColor,
                    radius = if (isSelected) 14f else 9f,
                    center = Offset(markerX, markerY)
                )

                // White outline
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 15f else 10f,
                    center = Offset(markerX, markerY),
                    style = Stroke(width = 3f)
                )
            }
        }

        // Compass orientation text
        Text(
            text = "N ↑\n航海圖資訊",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        )

        // Floating Header Overlay Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MarineCardBackground.copy(alpha = 0.85f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = "海圖港口",
                    tint = MarinePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "海洋雷達觀測地圖 (台灣近海)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "點擊標記 (Marker) 切換詳細觀測站海況資料",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Detailed floating overlay panel describing tapped station ("點擊 Marker 顯示詳細資料")
        AnimatedVisibility(
            visible = selectedStation != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            selectedStation?.let { st ->
                val levelColor = when (st.alertLevel) {
                    AlertLevel.SAFE -> SeaAlertSafe
                    AlertLevel.CAUTION -> SeaAlertCaution
                    AlertLevel.DANGER -> SeaAlertDanger
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("station_marker_dialog"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MarineCardBackground),
                    elevation = CardDefaults.cardElevation(8.dp),
                    border = BorderStroke(1.5.dp, levelColor.copy(alpha = 0.8f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = st.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "經緯度：%.2f°N , %.2f°E".format(st.lat, st.lng),
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = levelColor.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, levelColor),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = st.alertLevel.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        // 3 Key Sea variables
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MapDetailItem(
                                icon = Icons.Default.Waves,
                                label = "觀測波高",
                                value = "%.1f m".format(st.waveHeight),
                                accentColor = if (st.waveHeight >= 3.0) SeaAlertDanger else MarinePrimary
                            )
                            MapDetailItem(
                                icon = Icons.Default.Air,
                                label = "瞬間風速",
                                value = "%.1f m/s".format(st.windSpeed),
                                accentColor = if (st.windSpeed >= 15.0) SeaAlertDanger else MarineSecondary
                            )
                            MapDetailItem(
                                icon = Icons.Default.Thermostat,
                                label = "表層水溫",
                                value = "%.1f °C".format(st.seaTemp),
                                accentColor = SeaAlertDanger
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
