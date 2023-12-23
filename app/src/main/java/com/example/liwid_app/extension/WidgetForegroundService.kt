package com.example.liwid_app.extension
import android.Manifest
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*

class WidgetForegroundService:Service() {
    companion object{
        const val NOTIFICATION_KEY: String = "notification"
        const val START_BROADCAST_ACTION: String = "FOREGROUND_SERVICE_ACTION"
        const val WIDGET_TYPE_KEY: String = "widgetType"

        fun startService(
            context: Context,
            widgetType: LiveWidget.WidgetType
        ) {
            val serviceIntent = Intent(context, WidgetForegroundService::class.java).apply {
                putExtra(WIDGET_TYPE_KEY, widgetType)
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }

    private val CHANNEL_ID = "ForegroundServiceChannel"
    private var notificationJob: Job? = null
    private val NOTIFICATION_ID:Int=1


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification=intent?.getParcelableExtra<Notification>(NOTIFICATION_KEY)
        val widgetType=intent?.getSerializableExtra(WIDGET_TYPE_KEY) as LiveWidget.WidgetType
        if(notification!=null) {
            startForeground(NOTIFICATION_ID, notification)
            notificationJob = GlobalScope.launch(Dispatchers.IO) {
                while(isActive){
                    fetchDataAndUpdateWidget(widgetType)
                    delay(60000)
                }
            }
        }
        return START_STICKY
    }
    private fun fetchDataAndUpdateWidget(widgetType: LiveWidget.WidgetType) {
        try {
            val responseData = when (widgetType) {
                LiveWidget.WidgetType.SPORTS -> LiveSportsWidget.fetchSportsData()
                LiveWidget.WidgetType.TRACKING -> LiveTrackingWidget.fetchTrackingData()
            }

            // Update the notification content based on the API response data
            val notificationContent = when (widgetType) {
                LiveWidget.WidgetType.SPORTS -> "Sports Data: $responseData"
                LiveWidget.WidgetType.TRACKING -> "Tracking Data: $responseData"
            }

            // Create the updated notification
            val updatedNotification = when (widgetType) {
                LiveWidget.WidgetType.SPORTS -> createSportsWidget(notificationContent)
                LiveWidget.WidgetType.TRACKING -> createTrackingWidget(notificationContent)
            }

            // Notify the notification manager
            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        } catch (e: Exception) {
            Log.d("WidgetForegroundService", "Error fetching data: ${e.message}")
        }
    }

    private fun createSportsWidget(notificationContent: String): Notification {
        // Implement notification creation logic for SPORTS widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sports Notification")
            .setContentText(notificationContent)
            .build()
    }

    private fun createTrackingWidget(notificationContent: String): Notification {
        // Implement notification creation logic for TRACKING widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Notification")
            .setContentText(notificationContent)
            .build()
    }


    override fun onDestroy() {
        super.onDestroy()
        notificationJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
}
