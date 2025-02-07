package com.example.yeongkkuel.network.response.chat

import com.google.gson.annotations.SerializedName

data class ChatDetailResult (
    @SerializedName("chatRoomId") val chatRoomId: String,
    @SerializedName("chatRoomName") val chatRoomName: String,
    @SerializedName("chatUserCount") val chatUserCount: Int,
    @SerializedName("isActive") val isActive: Boolean, // soft delete 여부
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("profileImage") val profileImage: String,
    @SerializedName("chatRoomRole") val chatRoomRole: String,
    @SerializedName("participants") val participants: List<Participant>,
    @SerializedName("images") val images: List<ChatImage>,
    @SerializedName("groupRule") val groupRule: String,
    @SerializedName("messages") val messages: List<ChatMessage>
){
    data class Participant(
        @SerializedName("userId") val userId: Int,
        @SerializedName("nickname") val nickname: String,
        @SerializedName("profileImage") val profileImage: String,
        @SerializedName("chatRoomRole") val chatRoomRole: String
    )

    data class ChatImage(
        @SerializedName("imageId") val imageId: Int,
        @SerializedName("imageUrl") val imageUrl: String,
        @SerializedName("uploaderId") val uploaderId: Int,
        @SerializedName("uploaderProfileImage") val uploaderProfileImage: String,
        @SerializedName("uploadedAt") val uploadedAt: String
    )

    data class ChatMessage(
        @SerializedName("messageId") val messageId: Int,
        @SerializedName("senderId") val senderId: String,
        @SerializedName("nickname") val nickname: String,
        @SerializedName("senderProfileImage") val senderProfileImage: String,
        @SerializedName("content") val content: String?,
        @SerializedName("imageUrl") val imageUrl: String?,
        @SerializedName("type") val type: String, // TEXT, IMAGE, EXIT 등
        @SerializedName("timestamp") val timestamp: String,
        @SerializedName("isFirstMessageOfDay") val isFirstMessageOfDay: Boolean,
        @SerializedName("unreadChatCount") val unreadChatCount: Int
    )

}