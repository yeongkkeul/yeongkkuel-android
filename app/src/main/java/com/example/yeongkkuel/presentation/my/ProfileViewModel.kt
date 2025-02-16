package com.example.yeongkkuel.presentation.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.my.UserProfileRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.login.ReferralResponse
import com.example.yeongkkuel.network.response.my.UserProfileResponse
import com.example.yeongkkuel.network.response.my.UserProfileResult
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import com.example.yeongkkuel.network.service.MyPageService
import com.example.yeongkkuel.presentation.my.repository.ProfileRepository
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel : ViewModel() {

    private val repository: ProfileRepository

    init {
        repository = ProfileRepository(RetrofitClient.myPageService)
        fetchUserProfile()
        getReferralCode()
        // 읽지 않은 알림 여부
        getUnreadNotificationCount()
    }

    private val _profileResponse = MutableLiveData<Response<MyPageResult>>()
    val profileResponse: LiveData<Response<MyPageResult>> get() = _profileResponse


    private val _updateStatusEvent = MutableLiveData<Event<Boolean>>()
    val updateStatusEvent: LiveData<Event<Boolean>> get() = _updateStatusEvent



    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> get() = _gender

    private val _ageGroup = MutableLiveData<String>()
    val ageGroup: LiveData<String> get() = _ageGroup

    private val _job = MutableLiveData<String>()
    val job: LiveData<String> get() = _job

    private val _profileImageUrl = MutableLiveData<String>()
    val profileImageUrl: LiveData<String> get() = _profileImageUrl

    // 추천인 코드 응답
    private val _referralCode = MutableLiveData<String>()
    val referralCode: LiveData<String> get() = _referralCode

    // 읽지 않은 알림 여부
    private val _unreadNotificationCount = MutableLiveData<Boolean>()
    val unreadNotificationCount: LiveData<Boolean> get() = _unreadNotificationCount



    // setter
    fun updateNickname(newNickname: String) { _nickname.value = newNickname }
    fun updateGender(newGender: String) { _gender.value = newGender }
    fun updateAgeGroup(newAgeGroup: String) { _ageGroup.value = newAgeGroup }
    fun updateJob(newJob: String) { _job.value = newJob }
    fun updateProfileImageUrl(newUrl: String) { _profileImageUrl.value = newUrl }
    fun updateReferralCode(newCode: String) { _referralCode.value = newCode }


    // 프로필 조회
    fun fetchUserProfile() {
        viewModelScope.launch {
            val response = repository.getProfile()
            response?.let {
                if (it.isSuccess) {
                    val result = it.result
                    _nickname.value = result.nickname
                    _gender.value = result.gender
                    _ageGroup.value = result.ageGroup
                    _job.value = result.job
                    _profileImageUrl.value = result.profileImageUrl

                    _profileResponse.value = it
                }
            }
        }
    }

    fun getUnreadNotificationCount() {
        viewModelScope.launch {
            val response = repository.getUnreadNotificationCount()
            response?.let {
                if (it.isSuccess) {
                    _unreadNotificationCount.value = it.result
                }
            }
        }
    }
    // 추천인 코드 조회
    fun getReferralCode() {
        viewModelScope.launch {
            val response = repository.getReferralCode()
            response?.let {
                if (it.isSuccess) {
                    val result = it.result
                    _referralCode.value = result.userReferralCode
                }
            }
        }
    }

    fun saveUserProfile(selectedImageFile: File? = null) {
        val patchRequest = PatchMyPageRequest(
            nickname = nickname.value ?: "",
            gender = gender.value ?: "",
            ageGroup = ageGroup.value ?: "",
            job = job.value ?: ""
        )

        updateProfile(patchRequest, selectedImageFile)
    }

    private fun convertAgeGroup(apiAge: String): String {
        return when(apiAge.uppercase()) {
            "TEENAGER" -> "10대"
            "TWENTIES" -> "20대"
            "THIRTIES" -> "30대"
            "FORTIES" -> "40대"
            "FIFTIES" -> "50대"
            "SIXTIES_AND_ABOVE" -> "60대"
            else -> " 대"  // 알 수 없는 경우 원본 문자열 그대로 사용
        }
    }

    private fun convertJob(apiJob: String): String {
        return when(apiJob.uppercase()) {
            "STUDENT" -> "학생"
            "EMPLOYEE" -> "직장인"
            "SELF_EMPLOYED" -> "자영업자"
            "HOMEMAKER" -> "주부"
            "UNDECIDED" -> "무직"
            else -> "무직"  // 알 수 없는 경우 원본 문자열 그대로 사용
        }
    }




    fun updateProfile(info: PatchMyPageRequest, profileImageFile: File? = null ) {
        viewModelScope.launch {
            val patchResult = repository.updateProfile(info, profileImageFile)
            if (patchResult != null && patchResult.isSuccess) {
                // 수정 성공
                // profileResponse도 갱신할 수 있음
                _profileResponse.value = patchResult as Response<MyPageResult>
                _updateStatusEvent.value = Event(true)   // <-- 이벤트 발행
            } else {
                // 수정 실패
                _updateStatusEvent.value = Event(false)   // <-- 이벤트 발행
            }
        }
    }

}