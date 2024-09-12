package com.innerpeace.themoonha.data.repository

import android.util.Log
import com.innerpeace.themoonha.data.model.CommonResponse
import com.innerpeace.themoonha.data.network.AlimService

class AlimRepository(private val alimService: AlimService) {

    suspend fun registerFcmToken(token: String): CommonResponse? {
        return try {
            val response = alimService.registerFcmToken(token)

            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FCM 토큰 저장 실패", "${e.message}", e)
            null
        }
    }
}