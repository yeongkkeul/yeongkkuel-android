package com.example.yeongkkuel.presentation.network

import com.example.yeongkkuel.presentation.auth.ReissueApiService
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.login.LoginApiService
import com.example.yeongkkuel.presentation.network.data.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://dev.yeongkkeul.store"


    private val baseRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    private lateinit var retrofit: Retrofit

    fun init(tokenManager: TokenManager) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val authInterceptor = AuthInterceptor(tokenManager, baseRetrofit)

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val yeongkkuelService: YeongkkuelService by lazy {
        retrofit.create(YeongkkuelService::class.java)
    }

    val reissueApiService: ReissueApiService by lazy {
        retrofit.create(ReissueApiService::class.java)
    }

    val loginApiService: LoginApiService by lazy {
        retrofit.create(LoginApiService::class.java)
    }

}