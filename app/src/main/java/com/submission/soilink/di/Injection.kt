package com.submission.soilink.di

import android.content.Context
import com.submission.soilink.api.ApiConfig
import com.submission.soilink.data.SoilInkRepository
import com.submission.soilink.data.pref.UserPreference
import com.submission.soilink.data.pref.dataStore

object Injection {
    fun provideRepository(context: Context): SoilInkRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return SoilInkRepository.getInstance(pref, apiService)
    }
}