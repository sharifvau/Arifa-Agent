package com.arifa.assistant.core.common

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Base64 encode byte array safely without newlines
 */
fun ByteArray.toBase64(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

/**
 * Base64 decode to byte array
 */
fun String.fromBase64(): ByteArray {
    return Base64.decode(this, Base64.DEFAULT)
}

/**
 * Convert 16-bit PCM ByteArray into FloatArray normalized to [-1.0f, 1.0f]
 */
fun ByteArray.pcm16ToFloats(): FloatArray {
    val shortsCount = this.size / 2
    val floats = FloatArray(shortsCount)
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    for (i in 0 until shortsCount) {
        val sample = buffer.short
        floats[i] = sample / 32768.0f
    }
    return floats
}

/**
 * Calculate Root-Mean-Square (RMS) amplitude from 16-bit PCM bytes
 * Returns normalized value roughly between 0.0f and 1.0f
 */
fun ByteArray.calculateRmsLevel(): Float {
    if (this.isEmpty()) return 0.0f
    var sum = 0.0
    val buffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)
    val count = this.size / 2
    for (i in 0 until count) {
        val sample = buffer.short.toDouble()
        sum += sample * sample
    }
    val mean = sum / count
    val rms = Math.sqrt(mean)
    // Normalize against max short value (32767)
    return (rms / 32768.0).toFloat().coerceIn(0.0f, 1.0f)
}
