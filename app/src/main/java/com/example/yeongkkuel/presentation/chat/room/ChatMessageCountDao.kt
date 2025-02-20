package com.example.yeongkkuel.presentation.chat.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatMessageCountDao {

    @Query("SELECT * FROM chat_message_count WHERE chatRoomId = :chatRoomId")
    suspend fun getMessageCount(chatRoomId: Int): ChatMessageCount?

    // 기존 데이터와 충돌 시 교체하는 방식
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessageCount(count: ChatMessageCount)
}