package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.notification.NotificationRequest
import com.example.yeongkkuel.network.response.Response
import com.example.yeongkkuel.network.response.notification.NotificationResult
import com.example.yeongkkuel.presentation.my.notification.data.NotificationItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationService {

    @GET("/api/notifications")
    suspend fun getNotificationList(
        @Query("page") page: Int? = 1
    ): Response<NotificationResult>


    @POST("/api/notifications")
    suspend fun postNotification(
        @Body request: NotificationRequest
    ): Response<Any>

    @PATCH("/api/notifications/{notificationId}/read")
    suspend fun postNotificationRead(
        // path parameter로 notificationId를 받아옵니다.
        // notificationId는 Int형으로 받아옵니다. path parameter로 받아옵니다.
        @Query("notificationId") notificationId: Int
    ): Response<Boolean>

    @PATCH("/api/notifications/settings")
    suspend fun patchNotificationSettings(
        // request body로 NotificationRequest를 받아옵니다.
        // NotificationRequest는 notificationType, content, targetUrl을 가지고 있습니다.
        // notificationType은 String형으로 받아옵니다.
        // content는 String형으로 받아옵니다.
        // targetUrl은 String형으로 받아옵니다.
        @Body notificationAgreed: Boolean

    ): Response<Boolean>

    @GET("/api/notifications/unread")
    suspend fun getUnreadNotificationCount(
    ): Response<Boolean>





}