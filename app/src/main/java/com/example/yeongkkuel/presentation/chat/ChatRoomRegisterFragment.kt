package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomRegisterBinding

class ChatRoomRegisterFragment : Fragment() {

    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomRegisterBinding? = null
    private val binding: FragmentChatRoomRegisterBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRegisterBinding -> null"}

    private val expel = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomRegisterBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        binding.apply {
            Glide.with(ivChatRoomThumbnail.context)
                .load("https://helios-i.mashable.com/imagery/articles/04GeUVUQwZxpTYXdqbocKH2/hero-image.fill.size_1248x702.v1722586579.jpg")
                .into(ivChatRoomThumbnail)
        }

        binding.btnRegister.setOnClickListener {
            val dialog = ChatRoomPwDialog(
                context = requireContext(),
                onCancelClick = {  },
                onConfirmClick = { expenseAutoSendDialogShow() }
            )
            dialog.show()
        }
    }

    private fun expenseAutoSendDialogShow() {
        val dialog = ChatRoomExpenseAutoSendDialog(
            context = requireContext(),
            onCancelClick = {  },
            onConfirmClick = { checkExpel() }
        )
        dialog.show()
    }

    private fun checkExpel() {
        if (expel) {
            val dialog = ChatRoomExpelDialog(
                context = requireContext(),
                onConfirmClick = { }
            )
            dialog.show()
        } else {
            navController.navigate(R.id.action_navigation_chat_room_register_to_chat_group)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}