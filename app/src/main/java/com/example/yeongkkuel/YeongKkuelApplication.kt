package com.example.yeongkkuel

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class YeongKkuelApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}