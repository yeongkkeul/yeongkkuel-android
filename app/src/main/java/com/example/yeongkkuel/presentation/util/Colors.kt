package com.example.yeongkkuel.presentation.util

import com.example.yeongkkuel.R

enum class Colors(val id: Int, val red: Int, val green: Int, val blue: Int) {
    RED1(R.color.red1, 227, 0, 11),   // #E3000B
    RED2(R.color.red2, 254, 64, 64),   // #FE4040
    PINK3(R.color.pink3, 254, 64, 191), // #FE40BF
    PURPLE4(R.color.purple4, 191, 64, 254), // #BF40FE
    PURPLE5(R.color.purple5, 127, 64, 254), // #7F40FE
    BLUE6(R.color.blue6, 35, 95, 179), // #235FB3
    BLUE7(R.color.blue7, 63, 172, 218), // #3FACDA
    BLUE8(R.color.blue8, 83, 203, 255), // #53CBFF
    GREEN9(R.color.green9, 0, 191, 166), // #00BFA6
    GREEN10(R.color.green10, 75, 134, 46), // #4B862E
    GREEN11(R.color.green11, 81, 189, 27), // #51BD1B
    GREEN12(R.color.green12, 196, 224, 21), // #C4E015
    YELLOW13(R.color.yellow13, 254, 229, 64), // #FEE540
    ORANGE14(R.color.orange14, 251, 136, 9), // #FB8809
    ORANGE15(R.color.orange15, 255, 99, 22), // #FF6316
    TRASH(R.color.black2, 153, 153, 153); // #999999
//    BLACK1(R.color.black1, 238, 238, 238); // #EEEEEE

    // 추가: RGB 값을 계산해서 저장하는 프로퍼티
    val rgb: Int
        get() = (0xFF shl 24) or (red shl 16) or (green shl 8) or blue

    companion object {
        // id로 Colors Enum 찾기
        fun fromId(id: Int): Colors? {
            return entries.firstOrNull { it.id == id }
        }

        // RGB 값으로 Colors Enum 찾기
        fun fromRGB(red: Int, green: Int, blue: Int): Colors {
            return entries.firstOrNull { it.red == red && it.green == green && it.blue == blue } ?: TRASH
        }

        // 서버에서 사용하는 Code로 Colors Enum 찾기
        fun fromCode(code: String): Colors? {
            return entries.firstOrNull { it.name == code }
        }

    }
}
