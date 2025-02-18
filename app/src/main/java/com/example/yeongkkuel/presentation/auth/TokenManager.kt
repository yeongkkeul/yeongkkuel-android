package com.example.yeongkkuel.presentation.auth

import android.content.Context
import com.example.yeongkkuel.YeongKkuelApplication
import kotlin.contracts.contract

//import androidx.security.crypto.EncryptedSharedPreferences
//import androidx.security.crypto.MasterKey
//import timber.log.Timber


// 예: SharedPreferences Util (Singleton)
object TokenManager {
    private const val PREFS_NAME = "my_prefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private const val KEY_SOCIAL_TYPE = "social_type"
    private const val KEY_KAKAO_TOKEN = "kakao_token"
    private const val KEY_GOOGLE_ID_TOKEN = "google_id_token"

    private const val CATEGORY_ORDER = "category_order"

    enum class SocialType {
        KAKAO, GOOGLE, NONE
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)

        // ✅ Access Token 값 확인용 로그
        println("Saved Access Token in TokenManager: $accessToken")

        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(CATEGORY_ORDER)
            .apply()
    }

    // 소셜 타입 저장/조회
    fun saveSocialType(context: Context, socialType: SocialType) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_SOCIAL_TYPE, socialType.name)
            .apply()
    }

    fun getSocialType(context: Context): SocialType {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val typeName = prefs.getString(KEY_SOCIAL_TYPE, SocialType.NONE.name)
        return runCatching {
            SocialType.valueOf(typeName ?: SocialType.NONE.name)
        }.getOrDefault(SocialType.NONE)
    }

    // 카카오 토큰 저장/조회

    fun saveKakaoToken(context: Context, kakaoToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_KAKAO_TOKEN, kakaoToken)
            .apply()
    }

    fun getKakaoToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_KAKAO_TOKEN, null)
    }

    fun clearKaKaoToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_KAKAO_TOKEN)
            .apply()
    }

    // 구글 토큰 저장/조회
    fun saveGoogleIdToken(context: Context, googleIdToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_GOOGLE_ID_TOKEN, googleIdToken)
            .apply()
    }

    fun getGoogleIdToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_GOOGLE_ID_TOKEN, null)
    }

    fun clearGoogleIdToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_GOOGLE_ID_TOKEN)
            .apply()
    }


    // 모든 소셜 토큰 제거
    fun clearAllTokens(context: Context) {
        clearKaKaoToken(context)
        clearGoogleIdToken(context)
        clearTokens(context)
    }

    fun setCategoryOrder(context: Context = YeongKkuelApplication.applicationContext(), categoryOrderList: List<Int>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringList = categoryOrderList.joinToString(",") { it.toString() }
        val editor = prefs.edit()
        editor.putString(CATEGORY_ORDER, stringList)
        editor.apply()
    }

    fun getCategoryOrder(context: Context = YeongKkuelApplication.applicationContext()): List<Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stringList = prefs.getString(CATEGORY_ORDER, "") ?: return emptyList()
        return stringList.split(",").map { it.toIntOrNull() ?: 0 }
    }
}

