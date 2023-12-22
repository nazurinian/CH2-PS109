package com.submission.soilink.data

import androidx.lifecycle.liveData
import com.submission.soilink.api.ApiService
import com.submission.soilink.api.response.ErrorResponse
import com.submission.soilink.data.model.LoginRegistrationModel
import com.submission.soilink.data.pref.UserModel
import com.submission.soilink.data.pref.UserPreference
import com.google.gson.Gson
import com.submission.soilink.data.model.PostHistoryModel
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class SoilInkRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun register(registerData: LoginRegistrationModel) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.signup(
                registerData.name!!,
                registerData.email,
                registerData.password,
                registerData.confirmPassword!!
            )
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun login(loginData: LoginRegistrationModel) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.login(
                loginData.email,
                loginData.password
            )
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun profile(email: String) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.profile(email)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun resetPassword(email: String) = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.resetPassword(email)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun uploadImage(picture: File) = liveData {
        emit(ResultState.Loading)
        val requestStoryImageFile = picture.asRequestBody("image/*".toMediaType())

        val multipartBody = MultipartBody.Part.createFormData(
            "file",
            picture.name,
            requestStoryImageFile
        )
        try {
            val successResponse = apiService.uploadImage(
                multipartBody
            )
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun getSoilList() = liveData {
        emit(ResultState.Loading)
        try {
            val successResponse = apiService.getSoilList()
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    fun addHistory(postHistoryModel: PostHistoryModel) = liveData {
        emit(ResultState.Loading)
        val email = postHistoryModel.email.toRequestBody("text/plain".toMediaType())
        val image = postHistoryModel.image.asRequestBody("image/*".toMediaType())
        val soilType = postHistoryModel.soilType?.toRequestBody("text/plain".toMediaType())
        val description = postHistoryModel.description?.toRequestBody("text/plain".toMediaType())
        val note = postHistoryModel.note?.toRequestBody("text/plain".toMediaType())
        val dateTime = postHistoryModel.dateTime?.toRequestBody("text/plain".toMediaType())
        val lat = postHistoryModel.lat
        val lon = postHistoryModel.long

        val multipartBody = MultipartBody.Part.createFormData(
            "file",
            postHistoryModel.image.name,
            image
        )
        try {
            val successResponse = apiService.addHistory(
                email,
                multipartBody,
                soilType,
                description,
                note,
                dateTime,
                lat,
                lon
            )
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }


    companion object {
        @Volatile
        private var instance: SoilInkRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ) =
            instance ?: synchronized(this) {
                instance ?: SoilInkRepository(userPreference, apiService)
            }.also { instance = it }
    }
}