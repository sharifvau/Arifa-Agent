package com.arifa.assistant.core.audio

import com.arifa.assistant.core.common.calculateRmsLevel
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean

class EchoGuard {

    private val isSpeakerPlaying = AtomicBoolean(false)
    private var echoSuppressionThreshold = 0.08f
    private var bargeInThreshold = 0.16f

    fun notifySpeakerState(isPlaying: Boolean) {
        isSpeakerPlaying.set(isPlaying)
    }

    data class FilterResult(
        val filteredBytes: ByteArray,
        val isBargeInDetected: Boolean,
        val rmsLevel: Float
    )

    /**
     * Filters mic PCM to cancel acoustic feedback when speaker is active.
     */
    fun processMicrophoneChunk(rawPcm: ByteArray): FilterResult {
        val rms = rawPcm.calculateRmsLevel()
        val playing = isSpeakerPlaying.get()

        if (!playing) {
            // Speaker is silent: pass mic audio untouched
            return FilterResult(
                filteredBytes = rawPcm,
                isBargeInDetected = false,
                rmsLevel = rms
            )
        }

        // Speaker is actively outputting audio
        if (rms >= bargeInThreshold) {
            // User is speaking loudly over the assistant (Barge-in!)
            return FilterResult(
                filteredBytes = rawPcm,
                isBargeInDetected = true,
                rmsLevel = rms
            )
        }

        // Speaker is playing and energy is below barge-in threshold:
        // Attenuate to avoid echo loopback to Gemini Live API
        val buffer = ByteBuffer.wrap(rawPcm).order(ByteOrder.LITTLE_ENDIAN)
        val sampleCount = rawPcm.size / 2
        val attenuated = ByteArray(rawPcm.size)
        val outBuffer = ByteBuffer.wrap(attenuated).order(ByteOrder.LITTLE_ENDIAN)

        val attenuationFactor = if (rms < echoSuppressionThreshold) 0.0f else 0.2f
        for (i in 0 until sampleCount) {
            val sample = buffer.short
            val cleanSample = (sample * attenuationFactor).toInt().coerceIn(-32768, 32767).toShort()
            outBuffer.putShort(cleanSample)
        }

        return FilterResult(
            filteredBytes = attenuated,
            isBargeInDetected = false,
            rmsLevel = rms * attenuationFactor
        )
    }
}
