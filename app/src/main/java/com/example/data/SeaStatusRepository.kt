package com.example.data

import com.example.database.HistoryDao
import com.example.models.HistoryReading
import com.example.models.SeaStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class SeaStatusRepository(private val historyDao: HistoryDao) {

    // Predefined Taiwanese sea observation stations (海洋藍主題觀測站)
    private val baseStations = listOf(
        SeaStation("keelung", "基隆港 (Keelung)", 25.15, 121.75, 1.4, 8.5, "東北風 NE", 24.2, "04:00"),
        SeaStation("suao", "蘇澳 (Suao)", 24.60, 121.88, 3.4, 16.8, "東北東 ENE", 23.5, "04:00"), // Danger (Waves > 3m, Wind > 15m/s)
        SeaStation("hualien", "花蓮 (Hualien)", 24.02, 121.64, 2.2, 11.2, "東風 E", 25.1, "04:00"),     // Caution (Waves >= 2m)
        SeaStation("penghu", "澎湖 (Penghu)", 23.57, 119.56, 1.8, 14.5, "北風 N", 26.3, "04:00"),       // Caution (Wind >= 10m/s)
        SeaStation("fugui", "富貴角 (Fugui)", 25.30, 121.53, 2.5, 12.0, "東北風 NE", 22.8, "04:00"),    // Caution
        SeaStation("liuqiu", "小琉球 (Liuqiu)", 22.34, 120.37, 0.8, 4.2, "南風 S", 29.2, "04:00")       // Safe
    )

    // In-memory real-time state
    private var activeStations = baseStations.toMutableList()

    fun getStations(): List<SeaStation> {
        return activeStations
    }

    fun getStationById(id: String): SeaStation? {
        return activeStations.find { it.id == id }
    }

    /**
     * Get 24-hour historic readings for a station from the SQLite database.
     * If empty, populates it programmatically.
     */
    fun getHistoryForStation(stationId: String): Flow<List<HistoryReading>> {
        return historyDao.getHistoryForStation(stationId)
    }

    /**
     * Pre-populates mock SQLite data for line graphs
     */
    suspend fun populateMockHistory() {
        val allReadings = mutableListOf<HistoryReading>()
        val hours = (0..23).toList()

        for (station in baseStations) {
            // Generate nice waves using smooth math so lines look like natural ocean swells
            val seed = station.id.hashCode().toDouble()
            for (hour in hours) {
                val timeLabel = String.format("%02d:00", (hour + 5) % 24) // Simulated 24 hours back
                
                val waveOffset = kotlin.math.sin(hour * 0.4 + seed) * 0.7
                val waveHeight = (station.waveHeight + waveOffset).coerceIn(0.3, 5.0)

                val windOffset = kotlin.math.cos(hour * 0.4 + seed) * 4.0
                val windSpeed = (station.windSpeed + windOffset).coerceIn(2.0, 25.0)

                val tempOffset = kotlin.math.sin(hour * 0.2 + seed) * 1.5
                val seaTemp = (station.seaTemp + tempOffset).coerceIn(18.0, 31.0)

                allReadings.add(
                    HistoryReading(
                        stationId = station.id,
                        timestamp = timeLabel,
                        waveHeight = (Math.round(waveHeight * 10) / 10.0),
                        windSpeed = (Math.round(windSpeed * 10) / 10.0),
                        seaTemp = (Math.round(seaTemp * 10) / 10.0)
                    )
                )
            }
        }
        historyDao.clearAllHistory()
        historyDao.insertReadings(allReadings)
    }

    /**
     * Simulated refresh function that alters sea heights and speeds randomly to demo Live data updates.
     */
    fun simulateRefresh(currentTimeStr: String) {
        activeStations = baseStations.map { station ->
            val deltaWave = Random.nextDouble(-0.3, 0.3)
            val deltaWind = Random.nextDouble(-1.5, 1.5)
            val deltaTemp = Random.nextDouble(-0.2, 0.2)
            
            val newWave = (station.waveHeight + deltaWave).coerceIn(0.4, 4.5)
            val newWind = (station.windSpeed + deltaWind).coerceIn(2.0, 22.0)
            val newTemp = (station.seaTemp + deltaTemp).coerceIn(18.0, 31.0)

            station.copy(
                waveHeight = (Math.round(newWave * 10) / 10.0),
                windSpeed = (Math.round(newWind * 10) / 10.0),
                seaTemp = (Math.round(newTemp * 10) / 10.0),
                lastUpdated = currentTimeStr
            )
        }.toMutableList()
    }
}
