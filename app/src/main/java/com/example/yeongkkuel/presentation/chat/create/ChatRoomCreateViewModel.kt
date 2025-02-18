package com.example.yeongkkuel.presentation.chat.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.chat.ChatsRequest
import com.example.yeongkkuel.network.response.Response
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber

class ChatRoomCreateViewModel : ViewModel() {

    val selectedAgeOption: MutableLiveData<String?> = MutableLiveData(null)

    val selectedExpenseOption: MutableLiveData<String> = MutableLiveData()

    val selectedJobOption: MutableLiveData<String?> = MutableLiveData(null)

    fun postChat(request: ChatsRequest, imageMultipart: MultipartBody.Part?, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            try {
                val chatRoomInfoJson = Gson().toJson(request)
                val chatRoomInfoRequestBody = chatRoomInfoJson.toRequestBody("application/json".toMediaTypeOrNull())

                val response: Response<Int> = RetrofitClient.chatService.postChat(
                    chatRoomInfo = chatRoomInfoRequestBody,
                    chatRoomImage = imageMultipart
                )
                if (response.isSuccess) {
                    onResult(response.result)
                } else {
                    Timber.d("postChat failed: ${response.message}")
                    onResult(null)
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception while posting chat room")
                onResult(null)
            }
        }
    }
}