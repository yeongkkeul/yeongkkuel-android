package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomProfilePartyBinding
import com.example.yeongkkuel.network.response.chat.ChatRoomUserResult
import com.example.yeongkkuel.presentation.chat.data.Age
import com.example.yeongkkuel.presentation.chat.data.Job

class ChatRoomProfilePartyDialog(
    context: Context,
    private val userData: ChatRoomUserResult,
    private val onCancelClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomProfilePartyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomProfilePartyBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialogAppearance()
        setupListeners()

        binding.apply {
            Glide.with(ivProfileUser.context)
                .load(userData.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileUser)
            tvNicknameUser.text = userData.nickname
            val age = Age.valueOf(userData.age).displayName
            val job = Job.valueOf(userData.job).displayName
            tvStatusUser.text = "$age $job"
            tvDataRankExpense.text = userData.rank.toString()
            tvDataDateRegister.text = userData.createdAt
        }
    }

    private fun setupDialogAppearance() {
        window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_width),
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_profile_party_height)
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancelClick()
        }
    }
}