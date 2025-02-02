package com.example.yeongkkuel.presentation.chat

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentFilterExpenseDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterExpenseDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterExpenseDialogBinding? = null
    private val binding: FragmentFilterExpenseDialogBinding
        get() = requireNotNull(_binding){"FragmentFilterExpenseDialogBinding -> null"}

    private val viewModel: ChatSearchViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterExpenseDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
