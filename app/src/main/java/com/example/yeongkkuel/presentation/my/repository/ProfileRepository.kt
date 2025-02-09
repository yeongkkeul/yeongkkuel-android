package com.example.yeongkkuel.presentation.my.repository

import android.util.Log
import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.mypage.MyPageResult
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

    // 프로필 수정 API 호출
    suspend fun updateProfile(
        info: PatchMyPageRequest,
        profileImageFile: File? = null
    ): Response<MyPageResult>? {
        return try{

            val profileImage = profileImageFile?.let { file ->
                val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", file.name, reqFile)
            }

            val response = apiService.patchMyPage(info, profileImage)
            if (response.isSuccess) {
                response
            } else {
                // 에러 처리 (예: 로그 출력)
                Log.d("ProfileRepository", "updateProfile error: ${response.message}")
                null
            }
        } catch (e: Exception) {
            // 예외 처리 (네트워크 오류 등)
            e.printStackTrace()
            null
        }
    }
}