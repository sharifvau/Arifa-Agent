package com.arifa.assistant.core.voice

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.arifa.assistant.core.audio.AudioPlayer
import com.arifa.assistant.core.audio.AudioRecorder
import com.arifa.assistant.core.audio.EchoGuard
import com.arifa.assistant.core.common.calculateRmsLevel
import com.arifa.assistant.core.common.toBase64
import com.arifa.assistant.core.gemini.live.FunctionCall
import com.arifa.assistant.core.gemini.live.GeminiLiveClient
import com.arifa.assistant.core.gemini.live.LiveServerEvent
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.phone.PhoneActionController
import com.arifa.assistant.core.storage.ChatMessage
import com.arifa.assistant.core.storage.ConversationRepository
import com.arifa.assistant.core.storage.SettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class VoiceSessionManager private constructor(private val context: Context) {

    private val sessionScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val settingsManager = SettingsManager.getInstance(context)
    private val personalityManager = PersonalityManager.getInstance(context)
    private val conversationRepository = ConversationRepository.getInstance(context)
    private val phoneActionController = PhoneActionController.getInstance(context)

    private val echoGuard = EchoGuard()
    private val audioRecorder = AudioRecorder(sessionScope, echoGuard)
    private val audioPlayer = AudioPlayer(sessionScope, echoGuard)
    private val liveClient = GeminiLiveClient(sessionScope)

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _waveLevel = MutableStateFlow(0.0f)
    val waveLevel: StateFlow<Float> = _waveLevel.asStateFlow()

    private val _activeTranscript = MutableStateFlow("")
    val activeTranscript: StateFlow<String> = _activeTranscript.asStateFlow()

    private val _userTranscript = MutableStateFlow("")
    val userTranscript: StateFlow<String> = _userTranscript.asStateFlow()

    private val _metrics = MutableStateFlow(VoiceSessionMetrics())
    val metrics: StateFlow<VoiceSessionMetrics> = _metrics.asStateFlow()

    private val _websiteForgeRequested = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 4)
    val websiteForgeRequested: SharedFlow<String> = _websiteForgeRequested.asSharedFlow()

    private var eventCollectJob: Job? = null
    private var audioMicJob: Job? = null
    private var userSpeakingJob: Job? = null
    private var bargeInJob: Job? = null

    private var userSpeechStartTime: Long = 0
    private var waitingForResponseStartTime: Long = 0
    private val currentModelTurnText = StringBuilder()

    init {
        sessionScope.launch {
            conversationRepository.initialize()
        }
    }

    fun startSession() {
        if (_voiceState.value != VoiceState.IDLE && _voiceState.value != VoiceState.ERROR) {
            return
        }

        val apiKey = settingsManager.settings.value.apiKey
        if (apiKey.isBlank()) {
            _voiceState.value = VoiceState.ERROR
            return
        }

        _voiceState.value = VoiceState.CONNECTING

        // Start Foreground Service for uninterrupted background voice
        try {
            val serviceIntent = Intent(context, VoiceForegroundService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        } catch (e: Exception) {
            // Safe fallback if service fails to start
        }

        audioPlayer.start()
        audioRecorder.startRecording()

        val instruction = personalityManager.getSystemInstruction()
        liveClient.connect(apiKey, instruction)

        setupSubscriptions()
    }

    private fun setupSubscriptions() {
        eventCollectJob?.cancel()
        eventCollectJob = sessionScope.launch {
            liveClient.events.collect { event ->
                handleLiveEvent(event)
            }
        }

        audioMicJob?.cancel()
        audioMicJob = sessionScope.launch(Dispatchers.IO) {
            audioRecorder.audioChunks.collect { chunk ->
                if (_voiceState.value == VoiceState.LISTENING || _voiceState.value == VoiceState.THINKING) {
                    val base64 = chunk.toBase64()
                    liveClient.sendAudioChunk(base64)
                    _metrics.value = _metrics.value.copy(
                        totalAudioSentBytes = _metrics.value.totalAudioSentBytes + chunk.size
                    )
                }
            }
        }

        userSpeakingJob?.cancel()
        userSpeakingJob = sessionScope.launch {
            audioRecorder.isUserSpeaking.collect { isSpeaking ->
                if (isSpeaking) {
                    userSpeechStartTime = System.currentTimeMillis()
                    if (_voiceState.value != VoiceState.SPEAKING) {
                        _voiceState.value = VoiceState.LISTENING
                    }
                } else if (_voiceState.value == VoiceState.LISTENING && userSpeechStartTime > 0) {
                    waitingForResponseStartTime = System.currentTimeMillis()
                    _voiceState.value = VoiceState.THINKING
                }
            }
        }

        bargeInJob?.cancel()
        bargeInJob = sessionScope.launch {
            audioRecorder.bargeInTriggered.collect {
                // User interrupted assistant speech (Barge-in)
                triggerBargeIn()
            }
        }

        // Forward waveform levels (mic or speaker)
        sessionScope.launch {
            audioRecorder.micLevel.collect { mLevel ->
                if (_voiceState.value == VoiceState.LISTENING) {
                    _waveLevel.value = mLevel
                }
            }
        }
        sessionScope.launch {
            audioPlayer.speakerLevel.collect { sLevel ->
                if (_voiceState.value == VoiceState.SPEAKING) {
                    _waveLevel.value = sLevel
                }
            }
        }
    }

    private fun handleLiveEvent(event: LiveServerEvent) {
        when (event) {
            is LiveServerEvent.SetupComplete -> {
                _voiceState.value = VoiceState.LISTENING
            }

            is LiveServerEvent.AudioChunk -> {
                if (waitingForResponseStartTime > 0) {
                    val latency = System.currentTimeMillis() - waitingForResponseStartTime
                    _metrics.value = _metrics.value.copy(lastLatencyMs = latency)
                    waitingForResponseStartTime = 0
                }

                _voiceState.value = VoiceState.SPEAKING
                audioPlayer.enqueueAudioChunk(event.pcmData)
                _metrics.value = _metrics.value.copy(
                    totalAudioReceivedBytes = _metrics.value.totalAudioReceivedBytes + event.pcmData.size
                )
            }

            is LiveServerEvent.TextChunk -> {
                currentModelTurnText.append(event.text)
                _activeTranscript.value = currentModelTurnText.toString()
            }

            is LiveServerEvent.TurnComplete -> {
                val fullResponse = currentModelTurnText.toString().trim()
                if (fullResponse.isNotBlank()) {
                    sessionScope.launch {
                        conversationRepository.addMessageToCurrentSession(
                            ChatMessage(role = "model", content = fullResponse)
                        )
                    }
                }
                currentModelTurnText.clear()
                _voiceState.value = VoiceState.LISTENING
            }

            is LiveServerEvent.Interrupted -> {
                triggerBargeIn()
            }

            is LiveServerEvent.ToolCall -> {
                executeToolCalls(event.calls)
            }

            is LiveServerEvent.Error -> {
                _voiceState.value = VoiceState.ERROR
            }
        }
    }

    private fun triggerBargeIn() {
        audioPlayer.flushAndStop()
        currentModelTurnText.clear()
        _activeTranscript.value = ""
        _voiceState.value = VoiceState.LISTENING
    }

    private fun executeToolCalls(calls: List<FunctionCall>) {
        sessionScope.launch(Dispatchers.IO) {
            for (call in calls) {
                val resultJson = when (call.name) {
                    "open_app" -> {
                        val appName = call.args.optString("appName")
                        phoneActionController.openApp(appName)
                    }
                    "call_contact" -> {
                        val contact = call.args.optString("contactName")
                        phoneActionController.callContact(contact)
                    }
                    "send_sms" -> {
                        val contact = call.args.optString("contact")
                        val msg = call.args.optString("message")
                        phoneActionController.prepareSms(contact, msg)
                    }
                    "send_whatsapp" -> {
                        val target = call.args.optString("phoneOrName")
                        val msg = call.args.optString("message")
                        phoneActionController.prepareWhatsApp(target, msg)
                    }
                    "create_website" -> {
                        val topic = call.args.optString("topic")
                        _websiteForgeRequested.tryEmit(topic)
                        JSONObject().apply {
                            put("status", "success")
                            put("message", "Website Forge opened for topic: $topic")
                        }
                    }
                    else -> {
                        JSONObject().apply {
                            put("status", "error")
                            put("message", "Unknown tool: ${call.name}")
                        }
                    }
                }
                liveClient.sendToolResponse(call.id, resultJson)
            }
        }
    }

    fun stopSession() {
        _voiceState.value = VoiceState.IDLE
        _waveLevel.value = 0.0f
        _activeTranscript.value = ""
        currentModelTurnText.clear()

        eventCollectJob?.cancel()
        audioMicJob?.cancel()
        userSpeakingJob?.cancel()
        bargeInJob?.cancel()

        audioRecorder.stopRecording()
        audioPlayer.flushAndStop()
        audioPlayer.release()
        liveClient.disconnect()

        try {
            val serviceIntent = Intent(context, VoiceForegroundService::class.java)
            context.stopService(serviceIntent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        @Volatile
        private var instance: VoiceSessionManager? = null

        fun getInstance(context: Context): VoiceSessionManager {
            return instance ?: synchronized(this) {
                instance ?: VoiceSessionManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
