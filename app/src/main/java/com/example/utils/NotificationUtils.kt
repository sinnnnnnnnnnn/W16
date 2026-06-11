package com.example.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.models.SeaStation

object NotificationUtils {
    private const val CHANNEL_ID = "sea_alert_channel"
    private const val CHANNEL_NAME = "海況警戒即時通知"

    /**
     * Set up alert notifications channels for Oreo+
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "當觀測站波高或風速超標時發送緊急海況提醒"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Check current Sea Conditions and send notifications if danger levels are breached (警報系統)
     */
    fun checkAndSendAlerts(context: Context, station: SeaStation) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val isWaveDanger = station.waveHeight >= 3.0
        val isWindDanger = station.windSpeed >= 15.0

        if (isWaveDanger || isWindDanger) {
            val alertTitle = "🚨 海況安全警戒提醒: ${station.name}"
            val alertMessage = buildString {
                append("觀測站即時海況已達危險等級！")
                if (isWaveDanger) {
                    append("\n⚠️ 巨浪警戒: 浪高達 %.1f 米 (門檻: 3米)".format(station.waveHeight))
                }
                if (isWindDanger) {
                    append("\n⚠️ 強風警戒: 風速達 %.1f m/s (門檻: 15m/s)".format(station.windSpeed))
                }
                append("\n更新時間: ${station.lastUpdated}。請避免周遭海上作業！")
            }

            // Create notification
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(alertTitle)
                .setContentText(station.name + " 已達海況安全危險區。")
                .setStyle(NotificationCompat.BigTextStyle().bigText(alertMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            manager.notify(station.id.hashCode(), builder.build())
        }
    }
}
