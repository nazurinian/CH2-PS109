package com.submission.soilink.view.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.model.PostHistoryModel
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.ActivityCameraBinding
import com.submission.soilink.util.NetworkCheck
import com.submission.soilink.util.REQUIRED_COARSE_LOCATION_PERMISSION
import com.submission.soilink.util.REQUIRED_FINE_LOCATION_PERMISSION
import com.submission.soilink.util.createCustomTempFile
import com.submission.soilink.util.getCurrentDate
import com.submission.soilink.util.reduceFileImage
import com.submission.soilink.util.showLocation
import com.submission.soilink.util.showToast
import com.submission.soilink.util.uriToFile
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.home.HomeViewModel
import com.submission.soilink.view.result.ResultActivity
import com.submission.soilink.view.result.ResultActivity.Companion.EXTRA_IMAGE_URI
import com.submission.soilink.view.result.ResultActivity.Companion.RESULT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var networkCheck: NetworkCheck
    private var internetResult = true
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null

    private var latitude: Double? = null
    private var longitude: Double? = null
    private val locationPermissionCode = 2

    private lateinit var fusedLocationClient: FusedLocationProviderClient


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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.switchCam.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
                else CameraSelector.DEFAULT_BACK_CAMERA

            launchCamera()
        }

        binding.btnCamera.setOnClickListener {
            if (internetResult) {
                showToast(this, getString(R.string.get_image))
                requestLocationUpdates()
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
                showToast(this, getString(R.string.load_camera_failed))
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val pref = UserPreference.getInstance(applicationContext.dataStore)
        val user = runBlocking { pref.getSession().first() }

        val imageCapture = imageCapture ?: return
        val outputOptions = createCustomTempFile(this).build()
        showLoading(true)
        unBindCamera()

        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.check_soil_type))
            setCancelable(false)
            setNegativeButton(getString(R.string.btn_yes)) { _, _ ->
                showToast(context, getString(R.string.start_check))

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            output.savedUri?.let { uri ->
                                val imageFile = uriToFile(uri, context).reduceFileImage()

                                viewModel.locationUpdated.observe(this@CameraActivity) { locationResult ->
                                    if (locationResult) {
                                        val postHistory = PostHistoryModel(
                                            email = user.email,
                                            image = imageFile,
                                            dateTime = getCurrentDate(),
                                            lat = latitude,
                                            long = longitude
                                        )

                                        viewModel.addHistory(postHistory).observe(this@CameraActivity) { result ->
                                            if (result != null) {
                                                when (result) {
                                                    is ResultState.Loading -> {
                                                        showLoading(true)
                                                    }

                                                    is ResultState.Success -> {
                                                        showToast(
                                                            context,
                                                            getString(R.string.post_successfull)
                                                        )
                                                        val intent =
                                                            Intent(
                                                                context,
                                                                ResultActivity::class.java
                                                            )
                                                        intent.putExtra(RESULT, postHistory)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                    is ResultState.Error -> {
                                                        bindCamera()
                                                        showToast(context, result.error)
                                                        showLoading(false)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            bindCamera()
                            showLoading(false)
                            showToast(context, getString(R.string.failed_capture_image))
                        }
                    }
                )
            }
            setPositiveButton(getString(R.string.btn_no)) { _, _ ->
                showLoading(false)
                showToast(context, getString(R.string.not_processing))
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

    @Suppress("DEPRECATION")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(2000)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.lastLocation?.let { location ->
                    latitude = location.latitude
                    longitude = location.longitude

                    viewModel.setLocation(true)

                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                REQUIRED_COARSE_LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                REQUIRED_FINE_LOCATION_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(REQUIRED_COARSE_LOCATION_PERMISSION),
                locationPermissionCode
            )

            ActivityCompat.requestPermissions(
                this,
                arrayOf(REQUIRED_FINE_LOCATION_PERMISSION),
                locationPermissionCode
            )
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnBack.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.switchCam.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.btnCamera.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}