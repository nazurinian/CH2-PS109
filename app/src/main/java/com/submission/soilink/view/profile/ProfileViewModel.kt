package com.submission.soilink.view.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.soilink.data.SoilInkRepository
import kotlinx.coroutines.launch

class ProfileViewModel (private val repository: SoilInkRepository) : ViewModel() {
    var name = MutableLiveData<String>()

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}