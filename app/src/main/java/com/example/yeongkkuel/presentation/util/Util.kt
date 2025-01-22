package com.example.yeongkkuel.presentation.util

import android.content.Context
import android.graphics.Paint
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.example.yeongkkuel.R
import java.text.NumberFormat
import java.util.Locale

fun Int.toMoneyString(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
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
    // 밑줄 추가
    this.paintFlags = this.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    this.setTextColor(ContextCompat.getColor(context, R.color.main1)) // 밑줄 색상 설정
}

private fun EditText.removeUnderline() {
    // 밑줄 제거
    this.paintFlags = this.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
}