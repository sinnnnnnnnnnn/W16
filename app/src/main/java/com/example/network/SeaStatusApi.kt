package com.example.network

import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * API response structure for a Sea Station observation
 */
data class SeaStationResponse(
    @Json(name = "station_id") val stationId: String,
    @Json(name = "station_name") val name: String,
    @Json(name = "latitude") val lat: Double,
    @Json(name = "longitude") val lng: Double,
    @Json(name = "wave_height_meters") val waveHeight: Double,
    @Json(name = "wind_speed_mps") val windSpeed: Double,
    @Json(name = "wind_direction") val windDirection: String,
    @Json(name = "sea_temp_celsius") val seaTemp: Double,
    @Json(name = "last_updated_time") val lastUpdated: String
)

/**
 * Retrofit Sea Status API Service interface (API 串接範例)
 * This interface outlines how the app is structured to connect to a maritime oceanography API.
 */
interface SeaStatusApiService {
    @GET("v1/stations")
    suspend fun getActiveStations(
        @Query("apiKey") apiKey: String = "sea_demo"
    ): List<SeaStationResponse>

    @GET("v1/stations/{stationId}/history")
    suspend fun getStationHistory(
        @Path("stationId") stationId: String,
        @Query("hours") hours: Int = 24
    ): List<SeaStationResponse>
}

/**
 * API Client Builder
 */
object SeaStatusApiClient {
    private const val BASE_URL = "https://api.cwa.gov.tw/demo/" // Placeholder URL pointing towards simulated ocean forecasts or CWA Central Weather Administration API

    val service: SeaStatusApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        retrofit.create(SeaStatusApiService::class.java)
    }
}
