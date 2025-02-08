package com.example.yeongkkuel.presentation.my

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentMyBinding
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.base.MainActivity
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels({ requireActivity() })
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        // 클릭 리스너들
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            response.result?.let { result ->
                binding.tvNickname.text = result.nickname
                binding.tvAge.text = result.ageGroup + "대"
                binding.tvJob.text = result.job
                binding.ivProfile.setImageURI(Uri.parse(result.profileImageUrl))
                binding.tvEmail.text = result.email
                binding.tvDailyLimit.text = result.dayTargetExpenditure.toString() + "원"
                binding.tvDailyPercent.text = result.weeklyAchievementRate.toString() + "%"
            }
        }
    }


    private fun setupClickListeners() {
        // 1. 프로필 수정
        binding.tvProfileEdit.setOnClickListener {
            // MyPage -> ProfileEditFragment 로 이동
            findNavController().navigate(R.id.action_myFragment_to_navigation_my_edit)
        }

        // 2. 하루목표 지출액 '수정'
        binding.tvDailyEdit.setOnClickListener {
            // MyPage -> DailyGoalEditFragment (가정)
            findNavController().navigate(R.id.action_myFragment_to_navigation_daily_expense_goal)
        }

        // 알림 아이콘
        binding.ivNoti.setOnClickListener {
            // 이동: MyPage -> NotiFragment
            findNavController().navigate(R.id.action_myPageFragment_to_notificationFragment)
        }
        binding.ivRewardMore.setOnClickListener{
            // 이동: MyPage -> RewardFragment
            findNavController().navigate(R.id.action_myPageFragment_to_rewardFragment)
        }

        // 친구초대
        binding.tvInviteFriend.setOnClickListener {
            showInviteCodeModal()
        }

        // 로그아웃
        binding.tvLogout.setOnClickListener {
            showLogoutModal()
        }

        // 탈퇴
        binding.tvWithdraw.setOnClickListener {
            showWithdrawModal()
        }


    }

    // 모달
    private fun showInviteCodeModal() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // 어두워지는 정도 설정 (0.0 ~ 1.0)
        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        dialog.setContentView(R.layout.dialog_my_recommend_code)
        dialog.setCancelable(true)

        val codeTv: TextView = dialog.findViewById<TextView>(R.id.tv_my_recommend_code)
        val copyBtn = dialog.findViewById<ImageView>(R.id.iv_copy)


        //클립보드에 복사 하고 모달 나가기
        copyBtn.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(/* label = */ "inviteCode", /* text = */ codeTv.text)
            clipboard.setPrimaryClip(clip)
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun showLogoutModal() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // 어두워지는 정도 설정 (0.0 ~ 1.0)
        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        // View Binding을 Dialog에도 적용하려면 별도의 Binding 클래스를 inflate 해주거나
        // findViewById 방식을 계속 사용할 수 있습니다.
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(false)


        val logoutBtn = dialog.findViewById<TextView>(R.id.tv_logout)
        val cancelBtn = dialog.findViewById<TextView>(R.id.tv_cancel)
        // 건너뛰기
        //copybtn 클릭시 클립보드에 복사 하고 모달 나가기
        logoutBtn.setOnClickListener {
            //TODO: 로그아웃 로직

            // 1) 소셜 로그아웃
//            logoutSocialIfNeeded()

            // 2) 내부 JWT 삭제
            clearLocalToken()

            // 3) 필요 시 서버에 로그아웃 API (선택적)
            // logoutServerApi()

            // 4) 로그인 화면으로 이동
            navigateToLogin()

            dialog.dismiss()
        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showWithdrawModal() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // 어두워지는 정도 설정 (0.0 ~ 1.0)
        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        // View Binding을 Dialog에도 적용하려면 별도의 Binding 클래스를 inflate 해주거나
        // findViewById 방식을 계속 사용할 수 있습니다.
        dialog.setContentView(R.layout.dialog_withdraw)
        dialog.setCancelable(false)


        val withdrawBtn = dialog.findViewById<TextView>(R.id.tv_withdraw)
        val cancelBtn = dialog.findViewById<TextView>(R.id.tv_cancel)


        withdrawBtn.setOnClickListener {
            showWithdrawDetailModal()
            dialog.dismiss()


        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showWithdrawDetailModal() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        // 어두워지는 정도 설정 (0.0 ~ 1.0)
        val params = dialog.window?.attributes
        params?.dimAmount = 0.65f
        dialog.window?.attributes = params

        // View Binding을 Dialog에도 적용하려면 별도의 Binding 클래스를 inflate 해주거나
        // findViewById 방식을 계속 사용할 수 있습니다.
        dialog.setContentView(R.layout.dialog_withdraw_detail)
        dialog.setCancelable(false)


        val ivCheckLowUsage = dialog.findViewById<ImageView>(R.id.ivCheckLowUsage)
        val ivCheckService = dialog.findViewById<ImageView>(R.id.ivCheckService)
        val ivCheckBoredom = dialog.findViewById<ImageView>(R.id.ivCheckBoredom)
        val ivCheckDifficulty = dialog.findViewById<ImageView>(R.id.ivCheckDifficulty)
        val ivCheckElse = dialog.findViewById<ImageView>(R.id.ivCheckElse)

        val withdrawBtn = dialog.findViewById<TextView>(R.id.tv_withdraw)
        val cancelBtn = dialog.findViewById<TextView>(R.id.tv_cancel)
        val tvElseDetailCount = dialog.findViewById<TextView>(R.id.tvElseDetailCount)

        val etElseDetail = dialog.findViewById<EditText>(R.id.et_else_detail)

        ivCheckLowUsage.setOnClickListener {
            resetAllChecks(dialog)
            ivCheckLowUsage.isSelected = !ivCheckLowUsage.isSelected
            withdrawBtn.isEnabled = ivCheckLowUsage.isSelected

        }
        ivCheckService.setOnClickListener {
            resetAllChecks(dialog)
            ivCheckService.isSelected = !ivCheckService.isSelected
            withdrawBtn.isEnabled = ivCheckService.isSelected

        }
        ivCheckBoredom.setOnClickListener {
            resetAllChecks(dialog)
            ivCheckBoredom.isSelected = !ivCheckBoredom.isSelected
            withdrawBtn.isEnabled = ivCheckBoredom.isSelected
        }
        ivCheckDifficulty.setOnClickListener {
            resetAllChecks(dialog)
            ivCheckDifficulty.isSelected = !ivCheckDifficulty.isSelected
            withdrawBtn.isEnabled = ivCheckDifficulty.isSelected
        }
        ivCheckElse.setOnClickListener {
            resetAllChecks(dialog)
            ivCheckElse.isSelected = !ivCheckElse.isSelected
            etElseDetail.isEnabled = ivCheckElse.isSelected


            etElseDetail.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    tvElseDetailCount.text = "${s?.length ?: 0}/100"
                }

                override fun afterTextChanged(s: android.text.Editable?) {
                    withdrawBtn.isEnabled = s?.length!! > 0
                }
            })

        }

        withdrawBtn.setOnClickListener {
            //TODO: 탈퇴 로직
            // TODO: 실제로 탈퇴(연결 끊기) 로직 실행
            unlinkSocialIfNeeded()      // 카카오 unlink, 구글 revokeAccess
            clearLocalToken()           // 내부 JWT 삭제
            withdrawServerApi()         // 서버 DB에서 사용자 삭제 (탈퇴 API)
            navigateToLogin()           // or 앱 초기화
            dialog.dismiss()

        }
        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun resetAllChecks(dialog: Dialog) {
        val ivCheckLowUsage = dialog.findViewById<ImageView>(R.id.ivCheckLowUsage)
        val ivCheckService = dialog.findViewById<ImageView>(R.id.ivCheckService)
        val ivCheckBoredom = dialog.findViewById<ImageView>(R.id.ivCheckBoredom)
        val ivCheckDifficulty = dialog.findViewById<ImageView>(R.id.ivCheckDifficulty)
        val ivCheckElse = dialog.findViewById<ImageView>(R.id.ivCheckElse)
        val etElseDetail = dialog.findViewById<EditText>(R.id.et_else_detail)
        val withdrawBtn = dialog.findViewById<TextView>(R.id.tv_withdraw)

        ivCheckLowUsage.isSelected = false
        ivCheckService.isSelected = false
        ivCheckBoredom.isSelected = false
        ivCheckDifficulty.isSelected = false
        ivCheckElse.isSelected = false
        etElseDetail.isEnabled = false
        withdrawBtn.isEnabled = false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun logoutSocialIfNeeded() {
        val loginProvider = getLoginProvider() // "kakao", "google" 등

        when (loginProvider) {
            "kakao" -> {
                // 카카오 로그아웃
                UserApiClient.instance.logout { error ->
                    if (error != null) {
                        // 로그 남기기
                    } else {
                        // 카카오 로그아웃 완료
                    }
                }
            }
            "google" -> {
                // 구글 로그아웃
                /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                googleSignInClient.signOut().addOnCompleteListener {
                    // 구글 로그아웃 완료*/
                }
            else -> {
                // 일반 로그인 or 미로그인 상태
            }

            }

        }


    /**
     * 탈퇴(연결 끊기)
     * 카카오는 unlink, 구글은 revokeAccess
     */
    private fun unlinkSocialIfNeeded() {
        val loginProvider = getLoginProvider()
        when (loginProvider) {
            "kakao" -> {
                // 카카오 연결 끊기
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        // 에러 처리
                    } else {
                        // 연결 끊기 성공
                    }
                }
            }
            "google" -> {
                // 구글 연결 끊기
                /*val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    // 구글 계정 접근 해제
                }*/
            }
        }
    }

    private fun clearLocalToken() {
        //tokenmanager 사용
        TokenManager.clearTokens(requireContext())
        //토큰이 잘 없어졌는지 로깅
        Timber.d("TokenManager: ${TokenManager.getAccessToken(requireContext())}")
    }

    private fun getLoginProvider(): String {
        // 예: SharedPreferences에서 "login_provider" 값 가져오기
        val prefs = requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("login_provider", "") ?: ""
    }


    private fun logoutServerApi() {
        // 예: Retrofit2, OkHttp 등을 사용해 서버 로그아웃 API 호출
        // ex) apiService.logout("Bearer $jwt").enqueue(...)
    }

    private fun withdrawServerApi() {
        // 예: Retrofit2, OkHttp로 탈퇴 API 호출
        // ex) apiService.withdraw("Bearer $jwt").enqueue(...)
    }

    private fun navigateToLogin() {
        //TODO : 로그인 화면으로 이동

        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


}