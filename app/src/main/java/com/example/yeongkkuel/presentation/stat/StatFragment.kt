package com.example.yeongkkuel.presentation.stat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.yeongkkuel.databinding.FragmentStatBinding
import com.google.android.material.tabs.TabLayoutMediator

class StatFragment : Fragment() {
    private lateinit var navController: NavController
    private var _binding: FragmentStatBinding? = null
    private val binding: FragmentStatBinding
        get() = requireNotNull(_binding){"FragmentStatBinding -> null"}

    private val viewPagerAdapter:StatViewPagerAdapter by lazy {
        StatViewPagerAdapter(this@StatFragment)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        initView()
    }

    private fun initView() = with(binding){
        vpStat.adapter = viewPagerAdapter
        vpStat.offscreenPageLimit= viewPagerAdapter.itemCount

        TabLayoutMediator(tlStat, vpStat){tab, position ->
            tab.setText(viewPagerAdapter.getTitle(position))
        }.attach()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}