package com.submission.soilink.view.forgotpassword

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import com.submission.soilink.R
import com.submission.soilink.data.model.LoginRegistrationModel
import com.submission.soilink.data.pref.UserModel
import com.submission.soilink.databinding.ActivityForgotPasswordBinding
import com.submission.soilink.databinding.ActivityLoginBinding
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.home.HomeActivity
import com.submission.soilink.view.login.LoginActivity
import com.submission.soilink.view.login.LoginViewModel
import com.submission.soilink.view.register.RegisterActivity

class ForgotPasswordActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
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
//            errorMessage = getString(R.string.error_email_too_short)
            errorMessage = "Email minimal 6 karakter"
            doOnTextChanged { text, _, _, _ ->
                email = if (text.toString()
                        .isEmpty() || text.toString().length < 6
                ) null else text.toString()
            }
        }

        binding.resetButton.setOnClickListener {
            if (!email.isNullOrEmpty()) {
                //email yg mau direset paswordnya

                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                AlertDialog.Builder(this).apply {
//                                    setTitle(getString(R.string.info_login_alert))
                    setTitle("Selamat...")
//                                    setMessage(getString(R.string.login_message))
                    setMessage("Link reset password berhasil dikirimkan, silahkan cek kotak masuk email anda.")
                    setCancelable(false)
                    setPositiveButton(getString(R.string.login)) { _, _ ->
                        val intentToHome = Intent(context, LoginActivity::class.java)
                        intentToHome.flags =
                            Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intentToHome)
                        finish()
                    }
                    create()
                    show()
                }
            } else {
                if (emailField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_email_is_empty)
                    val errorTextIsEmpty = "Email tidak boleh kosong"
                    emailLayout.error = errorTextIsEmpty
                }
            }
        }

        binding.loginPageButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
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
        val resetButton = ObjectAnimator.ofFloat(binding.resetButton, View.ALPHA, 1f).setDuration(250)

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