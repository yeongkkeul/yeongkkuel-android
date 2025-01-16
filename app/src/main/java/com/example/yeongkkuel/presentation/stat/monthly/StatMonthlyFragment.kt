package com.example.yeongkkuel.presentation.stat.monthly

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.yeongkkuel.databinding.FragmentStatMonthlyBinding

class StatMonthlyFragment : Fragment() {
    private var _binding: FragmentStatMonthlyBinding? = null
    private val binding: FragmentStatMonthlyBinding
        get() = requireNotNull(_binding) { "FragmentStatMonthlyBinding -> null" }

    private val viewModel: StatMonthlyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatMonthlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding){
    }


    private fun initViewModel() = with(viewModel) {}
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}