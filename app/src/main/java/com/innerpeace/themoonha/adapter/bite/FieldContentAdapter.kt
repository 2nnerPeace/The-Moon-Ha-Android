package com.innerpeace.themoonha.adapter.bite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.innerpeace.themoonha.data.model.field.FieldListResponse
import com.innerpeace.themoonha.databinding.FragmentFieldContentBinding

class FieldContentAdapter(
    private val fieldList: List<FieldListResponse>,
    private val itemClickListener: (FieldListResponse) -> Unit
    ) : RecyclerView.Adapter<FieldContentAdapter.FieldContentViewHolder>() {
    inner class FieldContentViewHolder(val binding: FragmentFieldContentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FieldContentViewHolder {
        val binding = FragmentFieldContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldContentViewHolder, position: Int) {
        val fieldItem = fieldList[position % fieldList.size]

        Glide.with(holder.itemView.context)
            .load(fieldItem.thumbnailUrl)
            .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
            .into(holder.binding.content)

        holder.binding.title.text = fieldItem.title
        holder.binding.memberName.text = fieldItem.memberName

        Glide.with(holder.itemView.context)
            .load(fieldItem.profileImgUrl)
            .circleCrop()
            .into(holder.binding.profileImage)

        holder.binding.root.setOnClickListener {
            itemClickListener(fieldItem)
        }
    }

    override fun getItemCount(): Int = fieldList.size
}
