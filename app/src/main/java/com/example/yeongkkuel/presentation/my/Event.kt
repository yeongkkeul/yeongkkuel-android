package com.example.yeongkkuel.presentation.my

open class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * 이벤트 처리 여부와 상관없이 값을 반환 (디버깅 등에서 사용)
     */
    fun peekContent(): T = content
}