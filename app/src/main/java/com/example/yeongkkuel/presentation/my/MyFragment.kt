package com.example.yeongkkuel.presentation.my

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.credentials.CredentialManager

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CustomCredential
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.yeongkkuel.R
import com.example.yeongkkuel.databinding.FragmentMyBinding
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.presentation.auth.TokenManager
import com.example.yeongkkuel.presentation.base.MainActivity
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.launch
import timber.log.Timber

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private var pressedTime = 0L

    private val notificationViewModel: NotificationViewModel by viewModels()
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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() > pressedTime + 2000) {
                    pressedTime = System.currentTimeMillis()
                    Toast.makeText(requireContext(), "한번 더 누르면 종료", Toast.LENGTH_SHORT).show()
                } else {
                    requireActivity().finish()
                }
            }
        })



        observeViewModel()
        viewModel.fetchUserProfile()
        setupClickListeners()
        initAppbar()
    }

    private fun initAppbar(){
        binding.includeTopbar.run {
            ivMore.visibility = View.GONE
            ivNoti.setOnClickListener {
                findNavController().navigate(R.id.navigation_notification)
            }

            val layoutParams = ivNoti.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = 12
            ivNoti.layoutParams = layoutParams

            notificationViewModel.checkUnreadNotifications()
            notificationViewModel.unreadNotification.observe(viewLifecycleOwner) { hasUnread ->
                binding.includeTopbar.ivNotiDot.visibility =
                    if (hasUnread) View.VISIBLE else View.INVISIBLE
            }
        }



    }
    private fun observeViewModel() {
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            response.result?.let { result ->

                if(result.ageGroup == "UNDECIDED" || result.job == "UNDECIDED"){
                    binding.tvDot.visibility = View.GONE
                } else {
                    binding.tvDot.visibility = View.VISIBLE
                }

                binding.tvNickname.text = result.nickname
                binding.tvAge.text = convertAgeGroup(result.ageGroup)

                binding.tvJob.text = convertJob(result.job)
                binding.tvRewardAmount.text = result.rewardBalance.toString()

                // 프로필 이미지 로드
                result.profileImageUrl?.takeIf { it.isNotEmpty() }?.let { url ->
                    val imageUrl = result.profileImageUrl
                    Glide.with(this)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE) // 캐시 끔
                        .skipMemoryCache(true)
                        .placeholder(null)
                        .circleCrop()
                        .error(R.drawable.bg_box_white)
                        .into(binding.ivProfile)
                } ?: run {
                    // 기본 이미지 설정 또는 아무 작업도 하지 않음
                    binding.ivProfile.setImageResource(R.drawable.ic_my_profile)
                }
                binding.tvEmail.text = result.email
                binding.tvDailyLimit.text = if (result.dayTargetExpenditure == 0) {
                    "- 원"
                } else {
                    val dayTarget = String.format("%,d", result.dayTargetExpenditure)
                    "${dayTarget} 원"
                }

                binding.tvDailyPercent.text = if (result.weeklyAchievementRate == 0.0) {
                    "- %"
                } else {
                    "${result.weeklyAchievementRate} %"
                }
            }
        }


    }


    private fun setupClickListeners() {
        // 1. 프로필 수정
        binding.tvProfileEdit.setOnClickListener {
            // MyPage -> ProfileEditFragment 로 이동
            findNavController().navigate(R.id.action_myFragment_to_navigation_my_edit)
        }

        binding.tvTerms.setOnClickListener {
            // MyPage -> ServiceTermFragment 로 이동
            findNavController().navigate(R.id.action_myFragment_to_serviceTermFragment)
        }

        // 2. 하루목표 지출액 '수정'
        binding.tvDailyEdit.setOnClickListener {
            // MyPage -> DailyGoalEditFragment (가정)
            findNavController().navigate(R.id.action_myFragment_to_navigation_daily_expense_goal)
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

        binding.tvFaq.setOnClickListener() {
            val url = "https://sugared-college-51e.notion.site/0-FAQ-190624a0a41b8097b9cbc4141527da8c?pvs=74"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

        }
        binding.tvSupport.setOnClickListener() {
            val url = "https://sugared-college-51e.notion.site/0-190624a0a41b80d0ba16fa178c3acc40?pvs=73"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)

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

        // 추천 코드 가져와서 codeTV에 넣기
        val referralCode = viewModel.referralCode.value
        codeTv.text = referralCode

        //클립보드에 복사 하고 모달 나가기
        copyBtn.setOnClickListener {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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

            // (1) 서버에 로그아웃 API 호출 (socialToken 전달이 필요하면 여기에 구현)
//            logoutServerApi()

            // (2) SDK 로그아웃
//            logoutSocialIfNeeded()

            // (3) 앱 내부 토큰 삭제
            clearLocalToken()

            // (4) 로그인 화면으로 이동
            navigateToLogin()


//            clearLocalToken()
//            navigateToLogin()

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
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
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
            val reason = when {
                ivCheckLowUsage.isSelected -> "사용 빈도가 줄어서"
                ivCheckService.isSelected -> "대체 서비스를 발견해서"
                ivCheckBoredom.isSelected -> "재미가 없어서"
                ivCheckDifficulty.isSelected -> "사용 방법이 어려워서"
                ivCheckElse.isSelected -> "기타"
                else -> "기타"
            }
            // null 인 detail 생성
            val detail: String? = if (reason == "기타") etElseDetail.text.toString() else null
            val request = DeleteMemberRequest(reason, detail)


            withdrawServerApi(request)
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
                        Log.e("MyFragment", "카카오 로그아웃 실패", error)
                    } else {
                        // 카카오 로그아웃 완료
                        Log.d("MyFragment", "카카오 로그아웃 성공")
                    }
                }
            }

            "google" -> {
                lifecycleScope.launch {
                    // 로그인할 때 저장해둔 googleCredential(또는 별도 보관한 Credential) 사용
                    clearGoogleCredential(requireContext())
                }
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
        Log.d("MyFragment", "loginProvider: $loginProvider")
        when (loginProvider) {
            "kakao" -> {
                // 카카오 연결 끊기
                UserApiClient.instance.unlink { error ->
                    if (error != null) {
                        // 에러 처리
                        Log.e("MyFragment", "카카오 연결 끊기 실패", error)

                    } else {
                        Log.d("MyFragment", "카카오 연결 끊기 성공")
                    }
                }
            }
            "google" -> {

                lifecycleScope.launch {
                    // 로그인할 때 저장해둔 googleCredential(또는 별도 보관한 Credential) 사용
                    clearGoogleCredential(requireContext())
                }

            }
        }
    }
    private suspend fun clearGoogleCredential(context: Context, ) {
        val credentialManager = CredentialManager.create(context)
        val request = ClearCredentialStateRequest() // 생성자/빌더 여부는 버전에 따라 다름
        credentialManager.clearCredentialState(request)


    }


    private fun clearLocalToken() {
        //tokenmanager 사용
        TokenManager.clearAllTokens(requireContext())
        //토큰이 잘 없어졌는지 로깅
        Timber.d("TokenManager: ${TokenManager.getAccessToken(requireContext())}")
    }

    private fun getLoginProvider(): String {
        // 소셜 타입 가져오기
        return TokenManager.getSocialType(requireContext()).name.lowercase()
    }


    private fun logoutServerApi() {

        val socialType = TokenManager.getSocialType(requireContext())
        val socialToken = when (socialType) {
            TokenManager.SocialType.KAKAO -> TokenManager.getKakaoToken(requireContext())
            TokenManager.SocialType.GOOGLE -> TokenManager.getGoogleIdToken(requireContext())
            else -> null
        }
        // 로그아웃 api 호출
        val response = RetrofitClient.loginApiService.logout(socialToken)
        if (response.isSuccess) {
            // 로그아웃 성공
            Log.d("MyFragment", "로그아웃 성공")
        } else {
            // 로그아웃 실패
            Log.e("MyFragment", "로그아웃 실패: ${response.code}, ${response.message}")
        }

    }

    private fun withdrawServerApi(request: DeleteMemberRequest) {
        lifecycleScope.launch {
            val token = TokenManager.getAccessToken(requireContext())

            if (token.isNullOrEmpty()) {
                Timber.e("Token is missing. Cannot proceed with withdrawal.")
                return@launch
            }

            try {
                val response = RetrofitClient.myPageService.deleteMember(request)

                if (response.isSuccess) {
                    Log.d("MyFragment", "탈퇴 성공")

                    unlinkSocialIfNeeded()  // 카카오, 구글 계정 연결 끊기
                    clearLocalToken()       // 내부 JWT 삭제
                    navigateToLogin()       // 로그인 화면으로 이동
                } else {
                    Log.e("MyFragment", "탈퇴 실패: ${response.code}, ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("MyFragment", "네트워크 오류: ${e.localizedMessage}")
            }
        }
    }


    private fun navigateToLogin() {
        // 로그인 화면으로 이동
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun convertJob(apiJob: String): String {
        return when (apiJob.uppercase()) {
            "STUDENT" -> "학생"
            "EMPLOYEE" -> "직장인"
            "SELF_EMPLOYED" -> "자영업자"
            "HOMEMAKER" -> "주부"
            "UNDECIDED" -> ""
            else -> ""  // 알 수 없는 경우 원본 문자열 그대로 사용
        }
    }

    private fun convertAgeGroup(apiAge: String): String {
        return when (apiAge.uppercase()) {
            "TEENAGER" -> "10대"
            "TWENTIES" -> "20대"
            "THIRTIES" -> "30대"
            "FORTIES" -> "40대"
            "FIFTIES" -> "50대"
            "SIXTIES_AND_ABOVE" -> "60대"
            else -> ""  // 알 수 없는 경우 원본 문자열 그대로 사용
        }
    }


}