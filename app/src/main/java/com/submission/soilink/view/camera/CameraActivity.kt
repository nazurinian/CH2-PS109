package com.submission.soilink.view.camera

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.databinding.ActivityCameraBinding
import com.submission.soilink.util.NetworkCheck
import com.submission.soilink.util.createCustomTempFile
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showToast
import com.submission.soilink.util.uriToFile
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.home.HomeViewModel
import com.submission.soilink.view.result.ResultActivity
import com.submission.soilink.view.result.ResultActivity.Companion.EXTRA_IMAGE_URI

/**
 * 1. get Lokasi
 * #2. get Tanggal (diutils ya)
 * 3. setup list soil
 * 3. setup home information ntr bisa di expand
 * */
class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var networkCheck: NetworkCheck
    private var internetResult = true
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null


    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProvider = cameraProviderFuture!!.get()

        preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewCamera.surfaceProvider)
            }

        setupAction()
        setupView()
        checkInternetConnection()
    }

    private fun setupAction() {
        networkCheck = NetworkCheck(this)

        binding.switchCam.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA

            launchCamera()
        }

        binding.btnCamera.setOnClickListener {
            if (internetResult) {
                showToast(this, "Mengambil gambar")
                takePhoto()
            } else {
                showToast(this, getString(R.string.no_internet_connection))
            }
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun checkInternetConnection() {
        networkCheck.observe(this) { hasNetwork ->
            internetResult = hasNetwork
        }
    }

    private val orientationEventListener by lazy {
        object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }
                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                imageCapture?.targetRotation = rotation
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }

    public override fun onResume() {
        super.onResume()
        launchCamera()
    }

    private fun launchCamera() {

        cameraProviderFuture?.addListener({
            imageCapture = ImageCapture.Builder().build()

            try {
                bindCamera()
            } catch (exc: Exception) {
                showToast(this, "Gagal memulai kamera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val outputOptions = createCustomTempFile(this).build()
        unBindCamera()
        showLoading(true)

        AlertDialog.Builder(this).apply {
            setTitle("Cek tipe tanah?")
            setCancelable(false)
            setNegativeButton("Yes") { _, _ ->
                showToast(context, "Memulai proses cek tipe tanah")
                //logika upload data ke internet sekaligus proses bar on loading, success, error
                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            output.savedUri?.let { uri ->
                                val imageFile = uriToFile(uri, context).reduceFileImage()

                                viewModel.uploadPicture(imageFile)
                                    .observe(this@CameraActivity) { result ->
                                        if (result != null) {
                                            when (result) {
                                                is ResultState.Loading -> {
                                                    showLoading(true)
                                                }

                                                is ResultState.Success -> {
                                                    val intent =
                                                        Intent(context, ResultActivity::class.java)
                                                    intent.putExtra(
                                                        EXTRA_IMAGE_URI,
                                                        output.savedUri.toString()
                                                    )
                                                    startActivity(intent)
                                                    finish()
                                                    showToast(
                                                        context,
                                                        getString(R.string.post_successfull)
                                                    )
                                                }

                                                is ResultState.Error -> {
                                                    showToast(context, result.error)
                                                    showLoading(false)
                                                }
                                            }
                                        }
                                    }

                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            showToast(context, "Gagal mengambil gambar.")
                        }
                    }
                )
            }
            setPositiveButton("No") { _, _ ->
                showLoading(false)
                showToast(context, "Tidak jadi memprosess")
                bindCamera()
            }
            create()
            show()
        }
    }

    private fun bindCamera() {
        cameraProvider?.unbindAll()
        cameraProvider?.bindToLifecycle(
            this,
            cameraSelector,
            preview,
            imageCapture
        )
    }

    private fun unBindCamera() {
        cameraProvider?.unbind(preview)

    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnBack.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.switchCam.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.btnCamera.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}