package com.example.yeongkkuel.network

import android.content.Context
import com.example.yeongkkuel.network.data.AuthInterceptor

import com.example.yeongkkuel.presentation.home.HomeApiService
import com.example.yeongkkuel.presentation.home.store.StoreApiService

import com.example.yeongkkuel.network.service.MyPageService
import com.example.yeongkkuel.network.service.StatService
import com.example.yeongkkuel.presentation.home.entry.data.ExpenseApiService

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

    /**
     * HttpLoggingInterceptor 추가! (네트워크 요청 및 응답 로그 출력)
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청과 응답 바디를 로그로 출력
    }

    fun init(context: Context) {
        if (baseRetrofit == null) {
            baseRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        if (authRetrofit == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(context))  // JWT 헤더 자동 추가
                .addInterceptor(loggingInterceptor) // 📌 HttpLoggingInterceptor 추가!
                .build()

            authRetrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    val reissueApiService: ReissueApiService by lazy {
        authRetrofit!!.create(ReissueApiService::class.java)
    }

    val loginApiService: LoginApiService by lazy {
        authRetrofit!!.create(LoginApiService::class.java)
    }

    val statService: StatService by lazy {
        authRetrofit!!.create(StatService::class.java)
    }


    val categoryApiService: CategoryApiService by lazy {
        authRetrofit!!.create(CategoryApiService::class.java)
    }

    val homeApiService: HomeApiService by lazy {
        authRetrofit!!.create(HomeApiService::class.java)
    }

    val storeapiService: StoreApiService by lazy {
        authRetrofit!!.create(StoreApiService::class.java)
    }

    val myPageService: MyPageService by lazy {
        authRetrofit!!.create(MyPageService::class.java)
    }

    val expenseApiService: ExpenseApiService by lazy {
        authRetrofit!!.create(ExpenseApiService::class.java)
    }

}