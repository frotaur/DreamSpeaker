package com.dreamspeaker.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object DreamUploader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    sealed class UploadResult {
        data object Success : UploadResult()
        data class Error(val message: String) : UploadResult()
    }

    suspend fun upload(serverUrl: String, apiKey: String, audioFile: File): UploadResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = serverUrl.trimEnd('/') + "/upload"

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "audio",
                        audioFile.name,
                        audioFile.asRequestBody("audio/mp4".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url(url)
                    .header("X-API-Key", apiKey)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                response.use {
                    if (it.isSuccessful) {
                        UploadResult.Success
                    } else {
                        UploadResult.Error("Server error: ${it.code}")
                    }
                }
            } catch (e: Exception) {
                UploadResult.Error(e.message ?: "Unknown error")
            }
        }
    }
}
