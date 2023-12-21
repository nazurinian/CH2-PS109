package com.submission.soilink.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.submission.soilink.data.SoilInkRepository
import com.submission.soilink.di.Injection
import com.submission.soilink.view.forgotpassword.ForgetPasswordModel
import com.submission.soilink.view.home.HomeViewModel
import com.submission.soilink.view.login.LoginViewModel
import com.submission.soilink.view.profile.ProfileViewModel
import com.submission.soilink.view.register.RegisterViewModel
import com.submission.soilink.view.soillist.SoilListViewModel

class ViewModelFactory(private val repository: SoilInkRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }

            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }

            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                RegisterViewModel(repository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }

            modelClass.isAssignableFrom(ForgetPasswordModel::class.java) -> {
                ForgetPasswordModel(repository) as T
            }

            modelClass.isAssignableFrom(SoilListViewModel::class.java) -> {
                SoilListViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    INSTANCE = ViewModelFactory(Injection.provideRepository(context))
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}