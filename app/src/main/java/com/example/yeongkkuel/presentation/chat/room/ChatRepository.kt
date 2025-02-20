package com.example.yeongkkuel.presentation.chat.room

class ChatRepository(private val dao: ChatMessageCountDao) {

    suspend fun saveMessageCount(chatRoomId: Int, count: Int) {
        val entity = ChatMessageCount(chatRoomId, count)
        dao.insertMessageCount(entity)
    }

    suspend fun getMessageCount(chatRoomId: Int): ChatMessageCount? {
        return dao.getMessageCount(chatRoomId)
    }
}