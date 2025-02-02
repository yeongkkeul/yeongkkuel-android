package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogChatRoomExitPartyManagerBinding

class ChatRoomExitPartyManagerDialog(
    context: Context,
    private val onCancelClick: () -> Unit,
    private val onExitClick: () -> Unit
) : Dialog(context, R.style.CustomDialogDimmed) {

    private lateinit var binding: DialogChatRoomExitPartyManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChatRoomExitPartyManagerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialogAppearance()
        setupListeners()

        binding.apply {
            Glide.with(ivThumbnailChatRoom.context)
                .load("https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg")
                .into(ivThumbnailChatRoom)
        }
    }

    private fun setupDialogAppearance() {
        window?.setLayout(
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_width),
            context.resources.getDimensionPixelSize(R.dimen.dialog_chat_room_exit_party_manager_height)
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