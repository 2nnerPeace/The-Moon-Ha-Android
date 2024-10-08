package com.innerpeace.themoonha.adapter.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.innerpeace.themoonha.data.model.live.LiveLessonResponse
import com.innerpeace.themoonha.databinding.FragmentLiveContentBinding

/**
 * 멤버가 소유하지 않은 실시간 강좌 목록용 어댑터
 * @author 김진규
 * @since 2024.09.06
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.06  	김진규       최초 생성
 * </pre>
 */
class LiveOnAirListAdapter(
    private var onAirList: List<LiveLessonResponse>,
    private val itemClickListener: (LiveLessonResponse) -> Unit
) : RecyclerView.Adapter<LiveOnAirListAdapter.LiveOnAirListViewHolder>() {
    inner class LiveOnAirListViewHolder(val binding: FragmentLiveContentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiveOnAirListViewHolder {
        return LiveOnAirListViewHolder(
            FragmentLiveContentBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: LiveOnAirListViewHolder, position: Int) {
        val onAirContent = onAirList[position]
        listenEvent(holder, onAirContent)
        setUpContent(holder, onAirContent)
        setUpBottomContent(holder, onAirContent)
    }

    private fun setUpBottomContent(
        holder: LiveOnAirListViewHolder,
        onAirContent: LiveLessonResponse
    ) {
        holder.binding.title.text = onAirContent.title
        Glide.with(holder.binding.root.context)
            .load(onAirContent.profileImgUrl)
            .circleCrop()
            .into(holder.binding.profileImage)
        holder.binding.instructorName.text = onAirContent.instructorName
        if (onAirContent.minutesAgo >= 60) {
            var hour = onAirContent.minutesAgo / 60
            holder.binding.minutesAgo.text = "시작: ${hour}시간 전"
        } else {
            holder.binding.minutesAgo.text = "시작: ${onAirContent.minutesAgo}분 전"
        }
    }

    private fun setUpContent(
        holder: LiveOnAirListViewHolder,
        onAirContent: LiveLessonResponse
    ) {
        Glide.with(holder.binding.root.context)
            .load(onAirContent.thumbnailUrl)
            .into(holder.binding.liveContentImage)
    }

    private fun listenEvent(
        holder: LiveOnAirListViewHolder,
        content: LiveLessonResponse
    ) {
        holder.binding.root.setOnClickListener {
            itemClickListener(content)
        }
    }

    fun updateContents(contents: List<LiveLessonResponse>) {
        onAirList = contents
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = onAirList.size
}