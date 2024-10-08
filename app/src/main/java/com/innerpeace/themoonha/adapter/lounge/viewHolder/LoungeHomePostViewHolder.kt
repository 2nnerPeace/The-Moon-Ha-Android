package com.innerpeace.themoonha.adapter.lounge.viewHolder

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.innerpeace.themoonha.data.model.lounge.LoungePostListResponse
import com.innerpeace.themoonha.databinding.ItemPostBinding

/**
 * 라운지 게시물 Recycler View Holder
 * @author 조희정
 * @since 2024.09.01
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.01  	조희정       최초 생성
 * </pre>
 */
class LoungeHomePostViewHolder(private val binding: ItemPostBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(item: LoungePostListResponse, clickListener: (LoungePostListResponse) -> Unit) {
        // 프로필 이미지
        Glide.with(binding.ivProfileImage.context)
            .load(item.loungeMember.profileImgUrl)
            .circleCrop()
            .into(binding.ivProfileImage)

        // 텍스트
        binding.tvName.text = item.loungeMember.name
        binding.tvDate.text = item.createdAt
        binding.tvContent.text = item.content

        // 이미지 리스트
        bindImages(item.loungePostImgList)

        // 클릭 이벤트
        binding.root.setOnClickListener {
            clickListener(item)
        }
    }

    private fun bindImages(imageUrls: List<String>?) {
        val gridLayout = binding.glImages
        gridLayout.removeAllViews()

        if (imageUrls.isNullOrEmpty()) {
            gridLayout.visibility = View.GONE
            return
        } else {
            gridLayout.visibility = View.VISIBLE
        }

        val context = gridLayout.context
        val marginSize = 0.dpToPx()
        val specialMargin = 5.dpToPx()

        // 이미지 보여주기 (개수에 따라)
        when (imageUrls.size) {
            1 -> {
                gridLayout.columnCount = 1
                gridLayout.rowCount = 1
                val imageView = createImageView(context, marginSize)
                Glide.with(context)
                    .load(imageUrls[0])
                    .transform(CenterCrop(), RoundedCorners(20))
                    .into(imageView)
                gridLayout.addView(imageView, createGridLayoutParams(0, 0, 1, 1, marginSize))
            }
            2 -> {
                gridLayout.columnCount = 2
                gridLayout.rowCount = 1
                for (i in 0 until 2) {
                    val imageView = createImageView(context, marginSize)
                    Glide.with(context)
                        .load(imageUrls[i])
                        .transform(CenterCrop(), RoundedCorners(20))
                        .into(imageView)

                    val params = createGridLayoutParams(i % 2, 0, 1, 1, marginSize)
                    if (i == 0) {
                        params.setMargins(marginSize, marginSize, specialMargin, marginSize)
                    } else if (i == 1) {
                        params.setMargins(specialMargin, marginSize, marginSize, marginSize)
                    }
                    gridLayout.addView(imageView, params)
                }
            }
            3 -> {
                gridLayout.columnCount = 2
                gridLayout.rowCount = 2
                for (i in 0 until 3) {
                    val imageView = createImageView(context, marginSize)
                    Glide.with(context)
                        .load(imageUrls[i])
                        .transform(CenterCrop(), RoundedCorners(20))
                        .into(imageView)

                    val params = if (i == 0) {
                        createGridLayoutParams(0, 0, 2, 1, marginSize).apply {
                            setMargins(marginSize, marginSize, marginSize, specialMargin)
                        }
                    } else {
                        createGridLayoutParams((i + 1) % 2, 1, 1, 1, marginSize).apply {
                            if (i == 1) {
                                setMargins(marginSize, specialMargin, specialMargin, marginSize)
                            } else {
                                setMargins(specialMargin, specialMargin, marginSize, marginSize)
                            }
                        }
                    }
                    gridLayout.addView(imageView, params)
                }
            }
            else -> {
                gridLayout.columnCount = 2
                gridLayout.rowCount = 2
                for (i in 0 until 4) {
                    val imageView = createImageView(context, marginSize)
                    Glide.with(context)
                        .load(imageUrls[i])
                        .transform(CenterCrop(), RoundedCorners(20))
                        .into(imageView)

                    val params = createGridLayoutParams(i % 2, i / 2, 1, 1, marginSize)
                    when (i) {
                        0 -> params.setMargins(marginSize, marginSize, specialMargin, specialMargin)
                        1 -> params.setMargins(specialMargin, marginSize, marginSize, specialMargin)
                        2 -> params.setMargins(marginSize, specialMargin, specialMargin, marginSize)
                        3 -> params.setMargins(specialMargin, specialMargin, marginSize, marginSize)
                    }
                    gridLayout.addView(imageView, params)
                }
            }
        }
    }

    private fun createImageView(context: android.content.Context, marginSize: Int): ImageView {
        return ImageView(context).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(marginSize, marginSize, marginSize, marginSize)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun createGridLayoutParams(column: Int, row: Int, colSpan: Int, rowSpan: Int, marginSize: Int): GridLayout.LayoutParams {
        return GridLayout.LayoutParams().apply {
            this.columnSpec = GridLayout.spec(column, colSpan, 1f)
            this.rowSpec = GridLayout.spec(row, rowSpan, 1f)
            this.width = 0
            this.height = 0
            setMargins(marginSize, marginSize, marginSize, marginSize)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    companion object {
        fun from(parent: ViewGroup): LoungeHomePostViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPostBinding.inflate(inflater, parent, false)
            return LoungeHomePostViewHolder(binding)
        }
    }
}