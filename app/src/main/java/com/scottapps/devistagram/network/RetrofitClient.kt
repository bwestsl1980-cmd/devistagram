package com.scottapps.devistagram.network

import com.scottapps.devistagram.auth.DeviantArtAuthConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Auth API (OAuth endpoints)
    private val authRetrofit = Retrofit.Builder()
        .baseUrl("https://www.deviantart.com/oauth2/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val authApi: DeviantArtAuthApi = authRetrofit.create(DeviantArtAuthApi::class.java)
    
    // Content API (DeviantArt API endpoints)
    private val apiRetrofit = Retrofit.Builder()
        .baseUrl("https://www.deviantart.com/api/v1/oauth2/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val contentApi: DeviantArtApi = apiRetrofit.create(DeviantArtApi::class.java)
}
