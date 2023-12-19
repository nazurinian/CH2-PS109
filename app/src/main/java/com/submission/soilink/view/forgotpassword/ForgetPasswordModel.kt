package com.submission.soilink.view.forgotpassword

import androidx.lifecycle.ViewModel
import com.submission.soilink.data.SoilInkRepository

class ForgetPasswordModel(private val repository: SoilInkRepository) : ViewModel() {
    fun sendLinkResetPassword(email: String) = repository.resetPassword(email)
}