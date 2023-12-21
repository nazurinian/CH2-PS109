package com.submission.soilink.view.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.soilink.data.SoilInkRepository
import kotlinx.coroutines.launch

class ProfileViewModel (private val repository: SoilInkRepository) : ViewModel() {

    fun getUserProfile(email: String) = repository.profile(email)

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}