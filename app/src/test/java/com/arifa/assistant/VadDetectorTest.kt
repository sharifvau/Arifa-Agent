package com.arifa.assistant

import com.arifa.assistant.core.audio.VadDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class VadDetectorTest {

    @Test
    fun testSilenceDetection() {
        val vad = VadDetector(energyThreshold = 0.02f)
        val silencePcm = ByteArray(3200) // All zeros
        val isSpeech = vad.isSpeech(silencePcm)
        assertFalse(isSpeech)
    }

    @Test
    fun testSpeechSignalDetection() {
        val vad = VadDetector(energyThreshold = 0.02f)
        val pcm = ByteArray(3200)
        val buffer = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)

        // Generate synthetic speech-like oscillating waveform (200Hz sine wave at 16kHz)
        val sampleCount = 1600
        for (i in 0 until sampleCount) {
            val angle = 2.0 * Math.PI * 200.0 * (i.toDouble() / 16000.0)
            val sample = (Math.sin(angle) * 16000.0).toInt().toShort()
            buffer.putShort(sample)
        }

        val isSpeech = vad.isSpeech(pcm)
        assertTrue(isSpeech)
    }
}
