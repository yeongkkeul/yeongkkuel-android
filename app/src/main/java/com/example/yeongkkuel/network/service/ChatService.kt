package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.chat.ChatsRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.chat.ChatSummaryResult
import com.example.yeongkkuel.network.response.chat.ChatDetailResult
import com.example.yeongkkuel.network.response.chat.ChatSearchResult
import com.example.yeongkkuel.network.response.chat.ChatsResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {
//    // 전체 채팅방 조회
//    @GET("/api/chats")
//    suspend fun getChatList(): Response<ChatsResult>
//
//    // 채팅방 만들기
//    @POST("/api/chats")
//    suspend fun postChat(
//        @Body request: ChatsRequest
//    ): Response<Int> // 생성한 채팅방의 ID -> 클라이언트 로직에 따라 클라이언트 정보를 응답할 수도 있습니다.
//
//    // 특정 채팅방 조회
//    @GET("/api/chats/{chatRoomId}")
//    suspend fun getChatById(
//        @Path("chatRoomId") chatRoomId:Int
//    ): Response<ChatDetailResult>
//
//    // 특정 채팅방 정보 조회
//    @GET("/api/chats/{chatRoomId}/detail")
//    suspend fun getChatDetailById(
//        @Path("chatRoomId") chatRoomId:Int
//    ): Response<ChatSummaryResult>
//
//    // 채팅방 둘러보기 - 채팅방 검색
//    @GET("/api/chats/search?age=&amont=&job=")
//    suspend fun getSearchChat(
//        @Query("age") age: String?,
//        @Query("amount") amount: String?,
//        @Query("job") job: String?
//    ): Response<ChatSearchResult>
//
//    // 채팅방 둘러보기 조회
//    // 시작 전
//
//    // 채팅방 패스워드 확인
//

}
