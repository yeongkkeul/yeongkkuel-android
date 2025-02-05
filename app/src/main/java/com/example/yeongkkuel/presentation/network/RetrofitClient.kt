package com.example.yeongkkuel.presentation.network

import android.content.Context
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

    /**
     * Interceptor 없는 Retrofit (재발급 전용)
     */
    private var baseRetrofit: Retrofit? = null

    /**
     * AuthInterceptor가 적용된 Retrofit
     */
    private var authRetrofit: Retrofit? = null

    fun init(context: Context) {
        if (baseRetrofit == null) {
            baseRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        if (authRetrofit == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))  // JWT 헤더 자동 추가
                .addInterceptor(logging)
                .build()

            authRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    val reissueApiService: ReissueApiService by lazy {
        baseRetrofit!!.create(ReissueApiService::class.java)
    }

    val loginApiService: LoginApiService by lazy {
        authRetrofit!!.create(LoginApiService::class.java)
    }

    val yeongkkuelService: YeongkkuelService by lazy {
        authRetrofit!!.create(YeongkkuelService::class.java)
    }

}