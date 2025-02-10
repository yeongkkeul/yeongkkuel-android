package com.example.yeongkkuel.presentation.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentChatRoomPhotoBinding
import com.example.yeongkkuel.utils.ItemMixDecoration

class ChatRoomPhotoFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentChatRoomPhotoBinding? = null
    private val binding: FragmentChatRoomPhotoBinding
        get() = requireNotNull(_binding){"FragmentChatRoomPhotoBinding -> null"}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChatRoomPhotoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        binding.btnBack.setOnClickListener {
            navController.navigateUp()
        }

        val placePhotoAdapter = ChatRoomDrawerPhotoAdapter(getPlacePhotoData())
        binding.rvPhoto.apply {
            adapter = placePhotoAdapter
            val itemSpace = resources.getDimensionPixelSize(R.dimen.chat_room_photo)
            addItemDecoration(ItemMixDecoration(itemSpace))
        }
    }

    private fun getPlacePhotoData() : List<String> {
        return listOf(
            "https://images.pexels.com/photos/303324/pexels-photo-303324.jpeg",
            "https://images.pexels.com/photos/374885/pexels-photo-374885.jpeg",
            "https://images.pexels.com/photos/1267320/pexels-photo-1267320.jpeg",
            "https://images.pexels.com/photos/1107840/pexels-photo-1107840.jpeg",
            "https://images.pexels.com/photos/110854/pexels-photo-110854.jpeg",
            "https://images.pexels.com/photos/549083/pexels-photo-549083.jpeg",
            "https://images.pexels.com/photos/1395158/pexels-photo-1395158.jpeg",
            "https://images.pexels.com/photos/303324/pexels-photo-303324.jpeg",
            "https://images.pexels.com/photos/374885/pexels-photo-374885.jpeg",
            "https://images.pexels.com/photos/1267320/pexels-photo-1267320.jpeg",
            "https://images.pexels.com/photos/1107840/pexels-photo-1107840.jpeg",
            "https://images.pexels.com/photos/110854/pexels-photo-110854.jpeg",
            "https://images.pexels.com/photos/549083/pexels-photo-549083.jpeg",
            "https://images.pexels.com/photos/1395158/pexels-photo-1395158.jpeg",
            "https://images.pexels.com/photos/303324/pexels-photo-303324.jpeg",
            "https://images.pexels.com/photos/374885/pexels-photo-374885.jpeg",
            "https://images.pexels.com/photos/1267320/pexels-photo-1267320.jpeg",
            "https://images.pexels.com/photos/1107840/pexels-photo-1107840.jpeg",
            "https://images.pexels.com/photos/110854/pexels-photo-110854.jpeg",
            "https://images.pexels.com/photos/549083/pexels-photo-549083.jpeg"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}