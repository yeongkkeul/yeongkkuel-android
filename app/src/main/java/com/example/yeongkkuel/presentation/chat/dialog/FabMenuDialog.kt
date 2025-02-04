package com.example.yeongkkuel.presentation.chat.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.DialogFabMenuBinding
import timber.log.Timber

class FabMenuDialog(
    context: Context,
    private val anchorView: View,
    private val onDismissCallback: () -> Unit,
    private val onMenuItem1Click: () -> Unit,
    private val onMenuItem2Click: () -> Unit
) : Dialog(context, R.style.CustomDialog) {

    private lateinit var binding: DialogFabMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogFabMenuBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        binding.root.setBackgroundResource(R.drawable.bg_menu_fab)

        binding.clFabMenuChat.setOnClickListener {
            dismiss()
            onMenuItem1Click()
        }
        binding.clFabMenuCompass.setOnClickListener {
            dismiss()
            onMenuItem2Click()
        }

        setOnDismissListener {
            dismiss()
            Timber.d("onDismiss")
            onDismissCallback()
        }

        setDialogPosition()
        setDimmedBackground()
    }

    private fun setDialogPosition() {
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val params = window?.attributes
        params?.gravity = 0
        params?.x = 300
        params?.y = 550
        window?.attributes = params
    }

    private fun setDimmedBackground() {
        val dimView = View(context)
        dimView.setBackgroundColor(Color.parseColor("#80000000"))
        (context as? Activity)?.addContentView(
            dimView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        anchorView.bringToFront() // FAB를 최상위로 가져옴
    }
}