package com.example.yeongkkuel

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.network.RetrofitClient
import com.kakao.sdk.common.KakaoSdk

class YeongKkuelApplication : Application() {
    companion object {
        private var instance: YeongKkuelApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        KakaoSdk.init(this, BuildConfig.kakao_NATIVE_APP_KEY)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        RetrofitClient.init(this)
    }
}