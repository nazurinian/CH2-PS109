package com.submission.soilink.view.forgotpassword

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.databinding.ActivityForgotPasswordBinding
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.login.LoginActivity

class ForgotPasswordActivity : AppCompatActivity() {
    private val viewModel by viewModels<ForgetPasswordModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()

        playAnimation()
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

    private fun setupAction() {
        val emailField = binding.emailEditText
        val emailLayout = binding.emailEditTextLayout
        var email: String? = null

        emailField.apply {
            layout = emailLayout
            minimumLength = 6
            errorMessage = getString(R.string.error_email_too_short)
            doOnTextChanged { text, _, _, _ ->
                email = if (text.toString()
                        .isEmpty() || text.toString().length < 6
                ) null else text.toString()
            }
        }

        binding.resetButton.setOnClickListener {
            if (!email.isNullOrEmpty()) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                viewModel.sendLinkResetPassword(email.toString()).observe(this) { result ->
                    when (result) {
                        is ResultState.Loading -> {
                            showLoading(true)
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            )
                        }

                        is ResultState.Success -> {
                            showLoading(false)
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            showToast(
                                this,
                                getString(R.string.success_send_link)
                            )

                            val intentToLogin = Intent(this, LoginActivity::class.java)
                            intentToLogin.flags =
                                Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intentToLogin)
                            finish()
                        }

                        is ResultState.Error -> {
                            showLoading(false)
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            showToast(this, result.error)
                        }
                    }
                }
            } else {
                if (emailField.text.toString().isEmpty()) {
                    val errorTextIsEmpty = getString(R.string.error_email_is_empty)
                    emailLayout.error = errorTextIsEmpty
                }
            }
        }

        binding.loginPageButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun playAnimation() {
        val ellipseAnimation = ObjectAnimator.ofFloat(binding.ellipseImage, View.SCALE_X, 0.1f, 1f)
        ellipseAnimation.duration = 1000
        ellipseAnimation.interpolator = OvershootInterpolator()
        ellipseAnimation.start()

        val welcomeLogin =
            ObjectAnimator.ofFloat(binding.loginTrouble, View.ALPHA, 1f).setDuration(250)
        val loginPage =
            ObjectAnimator.ofFloat(binding.loginTroubleInfo, View.ALPHA, 1f).setDuration(250)

        val emailEdit =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val resetButton =
            ObjectAnimator.ofFloat(binding.resetButton, View.ALPHA, 1f).setDuration(250)

        val haveAccount =
            ObjectAnimator.ofFloat(binding.haveAccount, View.ALPHA, 1f).setDuration(250)

        val firstAnimation = AnimatorSet().apply {
            playSequentially(emailEdit, resetButton, haveAccount)
            startDelay = 250
        }

        AnimatorSet().apply {
            playTogether(welcomeLogin, loginPage, firstAnimation, ellipseAnimation)
            start()
        }
    }
}