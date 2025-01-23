package com.example.yeongkkuel.presentation.my

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyPageViewModel : ViewModel() {

    // 예: 유저 정보(프로필 이미지 URL, 닉네임, 나이, 직업, 이메일)
    private val _userInfo = MutableLiveData<UserInfo>()
    val userInfo: LiveData<UserInfo> get() = _userInfo

    // 하루목표 지출액
    private val _dailyLimit = MutableLiveData<Int>()
    val dailyLimit: LiveData<Int> get() = _dailyLimit

    // 일주일 기준 목표 달성률(퍼센트)
    private val _weeklyPercent = MutableLiveData<Int>()
    val weeklyPercent: LiveData<Int> get() = _weeklyPercent

    init {
        // TODO: 실제로는 API 호출해서 받아온 뒤 값 세팅
        // 여기서는 더미 데이터
        _userInfo.value = UserInfo(
            profileImageUrl = "",  // 프로필 이미지 URL (또는 null)
            nickname = "0끌해서집산다",
            age = "20대",
            job = "직장인",
            email = "example@naver.com"
        )
        _dailyLimit.value = 5000
        _weeklyPercent.value = 63
    }

    // 백엔드에서 새로 가져오기 등
    fun refreshMyPageData() {
        // ex) repository.fetchMyPageData { result ->
        //     _userInfo.value = result.userInfo
        //     _dailyLimit.value = result.dailyLimit
        //     _weeklyPercent.value = result.weeklyPercent
        // }
    }
}

// 유저 정보 예시 데이터 모델
data class UserInfo(
    val profileImageUrl: String,
    val nickname: String,
    val age: String,
    val job: String,
    val email: String,
)