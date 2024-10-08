package com.innerpeace.themoonha.adapter.bite

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.beforeafter.BeforeAfterDetailResponse
import com.innerpeace.themoonha.databinding.FragmentBeforeAfterDetailItemBinding

/**
 * Before&After 상세보기용 어댑터
 * @author 김진규
 * @since 2024.08.25
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.26  	김진규       최초 생성
 * </pre>
 */
class BeforeAfterDetailAdapter(private val contents: List<BeforeAfterDetailResponse>) : RecyclerView.Adapter<BeforeAfterDetailAdapter.BeforeAfterDetailViewHolder>() {
    inner class BeforeAfterDetailViewHolder(private val binding: FragmentBeforeAfterDetailItemBinding): RecyclerView.ViewHolder(binding.root) {
        private var beforePlayer: ExoPlayer? = null
        private var afterPlayer: ExoPlayer? = null
        private var isTextExpanded = false

        fun bind(content: BeforeAfterDetailResponse) {
            setupBeforeContent(content)
            setupAfterContent(content)
            setupTextContent(content)
            setHashtags(content.hashtags)
        }

        private fun setupBeforeContent(content: BeforeAfterDetailResponse) {
            val beforeImageParams = binding.beforeImageDetail.layoutParams as ConstraintLayout.LayoutParams
            val beforeVideoParams = binding.beforeVideoDetail.layoutParams as ConstraintLayout.LayoutParams

            if (content.beforeIsImage == 1 && content.beforeUrl.isNullOrEmpty().not()) {
                binding.beforeImageDetail.visibility = View.VISIBLE
                binding.beforeVideoDetail.visibility = View.GONE

                Glide.with(binding.root.context)
                    .load(content.beforeUrl)
                    .error(R.drawable.ic_play)
                    .into(binding.beforeImageDetail)

                beforeImageParams.height = 0
                beforeImageParams.matchConstraintPercentHeight = 0.5f
            } else {
                binding.beforeImageDetail.visibility = View.GONE
                binding.beforeVideoDetail.visibility = View.VISIBLE
                beforePlayer = ExoPlayer.Builder(binding.root.context).build().apply {
                    setMediaItem(MediaItem.fromUri(content.beforeUrl))
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ALL
                }
                binding.beforeVideoDetail.player = beforePlayer
                binding.beforeVideoDetail.useController = false

                beforeVideoParams.height = 0
                beforeVideoParams.matchConstraintPercentHeight = 0.5f
            }
        }

        private fun setupAfterContent(content: BeforeAfterDetailResponse) {
            val afterImageParams = binding.afterImageDetail.layoutParams as ConstraintLayout.LayoutParams
            val afterVideoParams = binding.afterVideoDetail.layoutParams as ConstraintLayout.LayoutParams

            if (content.afterIsImage == 1) {
                binding.afterImageDetail.visibility = View.VISIBLE
                binding.afterVideoDetail.visibility = View.GONE
                Glide.with(binding.root.context)
                    .load(content.afterUrl)
                    .error(R.drawable.ic_play)
                    .into(binding.afterImageDetail)

                afterImageParams.height = 0
                afterImageParams.matchConstraintPercentHeight = 0.5f
            } else {
                binding.afterImageDetail.visibility = View.GONE
                binding.afterVideoDetail.visibility = View.VISIBLE
                afterPlayer = ExoPlayer.Builder(binding.root.context).build().apply {
                    setMediaItem(MediaItem.fromUri(content.afterUrl))
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ALL
                }
                binding.afterVideoDetail.player = afterPlayer
                binding.afterVideoDetail.useController = false

                afterVideoParams.height = 0
                afterVideoParams.matchConstraintPercentHeight = 0.5f
            }
        }

        private fun setupTextContent(content: BeforeAfterDetailResponse) {
            binding.titleDetail.text = content.title
            binding.memberNameDetail.text = content.memberName

            Glide.with(binding.root.context)
                .load(content.profileImgUrl)
                .circleCrop()
                .into(binding.profileImageDetail)

            binding.titleDetail.post {
                if (binding.titleDetail.layout.getEllipsisCount(0) > 0) {
                    binding.moreButton.visibility = View.VISIBLE
                } else {
                    binding.moreButton.visibility = View.GONE
                }
            }

            binding.moreButton.setOnClickListener {
                if (!isTextExpanded) {
                    binding.titleDetail.maxLines = Int.MAX_VALUE
                    binding.titleDetail.ellipsize = null
                    binding.moreButton.visibility = View.GONE
                    isTextExpanded = !isTextExpanded
                    binding.profileImageDetail.visibility = View.GONE
                    binding.memberNameDetail.visibility = View.GONE
                }
            }

            binding.titleDetail.setOnClickListener {
                if (isTextExpanded) {
                    binding.titleDetail.maxLines = 1
                    binding.titleDetail.ellipsize = TextUtils.TruncateAt.END
                    binding.moreButton.text = "더보기"
                    binding.moreButton.visibility = View.VISIBLE
                    isTextExpanded = false
                    binding.profileImageDetail.visibility = View.VISIBLE
                    binding.memberNameDetail.visibility = View.VISIBLE
                }
            }
        }

        private fun setHashtags(hashtags: List<String>) {
            val flow = binding.hashtagFlow
            val idList = mutableListOf<Int>()

            if (hashtags.isNullOrEmpty()) {
                flow.referencedIds = intArrayOf()
                return
            }

            for (hashtag in hashtags) {
                val textView = TextView(binding.root.context).apply {
                    id = View.generateViewId()
                    text = "#$hashtag"
                    setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.white))
                    setPadding(0, 4, 8, 4)
                    textSize = 12f
                    layoutParams = ViewGroup.LayoutParams (
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                binding.root.addView(textView)
                idList.add(textView.id)
            }
            flow.referencedIds = idList.toIntArray()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeforeAfterDetailAdapter.BeforeAfterDetailViewHolder {
        val binding = FragmentBeforeAfterDetailItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BeforeAfterDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BeforeAfterDetailViewHolder, position: Int) {
        holder.bind(contents[position])
    }

    override fun getItemCount(): Int = contents.size
}