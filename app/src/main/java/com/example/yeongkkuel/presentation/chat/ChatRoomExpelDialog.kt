package com.example.yeongkkuel.presentation.chat

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomExpelBinding

class ChatRoomExpelDialog(
    context: Context,
    private val onConfirmClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomExpelBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomExpelBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialogAppearance()
        setupListeners()
    }

    private fun setupDialogAppearance() {
        window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_width),
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_expel_height)
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupListeners() {
        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirmClick()
        }
    }
}