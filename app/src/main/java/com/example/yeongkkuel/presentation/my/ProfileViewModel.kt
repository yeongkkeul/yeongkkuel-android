package com.example.yeongkkuel.presentation.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.my.UserProfileRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
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
    }

    private val _profileResponse = MutableLiveData<Response<MyPageResult>>()
    val profileResponse: LiveData<Response<MyPageResult>> get() = _profileResponse


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

    // setter
    fun updateNickname(newNickname: String) { _nickname.value = newNickname }
    fun updateGender(newGender: String) { _gender.value = newGender }
    fun updateAgeGroup(newAgeGroup: String) { _ageGroup.value = newAgeGroup }
    fun updateJob(newJob: String) { _job.value = newJob }
    fun updateProfileImageUrl(newUrl: String) { _profileImageUrl.value = newUrl }


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
                }
            }
        }
    }

    fun saveUserProfile() {
        val patchRequest = PatchMyPageRequest(
            nickname = nickname.value ?: "",
            gender = gender.value ?: "",
            ageGroup = ageGroup.value ?: "",
            job = job.value ?: ""
        )
        val file = profileImageUrl.value?.let { File(it) }
        updateProfile(patchRequest, file)
    }


    fun updateProfile(info: PatchMyPageRequest, profileImageFile: File? = null ) {
        viewModelScope.launch {
            repository.updateProfile(info, profileImageFile).let {
                _profileResponse.value = it
            }
        }
    }

}