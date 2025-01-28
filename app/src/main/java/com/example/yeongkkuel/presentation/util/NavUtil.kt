package com.example.yeongkkuel.presentation.util

import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.yeongkkuel.R

val navOption = navOptions {
    popUpTo(R.id.navigation_stat) {
        inclusive = false
    }
    launchSingleTop = true
}

fun NavController.toNaviStat() {
    this.navigate(
        R.id.navigation_stat,  // 오타 수정
        null,
        navOption
    )
}