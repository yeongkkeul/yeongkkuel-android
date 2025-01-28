package com.example.yeongkkuel.presentation.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.example.yeongkkuel.databinding.PopupChatRoomRankBinding

class ChatRoomRankPopup(
    context: Context,
    anchorView: View,
) {
    private val popupWindow: PopupWindow
    private val binding: PopupChatRoomRankBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = PopupChatRoomRankBinding.inflate(inflater)

        popupWindow = PopupWindow(
            binding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true // 포커스 가능
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(null) // 배경 없애기

        popupWindow.showAsDropDown(anchorView, 230, 14)
    }
}