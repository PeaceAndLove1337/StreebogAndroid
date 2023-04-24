package com.mpei.vkr.streebog_hashing.domain.hashing.nonOptimized

import StreebogAdditionalFunctions.Functions.addTwoULongArraysMod512
import StreebogAdditionalFunctions.Functions.convertFromUByteArrayToULong
import StreebogAdditionalFunctions.Functions.convertFromULongToUByteArray
import StreebogHashFunction.Hashing.compressionFunction
import StreebogHashFunction.Hashing.initControlSum
import StreebogHashFunction.Hashing.initLengthOfControlSum
import StreebogHashFunction.Hashing.initVector256
import StreebogHashFunction.Hashing.initVector512
import StreebogHashFunction.Hashing.supplementBlock
import android.widget.ProgressBar
import com.mpei.vkr.streebog_hashing.presentation.ProgressHelper

//region Хэш Функция
private fun mainAndroidGetHash(inputArray: Array<UByte>, initVectorH: Array<ULong>, progressHelper:ProgressHelper): Array<UByte> {
    var message = inputArray
    var h = initVectorH
    var nLength = initLengthOfControlSum
    var sigmaSum = initControlSum
    lateinit var currentBlock: Array<ULong>

    val currentProgress= inputArray.size/64
    var countOfReadyBlocks=0
    var progress = 0.0


    while (message.size > 64) {
        val currentSize = message.size
        currentBlock = Array<ULong>(0) { 0U }
        val inUByteCurrentBlock = message.sliceArray(currentSize - 64 until currentSize)

        //Преобразование из 64 UByte в 8 ULong
        for (i in inUByteCurrentBlock.indices step 8) {
            val currentUnderBlock = convertFromUByteArrayToULong(inUByteCurrentBlock.sliceArray(i..i + 7))
            currentBlock = currentBlock.plus(currentUnderBlock)
        }

        h = compressionFunction(h, currentBlock, nLength)

        message = message.sliceArray(0..currentSize - 65)
        nLength = addTwoULongArraysMod512(
            nLength,
            arrayOf(0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x200U)
        )

        sigmaSum = addTwoULongArraysMod512(sigmaSum, currentBlock)

        countOfReadyBlocks+=1
        progress=(countOfReadyBlocks.toDouble()/currentProgress)*100
        progressHelper.setProgressToBarInAnotherThread(progress.toInt())
        // блок если сообщение больще 512 бит
    }
    if (message.isNotEmpty()) {
        val currentLength = (message.size * 8).toULong()
        message = supplementBlock(message)
        currentBlock = Array<ULong>(0) { 0U }
        //Преобразование из 64 UByte в 8 ULong
        for (i in message.indices step 8) {
            val currentUnderBlock = convertFromUByteArrayToULong(message.sliceArray(i..i + 7))
            currentBlock = currentBlock.plus(currentUnderBlock)
        }

        h = compressionFunction(h, currentBlock, nLength)

        val newLength = arrayOf<ULong>(0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, 0x0U, currentLength)

        nLength = addTwoULongArraysMod512(
            nLength,
            newLength
        )

        sigmaSum = addTwoULongArraysMod512(sigmaSum, currentBlock)

        h = compressionFunction(h, nLength, initVector512)

        h = compressionFunction(h, sigmaSum, initVector512)

        // обработка недополненного блока
        progressHelper.setProgressToBarInAnotherThread(99)

    }

    var result = Array<UByte>(0) { 0U }

    h.forEach {
        result = result.plus(convertFromULongToUByteArray(it))
    }


    return result
}



//Принимает UByte array, они конвертятся и все отправляется в getCurrentHash
fun mainAndroidGetHash512(inputArray: Array<UByte>, progressHelper: ProgressHelper): Array<UByte> {
    return mainAndroidGetHash(inputArray, initVector512, progressHelper)
}



fun mainAndroidGetHash256(inputArray: Array<UByte>, progressHelper: ProgressHelper): Array<UByte> {
    val hash = mainAndroidGetHash(inputArray, initVector256, progressHelper)
    return hash.sliceArray(0 until 32)
}