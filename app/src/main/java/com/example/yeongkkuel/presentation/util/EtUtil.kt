package com.example.yeongkkuel.presentation.util

import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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


fun EditText.setUnderlineBehavior() {
    // 초기 상태에서 텍스트가 없으면 밑줄 추가
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
