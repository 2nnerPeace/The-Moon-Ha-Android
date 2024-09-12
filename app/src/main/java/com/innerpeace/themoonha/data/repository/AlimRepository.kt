package com.innerpeace.themoonha.data.repository

import android.util.Log
import com.innerpeace.themoonha.data.model.CommonResponse
import com.innerpeace.themoonha.data.network.AlimService

/**
 * 알림 레포지토리
 * @author 조희정
 * @since 2024.09.11
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.11  	조희정       최초 생성
 * 2024.09.11   조희정       FCM 토큰 저장 구현
 * </pre>
 */
class AlimRepository(private val alimService: AlimService) {

    // FCM 토큰 저장
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