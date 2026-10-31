package com.arifa.assistant.core.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import com.arifa.assistant.core.common.calculateRmsLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class AudioPlayer(
    private val scope: CoroutineScope,
    private val echoGuard: EchoGuard
) {
    private var audioTrack: AudioTrack? = null
    private val audioQueue = ConcurrentLinkedQueue<ByteArray>()
    private val isPlaying = AtomicBoolean(false)
    private var playbackJob: Job? = null

    private val _speakerLevel = MutableStateFlow(0.0f)
    val speakerLevel: StateFlow<Float> = _speakerLevel.asStateFlow()

    private val _isAudioActive = MutableStateFlow(false)
    val isAudioActive: StateFlow<Boolean> = _isAudioActive.asStateFlow()

    fun start() {
        if (audioTrack != null) return

        val minBufferSize = AudioTrack.getMinBufferSize(
            AudioConfig.SAMPLE_RATE_OUT,
            AudioConfig.CHANNEL_CONFIG_OUT,
            AudioConfig.AUDIO_FORMAT
        )
        val bufferSize = Math.max(minBufferSize, 8192)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(AudioConfig.SAMPLE_RATE_OUT)
            .setChannelMask(AudioConfig.CHANNEL_CONFIG_OUT)
            .setEncoding(AudioConfig.AUDIO_FORMAT)
            .build()

        audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack?.play()
        isPlaying.set(true)

        playbackJob = scope.launch(Dispatchers.IO) {
            while (isActive && isPlaying.get()) {
                val chunk = audioQueue.poll()
                if (chunk != null && chunk.isNotEmpty()) {
                    _isAudioActive.value = true
                    echoGuard.notifySpeakerState(true)
                    _speakerLevel.value = chunk.calculateRmsLevel()

                    audioTrack?.write(chunk, 0, chunk.size)
                } else {
                    _isAudioActive.value = false
                    echoGuard.notifySpeakerState(false)
                    _speakerLevel.value = 0.0f
                    Thread.sleep(10) // Idle wait
                }
            }
        }
    }

    fun enqueueAudioChunk(pcmChunk: ByteArray) {
        if (pcmChunk.isNotEmpty()) {
            audioQueue.offer(pcmChunk)
        }
    }

    /**
     * Instantly stops playback and empties queued audio buffers.
     * Essential for low-latency barge-in support.
     */
    fun flushAndStop() {
        audioQueue.clear()
        _isAudioActive.value = false
        _speakerLevel.value = 0.0f
        echoGuard.notifySpeakerState(false)
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.play()
        } catch (e: Exception) {
            // Ignore track state reset errors
        }
    }

    fun release() {
        isPlaying.set(false)
        playbackJob?.cancel()
        playbackJob = null
        audioQueue.clear()
        echoGuard.notifySpeakerState(false)

        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignore release exception
        }
        audioTrack = null
        _isAudioActive.value = false
        _speakerLevel.value = 0.0f
    }
}
