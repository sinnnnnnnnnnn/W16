package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.AlertLevel
import com.example.models.SeaStation
import com.example.ui.theme.*

@Composable
fun QueryScreen(
    stations: List<SeaStation>,
    selectedStationId: String,
    onStationSelect: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredStations = stations.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val activeStation = stations.find { it.id == selectedStationId } ?: stations.firstOrNull()

    // Simulation trigger states for UI Demo
    var waveNotifyActive by remember { mutableStateOf(true) }
    var windNotifyActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("station_search_input"),
            placeholder = { Text("搜尋台灣觀測站 (例如: 蘇澳)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜尋") },
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "觀測站選單 (請選擇觀測站) ：",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            TextButton(onClick = onRefresh) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("即時更新測值", fontSize = 12.sp)
            }
        }

        // Horizontal Grid/List of Stations for Easy Selection
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .testTag("query_station_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredStations) { station ->
                val isSelected = station.id == selectedStationId
                val levelColor = when (station.alertLevel) {
                    AlertLevel.SAFE -> SeaAlertSafe
                    AlertLevel.CAUTION -> SeaAlertCaution
                    AlertLevel.DANGER -> SeaAlertDanger
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStationSelect(station.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.10f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = station.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "最後更新：${station.lastUpdated}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }

                        // Status badge inside lists
                        Surface(
                            color = levelColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = " %.1fm | %s ".format(station.waveHeight, station.alertLevel.label.split(" ").first()),
                                color = levelColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Selected Station Parameters and Setup Alerts Cards Section (海況安全系統)
        activeStation?.let { st ->
            val levelColor = when (st.alertLevel) {
                AlertLevel.SAFE -> SeaAlertSafe
                AlertLevel.CAUTION -> SeaAlertCaution
                AlertLevel.DANGER -> SeaAlertDanger
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "安全警戒參數與預警設定",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Alert limit parameters description (波高>3及風速>15警告)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = SeaAlertDanger.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, SeaAlertDanger.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("巨浪警戒門檻", fontSize = 10.sp, color = SeaAlertDanger, fontWeight = FontWeight.Bold)
                                    Text("波高 ≧ 3.0m", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                color = SeaAlertDanger.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, SeaAlertDanger.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("烈風警戒門檻", fontSize = 10.sp, color = SeaAlertDanger, fontWeight = FontWeight.Bold)
                                    Text("風速 ≧ 15.0m/s", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                }
                            }
                        }
                    }

                    // Toggle notifications setup (發送通知提醒)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("啟用超標巨浪即時通知", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Switch(
                                        checked = waveNotifyActive,
                                        onCheckedChange = { waveNotifyActive = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = OceanPrimary)
                                    )
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Air, contentDescription = null, tint = OceanSecondary, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("啟用強風預警推播提醒", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Switch(
                                        checked = windNotifyActive,
                                        onCheckedChange = { windNotifyActive = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = OceanSecondary)
                                    )
                                }
                            }
                        }
                    }

                    // Quick test notification button (測試警報)
                    item {
                        Button(
                            onClick = {
                                onRefresh()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = levelColor)
                        ) {
                            Text("立即重新整理與多站預演點擊", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
