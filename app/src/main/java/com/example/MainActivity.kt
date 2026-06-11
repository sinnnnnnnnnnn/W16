package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.SeaStatusViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.QueryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: SeaStatusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val stations by viewModel.stations.collectAsStateWithLifecycle()
                val selectedStationId by viewModel.selectedStationId.collectAsStateWithLifecycle()
                val selectedStation by viewModel.selectedStation.collectAsStateWithLifecycle()
                val historyReadings by viewModel.selectedHistory.collectAsStateWithLifecycle()
                val alertsLog by viewModel.alertsLog.collectAsStateWithLifecycle()

                var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == ScreenTab.DASHBOARD,
                                onClick = { currentTab = ScreenTab.DASHBOARD },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Dashboard,
                                        contentDescription = "首頁",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("觀報首頁") },
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )

                            NavigationBarItem(
                                selected = currentTab == ScreenTab.MAP,
                                onClick = { currentTab = ScreenTab.MAP },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "地圖",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("觀測地圖") },
                                modifier = Modifier.testTag("nav_tab_map")
                            )

                            NavigationBarItem(
                                selected = currentTab == ScreenTab.QUERY,
                                onClick = { currentTab = ScreenTab.QUERY },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "查詢",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("測站查詢") },
                                modifier = Modifier.testTag("nav_tab_query")
                            )

                            NavigationBarItem(
                                selected = currentTab == ScreenTab.HISTORY,
                                onClick = { currentTab = ScreenTab.HISTORY },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "歷史",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = { Text("24H歷史圖") },
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            ScreenTab.DASHBOARD -> {
                                DashboardScreen(
                                    station = selectedStation,
                                    alerts = alertsLog,
                                    onRefresh = { viewModel.refreshData() }
                                )
                            }
                            ScreenTab.MAP -> {
                                MapScreen(
                                    stations = stations,
                                    selectedStationId = selectedStationId,
                                    onStationSelect = { viewModel.selectStation(it) }
                                )
                            }
                            ScreenTab.QUERY -> {
                                QueryScreen(
                                    stations = stations,
                                    selectedStationId = selectedStationId,
                                    onStationSelect = { viewModel.selectStation(it) },
                                    onRefresh = { viewModel.refreshData() }
                                )
                            }
                            ScreenTab.HISTORY -> {
                                HistoryScreen(
                                    station = selectedStation,
                                    history = historyReadings
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ScreenTab {
    DASHBOARD, MAP, QUERY, HISTORY
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name! Welcome to SeaStatus.", modifier = modifier)
}
