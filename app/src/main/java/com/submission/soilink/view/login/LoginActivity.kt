package com.submission.soilink.view.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.submission.soilink.view.home.HomeFragment.Companion.USER_NAME
import com.submission.soilink.R
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.model.LoginRegistrationModel
import com.submission.soilink.data.pref.UserModel
import com.submission.soilink.databinding.ActivityLoginBinding
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.forgotpassword.ForgotPasswordActivity
import com.submission.soilink.view.home.HomeActivity
import com.submission.soilink.view.register.RegisterActivity

class LoginActivity : AppCompatActivity() {
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        val passwordField = binding.passwordEditText

        val emailLayout = binding.emailEditTextLayout
        val passwordLayout = binding.passwordEditTextLayout

        var email: String? = null
        var password: String? = null

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

        passwordField.apply {
            layout = passwordLayout
            minimumLength = 8
//            errorMessage = getString(R.string.error_password_too_short)
            errorMessage = "Kata sandi minimal 8 karakter"
            doOnTextChanged { text, _, _, _ ->
                password = if (text.toString()
                        .isEmpty() || text.toString().length < 8
                ) null else text.toString()
            }
        }

        binding.loginButton.setOnClickListener {
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.loginButton.windowToken, 0)

            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                val dataUser = LoginRegistrationModel(email = email.toString(), password = password.toString())

                viewModel.startLogin(dataUser).observe(this) { result ->
                    if (result != null) {
                        when (result) {
                            is ResultState.Loading -> {
                                showLoading(true)
                                window.setFlags(
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                )
                            }

                            is ResultState.Success -> {
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                showLoading(false)

                                val dataResult = result.data.user
                                viewModel.saveSession(
                                    UserModel(
                                        dataResult?.displayName.toString(),
                                        email.toString(),
                                        dataResult?.uid.toString(),
                                    )
                                )

                                Log.d("njirr", dataResult.toString())

                                AlertDialog.Builder(this).apply {
//                                    setTitle(getString(R.string.info_login_alert))
                                    setTitle("Selamat...")
//                                    setMessage(getString(R.string.login_message))
                                    setMessage("Login berhasil dilakukan dengan menggunakan email: $email")
                                    setCancelable(false)
                                    setPositiveButton(getString(R.string.login)) { _, _ ->
                                        val intentToHome = Intent(context, HomeActivity::class.java)
                                        intentToHome.flags =
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP and Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        intentToHome.putExtra(
                                            USER_NAME,
                                            dataResult?.displayName.toString()
                                        )
                                        startActivity(intentToHome)
                                        finish()
                                    }
                                    create()
                                    show()
                                }
                            }

                            is ResultState.Error -> {
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                if (result.error.contains("email")) {
                                    emailLayout.error = getString(R.string.email_pattern)
                                } else {
                                    showToast(this, result.error)
                                }
                                showLoading(false)
                            }
                        }
                    }
                }
            } else {
                if (emailField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_email_is_empty)
                    val errorTextIsEmpty = "Email tidak boleh kosong"
                    emailLayout.error = errorTextIsEmpty
                }
                if (passwordField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_password_is_empty)
                    val errorTextIsEmpty = "Password tidak boleh kosong"
                    passwordLayout.error = errorTextIsEmpty
                }
            }
        }

        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.signupPageButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
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
            ObjectAnimator.ofFloat(binding.welcomeLogin, View.ALPHA, 1f).setDuration(250)
        val loginPage =
            ObjectAnimator.ofFloat(binding.loginPage, View.ALPHA, 1f).setDuration(250)

        val emailEdit =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val passwordEdit =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val signup = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(250)

        val forgotPassword =
            ObjectAnimator.ofFloat(binding.forgotPassword, View.ALPHA, 1f).setDuration(250)
        val haveAccount =
            ObjectAnimator.ofFloat(binding.haveAccount, View.ALPHA, 1f).setDuration(250)

        val firstAnimation = AnimatorSet().apply {
            playSequentially(emailEdit, passwordEdit, signup)
            startDelay = 250
        }

        val secondAnimation = AnimatorSet().apply {
            playTogether(forgotPassword, haveAccount)
            startDelay = 1000
        }

        AnimatorSet().apply {
            playTogether(welcomeLogin, loginPage, firstAnimation, secondAnimation, ellipseAnimation)
            start()
        }
    }
}
