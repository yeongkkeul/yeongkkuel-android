package com.example.yeongkkuel.presentation.util

import com.example.yeongkkuel.R

enum class Colors(val id: Int) {
    RED1(R.color.red1),
    RED2(R.color.red2),
    PINK3(R.color.pink3),
    PURPLE4(R.color.purple4),
    PURPLE5(R.color.purple5),
    BLUE6(R.color.blue6),
    BLUE7(R.color.blue7),
    BLUE8(R.color.blue8),
    GREEN9(R.color.green9),
    GREEN10(R.color.green10),
    GREEN11(R.color.green11),
    GREEN12(R.color.green12),
    YELLOW13(R.color.yellow13),
    ORANGE14(R.color.orange14),
    ORANGE15(R.color.orange15);

    companion object {
        fun fromId(id: Int): Colors? {
            return values().firstOrNull { it.id == id }
        }
    }
}
