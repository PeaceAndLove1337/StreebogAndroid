package com.mpei.vkr.streebog_hashing.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mpei.vkr.streebog_hashing.R
import com.mpei.vkr.streebog_hashing.data.MyTrustManager
import com.mpei.vkr.streebog_hashing.domain.hashing.nonOptimized.mainAndroidGetHash256
import com.mpei.vkr.streebog_hashing.domain.hashing.nonOptimized.mainAndroidGetHash512
import com.mpei.vkr.streebog_hashing.domain.hashing.optimized.StreebogHasher
import com.mpei.vkr.streebog_hashing.presentation.ProgressHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class MainViewModel : ViewModel() {

    private val hashResultLiveData = MutableLiveData<String>()

    @ExperimentalUnsignedTypes
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

    @ExperimentalUnsignedTypes
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
                            MULTIPART_FORM_DATA_PART.toMediaTypeOrNull()
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
                hashResultLiveData.postValue(null)
                println("hash256DefaultByBackend ERROR CONNECTING TO")
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
                            MULTIPART_FORM_DATA_PART.toMediaTypeOrNull()
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
                println("hash512DefaultByBackend ERROR CONNECTING TO")
            }
        }.start()
    }

    fun hash256ByBackendSsl(context: Context, byteArray: ByteArray) {
        Thread {
            val keyStore = KeyStore.getInstance("BKS")
            val inputStream: InputStream = context.resources.openRawResource(R.raw.keystore)
            keyStore.load(inputStream, KEY_STORE_PASSWORD.toCharArray())
            val trustManager = MyTrustManager(keyStore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            val client: OkHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager)
                .callTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .build()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "file_name",
                        byteArray.toRequestBody(
                            MULTIPART_FORM_DATA_PART.toMediaTypeOrNull()
                        )
                    ).build()
                val request = Request.Builder()
                    .url(STREEBOG_DEFAULT_SSL_256_URL)
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val res = response.body?.bytes()
                hashResultLiveData.postValue(res?.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
            } catch (e: Exception) {
                hashResultLiveData.postValue(null)
                println("hash256ByBackendSsl ERROR CONNECTING TO")
            }
        }.start()
    }

    fun hash512ByBackendSsl(context: Context, byteArray: ByteArray) {
        Thread {
            val keyStore = KeyStore.getInstance("BKS")
            val inputStream: InputStream = context.resources.openRawResource(R.raw.keystore)
            keyStore.load(inputStream, KEY_STORE_PASSWORD.toCharArray())
            val trustManager = MyTrustManager(keyStore)
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, arrayOf(trustManager), SecureRandom())
            val client: OkHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustManager as X509TrustManager)
                .callTimeout(600, TimeUnit.SECONDS)
                .readTimeout(600, TimeUnit.SECONDS)
                .writeTimeout(600, TimeUnit.SECONDS)
                .hostnameVerifier { _, _ -> true }
                .build()
            try {
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file", "file_name",
                        byteArray.toRequestBody(
                            MULTIPART_FORM_DATA_PART.toMediaTypeOrNull()
                        )
                    ).build()
                val request = Request.Builder()
                    .url(STREEBOG_DEFAULT_SSL_512_URL)
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val res = response.body?.bytes()
                hashResultLiveData.postValue(res?.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
            } catch (e: Exception) {
                hashResultLiveData.postValue(null)
                println("hash512ByBackendSsl ERROR CONNECTING TO")
            }
        }.start()
    }

    fun getHashLiveData(): LiveData<String> = hashResultLiveData

    private companion object {
        const val LOCAL_ADDRESS_URL_NO_SSL = "http://192.168.1.125:8082/"
        const val LOCAL_ADDRESS_URL_SSL = "https://192.168.1.125:8443/"
        const val STREEBOG_ROUTE = "streebog"

        const val STREEBOG_DEFAULT_256_URL = "$LOCAL_ADDRESS_URL_NO_SSL$STREEBOG_ROUTE?length=256&mode=default"
        const val STREEBOG_DEFAULT_512_URL = "$LOCAL_ADDRESS_URL_NO_SSL$STREEBOG_ROUTE?length=512&mode=default"

        const val STREEBOG_DEFAULT_SSL_256_URL = "$LOCAL_ADDRESS_URL_SSL$STREEBOG_ROUTE?length=256&mode=default"
        const val STREEBOG_DEFAULT_SSL_512_URL = "$LOCAL_ADDRESS_URL_SSL$STREEBOG_ROUTE?length=512&mode=default"

        const val MULTIPART_FORM_DATA_PART = "multipart/form-data"
        const val KEY_STORE_PASSWORD = "passwordKeyStore123456"
    }
}
