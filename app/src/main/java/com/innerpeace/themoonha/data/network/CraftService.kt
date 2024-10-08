package com.innerpeace.themoonha.data.network

import com.innerpeace.themoonha.data.model.craft.CommonResponse
import com.innerpeace.themoonha.data.model.craft.CraftMainResponse
import com.innerpeace.themoonha.data.model.craft.SuggestionRequest
import com.innerpeace.themoonha.data.model.craft.SuggestionResponse
import com.innerpeace.themoonha.data.model.lesson.LessonDetailResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 문화공방 도메인 서비스
 *
 * @author 손승완
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.03  	손승완       최초 생성
 * </pre>
 * @since 2024.09.03
 */
interface CraftService {
    @GET("craft/list")
    suspend fun getCraftMain(): CraftMainResponse

    @GET("craft/suggestion")
    suspend fun getSuggestionList(@Query("pageNum") pageNum: Int): SuggestionResponse

    @POST("craft/prologue/{prologueId}")
    suspend fun likePrologue(@Path("prologueId") prologueId: Long): CommonResponse

    @POST("craft/wishlesson/{wishLessonId}")
    suspend fun voteWishLesson(@Path("wishLessonId") wishLessonId: Long): CommonResponse

    @POST("craft/suggestion")
    suspend fun writeSuggestion(@Body suggestionRequest: SuggestionRequest): CommonResponse

}