package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.databinding.FragmentChatRoomRankDailyBinding

class ChatRoomRankDailyFragment : Fragment() {

    private lateinit var navController: NavController

    private var _binding: FragmentChatRoomRankDailyBinding? = null
    private val binding: FragmentChatRoomRankDailyBinding
        get() = requireNotNull(_binding){"FragmentChatRoomRankDailyBinding -> null"}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomRankDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}