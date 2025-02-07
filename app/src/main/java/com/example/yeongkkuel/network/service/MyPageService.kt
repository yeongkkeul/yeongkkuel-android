package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.mypage.MyPagePatchResult
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import retrofit2.http.Body
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
    ): Response<MyPagePatchResult>
}