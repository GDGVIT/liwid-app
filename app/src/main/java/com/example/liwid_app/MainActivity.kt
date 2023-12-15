package com.example.liwid_app

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.liwid_app.data.model.MatchData
import com.example.liwid_app.data.model.MatchResponse
import com.example.liwid_app.ui.theme.LiwidappTheme
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : ComponentActivity() {
    private val apiService=ApiClient.apiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            LiwidappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
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
                                startNotificationTImer(60)
                            } else {
                                requestNotificationPermission()
                            }
                        }
                        else{
                            stopNotificationTimer()
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
                            enableFgservice()
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

    private fun terminateFgService() {
        TODO("Not yet implemented")
    }

    private fun enableFgservice() {
        TODO("Not yet implemented")
    }

    val CHANNEL_ID="LIVE_CHANNEL_ID"

    fun fetchAndShowNotification(){
        apiService.getMatch().enqueue(object : Callback<MatchResponse> {
            override fun onResponse(call: Call<MatchResponse>, response: Response<MatchResponse>) {
                if (response.isSuccessful) {
                    Log.d("MainActivity", "This is a debug message 3 on success")
                    val matchData = response.body()?.result?.firstOrNull()
                    Log.d("MainActivity", "MatchData")
                    println(matchData)
                    matchData?.let { showNotification(it) }
                }
            }

            override fun onFailure(call: Call<MatchResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error fetching match data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showNotification(matchData: MatchData) {
        Log.d("MainActivity", "This is a debug message 4")
//        val updateIntent= Intent(this,NotificationActionReceiver::class.java)
//        val updatePendingIntent=PendingIntent.getBroadcast(
//            this, 0, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT
//        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(matchData.leagueName)
            .setContentText("${matchData.homeTeamName} vs ${matchData.awayTeamName}")
            .setStyle(NotificationCompat
                .BigTextStyle()
                .bigText(
                            "\n${matchData.result}"+
                                    "\n${matchData.eventStatus}"
                ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
        with(NotificationManagerCompat.from(this)) {
            checkNotificationPermission()
            notify(1, builder.build())
        }
    }

    class NotificationActionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            (context as MainActivity).fetchAndShowNotification()
        }
    }
    private fun checkNotificationPermission(): Boolean {
        return NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

    private fun requestNotificationPermission() {
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
    private fun startNotificationTImer(intervalSeconds:Long){
        Log.d("MainActivity", "This is a debug message")
        notificationJob= CoroutineScope(Dispatchers.Main).launch {
            while (isActive){
                Log.d("MainActivity", "This is a debug message 2")
                fetchAndShowNotification()
                delay(intervalSeconds*1000)
            }
        }
    }

    private fun stopNotificationTimer(){
        notificationJob?.cancel()
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

}