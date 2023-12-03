package com.submission.soilink.data.pref

data class UserModel(
    val name: String,
    val email: String,
//    val token: String,
    val isLogin: Boolean = false
)