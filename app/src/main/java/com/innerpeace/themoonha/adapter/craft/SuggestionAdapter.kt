package com.innerpeace.themoonha.adapter.craft

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.craft.SuggestionDTO

/**
 * 문화공방 제안합니다 댓글 어댑터
 *
 * @author 손승완
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.04  	손승완       최초 생성
 * </pre>
 * @since 2024.09.04
 */
class SuggestionAdapter(private val suggestions: List<SuggestionDTO>) :
    RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_suggestion_item, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.bind(suggestion)
    }

    override fun getItemCount(): Int = suggestions.size

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val memberNameTextView: TextView = itemView.findViewById(R.id.memberName)
        private val memberProfileImageView: ImageView = itemView.findViewById(R.id.memberProfileImg)
        private val contentTextView: TextView = itemView.findViewById(R.id.content)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.createdAt)

        fun bind(suggestion: SuggestionDTO) {
            memberNameTextView.text = suggestion.memberName
            contentTextView.text = suggestion.content
            createdAtTextView.text = suggestion.createdAt
            if (!suggestion.memberProfileImgUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(suggestion.memberProfileImgUrl)
                    .into(memberProfileImageView)
            }
        }
    }
}