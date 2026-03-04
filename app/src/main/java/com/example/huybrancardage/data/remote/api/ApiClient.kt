package com.example.huybrancardage.data.remote.api

import com.example.huybrancardage.BuildConfig
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
     * URL de base de l'API - configurée via build.gradle.kts
     * Peut être paramétrée au moment de la build:
     * - ./gradlew assembleDebug -PDEBUG_BACKEND_URL="http://10.0.2.2:8080/api/v1/"
     * - ./gradlew assembleRelease -PRELEASE_BACKEND_URL="https://api.production.com/api/v1/"
     */
    private val BASE_URL: String = BuildConfig.BACKEND_URL

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

