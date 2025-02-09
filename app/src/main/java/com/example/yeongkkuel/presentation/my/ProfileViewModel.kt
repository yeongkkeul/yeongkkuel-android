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

class ProfileViewModel : ViewModel() {

    private val repository: ProfileRepository

    init {
        repository = ProfileRepository(RetrofitClient.myPageService)
        fetchUserProfile()
    }

    private val _profileResponse = MutableLiveData<Response<MyPageResult>>()
    val profileResponse: LiveData<Response<MyPageResult>> get() = _profileResponse

    // 프로필 조회
    fun fetchUserProfile() {
        viewModelScope.launch {
            repository.getProfile().let {
                _profileResponse.value = it
            }
        }
    }

    //TODO : 프로필 수정
}