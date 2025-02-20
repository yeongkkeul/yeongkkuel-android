package com.example.yeongkkuel.presentation.my

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentServiceTermBinding
import com.example.yeongkkuel.databinding.FragmentTerm4Binding
import com.example.yeongkkuel.presentation.signup.TermsViewModel


class ServiceTermFragment : Fragment() {
    private var _binding: FragmentServiceTermBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TermsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentServiceTermBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // (1) 뒤로가기 버튼 클릭 시 뒤로감
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}