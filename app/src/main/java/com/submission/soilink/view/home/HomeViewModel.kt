package com.submission.soilink.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.submission.soilink.data.SoilInkRepository
import com.submission.soilink.data.pref.UserModel
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(private val repository: SoilInkRepository) : ViewModel() {
    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun uploadPicture(picture: File) = repository.uploadImage(picture)
}