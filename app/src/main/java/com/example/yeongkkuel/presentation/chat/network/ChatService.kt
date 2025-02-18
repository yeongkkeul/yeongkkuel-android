package com.example.yeongkkuel.presentation.chat.network

import com.example.yeongkkuel.presentation.chat.data.ChatRequest
import com.example.yeongkkuel.presentation.chat.data.ChatResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse
}