package com.submission.soilink.api

import com.submission.soilink.api.response.ErrorResponse
import com.submission.soilink.api.response.LoginResponse
import com.submission.soilink.api.response.SoilListItems
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @FormUrlEncoded
    @POST("signup")
    suspend fun signup(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("confirmPassword") confirmPassword: String
    ): ErrorResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("profile")
    suspend fun profile(
        @Field("email") email: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("forgot-password")
    suspend fun resetPassword(
        @Field("email") email: String
    ): ErrorResponse

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
    ): ErrorResponse

    @GET("jenis-tanah")
    suspend fun getSoilList(): List<SoilListItems>

    @Multipart
    @POST("add-to-history")
    suspend fun addHistory(
        @Part("email") email: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("soil_type") soilType: RequestBody? = null,
        @Part("description") description: RequestBody? = null,
        @Part("note") note: RequestBody? = null,
        @Part("date_time") dateTime: RequestBody? = null,
        @Part("lat") lat: Double? = null,
        @Part("long") long: Double? = null
    ): ErrorResponse
}