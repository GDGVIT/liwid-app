package com.example.liwid_app.extension

import android.content.Context
import android.app.Activity
import android.util.Log
import com.example.liwid_app.extension.api.ApiClient
import com.example.liwid_app.extension.model.TrackerData
import com.example.liwid_app.extension.model.TrackerWidgetData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LiveTrackingWidget(
    context: Context,
    activity: Activity,
    widgetType: WidgetType,
    private val baseUrl: String,
    private val endpoint: String,
    private val params: Map<String, String>,
) : LiveWidget(context, activity, WidgetType.TRACKING) {

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

    companion object {
        fun create(
            context: Context,
            activity: Activity,
            baseUrl: String,
            endpoint: String,
            params: Map<String, String>,
        ): LiveTrackingWidget {
            return LiveTrackingWidget(context, activity, WidgetType.TRACKING, baseUrl, endpoint, params)
        }
    }
}
