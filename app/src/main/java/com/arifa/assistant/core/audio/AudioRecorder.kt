package com.arifa.assistant.core.audio

import android.annotation.SuppressLint
import android.media.AudioRecord
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.AutomaticGainControl
import android.media.audiofx.NoiseSuppressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class AudioRecorder(
    private val scope: CoroutineScope,
    private val echoGuard: EchoGuard
) {
    private var audioRecord: AudioRecord? = null
    private var aec: AcousticEchoCanceler? = null
    private var ns: NoiseSuppressor? = null
    private var agc: AutomaticGainControl? = null

    private val isRecording = AtomicBoolean(false)
    private var recordingJob: Job? = null

    private val vadDetector = VadDetector()

    private val _audioChunks = MutableSharedFlow<ByteArray>(replay = 0, extraBufferCapacity = 32)
    val audioChunks: SharedFlow<ByteArray> = _audioChunks.asSharedFlow()

    private val _micLevel = MutableStateFlow(0.0f)
    val micLevel: StateFlow<Float> = _micLevel.asStateFlow()

    private val _isUserSpeaking = MutableStateFlow(false)
    val isUserSpeaking: StateFlow<Boolean> = _isUserSpeaking.asStateFlow()

    private val _bargeInTriggered = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 8)
    val bargeInTriggered: SharedFlow<Unit> = _bargeInTriggered.asSharedFlow()

    @SuppressLint("MissingPermission")
    fun startRecording() {
        if (isRecording.get()) return

        val minBufferSize = AudioRecord.getMinBufferSize(
            AudioConfig.SAMPLE_RATE_IN,
            AudioConfig.CHANNEL_CONFIG_IN,
            AudioConfig.AUDIO_FORMAT
        )
        val bufferSize = Math.max(minBufferSize, AudioConfig.CHUNK_SIZE_BYTES * 2)

        try {
            audioRecord = AudioRecord(
                AudioConfig.AUDIO_SOURCE,
                AudioConfig.SAMPLE_RATE_IN,
                AudioConfig.CHANNEL_CONFIG_IN,
                AudioConfig.AUDIO_FORMAT,
                bufferSize
            )

            // Enable hardware audio effects if available on device
            val audioSessionId = audioRecord?.audioSessionId ?: 0
            if (audioSessionId != 0) {
                if (AcousticEchoCanceler.isAvailable()) {
                    aec = AcousticEchoCanceler.create(audioSessionId)?.apply { enabled = true }
                }
                if (NoiseSuppressor.isAvailable()) {
                    ns = NoiseSuppressor.create(audioSessionId)?.apply { enabled = true }
                }
                if (AutomaticGainControl.isAvailable()) {
                    agc = AutomaticGainControl.create(audioSessionId)?.apply { enabled = true }
                }
            }

            audioRecord?.startRecording()
            isRecording.set(true)

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(AudioConfig.CHUNK_SIZE_BYTES)
                while (isActive && isRecording.get()) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    if (readBytes > 0) {
                        val chunk = buffer.copyOf(readBytes)
                        val filterResult = echoGuard.processMicrophoneChunk(chunk)

                        _micLevel.value = filterResult.rmsLevel
                        val speechDetected = vadDetector.isSpeech(filterResult.filteredBytes)
                        _isUserSpeaking.value = speechDetected

                        if (filterResult.isBargeInDetected) {
                            _bargeInTriggered.tryEmit(Unit)
                        }

                        // Emit processed chunk for Gemini Live
                        _audioChunks.tryEmit(filterResult.filteredBytes)
                    }
                }
            }
        } catch (e: Exception) {
            stopRecording()
        }
    }

    fun stopRecording() {
        isRecording.set(false)
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            // Ignore clean teardown exceptions
        }
        audioRecord = null

        aec?.release()
        aec = null
        ns?.release()
        ns = null
        agc?.release()
        agc = null

        _micLevel.value = 0.0f
        _isUserSpeaking.value = false
    }

    fun isCapturing(): Boolean = isRecording.get()
}
