package com.innerpeace.themoonha.adapter.bite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.innerpeace.themoonha.data.model.field.FieldCategoryGroup
import com.innerpeace.themoonha.data.model.field.FieldListResponse
import com.innerpeace.themoonha.databinding.FragmentFieldCategoryBinding

/**
 * 분야별 한입 목록용 어댑터
 * @author 김진규
 * @since 2024.08.25
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.25  	김진규       최초 생성
 * </pre>
 */
class FieldListAdapter(
    private var fieldLists: List<FieldCategoryGroup>,
    private val onItemClick: (FieldListResponse) -> Unit
    ) : RecyclerView.Adapter<FieldListAdapter.FieldCategoryViewHolder>() {
    inner class FieldCategoryViewHolder(val binding: FragmentFieldCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FieldCategoryViewHolder {
        val binding = FragmentFieldCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FieldCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FieldCategoryViewHolder, position: Int) {
        val categoryGroup = fieldLists[position]

        holder.binding.categoryName.text = categoryGroup.category

        val fieldContentAdapter = FieldContentAdapter(categoryGroup.fieldList) { selectedField ->
            onItemClick(selectedField)
        }
        holder.binding.horizontalRecyclerView.apply {
            layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = fieldContentAdapter
            setHasFixedSize(true)
        }
    }

    fun update(newFieldLists: List<FieldCategoryGroup>) {
        this.fieldLists = newFieldLists
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = fieldLists.size
}