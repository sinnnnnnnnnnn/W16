package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SeaStatusRepository
import com.example.database.AppDatabase
import com.example.models.HistoryReading
import com.example.models.SeaStation
import com.example.utils.NotificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SeaStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SeaStatusRepository(db.historyDao())

    // All active stations state
    private val _stations = MutableStateFlow<List<SeaStation>>(emptyList())
    val stations: StateFlow<List<SeaStation>> = _stations.asStateFlow()

    // Current selected station state (defaults to the first one, e.g. Keelung)
    private val _selectedStationId = MutableStateFlow("keelung")
    val selectedStationId: StateFlow<String> = _selectedStationId.asStateFlow()

    // Reactive selected station object
    private val _selectedStation = MutableStateFlow<SeaStation?>(null)
    val selectedStation: StateFlow<SeaStation?> = _selectedStation.asStateFlow()

    // Real system alert log (list of generated warning messages)
    private val _alertsLog = MutableStateFlow<List<String>>(emptyList())
    val alertsLog: StateFlow<List<String>> = _alertsLog.asStateFlow()

    init {
        // Prepare notification channels
        NotificationUtils.createNotificationChannel(application)
        
        // Populate static records in database and load starting stations
        viewModelScope.launch {
            repository.populateMockHistory()
            refreshData()
        }
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Set the currently active station
     */
    fun selectStation(stationId: String) {
        _selectedStationId.value = stationId
        _selectedStation.value = repository.getStationById(stationId)
    }

    /**
     * Refresh individual observations values to simulate weather variance (模擬海況更新)
     */
    fun refreshData() {
        val timeStr = getCurrentTime()
        repository.simulateRefresh(timeStr)
        val list = repository.getStations()
        _stations.value = list
        
        // Keep active selection in sync
        val activeSelected = list.find { it.id == _selectedStationId.value } ?: list.firstOrNull()
        _selectedStation.value = activeSelected

        // Check if conditions warrant warnings notifications (警報系統)
        val activeAlerts = mutableListOf<String>()
        list.forEach { station ->
            val context = getApplication<Application>().applicationContext
            NotificationUtils.checkAndSendAlerts(context, station)

            if (station.waveHeight >= 3.0) {
                activeAlerts.add("🚨 [巨浪警報] ${station.name}：波高 %.1f m (已超過 3.0m)".format(station.waveHeight))
            }
            if (station.windSpeed >= 15.0) {
                activeAlerts.add("🚨 [烈風警報] ${station.name}：風速 %.1f m/s (已超過 15.0 m/s)".format(station.windSpeed))
            }
        }
        _alertsLog.value = activeAlerts
    }

    /**
     * Get 24h history for the selected station
     */
    val selectedHistory: StateFlow<List<HistoryReading>> = _selectedStationId
        .flatMapLatest { id ->
            repository.getHistoryForStation(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
