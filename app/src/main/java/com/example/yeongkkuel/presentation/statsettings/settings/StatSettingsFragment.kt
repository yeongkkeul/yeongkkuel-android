package com.example.yeongkkuel.presentation.statsettings.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.databinding.FragmentStatSettingsBinding
import com.example.yeongkkuel.presentation.statsettings.StatSettingsViewModel
import com.example.yeongkkuel.presentation.util.clearComma
import com.example.yeongkkuel.presentation.util.errorUnderline
import com.example.yeongkkuel.presentation.util.limitInt
import com.example.yeongkkuel.presentation.util.setUnderlineBehavior
import com.example.yeongkkuel.presentation.util.toMoneyString
import com.example.yeongkkuel.presentation.util.toNaviStat

class StatSettingsFragment : Fragment() {
    private var _binding: FragmentStatSettingsBinding? = null
    private val binding: FragmentStatSettingsBinding
        get() = requireNotNull(_binding) { "FragmentStatSettingsBinding -> null" }

    private val viewModel: StatSettingsViewModel by viewModels()

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
            ivTopArrow.setOnClickListener {
                activity?.onBackPressed()
            }
        }

        fun initEtListener() {
            etTargetSpending.run {
                toMoneyString()
                setUnderlineBehavior(tvTargetSpendingError)
                limitInt()
            }
        }

        fun onClickStorage() {
            tvStorage.setOnClickListener {
                val targetSpendingValue = etTargetSpending.text.toString().clearComma()

                if (targetSpendingValue != null) {
                    viewModel.setTargetSpending(
                        targetSpending = targetSpendingValue,
                        isSuccess = {
                            findNavController().toNaviStat()
                        }
                    )
                } else {
                    etTargetSpending.errorUnderline()
                    tvTargetSpendingError.visibility = View.VISIBLE
                }
            }

        }

        initBack()
        initEtListener()
        onClickStorage()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}