package com.example.liwid_app.extension.api
import com.example.liwid_app.example.util.BASE_URL
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {
    @GET
    fun getData(
        @Url url: String,
        @QueryMap params: Map<String, String>
    ): Call<Any>

    fun ApiService.getData(baseUrl: String,endpoint:String,params: Map<String, String>): Call<Any> {
        val url="$BASE_URL/$endpoint"
        return getData(endpoint,params)
    }
}

