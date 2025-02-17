package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import com.example.yeongkkuel.network.response.mypage.RewardsResult
import com.example.yeongkkuel.network.response.mypage.UserReferralCodeResult
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part

interface MyPageService {

    // 마이페이지 프로필 조회
    @GET("/api/mypage")
    suspend fun getMyPage(): Response<MyPageResult>

    // 마이페이지 프로필 수정
    @Multipart
    @PATCH("/api/mypage")
    suspend fun patchMyPage(
        @Part("myPageInfoRequestDto") request: RequestBody,

        @Part profileImage: MultipartBody.Part?
    ): Response<MyPageResult>

    // 추천인 코드 조회
    @GET("/api/userReferralCode")
    suspend fun getUserReferralCode(): Response<UserReferralCodeResult>

    // 리워드 목록 조회
    @GET("/api/rewards")
    suspend fun getRewards(): Response<List<RewardsResult>>

    // 회원탈퇴
    @HTTP(method = "DELETE", path = "/api/auth/delete", hasBody = true)
    suspend fun deleteMember(@Body request: DeleteMemberRequest): Response<String>
}