package com.example.liwid_app.extension.api
import android.telecom.CallEndpoint
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ApiClient (private val baseUrl:String){
    private val retrofit=Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun createApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}


