package com.innerpeace.themoonha.data.model.lounge

/**
 * 라운지 게시글 작성 Request
 * @author 조희정
 * @since 2024.09.03
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.03  	조희정       최초 생성
 * </pre>
 */
data class LoungePostRequest(
    val loungeId: Long,
    val content: String,
    val noticeYn: Boolean = false
)