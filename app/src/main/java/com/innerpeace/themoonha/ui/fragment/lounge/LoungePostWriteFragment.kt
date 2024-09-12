package com.innerpeace.themoonha.ui.fragment.lounge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.adapter.lounge.LoungePostImageAddAdapter
import com.innerpeace.themoonha.data.model.lounge.LoungePostRequest
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.LoungeService
import com.innerpeace.themoonha.data.repository.LoungeRepository
import com.innerpeace.themoonha.databinding.FragmentLoungePostWriteBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.LoungeViewModel
import com.innerpeace.themoonha.viewModel.factory.LoungeViewModelFactory

/**
 * 라운지 게시글 작성 프래그먼트
 * @author 조희정
 * @since 2024.08.24
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.24  	조희정       최초 생성
 * 2024.09.03  	조희정       게시글 작성, 앨범에서 이미지 불러오기 구현
 * </pre>
 */
class LoungePostWriteFragment : Fragment() {
    private var _binding: FragmentLoungePostWriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: LoungePostImageAddAdapter
    private val viewModel: LoungeViewModel by activityViewModels {
        LoungeViewModelFactory(LoungeRepository(ApiClient.getClient().create(LoungeService::class.java)))
    }

    private var images = ArrayList<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoungePostWriteBinding.inflate(inflater, container, false)
        val view = binding.root

        // 툴바 제목 변경
        (activity as? MainActivity)?.setToolbarTitle("글 작성")
        setHasOptionsMenu(true)

       return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 이미지 추가 버튼
        binding.btnGalleryAdd.setOnClickListener {
           val intent = Intent(Intent.ACTION_PICK).apply {
               type = "image/*"  // 이미지만 선택하도록 설정
               putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
               data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
           }
           startActivityForResult(intent, 200)
        }

        // 추가된 이미지 Recycler View
        adapter = LoungePostImageAddAdapter(images, requireContext())
        binding.rvImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvImages.adapter = adapter
    }


    // 이미지 등록
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == 200) {
            images.clear()

            if (data?.clipData != null) { // 사진 여러개 선택한 경우
                val count = data.clipData!!.itemCount
                if (count > 10) {
                    Toast.makeText(requireContext(), "사진은 10장까지 선택 가능합니다.", Toast.LENGTH_LONG).show()
                    return
                }
                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    images.add(imageUri)
                }

            } else { // 단일 선택
                data?.data?.let { uri ->
                    val imageUri: Uri? = uri
                    if (imageUri != null) {
                        images.add(imageUri)
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    // 툴바 메뉴 변경
    override fun onPrepareOptionsMenu(menu: Menu) {
        val item1 = menu.findItem(R.id.item1)
        val item2 = menu.findItem(R.id.item2)

        item1.isEnabled = false
        val saveView = TextView(requireContext()).apply {
            text = "저장     "
            typeface = ResourcesCompat.getFont(context, R.font.happiness_sans_bold)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

            setOnClickListener {
                registerPost()
            }
        }

        item2.actionView = saveView
    }

    // 게시물 등록
    private fun registerPost() {

        val contentText = binding.content.text.toString()
        val noticeYn = binding.cbNoticeYn.isChecked

        val selectedLoungeId = viewModel.selectedLoungeId.value ?: 0L
        val loungePostRequest = LoungePostRequest(
            content = contentText,
            loungeId = selectedLoungeId,
            noticeYn = noticeYn
        )
        viewModel.registerLoungePost(loungePostRequest, images, requireContext())


        viewModel.postResponse.observe(viewLifecycleOwner, Observer { response ->
            response?.let {
                if (it.success) {
                    Toast.makeText(context, "게시물 등록 성공!", Toast.LENGTH_SHORT).show()
                    // 페이지 이동
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.loungeHomeFragment, true)  // loungeHomeFragment까지 백스택에서 제거
                        .build()
                    findNavController().navigate(R.id.loungeHomeFragment, null, navOptions)
                } else {
                    Toast.makeText(context, "게시물 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}