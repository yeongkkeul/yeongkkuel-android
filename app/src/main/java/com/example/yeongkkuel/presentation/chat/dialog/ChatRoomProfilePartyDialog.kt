package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomProfilePartyBinding

class ChatRoomProfilePartyDialog(
    context: Context,
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
                .load("https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg")
                .apply(RequestOptions.circleCropTransform())
                .into(ivProfileUser)
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