package com.submission.soilink.api.response

import com.google.gson.annotations.SerializedName

data class ErrorResponse(

    @field:SerializedName("success")
    val success: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("error")
    val error: String? = null,
)