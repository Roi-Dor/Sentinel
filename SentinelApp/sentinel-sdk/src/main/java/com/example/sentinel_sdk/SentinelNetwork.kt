package com.example.sentinel_sdk

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.example.sentinel_sdk.SecurityReport


interface SentinelApi {
    @POST("/api/report")
    fun sendReport(@Body report: SecurityReport): Call<Void>
}

object SentinelNetwork {
    private const val BASE_URL = "http://54.163.206.55:5000/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: SentinelApi = retrofit.create(SentinelApi::class.java)
}