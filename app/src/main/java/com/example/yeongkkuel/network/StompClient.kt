package com.example.yeongkkuel.network

import okhttp3.OkHttpClient
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import java.util.concurrent.TimeUnit

object StompClient {
    private var stompClient: StompClient? = null

    fun getStompClient(): StompClient {
        if (stompClient == null) {
            val okHttpClient = createOkHttpClient()
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "wss://dev.yeongkkeul.store/ws")
            stompClient?.connect()
        }
        return stompClient!!
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)  // 예시로 readTimeout을 설정
            .build()
    }
}
