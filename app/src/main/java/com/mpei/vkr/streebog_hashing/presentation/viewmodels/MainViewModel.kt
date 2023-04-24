package com.mpei.vkr.streebog_hashing.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mpei.vkr.streebog_hashing.domain.hashing.nonOptimized.mainAndroidGetHash256
import com.mpei.vkr.streebog_hashing.domain.hashing.nonOptimized.mainAndroidGetHash512
import com.mpei.vkr.streebog_hashing.domain.hashing.optimized.StreebogHasher
import com.mpei.vkr.streebog_hashing.presentation.ProgressHelper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class MainViewModel : ViewModel() {

    private val hashResultLiveData = MutableLiveData<String>()

    fun hash256NonOptimized(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val currHash = mainAndroidGetHash256(
                byteArray.toUByteArray().toTypedArray(),
                progressHelper
            ).toUByteArray().toByteArray()

            hashResultLiveData.postValue(
                currHash.toUByteArray().toByteArray()
                    .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun hash512NonOptimized(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val currHash = mainAndroidGetHash512(
                byteArray.toUByteArray().toTypedArray(),
                progressHelper
            )
            hashResultLiveData.postValue(
                currHash.toUByteArray().toByteArray()
                    .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun hash256Optimized(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val hasher = StreebogHasher(byteArray)
            val result = hasher.generate256Hash(progressHelper)
            hashResultLiveData.postValue(result.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun hash512Optimized(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val hasher =
                StreebogHasher(byteArray)
            val result = hasher.generate512Hash(progressHelper)
            hashResultLiveData.postValue(result.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun hash256DefaultByBackend(byteArray: ByteArray) {
        Thread {
            val client = OkHttpClient()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "file_name",
                        byteArray.toRequestBody(
                            "multipart/form-data".toMediaTypeOrNull()
                        )
                    )
                    .build()
                val request = Request.Builder()
                    .url(STREEBOG_DEFAULT_256_URL)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val res = response.body?.bytes()
                hashResultLiveData.postValue(res?.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
            } catch (e: Exception) {
                println("ERROR CONNECTING TO")
            }
        }.start()
    }

    fun hash512DefaultByBackend(byteArray: ByteArray) {
        Thread {
            val client = OkHttpClient()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "file_name",
                        byteArray.toRequestBody(
                            "multipart/form-data".toMediaTypeOrNull()
                        )
                    ).build()

                val request = Request.Builder()
                    .url(STREEBOG_DEFAULT_512_URL)
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val res = response.body?.bytes()
                hashResultLiveData.postValue(res?.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
            } catch (e: Exception) {
                println("ERROR CONNECTING TO")
            }
        }.start()
    }

    fun hash256SteganographyByBackend(byteArray: ByteArray) {
    }

    fun hash512SteganographyByBackend(byteArray: ByteArray) {
    }

    fun getHashLiveData(): LiveData<String> = hashResultLiveData

    private companion object {
        const val LOCAL_ADDRESS_URL = "http://192.168.0.110:8082/"
        const val STREEBOG_ROUTE = "streebog"

        const val STREEBOG_DEFAULT_256_URL = "$LOCAL_ADDRESS_URL$STREEBOG_ROUTE?length=256&mode=default"
        const val STREEBOG_DEFAULT_512_URL = "$LOCAL_ADDRESS_URL$STREEBOG_ROUTE?length=512&mode=default"
        const val STREEBOG_STEGANOGRAPHY_256_URL = "$LOCAL_ADDRESS_URL$STREEBOG_ROUTE?length=256&mode=steganography"
        const val STREEBOG_STEGANOGRAPHY_512_URL = "$LOCAL_ADDRESS_URL$STREEBOG_ROUTE?length=512&mode=steganography"
    }
}