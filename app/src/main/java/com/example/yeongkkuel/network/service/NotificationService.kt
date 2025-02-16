package com.example.yeongkkuel.network.service

import com.example.yeongkkuel.network.request.notification.NotificationRequest
import com.example.yeongkkuel.network.request.notification.NotificationSettingRequest
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
        @Query("notificationId") notificationId: Int
    ): Response<Boolean>

    @PATCH("/api/notifications/settings")
    suspend fun patchNotificationSettings(
        @Body request: NotificationSettingRequest
    ): Response<Boolean>

    @GET("/api/notifications/unread")
    suspend fun getUnreadNotificationCount(
    ): Response<Boolean>

    @PATCH("api/notifications/read-all")
    suspend fun patchAllNotificationRead(
    ): Response<Int>




}