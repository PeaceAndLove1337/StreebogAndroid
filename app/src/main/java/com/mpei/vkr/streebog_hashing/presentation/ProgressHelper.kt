package com.mpei.vkr.streebog_hashing.presentation

import android.os.Handler
import android.widget.ProgressBar

class ProgressHelper(
    private val progressBar: ProgressBar,
    private val handler: Handler
) {
    fun setProgressToBarInAnotherThread(progress:Int){
        handler.post{
            progressBar.progress = progress
        }
    }
}