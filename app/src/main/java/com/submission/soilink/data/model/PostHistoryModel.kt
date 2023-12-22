package com.submission.soilink.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class PostHistoryModel (
    val email: String,
    val image: File,
    val soilType: String? = null,
    val description: String? = null,
    val note: String? = null,
    val dateTime: String? = null,
    val lat: Double? = null,
    val long: Double? = null
): Parcelable