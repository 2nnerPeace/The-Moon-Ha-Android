package com.innerpeace.themoonha.ui.fragment.field

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.data.model.field.FieldRequest
import com.innerpeace.themoonha.data.network.ApiClient
import com.innerpeace.themoonha.data.network.LessonService
import com.innerpeace.themoonha.data.repository.FieldRepository
import com.innerpeace.themoonha.data.repository.LessonRepository
import com.innerpeace.themoonha.databinding.FragmentFieldEnrollContentsPhraseBinding
import com.innerpeace.themoonha.ui.activity.common.MainActivity
import com.innerpeace.themoonha.viewModel.FieldViewModel
import com.innerpeace.themoonha.viewModel.LessonViewModel
import com.innerpeace.themoonha.viewModel.factory.FieldViewModelFactory
import com.innerpeace.themoonha.viewModel.factory.LessonViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * 분야별 한입 콘텐츠 등록 문구 추가용 프래그먼트
 * @author 김진규
 * @since 2024.09.03
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.09.03  	김진규       최초 생성
 * </pre>
 */
class FieldEnrollContentsPhraseFragment : Fragment() {
    private var _binding: FragmentFieldEnrollContentsPhraseBinding? = null
    private val binding get() = _binding!!

    private val fieldViewModel: FieldViewModel by viewModels {
        FieldViewModelFactory(FieldRepository())
    }

    private val lessonViewModel: LessonViewModel by viewModels {
        LessonViewModelFactory(LessonRepository(ApiClient.getClient().create(LessonService::class.java)))
    }

    private var contentUri: Uri? = null
    private var selectedLessonId: Long? = null
    private val hashtags = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contentUri = it.getParcelable("contentUri")
        }

        lessonViewModel.getLessonFieldEnroll()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFieldEnrollContentsPhraseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.apply {
            hideToolbar()
            hideBottomNavigation()
        }

        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        displayContent()
        val lessonSpinner = binding.lessonSpinner

        lessonViewModel.lessonFieldEnroll.observe(viewLifecycleOwner) { lessons ->
            val lessonTitles = lessons.map { it.title }
            val arrayAdapter =
                ArrayAdapter(requireContext(), R.layout.spinner_selected_item, lessonTitles)
            arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            lessonSpinner.adapter = arrayAdapter

            lessonSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    selectedLessonId = lessons[p2].lessonId

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    selectedLessonId = null
                }
            }

            binding.nextButton.setOnClickListener {
                submitFieldContent()
            }
        }

        binding.inputPhrase.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.endsWith(" ") || text.endsWith("\n")) {
                    val trimmedText = text.trim()
                    if (trimmedText.startsWith("#")) {
                        val originalHashTag = trimmedText.removePrefix("#")
                        hashtags.add(originalHashTag)
                        addHashtag(trimmedText)
                        binding.inputPhrase.text.clear()
                    }
                }
            }
        })

        binding.nextButton.setOnClickListener {
            binding.nextButton.isEnabled = false
            submitFieldContent()
        }
    }

    private fun addHashtag(tag: String) {
        val hashtagView = TextView(requireContext()).apply {
            id = View.generateViewId()
            text = tag
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            layoutParams = ConstraintLayout.LayoutParams (
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 8
                bottomMargin = 8
            }
        }

        binding.root.addView(hashtagView)
        binding.hashtagFlow.referencedIds += hashtagView.id
    }

    private fun submitFieldContent() {
        val lessonId = selectedLessonId ?: run {
            return
        }

        val fieldRequest = FieldRequest(
            lessonId = lessonId,
            title = binding.inputPhrase.text.toString(),
            hashtags = hashtags
        )

        val content = contentUri?.let { uri ->
            convertUriToMultiPart(uri, "content")
        }

        val thumbnail = contentUri?.let { uri ->
            val contentResolver = requireContext().contentResolver
            val type = contentResolver.getType(uri)

            if (type?.startsWith("image") == true) {
                convertUriToMultiPart(uri, "thumbnail")
            } else {
                createThumbnailMultiPart(uri, "thumbnail")
            }
        }

        makeFieldContent(content, thumbnail, fieldRequest)
    }

    private fun makeFieldContent(
        content: MultipartBody.Part?,
        thumbnail: MultipartBody.Part?,
        fieldRequest: FieldRequest
    ) {
        if (content != null && thumbnail != null) {
            lifecycleScope.launch {
                val result = fieldViewModel.makeField(
                    Gson().toJson(fieldRequest)
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()),
                    content,
                    thumbnail
                )

                result.fold(
                    onSuccess = {
                        val navOptions = NavOptions.Builder()
                            .setPopUpTo(R.id.fieldListFragment, true)
                            .build()
                        findNavController().navigate(R.id.fieldListFragment, null, navOptions)
                    },
                    onFailure = {
                        Toast.makeText(
                            requireContext(),
                            "필드 생성에 실패했습니다. 다시 시도해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }

    private fun createThumbnailMultiPart(uri: Uri, content: String): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val type = contentResolver.getType(uri)

        val thumbnailFile = if (type?.startsWith("video") == true) {
            createVideoThumbnail(uri)
        } else {
            null
        }

        return thumbnailFile?.let { file ->
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(content, file.name, requestBody)
        } ?: throw IllegalStateException("썸네일 생성 실패")
    }

    private fun createVideoThumbnail(uri: Uri): File {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(requireContext(), uri)

        val bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

        val tempFile = File.createTempFile("thumbnail", ".jpg", requireContext().cacheDir)
        val outputStream = FileOutputStream(tempFile)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        outputStream.flush()
        outputStream.close()

        retriever.release()

        return tempFile
    }

    private fun convertUriToMultiPart(uri: Uri, content: String): MultipartBody.Part {
        val contentResolver = requireContext().contentResolver
        val tempFile = File.createTempFile(content, ".tmp", requireContext().cacheDir)
        tempFile.outputStream().use {
            contentResolver.openInputStream(uri)?.copyTo(it)
        }
        return MultipartBody.Part.createFormData(
            content,
            tempFile.name,
            tempFile.asRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
        )
    }

    private fun displayContent() {
        contentUri?.let { uri ->
            val mimeType = requireContext().contentResolver.getType(uri)
            if (mimeType?.startsWith("image") == true) {
                binding.contentImage.visibility = View.VISIBLE
                binding.contentVideo.visibility = View.GONE
                binding.contentImage.setImageURI(uri)
            } else if (mimeType?.startsWith("video") == true) {
                binding.contentImage.visibility = View.GONE
                binding.contentVideo.visibility = View.VISIBLE
                binding.contentVideo.setVideoURI(uri)
                binding.contentVideo.start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}