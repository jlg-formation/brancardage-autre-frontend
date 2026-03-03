package com.example.huybrancardage.data.remote.api

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuration et création de l'instance Retrofit
 */
object ApiClient {

    /**
     * URL de base de l'API
     * - localhost:8080 fonctionne avec `adb reverse tcp:8080 tcp:8080` (téléphone USB)
     * - 10.0.2.2:8080 pour l'émulateur Android
     */
    private const val BASE_URL = "http://localhost:8080/api/v1/"

    /**
     * Configuration JSON pour la sérialisation
     */
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Client OkHttp avec logging
     */
    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instance Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    /**
     * Service API
     */
    val apiService: BrancardageApiService by lazy {
        retrofit.create(BrancardageApiService::class.java)
    }
}

