package com.mpei.vkr.streebog_hashing

fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }