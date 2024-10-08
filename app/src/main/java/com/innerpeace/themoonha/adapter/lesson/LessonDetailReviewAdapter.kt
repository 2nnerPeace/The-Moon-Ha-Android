package com.innerpeace.themoonha.adapter.lesson

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.lesson.LessonReview

/**
 * 강좌 상세 후기 어댑터
 *
 * @author 손승완
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.15  	손승완       최초 생성
 * </pre>
 * @since 2024.09.15
 */
class LessonDetailReviewAdapter(private val context: Context, private val reviews: List<LessonReview>) :
    RecyclerView.Adapter<LessonDetailReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageViewReviewDetails: ImageView = view.findViewById(R.id.imageViewReviewDetails)
        val textViewUsername: TextView = view.findViewById(R.id.textViewUsername)
        val textViewReviewContent: TextView = view.findViewById(R.id.textViewReviewContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.fragment_lesson_detail_review_item, parent, false)
        return ReviewViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.imageViewReviewDetails.setImageResource(review.imageResId)
        holder.textViewUsername.text = review.username
        holder.textViewReviewContent.text = review.content
    }

    override fun getItemCount(): Int {
        return reviews.size
    }
}