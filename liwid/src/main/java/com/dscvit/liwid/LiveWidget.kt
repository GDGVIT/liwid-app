package com.dscvit.liwid

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dscvit.liwid.WidgetForegroundService.Companion.startService
import retrofit2.Call

abstract class LiveWidget(
    protected val context: Context,
    protected val activity: Activity,
    protected var widgetType: WidgetType,
    var PERMISSON_REQUEST_CODE: Int=0,
    var CHANNEL_DESCRIPTION: String="Channel for Live Widget",
    var CHANNEL_NAME: String="Live Widget Channel",
    var CHANNEL_ID: String="Live_Widget_Channel_Id",
) {
    enum class WidgetType{
        SPORTS,
        TRACKING,
    }

    init {
        createLiveWidgetChannel()
        requestLiveWidgetPermission()
        startService(context, widgetType)
    }

    fun createLiveWidgetChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun requestLiveWidgetPermission() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSON_REQUEST_CODE
                )
            }
        }
    }
    fun <T> Call<T>.enqueue(t: T) {}
}