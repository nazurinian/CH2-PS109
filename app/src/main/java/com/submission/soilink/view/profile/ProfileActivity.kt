package com.submission.soilink.view.profile

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore
import com.submission.soilink.databinding.ActivityProfilBinding
import com.submission.soilink.util.CHANE_NAME_DIALOG
import com.submission.soilink.util.CHANE_PASSWORD_DIALOG
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.login.LoginActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ProfileActivity : AppCompatActivity() {

    private val viewModel by viewModels<ProfileViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivityProfilBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showProfile()
        setupAction()
    }

    private fun setupAction() {
        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.changeName.setOnClickListener {
            val changeNameFragment = ChangeNameFragment()
            changeNameFragment.show(supportFragmentManager, CHANE_NAME_DIALOG)
        }
        binding.changePassword.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            changePasswordFragment.show(supportFragmentManager, CHANE_PASSWORD_DIALOG)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showProfile() {
        val pref = UserPreference.getInstance(applicationContext.dataStore)
        val user = runBlocking { pref.getSession().first() }
        viewModel.getUserProfile(user.email).observe(this) { result ->
            when (result) {
                is ResultState.Loading -> {
                    showLoading(true)
                }

                is ResultState.Success -> {
                    showLoading(false)
                    val profile = result.data.user

//                    Glide.with(binding.root)
//                        .load(getDrawable(R.drawable.senyum))
//                        .placeholder(R.drawable.avatar_male)
//                        .into(binding.profile)
                    binding.textBelowImage.text = profile?.displayName
                    binding.textBottom.text = profile?.email
                }

                is ResultState.Error -> {
                    showLoading(false)
                    showToast(this, result.error)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        var detailProfile = binding.detailProfile
        detailProfile.apply {
            if (isLoading) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
                ObjectAnimator.ofFloat(detailProfile, View.ALPHA, 1f).apply {
                    duration = 500
                    start()
                }
            }
        }
    }

    companion object {
        const val DATA_PROFILE = "DATA_PROFILE"
    }
}