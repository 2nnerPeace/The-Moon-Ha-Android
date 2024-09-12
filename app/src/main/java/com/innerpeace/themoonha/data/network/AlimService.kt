package com.innerpeace.themoonha.data.network

import com.innerpeace.themoonha.data.model.CommonResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AlimService {

    @POST("alim/token")
    suspend fun registerFcmToken(@Body token: String): Response<CommonResponse>
}