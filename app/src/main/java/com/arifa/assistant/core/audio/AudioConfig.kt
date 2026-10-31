package com.arifa.assistant.core.audio

import android.media.AudioFormat
import android.media.MediaRecorder
import com.arifa.assistant.core.common.Constants

object AudioConfig {
    const val AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION
    const val SAMPLE_RATE_IN = Constants.SAMPLE_RATE_INPUT_HZ // 16000
    const val SAMPLE_RATE_OUT = Constants.SAMPLE_RATE_OUTPUT_HZ // 24000
    const val CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO
    const val CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO
    const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

    // 100ms at 16kHz 16-bit mono: 16000 * 0.1 = 1600 samples = 3200 bytes
    const val CHUNK_SIZE_BYTES = 3200
}
