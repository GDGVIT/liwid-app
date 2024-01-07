package com.dscvit.liwid.api
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient (private val baseUrl:String){
    private val retrofit=Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun createApiService(): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}


