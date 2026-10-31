package com.arifa.assistant.core.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

class VadDetector(
    private var energyThreshold: Float = 0.02f
) {
    /**
     * Checks if the given 16-bit PCM byte array has active speech
     */
    fun isSpeech(pcmBytes: ByteArray): Boolean {
        if (pcmBytes.isEmpty()) return false

        val sampleCount = pcmBytes.size / 2
        val buffer = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)

        var sumSquare = 0.0
        var zeroCrossings = 0
        var prevSample: Short = 0

        for (i in 0 until sampleCount) {
            val sample = buffer.short
            sumSquare += (sample.toDouble() * sample.toDouble())

            if (i > 0) {
                if ((sample >= 0 && prevSample < 0) || (sample < 0 && prevSample >= 0)) {
                    zeroCrossings++
                }
            }
            prevSample = sample
        }

        val rms = Math.sqrt(sumSquare / sampleCount) / 32768.0
        val zcr = zeroCrossings.toDouble() / sampleCount

        // Human speech typically has RMS above ambient threshold and ZCR between 0.02 and 0.45
        return (rms > energyThreshold) && (zcr in 0.015..0.48)
    }

    fun setThreshold(threshold: Float) {
        this.energyThreshold = threshold.coerceIn(0.005f, 0.2f)
    }
}
