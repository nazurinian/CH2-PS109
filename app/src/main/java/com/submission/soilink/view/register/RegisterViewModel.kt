package com.submission.soilink.view.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.submission.soilink.data.model.LoginRegistrationModel
import com.submission.soilink.data.SoilInkRepository
import com.submission.soilink.data.pref.UserModel
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: SoilInkRepository) : ViewModel() {
    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun startRegistration(register: LoginRegistrationModel) = repository.register(register)

    fun startLogin(login: LoginRegistrationModel) = repository.login(login)
}