/**
 * File:         VisionApiClient.kt
 * Owner:        Caroline
 * Phase:        3
 *
 * Builds the Retrofit + OkHttp client for the Vision API: JSON conversion,
 * timeouts, and adding the API key to every request.
 */
package com.example.drawingapp.vision

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object VisionApiClient {

    /** Google's Vision API server. */
    const val BASE_URL = "https://vision.googleapis.com/"

    /** JSON settings shared by the request and response. */
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /** Creates the API service. */
    fun create(apiKey: String, baseUrl: String = BASE_URL): VisionApiService {
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor(apiKey))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(VisionApiService::class.java)
    }

    /** Adds ?key=<apiKey> to every request, which is how the Vision API authenticates. */
    private fun apiKeyInterceptor(apiKey: String) = Interceptor { chain ->
        val original = chain.request()
        val urlWithKey = original.url.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()
        chain.proceed(original.newBuilder().url(urlWithKey).build())
    }
}