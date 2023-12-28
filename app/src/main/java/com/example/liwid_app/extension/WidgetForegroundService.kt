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
import com.example.liwid_app.extension.api.model.SportsData
import com.example.liwid_app.extension.api.model.TrackerData
import com.example.liwid_app.extension.widget.LiveSportsWidget
import com.example.liwid_app.extension.widget.LiveTrackingWidget
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
    var resSportsData: SportsData? = null
    var resTrackingData: TrackerData? = null
    private fun fetchDataAndUpdateWidget(widgetType: LiveWidget.WidgetType) {
        try {
           if (widgetType == LiveWidget.WidgetType.SPORTS) {
                var resSportsData: Unit = LiveSportsWidget.fetchSportsData()
            } else {
               var resTrackingData: Unit = LiveTrackingWidget.fetchTrackingData()
            }

            // Update the notification content based on the API response data
            val notificationContent = when (widgetType) {
                LiveWidget.WidgetType.SPORTS -> resSportsData
                LiveWidget.WidgetType.TRACKING -> resTrackingData
            }

            // Create the updated notification
            val updatedNotification = when (widgetType) {
                LiveWidget.WidgetType.SPORTS -> createSportsWidget(notificationContent as SportsData)
                LiveWidget.WidgetType.TRACKING -> createTrackingWidget(notificationContent as TrackerData)
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

    private fun createSportsWidget(notificationContent: SportsData): Notification {
        // Implement notification creation logic for SPORTS widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(notificationContent.leagueName)
            .setContentText("${notificationContent.homeTeamName} vs ${notificationContent.awayTeamName}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${notificationContent.homeTeamName} vs ${notificationContent.awayTeamName}")
            )
            .build()
    }

    private fun createTrackingWidget(notificationContent: TrackerData): Notification {
        // Implement notification creation logic for TRACKING widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .build()
    }


    override fun onDestroy() {
        super.onDestroy()
        notificationJob?.cancel()
        stopForeground(true)
        stopSelf()
    }
}
