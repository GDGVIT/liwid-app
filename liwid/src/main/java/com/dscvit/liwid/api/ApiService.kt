package com.dscvit.liwid.api
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {
    val BASE_URL: String

    @GET
    fun getData(
        @Url url: String,
        @QueryMap params: Map<String, String>
    ): Call<Any>

    fun ApiService.getData(baseUrl: String, endpoint:String, params: Map<String, String>): Call<Any> {
        val url="$BASE_URL/$endpoint"
        return getData(endpoint,params)
    }
}

