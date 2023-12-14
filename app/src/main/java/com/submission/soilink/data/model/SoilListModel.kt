package com.submission.soilink.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class SoilListModel (
    @StringRes val soilTitle: Int,
    @DrawableRes val soilImage: Int,
)