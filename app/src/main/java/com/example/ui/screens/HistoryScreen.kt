package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.HistoryReading
import com.example.models.SeaStation
import com.example.ui.theme.*

@Composable
fun HistoryScreen(
    station: SeaStation?,
    history: List<HistoryReading>
) {
    if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    var chartType by remember { mutableStateOf(0) } // 0 = Wave Height, 1 = Wind Speed
    var selectedIndex by remember { mutableStateOf(-1) } // Tap interactive tooltip

    val valuesList = history.map {
        if (chartType == 0) it.waveHeight else it.windSpeed
    }

    // Historical statistics Calculations
    val maxVal = valuesList.maxOrNull() ?: 0.0
    val minVal = valuesList.minOrNull() ?: 0.0
    val avgVal = if (valuesList.isNotEmpty()) valuesList.average() else 0.0
    val unitStr = if (chartType == 0) "米 (m)" else "m/s"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Station Title and Time Window Label
        Column {
            Text(
                text = "${station.name} 近24小時歷史趨勢圖",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "氣象觀測及湧浪數據紀錄 (歷史資料頁面)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }

        // Toggle Switch for Chart metrics Types
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { 
                    chartType = 0
                    selectedIndex = -1
                },
                modifier = Modifier.weight(1f).testTag("wave_history_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (chartType == 0) OceanPrimary else MaterialTheme.colorScheme.surface,
                    contentColor = if (chartType == 0) Color.White else MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("24H 波高紀錄", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { 
                    chartType = 1
                    selectedIndex = -1
                },
                modifier = Modifier.weight(1f).testTag("wind_history_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (chartType == 1) OceanSecondary else MaterialTheme.colorScheme.surface,
                    contentColor = if (chartType == 1) Color.White else MaterialTheme.colorScheme.onBackground
                ),
                shape = RoundedCornerShape(10.dp),
                elevation = ButtonDefaults.buttonElevation(1.dp)
            ) {
                Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("24H 風速監測", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bespoke High-Contrast Interactive Canvas Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .testTag("history_canvas_chart_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("載入歷史海象數據中...", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 20.dp)
                ) {
                    val primaryColor = if (chartType == 0) OceanPrimary else OceanSecondary
                    val secondaryColor = if (chartType == 0) WaveThemeLightColor else WindThemeLightColor

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(history) {
                                detectTapGestures { offset ->
                                    val pointsCount = history.size
                                    val width = size.width
                                    val sectionWidth = width / (pointsCount - 1)
                                    val index = ((offset.x + (sectionWidth / 2)) / sectionWidth).toInt()
                                    if (index in 0 until pointsCount) {
                                        selectedIndex = index
                                    }
                                }
                            }
                    ) {
                        val width = size.width
                        val height = size.height

                        // Calculate bounds for drawing y-coordinates
                        val minBound = 0.0
                        val maxBound = if (chartType == 0) 5.0 else 24.0 // 5m max wave, 24m/s max wind
                        val deltaVal = maxBound - minBound

                        // 1. Horizontal Reference Line labels
                        val gridLinesCount = 4
                        for (i in 0..gridLinesCount) {
                            val ratio = i.toFloat() / gridLinesCount
                            val y = height * (1.0f - ratio)
                            val gridVal = minBound + (ratio * deltaVal)

                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // 2. Generate point offsets
                        val pointsCount = history.size
                        val sectionWidth = width / (pointsCount - 1)
                        val pointsList = mutableListOf<Offset>()

                        for (i in 0 until pointsCount) {
                            val rawValue = if (chartType == 0) history[i].waveHeight else history[i].windSpeed
                            val ratio = ((rawValue - minBound) / deltaVal).toFloat().coerceIn(0f, 1f)
                            val py = height * (1.0f - ratio)
                            val px = i * sectionWidth
                            pointsList.add(Offset(px, py))
                        }

                        // 3. Draw gradient background shading under the chart line curves
                        val fillPath = Path().apply {
                            moveTo(0f, height)
                            pointsList.forEach { lineTo(it.x, it.y) }
                            lineTo(width, height)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // 4. Draw smooth continuous spline line curves
                        val linePath = Path().apply {
                            if (pointsList.isNotEmpty()) {
                                moveTo(pointsList[0].x, pointsList[0].y)
                                for (i in 1 until pointsList.size) {
                                    // quadratic curves for organic waveform look
                                    val prevP = pointsList[i - 1]
                                    val curP = pointsList[i]
                                    val midX = (prevP.x + curP.x) / 2
                                    quadraticTo(prevP.x, prevP.y, midX, (prevP.y + curP.y) / 2)
                                }
                                lineTo(pointsList.last().x, pointsList.last().y)
                            }
                        }

                        drawPath(
                            path = linePath,
                            color = primaryColor,
                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                        )

                        // 5. If point is selected, draw vertical indicators line + glow dot
                        if (selectedIndex in history.indices) {
                            val selectP = pointsList[selectedIndex]
                            drawLine(
                                color = primaryColor.copy(alpha = 0.35f),
                                start = Offset(selectP.x, 0f),
                                end = Offset(selectP.x, height),
                                strokeWidth = 1.5f
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 8f,
                                center = selectP
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 4f,
                                center = selectP
                            )
                        }
                    }
                }
            }
        }

        // Tapped point interactive value info card panel (互動提示)
        AnimatedVisibility(
            visible = selectedIndex in history.indices,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (selectedIndex in history.indices) {
                val reading = history[selectedIndex]
                val itemValue = if (chartType == 0) reading.waveHeight else reading.windSpeed
                val statusColor = if (chartType == 0) OceanPrimary else OceanSecondary

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "選定歷史時間：【${reading.timestamp}】",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "即時測得：%.1f %s".format(itemValue, unitStr),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = statusColor
                        )
                    }
                }
            }
        }

        // Summary Performance Board (極值與平均值資訊)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "近 24 小時觀測極值統計",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatSummaryItem(
                        label = "最高讀值 Max",
                        value = "%.1f %s".format(maxVal, unitStr),
                        valueColor = SeaAlertDanger
                    )
                    StatSummaryItem(
                        label = "最低讀值 Min",
                        value = "%.1f %s".format(minVal, unitStr),
                        valueColor = SeaAlertSafe
                    )
                    StatSummaryItem(
                        label = "平均觀估 Avg",
                        value = "%.1f %s".format(avgVal, unitStr),
                        valueColor = OceanPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatSummaryItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// Custom aesthetic colors for graphs
val WaveThemeLightColor = Color(0xFFA5F3FC)
val WindThemeLightColor = Color(0xFFCCFBF1)
