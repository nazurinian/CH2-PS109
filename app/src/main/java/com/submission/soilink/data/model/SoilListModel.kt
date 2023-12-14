package com.submission.soilink.data.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoilListModel (
    val id: Int,
    @StringRes val soilTitle: Int,
    @DrawableRes val soilImage: Int,
    val description: String
):Parcelable