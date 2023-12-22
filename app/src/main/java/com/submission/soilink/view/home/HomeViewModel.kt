package com.submission.soilink.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.submission.soilink.data.SoilInkRepository
import com.submission.soilink.data.model.PostHistoryModel
import com.submission.soilink.data.pref.UserModel
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel(private val repository: SoilInkRepository) : ViewModel() {

    private val _locationUpdated = MutableLiveData<Boolean>()
    val locationUpdated: LiveData<Boolean> = _locationUpdated

    init {
        _locationUpdated.value = false
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun uploadPicture(picture: File) = repository.uploadImage(picture)

    fun addHistory(postHistoryModel: PostHistoryModel) = repository.addHistory(postHistoryModel)

    fun setLocation(location: Boolean) {
        _locationUpdated.value = location
    }
}