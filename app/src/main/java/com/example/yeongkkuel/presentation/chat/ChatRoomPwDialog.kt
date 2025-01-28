package com.example.yeongkkuel.presentation.chat

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomPwBinding

class ChatRoomPwDialog(
    context: Context,
    private val onCancelClick: () -> Unit,
    private val onConfirmClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomPwBinding
    private var isPasswordVisible = false // 비밀번호 표시 상태

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomPwBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialogAppearance()
        setupListeners()
    }

    private fun setupDialogAppearance() {
        window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_width),
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_pw_height)
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancelClick()
        }

        binding.btnConfirm.setOnClickListener {
            if (binding.etPassword.text.toString() == "1234") {
                dismiss()
                onConfirmClick()
            } else {
                binding.etPassword.setBackgroundResource(R.drawable.bg_edittext_pw_error)
                binding.tvWarningChatRoomPw.visibility = View.VISIBLE
            }
        }

        binding.etPassword.setOnClickListener{
            binding.tvWarningChatRoomPw.visibility = View.GONE
            binding.etPassword.setBackgroundResource(R.drawable.bg_edittext_pw)
        }

        binding.btnShowOffPw.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 비밀번호 숨김 모드
            binding.etPassword.inputType = InputType.TYPE_CLASS_NUMBER
            binding.btnShowOffPw.setImageResource(R.drawable.btn_eye_on) // 눈 아이콘 변경
        } else {
            // 비밀번호 보임 모드
            binding.etPassword.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            binding.btnShowOffPw.setImageResource(R.drawable.btn_eye_off) // 눈 아이콘 변경
        }
        isPasswordVisible = !isPasswordVisible
    }
}