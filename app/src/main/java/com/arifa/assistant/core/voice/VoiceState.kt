package com.arifa.assistant.core.voice

enum class VoiceState(val description: String) {
    IDLE("Idle"),
    CONNECTING("Connecting to Gemini Live..."),
    LISTENING("Listening..."),
    THINKING("Thinking..."),
    SPEAKING("Speaking..."),
    ERROR("Connection Error")
}

data class VoiceSessionMetrics(
    val lastLatencyMs: Long = 0,
    val totalAudioSentBytes: Long = 0,
    val totalAudioReceivedBytes: Long = 0,
    val reconnectCount: Int = 0
)
