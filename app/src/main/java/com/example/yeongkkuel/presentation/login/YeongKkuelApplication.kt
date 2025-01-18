package com.example.yeongkkuel.presentation.login

import android.app.Application
import com.example.yeongkkuel.BuildConfig.kakao_NATIVE_APP_KEY
import com.kakao.sdk.common.KakaoSdk

// kakao sdk 연동 (초기화작업)
class YeongKkuelApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, kakao_NATIVE_APP_KEY)

    }
}