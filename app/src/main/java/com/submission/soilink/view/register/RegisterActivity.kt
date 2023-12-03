package com.submission.soilink.view.register

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.submission.soilink.R
import com.submission.soilink.data.model.LoginRegistrationModel
import com.submission.soilink.data.ResultState
import com.submission.soilink.data.pref.UserModel
import com.submission.soilink.databinding.ActivityRegisterBinding
import com.submission.soilink.util.showToast
import com.submission.soilink.view.ViewModelFactory
import com.submission.soilink.view.home.HomeActivity
import com.submission.soilink.view.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private val viewModel by viewModels<RegisterViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
        val nameField = binding.nameEditText
        val emailField = binding.emailEditText
        val passwordField = binding.passwordEditText

        val nameLayout = binding.nameEditTextLayout
        val emailLayout = binding.emailEditTextLayout
        val passwordLayout = binding.passwordEditTextLayout

        var name: String? = null
        var email: String? = null
        var password: String? = null

        nameField.apply {
            layout = nameLayout
            minimumLength = 3
//            errorMessage = getString(R.string.error_name_too_short)
            errorMessage = "Nama minimal 3 karakter"
            doOnTextChanged { text, _, _, _ ->
                name = if (text.toString()
                        .isEmpty() || text.toString().length < 3
                ) null else text.toString()
            }
        }

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

        binding.signupButton.setOnClickListener {
            if (!name.isNullOrEmpty() && !email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                val dataUser =
                    LoginRegistrationModel(name.toString(), email.toString(), password.toString())
                viewModel.startRegistration(dataUser).observe(this) { result ->
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

                                                val dataResult = result.data.loginResult
                                                viewModel.saveSession(
                                                    UserModel(
                                                        dataResult?.name.toString(),
                                                        email.toString(),
//                                                        dataResult?.token.toString()
                                                    )
                                                )

                                                AlertDialog.Builder(this).apply {
//                                                    setTitle(getString(R.string.info_register_alert))
                                                    setTitle("Selamat...")
//                                                    setMessage(getString(R.string.register_message, email))
                                                    setMessage("Akun dengan email: $email berhasil dibuat")
                                                    setCancelable(false)
                                                    setPositiveButton(getString(R.string.login)) { _, _ ->
                                                        val intentToHome = Intent(context, HomeActivity::class.java)
                                                        intentToHome.flags =
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                                        intentToHome.putExtra(HomeActivity.USER_NAME, name)
                                                        startActivity(intentToHome)
                                                        finish()
                                                    }
                                                    create()
                                                    show()
                                                }
                                            }

                                            is ResultState.Error -> {
                                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                                showToast(this, result.error)
                                                showLoading(false)
                                            }
                                        }
                                    }
                                }
                            }

                            is ResultState.Error -> {
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                if (result.error.contains("email")) {
//                                    emailLayout.error = getString(R.string.email_pattern)
                                    emailLayout.error = "Isi dengan email"
                                } else {
                                    showToast(this, result.error)
                                }
                                showLoading(false)
                            }
                        }
                    }
                }
            } else {
                if (nameField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_name_is_empty)
                    val errorTextIsEmpty = "Tidak boleh kosong"
                    nameLayout.error = errorTextIsEmpty
                }
                if (emailField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_email_is_empty)
                    val errorTextIsEmpty = "Tidak boleh kosong"
                    emailLayout.error = errorTextIsEmpty
                }
                if (passwordField.text.toString().isEmpty()) {
//                    val errorTextIsEmpty = getString(R.string.error_password_is_empty)
                    val errorTextIsEmpty = "Tidak boleh kosong"
                    passwordLayout.error = errorTextIsEmpty
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

        val helloSign =
            ObjectAnimator.ofFloat(binding.helloSign, View.ALPHA, 1f).setDuration(250)
        val signupPage =
            ObjectAnimator.ofFloat(binding.signupPage, View.ALPHA, 1f).setDuration(250)

        val nameEdit =
            ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val emailEdit =
            ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val passwordEdit =
            ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(250)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(250)

        val haveAccount =
            ObjectAnimator.ofFloat(binding.haveAccount, View.ALPHA, 1f).setDuration(250)

        val firstAnimation = AnimatorSet().apply {
            playSequentially(nameEdit, emailEdit, passwordEdit, signup)
            startDelay = 250
        }

        val secondAnimation = AnimatorSet().apply {
            play(haveAccount)
            startDelay = 1250
        }

        AnimatorSet().apply {
            playTogether(helloSign, signupPage, firstAnimation, secondAnimation, ellipseAnimation)

            startDelay = 250
            start()
        }
    }
}