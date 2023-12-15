package com.example.liwid_app

import com.example.liwid_app.data.model.ApiService
import com.example.liwid_app.util.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit=Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService:ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}