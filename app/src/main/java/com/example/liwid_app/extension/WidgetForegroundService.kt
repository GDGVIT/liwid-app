package com.example.liwid_app.extension
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
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
        if(notification!=null && widgetType!=null) {
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
    private suspend fun fetchDataAndUpdateWidget(widgetType: LiveWidget.WidgetType) {
        // Implement data fetching logic here based on widgetType
        // Update the notification content accordingly
        val updatedNotification = when (widgetType) {
            LiveWidget.WidgetType.SPORTS -> createSportsNotification()
            LiveWidget.WidgetType.TRACKING -> createTrackingNotification()
        }

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, updatedNotification)
        }
    }

    private fun createSportsNotification(): Notification {
        // Implement notification creation logic for SPORTS widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sports Notification")
            .setContentText("Fetching sports data in the background")
            .build()
    }

    private fun createTrackingNotification(): Notification {
        // Implement notification creation logic for TRACKING widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking Notification")
            .setContentText("Fetching tracking data in the background")
            .build()
    }


    override fun onDestroy() {
        super.onDestroy()
        notificationJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
}
