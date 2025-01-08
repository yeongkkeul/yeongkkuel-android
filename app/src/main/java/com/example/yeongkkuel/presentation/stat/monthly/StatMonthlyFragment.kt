package com.example.yeongkkuel.presentation.stat.monthly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentStatDailyBinding
import com.example.yeongkkuel.databinding.FragmentStatMonthlyBinding

class StatMonthlyFragment : Fragment() {
    private var _binding: FragmentStatMonthlyBinding? = null
    private val binding: FragmentStatMonthlyBinding
        get() = requireNotNull(_binding) { "FragmentStatMonthlyBinding -> null" }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatMonthlyBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}