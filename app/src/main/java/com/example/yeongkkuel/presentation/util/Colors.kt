package com.example.yeongkkuel.presentation.util

import androidx.annotation.ColorRes
import com.example.yeongkkuel.R

enum class Colors(@ColorRes val id: Int, val code: String) {
    BLUE(R.color.blue, "4385F7"), PINK(R.color.pink, "FF5CA3"), GREEN(R.color.green, "77BF48");

    companion object {
        fun fromCode(code: String): Colors {
            return values().find { it.code == code } ?: BLUE
        }
    }
}
