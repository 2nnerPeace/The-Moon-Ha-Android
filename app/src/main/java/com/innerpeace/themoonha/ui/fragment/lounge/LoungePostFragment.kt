package com.innerpeace.themoonha.ui.fragment.lounge

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.innerpeace.themoonha.adapter.lounge.LoungeCommentViewAdapter
import com.innerpeace.themoonha.adapter.lounge.LoungePostImageAdapter
import com.innerpeace.themoonha.data.model.lounge.LoungeCommentRequest
import com.innerpeace.themoonha.data.model.lounge.LoungePostResponse
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.LoungeService
import com.innerpeace.themoonha.data.repository.LoungeRepository
import com.innerpeace.themoonha.databinding.FragmentLoungePostBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.LoungeViewModel
import com.innerpeace.themoonha.viewModel.factory.LoungeViewModelFactory

/**
 * 라운지 게시글 상세 프래그먼트
 * @author 조희정
 * @since 2024.08.23
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.24  	조희정       최초 생성
 * 2024.09.02   조희정       이미지, 댓글 recycler view 구현
 * 2024.09.03   조희정       댓글 등록 구현
 * </pre>
 */
class LoungePostFragment : Fragment() {
    private var _binding: FragmentLoungePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: LoungeCommentViewAdapter
    private lateinit var imageAdapter: LoungePostImageAdapter

    private val viewModel: LoungeViewModel by activityViewModels {
        LoungeViewModelFactory(LoungeRepository(ApiClient.getClient().create(LoungeService::class.java)))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoungePostBinding.inflate(inflater, container, false)
        val view = binding.root

        (activity as? MainActivity)?.showToolbar()


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 데이터 불러오기
        viewModel.selectedLoungeId.observe(viewLifecycleOwner, Observer { loungeId ->
            if (loungeId != null) {
                viewModel.selectedLoungePostId.observe(viewLifecycleOwner, Observer { postId ->
                    if (postId != null) {
                        viewModel.fetchPostDetail(loungeId, postId)
                    }
                })
            }
        })

        viewModel.postDetail.observe(viewLifecycleOwner, Observer { postDetail ->
            postDetail?.let {
                Glide.with(binding.ivProfileImage.context)
                    .load(postDetail.loungePost.loungeMember.profileImgUrl)
                    .circleCrop()
                    .into(binding.ivProfileImage)
                binding.tvName.text = postDetail.loungePost.loungeMember.name
                binding.tvDate.text = postDetail.loungePost.createdAt
                binding.tvContent.text = postDetail.loungePost.content
                setupImageRecyclerView(it.loungePost.loungePostImgList)
                if (postDetail.loungeCommentList.isNotEmpty()) {
                    binding.line.visibility = View.VISIBLE
                    setupCommentRecyclerView(it.loungeCommentList)
                }
            }
        })

        // 댓글 등록 버튼
        binding.btnCommentRegister.setOnClickListener {
            registerComment()
        }
    }

    // 이미지 Recycler View
    private fun setupImageRecyclerView(item: List<String>) {
        imageAdapter = LoungePostImageAdapter(item)
        binding.rvImageList.adapter = imageAdapter

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvImageList.layoutManager = linearLayoutManager
    }

    // 댓글 Recycler View
    private fun setupCommentRecyclerView(item: List<LoungePostResponse.LoungeComment>) {
        commentAdapter = LoungeCommentViewAdapter()
        binding.rvCommentList.adapter = commentAdapter

        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvCommentList.layoutManager = linearLayoutManager

        commentAdapter.setItems(item)
    }

    // 댓글 등록
    private fun registerComment() {
        val commentText = binding.etNewComment.text.toString()

        val selectedLoungePostId = viewModel.selectedLoungePostId.value ?: 0L
        val selectedLoungeId = viewModel.selectedLoungeId.value ?: 0L
        val loungeCommentRequest = LoungeCommentRequest(
            loungePostId = selectedLoungePostId,
            content = commentText
        )
        viewModel.registerComment(loungeCommentRequest)

        viewModel.commentResponse.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                if (it.success) {
                    Toast.makeText(context, "댓글 등록 성공!", Toast.LENGTH_SHORT).show()
                    binding.etNewComment.text = null
                    viewModel.fetchPostDetail(selectedLoungeId, selectedLoungePostId)
                } else {
                    Toast.makeText(context, "댓글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}