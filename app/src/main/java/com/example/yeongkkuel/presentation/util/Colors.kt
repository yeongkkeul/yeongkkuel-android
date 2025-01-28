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
        // fromId 메서드 (기존 유지)
        fun fromId(id: Int): Colors? {
            return values().firstOrNull { it.id == id }
        }

        // fromCode 메서드 추가
        fun fromCode(code: String): Colors? {
            return when (code) {
                "RED1" -> RED1
                "RED2" -> RED2
                "PINK3" -> PINK3
                "PURPLE4" -> PURPLE4
                "PURPLE5" -> PURPLE5
                "BLUE6" -> BLUE6
                "BLUE7" -> BLUE7
                "BLUE8" -> BLUE8
                "GREEN9" -> GREEN9
                "GREEN10" -> GREEN10
                "GREEN11" -> GREEN11
                "GREEN12" -> GREEN12
                "YELLOW13" -> YELLOW13
                "ORANGE14" -> ORANGE14
                "ORANGE15" -> ORANGE15
                else -> null // 잘못된 코드 처리
            }
        }
    }
}