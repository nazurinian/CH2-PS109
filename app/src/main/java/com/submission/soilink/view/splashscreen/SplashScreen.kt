package com.submission.soilink.view.splashscreen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.submission.soilink.view.home.HomeFragment.Companion.USER_NAME
import com.submission.soilink.databinding.ActivitySplashScreenBinding
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.home.HomeActivity
import com.submission.soilink.view.home.HomeViewModel
import com.submission.soilink.view.login.LoginActivity

class SplashScreen : AppCompatActivity() {
    private val viewModel by viewModels<HomeViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val splash = ObjectAnimator.ofFloat(binding.imageView, View.ALPHA, 1f).setDuration(3000)
        splash.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                loginCheck()
            }
        })
        splash.start()
    }

    private fun loginCheck() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                val intentToHome = Intent(this, HomeActivity::class.java)
                intentToHome.putExtra(USER_NAME, user.name)
                startActivity(intentToHome)
                finish()
            }
        }
    }
}