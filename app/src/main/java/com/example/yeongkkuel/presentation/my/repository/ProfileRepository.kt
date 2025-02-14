package com.example.yeongkkuel.presentation.my.repository

import android.util.Log
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.RetrofitClient.myPageService
import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.login.ReferralResponse
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import com.example.yeongkkuel.network.response.mypage.UserReferralCodeResult
import com.example.yeongkkuel.network.service.MyPageService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProfileRepository(private val apiService: MyPageService) {

    // 프로필 조회 API 호출
    suspend fun getProfile() : Response<MyPageResult>? {
        return try {
            val response = apiService.getMyPage()
            if (response.isSuccess) {
                response
            } else {
                // 에러 처리 (예: 로그 출력)
                null
            }
        } catch (e: Exception) {
            // 예외 처리 (네트워크 오류 등)
            e.printStackTrace()
            null
        }
    }

    suspend fun getReferralCode() : Response<UserReferralCodeResult>? {
        return try {
            val response = apiService.getUserReferralCode()
            if(response.isSuccess){
                response
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getUnreadNotificationCount() : Response<Boolean>? {
        return try {
            val response = RetrofitClient.notificationService.getUnreadNotificationCount()
            if(response.isSuccess){
                response
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateProfile(
        info: PatchMyPageRequest,
        profileImageFile: File? = null
    ): Response<MyPageResult>? {
        return try {
            // 1. PatchMyPageRequest를 JSON 문자열로 변환 후 RequestBody 생성
            val gson = Gson()
            val json = gson.toJson(info)
            val infoBody = json.toRequestBody("application/json".toMediaTypeOrNull())

            // 2. 프로필 이미지 파트 생성
            val imagePart: MultipartBody.Part = if (profileImageFile != null && profileImageFile.exists()) {
                // 파일이 있을 경우, 파일 데이터를 RequestBody로 변환
                val reqFile = profileImageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", profileImageFile.name, reqFile)
            } else {
                // 파일이 없으면 빈 값을 전송 (Swagger 명세: Send empty value)
                val emptyRequestBody = "".toRequestBody("text/plain".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", "", emptyRequestBody)
            }

            // 3. API 호출
            val response = apiService.patchMyPage(infoBody, imagePart)
            if (response.isSuccess) {
                response
            } else {
                // 에러 상황 처리 (로그 출력 등)
                Log.e("ProfileRepository", "updateProfile: ${response.message}")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}