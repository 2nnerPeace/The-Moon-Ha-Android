package com.innerpeace.themoonha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.craft.PrologueDTO
import com.innerpeace.themoonha.databinding.FragmentPrologueItemBinding

class PrologueAdapter(
    private var prologueList: List<PrologueDTO>,
    private val onPrologueClick: (PrologueDTO) -> Unit
) : RecyclerView.Adapter<PrologueAdapter.PrologueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrologueViewHolder {
        val binding =
            FragmentPrologueItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrologueViewHolder(binding, onPrologueClick)
    }

    override fun onBindViewHolder(holder: PrologueViewHolder, position: Int) {
        val prologue = prologueList[position]
        holder.bind(prologue)
    }

    override fun getItemCount(): Int = prologueList.size

    fun updatePrologueList(newPrologueList: List<PrologueDTO>) {
        prologueList = newPrologueList
        notifyDataSetChanged()
    }

    class PrologueViewHolder(private val binding: FragmentPrologueItemBinding,
        private val onPrologueClick: (PrologueDTO) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(prologue: PrologueDTO) {
            binding.titleTextView.text = prologue.title
            binding.likeCountTextView.text = prologue.likeCnt.toString()
            binding.tutorNameTextView.text = prologue.tutorName


            val heartDrawable = if (prologue.alreadyLiked) {
                R.drawable.prologue_already_liked
            } else {
                R.drawable.prologue_like
            }

            binding.heartImageView.setImageResource(heartDrawable)

            Glide.with(binding.thumbnailImageView.context)
                .load(prologue.thumbnailUrl)
                .into(binding.thumbnailImageView)

            binding.root.setOnClickListener {
                onPrologueClick(prologue)
            }
        }
    }
}
