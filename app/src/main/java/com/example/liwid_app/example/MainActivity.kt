package com.example.liwid_app.example

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.Service
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.example.liwid_app.example.api.ApiClient
import com.example.liwid_app.example.model.MatchData
import com.example.liwid_app.example.model.MatchResponse
import com.example.liwid_app.R

class MainActivity : ComponentActivity() {
    companion object {
        const val STOP_BROADCAST_ACTION = "STOP_FOREGROUND_SERVICE"
    }

    private val apiService= ApiClient.apiService
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ForegroundService.START_BROADCAST_ACTION) {
                Log.d("BroadCast", "Foreground Called Notification Routine")
                startNotificationTimer(60,false,true)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            App()
        }
        val filter=IntentFilter().apply {
            addAction("START_NOTIFICATION_TIMER")
        }
        registerReceiver(notificationReceiver,filter)
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }

    @Composable
    @Preview
    fun App(){
        var isBackgroundEnabled by remember{ mutableStateOf(false)}
        var isForegroundEnabled by remember{ mutableStateOf(false)}
        Column (
            Modifier.fillMaxSize()
                .padding(16.dp)){
            Row(Modifier.padding(10.dp)) {
                Text(
                    text = if (isBackgroundEnabled) "Disable Background Service" else "Enable Background Service",
                    Modifier.align(Alignment.CenterVertically)
                )
                Switch(
                    checked = isBackgroundEnabled,
                    onCheckedChange = {
                        isBackgroundEnabled=it
                        if(it){
                            if (checkNotificationPermission()) {
                                Log.d("SBackground", "Started")
                                startNotificationTimer(60,true,false)
                            } else {
                                requestNotificationPermission()
                            }
                        }
                        else{
                            stopNotificationTimer()
                            Log.d("TBackground", "Terminated")
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
            Row(Modifier.padding(10.dp)) {
                Text(
                    text = if (isForegroundEnabled) "Disable Foreground Service" else "Enable Foreground Service",
                    Modifier.align(Alignment.CenterVertically),
                )
                Switch(
                    checked = isForegroundEnabled,
                    onCheckedChange = {
                        isForegroundEnabled=it
                        if(it){
                            fetchAndShowNotification(1)
                        }else{
                            stopNotificationTimer()
                            terminateFgService()
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }

    private fun enableFgservice(matchData: MatchData) {
        val notification = showNotification(matchData).build()
        val serviceIntent = Intent(this, ForegroundService::class.java).apply {
            putExtra(ForegroundService.NOTIFICATION_KEY, notification)
        }
        ContextCompat.startForegroundService(this, serviceIntent)
        Log.d("SForegnd", "Started")
    }

    private fun terminateFgService() {
        val stopServiceIntent = Intent(STOP_BROADCAST_ACTION)
        sendBroadcast(stopServiceIntent)
        Log.d("TForegnd", "Terminated")
    }

    val CHANNEL_ID="LIVE_CHANNEL_ID"

    fun fetchAndShowNotification(flag: Int){
        apiService.getMatch().enqueue(object : Callback<MatchResponse> {
            override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                if (response.isSuccessful) {
                    val matchData = response.body()?.result?.firstOrNull()
                    Log.d("MatchData", "matchdata")
                    println(matchData)
                    if(flag==1){
                        Log.d("FgService", "service is created")
                        matchData?.let { enableFgservice(it) }
                    }
                    else{
                        Log.d("BgService", "notification produced for bg")
                        matchData?.let { showNotification(it) }
                    }
                }
            }

            override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error fetching match data", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showNotification(matchData: MatchData): NotificationCompat.Builder {
        Log.d("ShowNotification", "Check for notification")
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(matchData.leagueName)
            .setContentText("${matchData.homeTeamName} vs ${matchData.awayTeamName}")
            .setStyle(NotificationCompat
                .BigTextStyle()
                .bigText(
                            "\n${matchData.homeTeamResult}"+ "\n${matchData.eventStatus}"+"\n${matchData.awayTeamResult}"
                ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
        with(NotificationManagerCompat.from(this)) {
            checkNotificationPermission()
            notify(1, builder.build())
        }
        return builder
    }

    private fun checkNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
        )
        if(ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            )!=PackageManager.PERMISSION_GRANTED
        ){
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=NotificationChannel(
                CHANNEL_ID,
                "Live Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description="This is Live Channel test"
            val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private var notificationJob: Job?=null
    private fun startNotificationTimer(intervalSeconds:Long,isBgEn:Boolean,isFgEn:Boolean){
        Log.d("StartNotificationTimer", "In main")
        notificationJob= CoroutineScope(Dispatchers.Main).launch {
            while (isActive){
                Log.d("StartNotification","Started ${isBgEn} and ${isFgEn}")
                if(isBgEn==true && isFgEn==false){
                    Log.d("Backgrnd", "Called fetch and show in bg")
                    fetchAndShowNotification(0)
                }
                else if (isBgEn==false && isFgEn==true){
                    Log.d("Foregrnd", "Called fetch and show in fg")
                    fetchAndShowNotification(1)
                }

                delay(intervalSeconds*1000)
            }
        }
    }

    private fun stopNotificationTimer(){
        notificationJob?.cancel()
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
        Log.d("Terminated","StopNotificationTimer")
    }
}
class ForegroundService:Service(){
    companion object {
        val NOTIFICATION_KEY: String?="notification"
        val START_BROADCAST_ACTION: String?="FOREGROUND_SERVICE_ACTION"
    }
    private val CHANNEL_ID = "ForegroundServiceChannel"
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification=intent?.getParcelableExtra<Notification>("notification")
        if(notification!=null){
            startForeground(1,notification)
            val startServiceIntent = Intent(START_BROADCAST_ACTION)
            sendBroadcast(startServiceIntent)
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private val stopServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MainActivity.STOP_BROADCAST_ACTION) {
                stopForegroundService()
            }
        }
    }
    private fun stopForegroundService() {
        stopForeground(1)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BroadCast","Stop foreground broadcast")
        unregisterReceiver(stopServiceReceiver)
    }
}