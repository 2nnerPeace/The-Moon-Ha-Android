package com.innerpeace.themoonha.adapter.lounge

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.innerpeace.themoonha.adapter.lounge.viewHolder.LoungeListViewHolder
import com.innerpeace.themoonha.data.model.lounge.LoungeListResponse

/**
 * 라운지 목록 Recycler View
 * @author 조희정
 * @since 2024.08.30
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.30  	조희정       최초 생성
 * </pre>
 */
class LoungeListViewAdapter(
    private val clickListener: (LoungeListResponse) -> Unit) : RecyclerView.Adapter<LoungeListViewHolder>() {
    private val loungeList: ArrayList<LoungeListResponse> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoungeListViewHolder {
        return LoungeListViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LoungeListViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams

        // 이미지 마진 설정
        if (position % 2 == 0) {
            layoutParams.setMargins(0, 0, 20, 0)
        } else {
            layoutParams.setMargins(20, 0, 0, 0)
        }

        holder.itemView.layoutParams = layoutParams
        holder.onBind(loungeList[position], clickListener)
    }

    override fun getItemCount(): Int = loungeList.size

    fun setItems(items: List<LoungeListResponse>) {
        loungeList.clear()
        loungeList.addAll(items)
        notifyDataSetChanged()
    }
}