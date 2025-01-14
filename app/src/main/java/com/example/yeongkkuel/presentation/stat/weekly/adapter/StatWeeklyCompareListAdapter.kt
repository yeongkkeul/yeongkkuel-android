package com.example.yeongkkuel.presentation.stat.weekly.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yeongkkuel.databinding.ItemStatWeeklyCompareBinding
import com.example.yeongkkuel.presentation.stat.weekly.SpendingUnit
import com.example.yeongkkuel.presentation.stat.weekly.StatWeeklyUiState
import com.example.yeongkkuel.presentation.util.toMoneyString

class StatWeeklyCompareListAdapter(
) : ListAdapter<StatWeeklyUiState.CompareData, StatWeeklyCompareListAdapter.ViewHolder>(
    StatWeeklyCompareDiffUtil()
) {
    inner class ViewHolder(
        private val binding: ItemStatWeeklyCompareBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: StatWeeklyUiState.CompareData) = with(binding) {
            when (item) {
                is StatWeeklyUiState.CompareData.OthersCompare -> {
                    tvComparedUnit.text = item.spendingUnit.kor
                    tvMyUnit.text = item.spendingUnit.kor

                    tvCompareDescription.text = "${item.target} 중 상위 ${item.percentile}%"

                    tvComparedTarget.text = "${item.target} 평균"
                    tvComparedMoney.text = "${item.targetSpending.toMoneyString()}원"

                    tvMyTarget.text = "나"
                    tvMyMoney.text = "${item.mySpending.toMoneyString()}원"
                }

                is StatWeeklyUiState.CompareData.PastCompare -> {
                    tvComparedUnit.text = item.spendingUnit.kor
                    tvMyUnit.text = item.spendingUnit.kor

                    val spendingDiff = item.currentSpending - item.pastSpending

                    when (item.spendingUnit) {
                        SpendingUnit.DAY -> {
                            tvComparedTarget.text = "어제"
                            tvMyTarget.text = "오늘"

                            tvCompareDescription.text =
                                if (spendingDiff >= 0) "어제보다 ${spendingDiff} 덜 썼어요"
                                else "어제보다 ${-spendingDiff} 더 썼어요"
                        }

                        SpendingUnit.WEEK -> {
                            tvComparedTarget.text = "저번 주"
                            tvMyTarget.text = "이번 주"

                            tvCompareDescription.text =
                                if (spendingDiff >= 0) "저번 주보다 ${spendingDiff} 덜 썼어요"
                            else "저번 주보다 ${-spendingDiff} 더 썼어요"
                        }

                        SpendingUnit.MONTH -> {
                            tvComparedTarget.text = "저번 달"
                            tvMyTarget.text = "이번 달"

                            tvCompareDescription.text =
                                if (spendingDiff >= 0) "저번 달보다 ${spendingDiff} 덜 썼어요"
                                else "저번 달보다 ${-spendingDiff} 더 썼어요"
                        }
                    }

                    tvComparedMoney.text = "${item.pastSpending.toMoneyString()}원"
                    tvMyMoney.text = "${item.currentSpending.toMoneyString()}원"
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            binding = ItemStatWeeklyCompareBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}

private class StatWeeklyCompareDiffUtil : DiffUtil.ItemCallback<StatWeeklyUiState.CompareData>() {
    override fun areItemsTheSame(
        oldItem: StatWeeklyUiState.CompareData,
        newItem: StatWeeklyUiState.CompareData
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: StatWeeklyUiState.CompareData,
        newItem: StatWeeklyUiState.CompareData
    ): Boolean {
        return when {
            oldItem is StatWeeklyUiState.CompareData.OthersCompare &&
                    newItem is StatWeeklyUiState.CompareData.OthersCompare -> {
                oldItem.target == newItem.target &&
                        oldItem.spendingUnit == newItem.spendingUnit
            }

            oldItem is StatWeeklyUiState.CompareData.PastCompare &&
                    newItem is StatWeeklyUiState.CompareData.PastCompare -> {
                oldItem.pastSpending == newItem.pastSpending &&
                        oldItem.currentSpending == newItem.currentSpending &&
                        oldItem.spendingUnit == newItem.spendingUnit
            }

            else -> false
        }
    }
}