package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.models.HistoryReading
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM sea_history WHERE stationId = :stationId ORDER BY id ASC")
    fun getHistoryForStation(stationId: String): Flow<List<HistoryReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<HistoryReading>)

    @Query("DELETE FROM sea_history")
    suspend fun clearAllHistory()
}
