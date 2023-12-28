package com.example.liwid_app.extension
import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.liwid_app.R
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
    val sportsNotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val trackingNotificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



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

    val sportsNotificationLayout= RemoteViews("com.example.liwid_app",R.layout.sports_widget_layout_wrapped)
//    val sportsNotificationLayoutExp= RemoteViews("com.example.liwid_app",R.layout.sports_widget_layout_expanded)


    private fun createSportsWidget(notificationContent: SportsData): Notification {
        sportsNotificationLayout.setTextViewText(R.id.league_name,notificationContent.leagueName)
        sportsNotificationLayout.setTextViewText(R.id.homeTeamName,notificationContent.homeTeamName)
        sportsNotificationLayout.setTextViewText(R.id.awayTeamName,notificationContent.awayTeamName)
        sportsNotificationLayout.setTextViewText(R.id.homeTeamScore,notificationContent.homeTeamResult.toString())
        sportsNotificationLayout.setTextViewText(R.id.awayTeamScore,notificationContent.awayTeamResult.toString())
        sportsNotificationLayout.setTextViewText(R.id.centralMatchResult,notificationContent.matchResult)
        sportsNotificationLayout.setTextViewText(R.id.eventStatus,notificationContent.eventStatus)
        sportsNotificationLayout.setTextViewText(R.id.smallAwayLogo,notificationContent.awayTeamLogo)
        sportsNotificationLayout.setTextViewText(R.id.smallHomeLogo,notificationContent.homeTeamLogo)
        val homeTeamLogoBitmap=Glide.with(applicationContext).asBitmap().load(notificationContent.homeTeamLogo).submit().get()
        val awayTeamLogoBitmap=Glide.with(applicationContext).asBitmap().load(notificationContent.awayTeamLogo).submit().get()
        sportsNotificationLayout.setImageViewBitmap(R.id.smallHomeLogo,homeTeamLogoBitmap)
        sportsNotificationLayout.setImageViewBitmap(R.id.smallAwayLogo,awayTeamLogoBitmap)
        // Implement notification creation logic for SPORTS widget
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(sportsNotificationLayout)
//            .setCustomBigContentView(notificationLayoutExpanded)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
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
