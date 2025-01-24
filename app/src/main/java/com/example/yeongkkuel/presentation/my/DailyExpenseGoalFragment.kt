package com.example.yeongkkuel.presentation.my

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentDailyExpenseGoalBinding
import com.example.yeongkkuel.databinding.FragmentEditProfileBinding

class DailyExpenseGoalFragment : Fragment() {
    private var _binding: FragmentDailyExpenseGoalBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyExpenseGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // EditText에 포커스를 설정하고 키보드 표시
        val editText = binding.etAmount
        editText.requestFocus()

        // 키보드 강제 표시
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)


        // 뒤로가기
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.tvEdit.setOnClickListener() {
            // TODO: 하루목표지출액 수정 - 백엔드 전송
        }

        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val amount = s.toString().replace(",", "").toIntOrNull() ?: 0
                val formatted = String.format("%,d", amount)
                binding.etAmount.removeTextChangedListener(this)
                binding.etAmount.setText(formatted)
                binding.etAmount.setSelection(formatted.length)
                binding.etAmount.addTextChangedListener(this)
            }
        })


    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





}