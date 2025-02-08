package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import com.example.yeongkkuel.network.response.mypage.RewardsResult
import com.example.yeongkkuel.network.response.mypage.UserReferralCodeResult
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MyPageService {
    // 마이페이지 프로필 조회
    @GET("/api/mypage")
    suspend fun getMyPage(): Response<MyPageResult>

    // 마이페이지 프로필 수정
    @PATCH("/api/mypage")
    suspend fun patchMyPage(
        @Body request: PatchMyPageRequest
    ): Response<MyPageResult>

    // 추천인 코드 조회
    @GET("/api/userreferralcode")
    suspend fun getUserReferralCode(): Response<UserReferralCodeResult>

    // 리워드 목록 조회
    @GET("/api/rewards")
    suspend fun getRewards(): Response<RewardsResult>

    // 회원탈퇴
    @DELETE("/api/auth/delete")
    suspend fun deleteMember(
        @Body request: DeleteMemberRequest
    ):Response<String>
}