package com.innerpeace.themoonha.adapter.lesson

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.lesson.ShortFormDTO

/**
 * 숏폼 어댑터
 *
 * @author 손승완
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.31 	손승완       최초 생성
 * </pre>
 * @since 2024.08.31
 */
class ShortFormAdapter( private var shortForms: List<ShortFormDTO>,
                        private val onItemClicked: (ShortFormDTO, Int) -> Unit
) : RecyclerView.Adapter<ShortFormAdapter.ShortFormViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortFormViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_short_form_item, parent, false)
        return ShortFormViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShortFormViewHolder, position: Int) {
        val shortForm = shortForms[position]
        holder.bind(shortForm)
        holder.itemView.setOnClickListener {
            onItemClicked(shortForm, position)
        }
    }

    override fun getItemCount(): Int = shortForms.size

    fun updateShortForms(newShortForms: List<ShortFormDTO>) {
        this.shortForms = newShortForms
        notifyDataSetChanged()
    }

    class ShortFormViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val thumbnail: ImageView = itemView.findViewById(R.id.thumbnail)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val targetDescription: TextView? = itemView.findViewById(R.id.targetDescription)

        fun bind(shortForm: ShortFormDTO) {
            name.text = shortForm.shortFormName
            targetDescription?.text = shortForm.getTargetDescription()
            Glide.with(itemView.context)
                .load(shortForm.thumbnailUrl)
                .into(thumbnail)
        }
    }
}
