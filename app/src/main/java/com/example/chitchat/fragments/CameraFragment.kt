package com.example.chitchat.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.chitchat.R
import com.example.chitchat.viewModels.ConversationViewModel
import com.example.chitchat.viewModels.ConversationViewModelFactory
import com.example.chitchat.repository.ChatDatabaseImpl
import com.example.chitchat.repository.ChatDatabaseRepositoryImpl
import com.example.chitchat.databinding.FragmentCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    companion object {
        private const val TAG = "CameraXBasic"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }


    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: FragmentCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var viewModel: ConversationViewModel
    private var imageUri: Uri? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Camera implemented according to docs and official android CameraX tutorials
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelFactory =
            ConversationViewModelFactory(
                ChatDatabaseImpl(ChatDatabaseRepositoryImpl())
            )
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ConversationViewModel::class.java)

        cameraExecutor = Executors.newSingleThreadExecutor()
        setOnTakePhotoClickListener()
        setOnClosePreviewClickListener()
        setOnAcceptClickListener()
        setOnSwitchCameraClickListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_camera, container, false)

        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }


        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture

        val photoFile =
            File(requireContext().getExternalFilesDir(null)?.absolutePath + "/${System.currentTimeMillis()}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    showPreview(savedUri)
                    Log.d(TAG, msg)
                }
            })
    }

    private fun setOnTakePhotoClickListener() {
        binding.buttonTakePhoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun showPreview(imageUri: Uri) {
        binding.imagePreview.setImageURI(imageUri)
        binding.layoutImagePreview.visibility = View.VISIBLE
        binding.layoutViewFinder.visibility = View.GONE
        this.imageUri = imageUri
    }

    private fun closePreview() {
        binding.imagePreview.setImageURI(null)
        binding.layoutImagePreview.visibility = View.GONE
        binding.layoutViewFinder.visibility = View.VISIBLE
        this.imageUri = null
    }

    private fun setOnClosePreviewClickListener() {
        binding.imageButtonReturn.setOnClickListener {
            closePreview()
        }
    }

    private fun setOnSwitchCameraClickListener() {
        binding.buttonSwitchCamera.setOnClickListener {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera()
        }

    }

    private fun setOnAcceptClickListener() {
        binding.imageButtonAccept.setOnClickListener {
            if (this.imageUri != null)
                viewModel.updateCurrentPhotoUri(this.imageUri) //if user`decides to keep the image

            findNavController().popBackStack()
        }
    }

}