package com.example.yeongkkuel

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.yeongkkuel.network.RetrofitClient
import com.example.yeongkkuel.network.request.mypage.PatchMyPageRequest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Base64

@RunWith(AndroidJUnit4::class)
class MyPageTest {
    private val loginService = RetrofitClient.loginApiService
    private val myPageService = RetrofitClient.myPageService

    private val accessToken: String? = null

    @Test
    fun test(){
        runBlocking {
            patchMyPage()
        }
    }

    suspend fun patchMyPage(){
        try {
            myPageService.patchMyPage(
                request = PatchMyPageRequest(
                    nickname = "dd",
                    gender = "Female",
                    ageGroup = "20s",
                    job = "직장인"
                ),
                profileImage = null
            ).let {
                Log.d("test", it.toString())
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        try {
            myPageService.getMyPage().let {
                Log.d("test", it.toString())
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun encodeImageToBase64(file: File): String {
        val byteArray = file.readBytes()
        return Base64.getEncoder().encodeToString(byteArray)
    }
}
