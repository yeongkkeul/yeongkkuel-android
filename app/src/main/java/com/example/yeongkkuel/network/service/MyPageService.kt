package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.mypage.DeleteMemberRequest
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.mypage.MyPageResult
import com.example.yeongkkuel.network.response.mypage.RewardsResult
import com.example.yeongkkuel.network.response.mypage.UserReferralCodeResult
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part

interface MyPageService {
    //하루 목표 지출액 설정은 statService에서 사용정되어 프론트에서 지출설 기능까지 구현되어 있습니다.
    // swagger에는 mypage 범주로 되어있기에 확인하면서 혼동되지 않도록 주석을 답니다. 읽었으면 지워주세요.

    // 마이페이지 프로필 조회
    @GET("/api/mypage")
    suspend fun getMyPage(): Response<MyPageResult>

    // 마이페이지 프로필 수정
    @Multipart
    @PATCH("/api/mypage")
    suspend fun patchMyPage(
        @Part("myPageInfoRequestDto") request: PatchMyPageRequest,
        @Part profileImage: MultipartBody.Part?
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