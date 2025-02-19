package com.example.yeongkkuel.presentation.home.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentCategoryDetailBinding
import com.example.yeongkkuel.databinding.ItemMenuPopupBinding
import com.example.yeongkkuel.presentation.botsheet.BotSheetViewModel
import com.example.yeongkkuel.presentation.util.Colors

class CategoryDetailFragment : Fragment() {

    private var _binding: FragmentCategoryDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by activityViewModels()
    private val botSheetViewModel: BotSheetViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 전달받은 카테고리 데이터 가져오기 (Bundle 사용)
        val categoryName = arguments?.getString("categoryName")
        val categoryColorId = arguments?.getInt("categoryColor")

        // Colors에서 id로 정확한 색상 값을 가져옴
        val categoryColor = categoryColorId?.let { Colors.fromId(it)?.id }
        val textColor = categoryColor?.let { ContextCompat.getColor(requireContext(), it) }
            ?: android.graphics.Color.BLACK

        // 데이터 화면에 표시
        binding.tvCategoryDetailInput.setText(categoryName)
        binding.tvCategoryDetailInput.setTextColor(textColor)
        binding.ivSelectedColor.setBackgroundResource(R.drawable.bg_color_circle) // 원형 배경 설정
        binding.ivSelectedColor.background.setTint(textColor) // 선택된 색상 적용

        // 뒤로가기 버튼 클릭 이벤트
        binding.ivBack.setOnClickListener {
            findNavController().popBackStack() // 이전 화면으로 이동
        }

        // 더보기 버튼 클릭 이벤트: PopupWindow 대신 cl_more의 visibility 토글
        binding.tvCategoryMore.setOnClickListener {
            binding.clMore.visibility =
                if (binding.clMore.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        // cl_more 내 수정 버튼 클릭 이벤트
        binding.clMore.findViewById<TextView>(R.id.tv_modify).setOnClickListener {
            // 수정 화면으로 이동
            val bundle = Bundle().apply {
                putString("categoryName", categoryName)
                putInt("categoryColor", categoryColorId ?: android.graphics.Color.BLACK)
            }
            findNavController().navigate(
                R.id.action_categoryDetailFragment_to_categoryEditFragment,
                bundle
            )
            binding.clMore.visibility = View.GONE
        }

        // cl_more 내 삭제 버튼 클릭 이벤트
        binding.clMore.findViewById<TextView>(R.id.tv_delete).setOnClickListener {
            categoryName?.let {
                showDeleteConfirmationDialog(it) // Non-nullable로 전달
            } ?: run {
                Toast.makeText(requireContext(), "카테고리 이름이 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.clMore.visibility = View.GONE
        }
    }

    // 삭제 확인 다이얼로그 표시
    private fun showDeleteConfirmationDialog(categoryName: String) {
        // 다이얼로그 뷰 inflate
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_delete, null)

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 다이얼로그 스타일 설정
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // 다이얼로그 내 버튼 설정
        val cancelBtn = dialogView.findViewById<TextView>(R.id.tv_cancel_btn)
        val deleteBtn = dialogView.findViewById<TextView>(R.id.tv_delete_btn)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        deleteBtn.setOnClickListener {
            // ViewModel을 통해 삭제 처리
            botSheetViewModel.removeCategory(
                categoryName = categoryName,
                isSuccess = {
                    val remainingCategories = botSheetViewModel.uiState.value.spendingList.size
                    if (remainingCategories < 1) {
                        // 홈 화면으로 이동
                        findNavController().navigate(R.id.action_categoryDetailFragment_to_navigation_home)
                        Toast.makeText(
                            requireContext(),
                            "모든 카테고리가 삭제되어 홈 화면으로 이동합니다.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        // 단순히 이전 화면으로 이동
                        Toast.makeText(requireContext(), "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack() // 이전 화면으로 이동
                    }
                },
                isFalse = {
                    Toast.makeText(requireContext(), "오류 발생. 잠시 후 시도해주세요.", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().popBackStack() // 이전 화면으로 이동
                })

            // 다이얼로그 먼저 닫기
            dialog.dismiss()

            // 남아 있는 카테고리 확인
        }

        dialog.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
