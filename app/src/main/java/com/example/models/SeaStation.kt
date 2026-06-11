package com.example.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sea Observation Station model representing current conditions
 */
data class SeaStation(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val waveHeight: Double, // in meters
    val windSpeed: Double,  // in m/s
    val windDirection: String, // e.g. "東北東 ENE"
    val seaTemp: Double,    // in °C
    val lastUpdated: String  // e.g. "12:00"
) {
    val alertLevel: AlertLevel
        get() = when {
            waveHeight >= 3.0 || windSpeed >= 15.0 -> AlertLevel.DANGER
            waveHeight >= 2.0 || windSpeed >= 10.0 -> AlertLevel.CAUTION
            else -> AlertLevel.SAFE
        }
}

enum class AlertLevel(val label: String) {
    SAFE("安全 SAFE"),
    CAUTION("注意 CAUTION"),
    DANGER("危險 DANGER")
}

/**
 * Historical reading representing a measurement at a specific time
 */
@Entity(tableName = "sea_history")
data class HistoryReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stationId: String,
    val timestamp: String, // e.g., "08:00", "09:00", etc.
    val waveHeight: Double,
    val windSpeed: Double,
    val seaTemp: Double
)
