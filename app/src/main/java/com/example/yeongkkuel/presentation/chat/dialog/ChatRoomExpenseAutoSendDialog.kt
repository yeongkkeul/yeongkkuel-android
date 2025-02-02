package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomExpenseAutoSendBinding

class ChatRoomExpenseAutoSendDialog(
    context: Context,
    private val onCancelClick: () -> Unit,
    private val onConfirmClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomExpenseAutoSendBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomExpenseAutoSendBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialogAppearance()
        setupListeners()
    }

    private fun setupDialogAppearance() {
        window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_width),
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_expense_auto_send_height)
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancelClick()
        }

        binding.btnConfirm.setOnClickListener {
            dismiss()
            onConfirmClick()
        }
    }
}