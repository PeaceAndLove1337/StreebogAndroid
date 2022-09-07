package com.mpei.vkr.streebog_hashing.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mpei.vkr.streebog_hashing.domain.hashing.StreebogHasher

class MainViewModel : ViewModel() {

    private val hashResultLiveData = MutableLiveData<String>()

    fun hash256InThread(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val hasher = StreebogHasher(byteArray)
            val result = hasher.generate256Hash(progressHelper)
            hashResultLiveData.postValue(result.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun hash512InThread(byteArray: ByteArray, progressHelper: ProgressHelper) {
        Thread {
            val hasher = StreebogHasher(byteArray)
            val result = hasher.generate512Hash(progressHelper)
            hashResultLiveData.postValue(result.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) })
        }.start()
    }

    fun getHashLiveData(): LiveData<String> = hashResultLiveData
}