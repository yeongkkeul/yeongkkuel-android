package com.example.yeongkkuel.presentation.util

enum class Job(val en: String, val kor: String) {
    STUDENT("STUDENT", "학생"),
    EMPLOYEE("EMPLOYEE", "직장인"),
    HOMEMAKER("HOMEMAKER", "주부"),
    SELF_EMPLOYED("SELF_EMPLOYED", "자영업자"),
    UNDECIDED("UNDECIDED", "선택 X 경우");

    override fun toString(): String {
        return kor // 기본적으로 한글로 반환
    }

    companion object {
        fun getEnToKor(en: String): String {
            return values().find { it.en == en }?.kor ?: "알 수 없음"
        }
    }
}


enum class Age(val en: String, val kor: String) {
    UNDECIDED("UNDECIDED", "선택 X 경우"),
    TEENAGER("TEENAGER", "14~19세"),
    TWENTIES("TWENTIES", "20대"),
    THIRTIES("THIRTIES", "30대"),
    FORTIES("FORTIES", "40대"),
    FIFTIES("FIFTIES", "50대"),
    SIXTIES_AND_ABOVE("SIXTIES_AND_ABOVE", "60대 이상");

    override fun toString(): String {
        return kor
    }

    companion object{
        fun getEnToKor(en: String): String {
            return Age.values().find { it.en == en }?.kor ?: "알 수 없음"
        }
    }
}