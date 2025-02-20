package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.chat.ChatPwValidateRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.chat.ChatBannerResult
import com.example.yeongkkuel.network.response.chat.ChatDetailResult
import com.example.yeongkkuel.network.response.chat.ChatMessageResponse
import com.example.yeongkkuel.network.response.chat.ChatRoomInfoResult
import com.example.yeongkkuel.network.response.chat.ChatSearchResult
import com.example.yeongkkuel.network.response.chat.ReceiptResult
import com.example.yeongkkuel.network.response.chat.ChatRoomRankResponse
import com.example.yeongkkuel.network.response.chat.ChatRoomUserResult
import com.example.yeongkkuel.network.response.chat.ChatRoomUsersResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {
    // 클라이언트와 서버의 채팅방 정보 조회
    @GET("/api/chats")
    suspend fun getChatList(): Response<List<ChatRoomInfoResult>>

    // 채팅방 만들기
    @Multipart
    @POST("/api/chats")
    suspend fun postChat(
        @Part("chatRoomInfo") chatRoomInfo: RequestBody, // ChatsRequest 객체의 JSON 문자열
        @Part chatRoomImage: MultipartBody.Part? // 선택한 이미지 (없으면 null)
    ): Response<Int> // 생성한 채팅방의 ID -> 클라이언트 로직에 따라 클라이언트 정보를 응답할 수도 있습니다.

    // 채팅방 패스워드 확인
    @POST("/api/chats/{chatRoomId}/validate")
    suspend fun postChatroomPwValidate(
        @Path("chatRoomId") chatRoomId:Int,
        @Body request:ChatPwValidateRequest
    ): Response<Boolean>

    // 채팅방 이미지 조회
    @GET("/api/chats/{chatRoomId}/images")
    suspend fun getChatroomImages(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<List<String>>

    // 채팅방 이미지 업로드
    // request body에 chatPicture(string($binary)) 넣으라고 나와있음. 아직 넣어있지 않음.
    // multipart로 바꿔달라고 요청했다고 들었던 것 같아서 내비둠(승원님 제가 착각한거면 그냥 넣어주세요...)
    @POST("/api/chats/{chatRoomId}/images")
    suspend fun postChatroomImages(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<String>

    @GET("/api/chats/{chatRoomId}/users")
    suspend fun getChatroomUsers(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<ChatRoomUsersResponse>


    // 클라이언트와 서버의 메세지 조회
    // 웹소켓이 재연결되거나 오류로 인해 메시지를 받지 못할 경우
    @GET("/api/chats/{chatRoomId}/messages")
    suspend fun getChatroomMessages(
        @Path("chatRoomId") chatRoomId:Int,
        @Query("messageId") messageId: Int
    ): Response<List<ChatMessageResponse>>


    // 특정 채팅방의 모든 메시지를 조회
    // 실제 환경에서 채팅방의 모든 메시지를 가져오는 것은 성능의 문제가 있으므로 테스트 용도
    @GET("/api/chats/{chatRoomId}/messages/test")
    suspend fun getChatroomMessageTest(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<List<ChatMessageResponse>>

    // 채팅방 이미지 다운로드
    @GET("/api/chats/{chatRoomId}/images/{messageId}/download")
    suspend fun getChatroomImagesDownload(
        @Path("chatRoomId") chatRoomId:Int,
        @Path("messageId") messageId:Int
    ): List<String> // swagger에 공통 Response 형식으로 감싸져 있지 않아 있습니다.

    // 채팅방 정보 조회
    @GET("/api/chats/{chatRoomId}/detail")
    suspend fun getChatroomDetail(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<ChatDetailResult>

    // 채팅방 배너 조회
    @GET("/api/chats/{chatRoomId}/banner")
    suspend fun getChatroomBanner(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<ChatBannerResult>

    // 채팅방 검색
    // 키워드에 맞는 모든 채팅방을 페이징 단위로 조회
    @GET("/api/chats/search")
    suspend fun getChatroomSearch(
        @Query("keyword") keyword:String,
        @Query("page") page:Int
    ): ChatSearchResult

    // 영수증 조회
    @GET("api/chats/receipts/{expenseId}")
    suspend fun getReceiptsByExpendsId(
        @Path("expenseId") expenseId:Int
    ): Response<ReceiptResult>

    // 채팅방 둘러보기
    @GET("/api/chats/explore")
    suspend fun getChatroomExplore(
        @Query("age") age:String? = null,
        @Query("minAmount") minAmount: Int? = null,
        @Query("maxAmount") maxAmount:Int? = null,
        @Query("job") job :String? = null,
        @Query("page") page:Int
    ): ChatSearchResult

    // 채팅방 랭킹 조회
    @GET("/api/chats/{chatRoomId}/ranks")
    suspend fun getChatroomRanks(
        @Path("chatRoomId") chatRoomId:Int
    ): Response<ChatRoomRankResponse>

    // 채팅방 사용자 프로필 조회
    @GET("/api/chats/{chatRoomId}/user/{userId}")
    suspend fun getChatroomUser(
        @Path("chatRoomId") chatRoomId:Int,
        @Path("userId") userId:Int
    ): Response<ChatRoomUserResult>
}
