package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomExpelUserBinding

class ChatRoomExpelUserDialog(
    context: Context,
    private val onCancelClick: () -> Unit,
    private val onExitClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomExpelUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomExpelUserBinding.inflate(LayoutInflater.from(context))
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
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_expel_user_height)
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
            onCancelClick()
        }

        binding.btnExit.setOnClickListener {
            dismiss()
            onExitClick()
        }
    }
}