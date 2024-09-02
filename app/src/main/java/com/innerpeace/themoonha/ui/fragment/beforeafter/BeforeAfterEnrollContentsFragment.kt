package com.innerpeace.themoonha.ui.fragment.beforeafter

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import android.widget.Toast.makeText
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.innerpeace.themoonha.R
import com.innerpeace.themoonha.databinding.FragmentBeforeAfterEnrollContentsBinding
import java.io.File

/**
 * Before&After Enroll Contents 프래그먼트
 * @author 김진규
 * @since 2024.08.28
 * @version 1.0
 *
 * <pre>
 * 수정일        수정자        수정내용
 * ----------  --------    ---------------------------
 * 2024.08.28  	김진규       최초 생성
 * </pre>
 */
class BeforeAfterEnrollContentsFragment : Fragment() {
    private var _binding: FragmentBeforeAfterEnrollContentsBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CAMERA_PERMISSION = 100
    private val REQUEST_GALLERY_BEFORE = 1
    private val REQUEST_GALLERY_AFTER = 2
    private val REQUEST_CAMERA_BEFORE_PHOTO = 3
    private val REQUEST_CAMERA_AFTER_PHOTO = 4
    private val REQUEST_CAMERA_BEFORE_VIDEO = 5
    private val REQUEST_CAMERA_AFTER_VIDEO = 6

    private var currentRequestCode: Int = REQUEST_CAMERA_BEFORE_PHOTO
    private var photoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBeforeAfterEnrollContentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<Toolbar>(R.id.toolbar)?.visibility = View.GONE
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)?.visibility = View.GONE

        binding.backButton.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.backButton.setOnClickListener {
            activity?.onBackPressed()
        }

        binding.cameraButton.setColorFilter(ContextCompat.getColor(requireContext(), android.R.color.white))
        binding.cameraButton.setOnClickListener {
            if (hasCameraPermission()) {
                selectCameraMode()
            } else {
                requestCameraPermission()
            }
        }

        binding.gallery.setOnClickListener {
            selectGalleryTarget()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                selectCameraMode()
            } else {
                makeText(requireContext(), "카메라 및 저장소 권한이 필요합니다.", LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val contentUri = when (requestCode) {
                REQUEST_GALLERY_BEFORE, REQUEST_GALLERY_AFTER -> data?.data
                REQUEST_CAMERA_BEFORE_PHOTO, REQUEST_CAMERA_AFTER_PHOTO -> photoUri
                REQUEST_CAMERA_BEFORE_VIDEO, REQUEST_CAMERA_AFTER_VIDEO -> data?.data
                else -> null
            }

            contentUri?.let { uri ->
                val contentResolver = requireContext().contentResolver
                val mimeType = contentResolver.getType(uri)

                when {
                    mimeType?.startsWith("image") == true -> {
                        if (requestCode == REQUEST_GALLERY_BEFORE || requestCode == REQUEST_CAMERA_BEFORE_PHOTO) {
                            binding.beforeContentImage.visibility = View.VISIBLE
                            binding.beforeContentVideo.visibility = View.GONE
                            binding.beforeContentImage.setImageURI(uri)
                        } else {
                            binding.afterContentImage.visibility = View.VISIBLE
                            binding.afterContentVideo.visibility = View.GONE
                            binding.afterContentImage.setImageURI(uri)
                        }
                    }
                    mimeType?.startsWith("video") == true -> {
                        if (requestCode == REQUEST_GALLERY_BEFORE || requestCode == REQUEST_CAMERA_BEFORE_VIDEO) {
                            binding.beforeContentImage.visibility = View.GONE
                            binding.beforeContentVideo.visibility = View.VISIBLE
                            binding.beforeContentVideo.setVideoURI(uri)
                            binding.beforeContentVideo.start()
                        } else {
                            binding.afterContentImage.visibility = View.GONE
                            binding.afterContentVideo.visibility = View.VISIBLE
                            binding.afterContentVideo.setVideoURI(uri)
                            binding.afterContentVideo.requestFocus()
                            binding.afterContentVideo.start()
                        }
                    }
                }
            }
        }
    }

    private fun selectGalleryTarget() {
        val options = arrayOf("Before 콘텐츠", "After 콘텐츠")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("콘텐츠 종류")
        builder.setItems(options) { _, option ->
            currentRequestCode = when (option) {
                0 -> REQUEST_GALLERY_BEFORE
                1 -> REQUEST_GALLERY_AFTER
                else -> REQUEST_GALLERY_BEFORE
            }
            openGalleryForContent()
        }
        builder.show()
    }

    private fun openGalleryForContent() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        startActivityForResult(intent, currentRequestCode)
    }

    private fun requestCameraPermission() {
        requestPermissions(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
            ),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun selectCameraMode() {
        val targetOptions = arrayOf("Before 콘텐츠", "After 콘텐츠")
        val targetBuilder = AlertDialog.Builder(requireContext())
        targetBuilder.setTitle("콘텐츠 종류")
        targetBuilder.setItems(targetOptions) { _, targetOption ->
            val isBefore = (targetOption == 0)

            val actionOptions = arrayOf("사진 촬영", "비디오 녹화")
            val actionBuilder = AlertDialog.Builder(requireContext())
            actionBuilder.setTitle("촬영 방식")
            actionBuilder.setItems(actionOptions) { _, actionOption ->
                currentRequestCode = when (actionOption) {
                    0 -> if (isBefore) REQUEST_CAMERA_BEFORE_PHOTO else REQUEST_CAMERA_AFTER_PHOTO
                    1 -> if (isBefore) REQUEST_CAMERA_BEFORE_VIDEO else REQUEST_CAMERA_AFTER_VIDEO
                    else -> if (isBefore) REQUEST_CAMERA_BEFORE_PHOTO else REQUEST_CAMERA_AFTER_PHOTO
                }
                when (actionOption) {
                    0 -> openCameraForPhoto()
                    1 -> openCameraForVideo()
                }
            }
            actionBuilder.show()
        }
        targetBuilder.show()
    }

    private fun openCameraForVideo() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        startActivityForResult(intent, currentRequestCode)
    }

    private fun openCameraForPhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoUri = createImageFileUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, currentRequestCode)
    }

    private fun createImageFileUri(): Uri {
        val imageFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}",
            ".jpg",
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            imageFile
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}