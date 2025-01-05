package com.example.yeongkkuel.presentation.stat

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

data class StatTapModel(
    val fragment: Fragment,
    @StringRes val title: Int
)