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
import com.submission.soilink.util.Permission
import com.submission.soilink.util.REQUIRED_CAMERA_PERMISSION
import com.submission.soilink.util.REQUIRED_COARSE_LOCATION_PERMISSION
import com.submission.soilink.util.REQUIRED_FINE_LOCATION_PERMISSION
import com.submission.soilink.util.REQUIRED_WRITE_STORAGE_PERMISSION
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.camera.CameraActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var permission: Permission

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permission = Permission(this@HomeActivity) {
            setupCamera()
        }
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
            Permission.handlePermissionFlow(permission)
        }
    }

    private fun setupCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }
}
