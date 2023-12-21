package com.submission.soilink.api.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SoilListItems(

	@field:SerializedName("nama")
	val nama: String,

	@field:SerializedName("deskripsi")
	val deskripsi: String,

	@field:SerializedName("gambar")
	val gambar: String
): Parcelable
