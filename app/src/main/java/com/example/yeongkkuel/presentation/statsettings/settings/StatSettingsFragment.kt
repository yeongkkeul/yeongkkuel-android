package com.example.yeongkkuel.presentation.statsettings.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatSettingsBinding

class StatSettingsFragment : Fragment() {
    private var _binding: FragmentStatSettingsBinding? = null
    private val binding: FragmentStatSettingsBinding
        get() = requireNotNull(_binding) { "FragmentStatSettingsBinding -> null" }

    val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val navOptions = navOptions {
                popUpTo(R.id.navigation_stat) {
                    inclusive = false // 특정 프래그먼트를 제외하고 그 위의 프래그먼트들을 pop
                }
                launchSingleTop = true // 새로운 목적지로 이동할 때 중복되지 않도록 설정
            }

            findNavController().navigate(R.id.navigation_stat, null, navOptions)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {

        fun initBack() {
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                backPressedCallback
            )

            ivTopArrow.setOnClickListener {
                activity?.onBackPressed()
            }
        }

        initBack()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        backPressedCallback.remove()
    }
}