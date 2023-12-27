package com.example.liwid_app.extension.widget

import android.content.Context
import android.app.Activity
import android.util.Log
import com.example.liwid_app.extension.LiveWidget
import com.example.liwid_app.extension.api.ApiClient
import com.example.liwid_app.extension.api.model.TrackerData
import com.example.liwid_app.extension.api.model.TrackerWidgetData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveTrackingWidget(
    context: Context,
    activity: Activity,
    private val baseUrl: String,
    private val endpoint: String,
    private val params: Map<String, String>
) : LiveWidget(context, activity, WidgetType.TRACKING) {

    companion object {
        private lateinit var baseUrl: String
        private lateinit var endpoint: String
        private lateinit var params: Map<String, String>
        fun create(
            context: Context,
            activity: Activity,
            baseUrl: String,
            endpoint: String,
            params: Map<String, String>
        ): LiveTrackingWidget {
            return LiveTrackingWidget(context, activity, baseUrl, endpoint, params)
        }

        fun fetchTrackingData() {
            val apiClient = ApiClient(baseUrl)
            val trackingCall = apiClient.createApiService().getData(endpoint, params)

            trackingCall.enqueue(object : Callback<TrackerWidgetData> {
                override fun onResponse(
                    call: Call<TrackerWidgetData>,
                    response: Response<TrackerWidgetData>
                ) {
                    if (response.isSuccessful) {
                        val trackingData = response.body()?.result?.firstOrNull()
                        trackingData?.let { onSuccess(it) }
                    } else {
                        onFailure(call, Throwable(response.message()))
                    }
                }

                override fun onFailure(call: Call<TrackerWidgetData>, t: Throwable) {
                    Log.d("LiveTrackingWidget", "onFailure: ${t.message}")
                }
            })
        }

        private fun onSuccess(it: TrackerData) {
            // Handle the success here
        }
    }
}

fun <T> Call<T>.enqueue(t: T) {

}
