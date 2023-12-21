package com.submission.soilink.view.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.submission.soilink.R
import com.submission.soilink.databinding.ActivityHomeBinding
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.camera.CameraActivity

class HomeActivity : AppCompatActivity() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityHomeBinding
    private var imageUri: Uri? = null

    private val requestPermissionLaunch =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                showToast(this, "Camera permission denied")
            } else {
                showToast(this, "Camera permission granted")
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavbar()
    }

    private fun setupNavbar() {
        val navView1: BottomNavigationView = binding.navView1
        val navView2: BottomNavigationView = binding.navView2

        val navController = findNavController(R.id.nav_host_fragment_activity_home)

        navView1.apply {
            setupWithNavController(navController)
            itemIconTintList = null
        }
        navView2.apply {
            setupWithNavController(navController)
            itemIconTintList = null
        }

        binding.btnCamera.setOnClickListener {
            setupCamera()
        }
    }

    private fun setupCamera() {
        if (!allPermissionsGranted()) {
            requestPermissionLaunch.launch(REQUIRED_PERMISSION)
        } else{
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
