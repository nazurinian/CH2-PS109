package com.submission.soilink.view.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.submission.soilink.databinding.ActivityProfilBinding
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.login.LoginActivity

class ProfileActivity : AppCompatActivity() {

    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory.getInstance(this)
    }


    private lateinit var binding: ActivityProfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAction()
        showProfile()
    }

    private fun setupAction() {
//        val user = intent.getStringExtra(DATA_PROFILE)

        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
//            title = "Profile"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showProfile() {

    }

    companion object {
        const val DATA_PROFILE = "DATA_PROFILE"
    }
}