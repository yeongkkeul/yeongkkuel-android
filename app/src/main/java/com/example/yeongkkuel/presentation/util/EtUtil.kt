package com.example.yeongkkuel.presentation.util

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.yeongkkuel.R

fun EditText.toMoneyString() {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrEmpty()) return

            // 포맷팅 전 문자열 제거 후 다시 설정
            removeTextChangedListener(this)

            try {
                val originalString = s.toString().replace(",", "")
                val formattedString = String.format("%,d", originalString.toLong())
                setText(formattedString)
                setSelection(formattedString.length)
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }

            addTextChangedListener(this)
        }

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
        }
    })
}


fun EditText.setUnderlineBehavior(errorText: TextView) {
    // 초기 상태에서 텍스트가 없으면 밑줄 추가
    if (this.text.isEmpty()) {
        this.addUnderline()
    }

    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            errorText.visibility = View.GONE
            if (s.isNullOrEmpty()) {
                addUnderline()
            } else {
                removeUnderline()
            }
        }
    })
}

fun EditText.setUnderlineBehavior() {
    if (this.text.isEmpty()) {
        this.addUnderline()
    }

    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s.isNullOrEmpty()) {
                addUnderline()
            } else {
                removeUnderline()
            }
        }
    })
}

private fun EditText.addUnderline() {
    this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.main1)
}

private fun EditText.removeUnderline() {
    this.backgroundTintList = ContextCompat.getColorStateList(context, android.R.color.transparent)
}

fun EditText.errorUnderline() {
    this.backgroundTintList = ContextCompat.getColorStateList(context, R.color.error)
}

fun EditText.setLimit(limit: Int) {
    val filter = InputFilterMinMax(0, limit)
    this.filters = arrayOf(filter)  // 입력 필터 적용
}

class InputFilterMinMax(private val min: Int, private val max: Int) : InputFilter {
    constructor(min: String, max: String) : this(min.toInt(), max.toInt())

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence? {
        // 현재 EditText에 입력된 텍스트를 쉼표 제거 후 처리
        val currentText = (dest?.toString() ?: "") + source.toString()
        val textWithoutComma = currentText.replace(",", "")

        try {
            val input = textWithoutComma.toInt() // 쉼표 제거 후 숫자 변환

            // 입력 값이 min과 max 범위에 속하는지 체크
            if (isInRange(min, max, input)) {
                return null // 범위 내 값이면 입력 허용
            }
        } catch (nfe: NumberFormatException) {
            // 숫자 형식 오류 처리
        }

        return "" // 범위 외 값이면 입력을 막음
    }

    private fun isInRange(a: Int, b: Int, c: Int): Boolean {
        return if (b > a) {
            c in a..b
        } else {
            c in b..a
        }
    }
}




