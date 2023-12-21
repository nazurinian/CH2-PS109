package com.submission.soilink.view.soillist

import androidx.lifecycle.ViewModel
import com.submission.soilink.data.SoilInkRepository

class SoilListViewModel(private val repository: SoilInkRepository) : ViewModel() {
    fun getSoilList() = repository.getSoilList()
}