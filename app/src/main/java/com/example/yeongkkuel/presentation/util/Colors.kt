package com.example.yeongkkuel.presentation.util

import com.example.yeongkkuel.R

enum class Colors(val id: Int, val rgb: Int, val red: Int, val green: Int, val blue: Int) {
    RED1(R.color.red1, 0xFFE3000B.toInt(), 227, 0, 11),   // #E3000B
    RED2(R.color.red2, 0xFFFE4040.toInt(), 254, 64, 64),   // #FE4040
    PINK3(R.color.pink3, 0xFFFE40BF.toInt(), 254, 64, 191), // #FE40BF
    PURPLE4(R.color.purple4, 0xFFBF40FE.toInt(), 191, 64, 254), // #BF40FE
    PURPLE5(R.color.purple5, 0xFF7F40FE.toInt(), 127, 64, 254), // #7F40FE
    BLUE6(R.color.blue6, 0xFF235FB3.toInt(), 35, 95, 179), // #235FB3
    BLUE7(R.color.blue7, 0xFF3FACDA.toInt(), 63, 172, 218), // #3FACDA
    BLUE8(R.color.blue8, 0xFF53CBFF.toInt(), 83, 203, 255), // #53CBFF
    GREEN9(R.color.green9, 0xFF00BFA6.toInt(), 0, 191, 166), // #00BFA6
    GREEN10(R.color.green10, 0xFF4B862E.toInt(), 75, 134, 46), // #4B862E
    GREEN11(R.color.green11, 0xFF51BD1B.toInt(), 81, 189, 27), // #51BD1B
    GREEN12(R.color.green12, 0xFFC4E015.toInt(), 196, 224, 21), // #C4E015
    YELLOW13(R.color.yellow13, 0xFFFEE540.toInt(), 254, 229, 64), // #FEE540
    ORANGE14(R.color.orange14, 0xFFFB8809.toInt(), 251, 136, 9), // #FB8809
    ORANGE15(R.color.orange15, 0xFFFF6316.toInt(), 255, 99, 22); // #FF6316

    companion object {
        // id로 Colors Enum 찾기
        fun fromId(id: Int): Colors? {
            return values().firstOrNull { it.id == id }
        }

        // ARGB 값으로 Colors Enum 찾기 (ARGB → Enum 매칭)
        fun fromARGB(argb: Int): Colors? {
            return values().firstOrNull { it.rgb == argb }
        }

        // 서버에서 사용하는 Code로 Colors Enum 찾기
        fun fromCode(code: String): Colors? {
            return values().firstOrNull { it.name == code }
        }
    }
}
