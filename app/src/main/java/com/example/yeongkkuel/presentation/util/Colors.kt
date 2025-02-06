package com.example.yeongkkuel.presentation.util

import com.example.yeongkkuel.R

enum class Colors(val id: Int, val rgb: Int) {
    RED1(R.color.red1, 0xFFE3000B.toInt()),   // #E3000B
    RED2(R.color.red2, 0xFFFE4040.toInt()),   // #FE4040
    PINK3(R.color.pink3, 0xFFFE40BF.toInt()), // #FE40BF
    PURPLE4(R.color.purple4, 0xFFBF40FE.toInt()), // #BF40FE
    PURPLE5(R.color.purple5, 0xFF7F40FE.toInt()), // #7F40FE
    BLUE6(R.color.blue6, 0xFF235FB3.toInt()), // #235FB3
    BLUE7(R.color.blue7, 0xFF3FACDA.toInt()), // #3FACDA
    BLUE8(R.color.blue8, 0xFF53CBFF.toInt()), // #53CBFF
    GREEN9(R.color.green9, 0xFF00BFA6.toInt()), // #00BFA6
    GREEN10(R.color.green10, 0xFF4B862E.toInt()), // #4B862E
    GREEN11(R.color.green11, 0xFF51BD1B.toInt()), // #51BD1B
    GREEN12(R.color.green12, 0xFFC4E015.toInt()), // #C4E015
    YELLOW13(R.color.yellow13, 0xFFFEE540.toInt()), // #FEE540
    ORANGE14(R.color.orange14, 0xFFFB8809.toInt()), // #FB8809
    ORANGE15(R.color.orange15, 0xFFFF6316.toInt()), // #FF6316,
    BLACK1(R.color.black1, 0xFFEEEEEE.toInt()); //#EEEEEE

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

        fun getRGB(red:Int, blue:Int, green:Int): Colors{
            return RED1
        }
    }
}
