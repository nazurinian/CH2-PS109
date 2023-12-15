package com.submission.soilink.view.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.submission.soilink.R
import com.submission.soilink.databinding.ActivityHomeBinding
import com.submission.soilink.view.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavbar()
    }

    private fun setupNavbar() {
        val navView1: BottomNavigationView = binding.navView1
        val navView2: BottomNavigationView = binding.navView2

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView1.apply {
            setupWithNavController(navController)
            itemIconTintList = null
        }
        navView2.apply {
            setupWithNavController(navController)
            itemIconTintList = null
        }
    }
}
