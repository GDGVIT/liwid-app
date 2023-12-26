package com.example.liwid_app.extension
import android.content.Context
import android.app.Activity
import android.util.Log
import com.example.liwid_app.extension.api.ApiClient
import com.example.liwid_app.extension.model.SportsData
import com.example.liwid_app.extension.model.SportsDataResponse
import retrofit2.Call


class LiveSportsWidget(
    context: Context,
    activity: Activity,
    private val baseUrl: String,
    private val endpoint: String,
    private val params: Map<String, String>,
):LiveWidget(context, activity, WidgetType.SPORTS) {
    companion object {
        private lateinit var baseUrl: String
        private lateinit var endpoint: String
        private lateinit var params: Map<String, String>

        fun create(
            context: Context,
            activity: Activity,
            baseUrl: String,
            endpoint: String,
            params: Map<String, String>,
        ): LiveSportsWidget {
            this.baseUrl = baseUrl
            this.endpoint = endpoint
            this.params = params
            return LiveSportsWidget(context, activity, baseUrl, endpoint, params)
        }

        fun fetchSportsData() {
            val apiClient = ApiClient(baseUrl)
            val sportsCall = apiClient.createApiService().getData(endpoint, params)

            sportsCall.enqueue(object : retrofit2.Callback<SportsDataResponse> {
                override fun onResponse(
                    call: Call<SportsDataResponse>,
                    response: retrofit2.Response<SportsDataResponse>
                ) {
                    if (response.isSuccessful) {
                        val sportsData = response.body()?.result?.firstOrNull()
                        sportsData?.let {onSuccess(it) }
                    } else {
                        onFailure(call, Throwable(response.message()))
                    }
                }

                override fun onFailure(call: Call<SportsDataResponse>, t: Throwable) {
                    Log.d("LiveSportsWidget", "onFailure: ${t.message}")
                }
            })
        }
        private fun onSuccess(it: SportsData) {
            Log.d("LiveSportsWidget", "onSuccess: $it")
        }
    }
}
