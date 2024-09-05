package com.innerpeace.themoonha.data.network

import com.innerpeace.themoonha.data.model.lesson.LessonDetailResponse
import com.innerpeace.themoonha.data.model.lesson.LessonEnrollResponse
import com.innerpeace.themoonha.data.model.lesson.LessonListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface LessonService {
    @GET("lesson/list")
    suspend fun getLessonList(@QueryMap lessonListRequestMap: Map<String, String>): LessonListResponse

    @GET("lesson/detail/{lessonId}")
    suspend fun getLessonDetail(@Path("lessonId") lessonId: Long): LessonDetailResponse

    @GET("lesson/enroll")
    suspend fun getLessonListByMember(): List<LessonEnrollResponse>
}