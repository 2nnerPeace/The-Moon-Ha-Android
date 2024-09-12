package com.innerpeace.themoonha.data.network

import com.innerpeace.themoonha.data.model.CommonResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 알림 서비스
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
interface AlimService {

    @POST("alim/token")
    suspend fun registerFcmToken(@Body token: String): Response<CommonResponse>
}