package com.example.yeongkkuel.presentation.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yeongkkuel.network.request.my.UserProfileRequest
import com.example.yeongkkuel.network.response.my.UserProfileResponse
import com.example.yeongkkuel.network.response.my.UserProfileResult

class ProfileViewModel : ViewModel()  {

    private val _userProfileRequest = MutableLiveData<UserProfileRequest>()
    val userProfileRequest: LiveData<UserProfileRequest> get() = _userProfileRequest

    private val _updateStatus = MutableLiveData<Result<Boolean>>()
    val updateStatus: LiveData<Result<Boolean>> get() = _updateStatus

    private val _profileResponse = MutableLiveData<UserProfileResponse>()
    val profileResponse: LiveData<UserProfileResponse> get() = _profileResponse

    fun setUserProfile(profile: UserProfileRequest) {
        _userProfileRequest.value = profile
    }

    fun updateNickname(newNickname: String) {
        _userProfileRequest.value = _userProfileRequest.value?.copy(nickname = newNickname)
    }

    fun updateGender(newGender: String) {
        _userProfileRequest.value = _userProfileRequest.value?.copy(gender = newGender)
    }

    fun updateAgeGroup(newAgeGroup: String) {
        _userProfileRequest.value = _userProfileRequest.value?.copy(age_group = newAgeGroup)
    }

    fun updateJob(newJob: String) {
        _userProfileRequest.value = _userProfileRequest.value?.copy(job = newJob)
    }

    fun updateProfileImageUrl(url: String) {
        _userProfileRequest.value = _userProfileRequest.value?.copy(profileImageUrl = url)
    }

    init {
        // 초기화 시 더미 데이터를 로드
        loadDummyProfile()
    }

    private fun loadDummyProfile() {
        val dummyResponse = UserProfileResponse(
            isSuccess = true,
            code = "2000",
            message = "마이페이지 프로필 조회",
            result = UserProfileResult(
                nickname = "0끌해서 집산다",
                gender = "여자",
                job = "직장인",
                ageGroup = "20대",
                email = "dlfkscigs@naver.com",
                profileImageUrl = "https://via.placeholder.com/100", // 예제 이미지 URL
                dayTargetExpenditure = 35000,
                rewardBalance = 3122,
                weeklyAchievementRate = 28.5
            )
        )
        _profileResponse.value = dummyResponse

        _userProfileRequest.value = UserProfileRequest(
            nickname = dummyResponse.result!!.nickname,
            gender = dummyResponse.result.gender,
            age_group = dummyResponse.result.ageGroup,
            job = dummyResponse.result.job,
            profileImageUrl = dummyResponse.result.profileImageUrl
        )
    }

    fun fetchUserProfile() {
        /*viewModelScope.launch {

            try {
                val response = ApiService.getUserProfileRequest()
                if (response.isSuccess) {
                    _profileResponse.postValue(response)
                } else {
                    _profileResponse.postValue(response)
                }
            } catch (e: Exception) {
                _profileResponse.postValue(
                    UserProfileResponse(false, "5000", e.message ?: "Unknown Error", null)
                )
            }


        }*/

        // 테스트용 더미데이터 사용
        loadDummyProfile()
    }

    fun saveUserProfile() {

        _profileResponse.value = UserProfileResponse(
            isSuccess = true,
            code = "2000",
            message = "프로필 수정 완료",
            result = UserProfileResult(
                nickname = _userProfileRequest.value!!.nickname,
                gender = _userProfileRequest.value!!.gender,
                job = _userProfileRequest.value!!.job,
                ageGroup = _userProfileRequest.value!!.age_group,
                email = "example@naver.com",
                profileImageUrl = _userProfileRequest.value!!.profileImageUrl,
                dayTargetExpenditure = 35000,
                rewardBalance = 3122,
                weeklyAchievementRate = 28.5
            )
        )

    }

    fun saveChangesToServer() {
        // api 호출 주석 처리
        /*_userProfileRequest.value?.let { profile ->
            viewModelScope.launch {
                try {
                    val response = ApiService.updateUserProfile(profile)
                    if (response.isSuccess) {
                        _updateStatus.postValue(Result.success(true))
                    } else {
                        _updateStatus.postValue(Result.failure(Exception(response.message)))
                    }
                } catch (e: Exception) {
                    _updateStatus.postValue(Result.failure(e))
                }
            }
        }*/

        _updateStatus.postValue(Result.success(true)) // 성공 시
        // _updateStatus.postValue(Result.failure(Exception("닉네임을 입력해주세요."))) // 실패 시
    }
}