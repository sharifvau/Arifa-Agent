package com.arifa.assistant.core.common

object Constants {
    const val APP_NAME = "ARIFA AI"
    const val DEFAULT_ASSISTANT_NAME = "Arifa"
    const val DEFAULT_ASSISTANT_NAME_BN = "আরিফা"

    // Gemini Endpoints
    const val GEMINI_LIVE_WS_URL = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent"
    const val GEMINI_REST_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    const val DEFAULT_LIVE_MODEL = "models/gemini-2.0-flash-exp"
    const val DEFAULT_REST_MODEL = "gemini-2.0-flash"

    // Audio Configurations
    const val SAMPLE_RATE_INPUT_HZ = 16000
    const val SAMPLE_RATE_OUTPUT_HZ = 24000
    const val CHANNELS_MONO = 1
    const val BITS_PER_SAMPLE = 16
    const val AUDIO_CHUNK_DURATION_MS = 100 // 100ms per packet
    const val BYTES_PER_SAMPLE = 2 // 16-bit PCM

    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "arifa_voice_service_channel"
    const val NOTIFICATION_ID = 2026

    // Intent Actions
    const val ACTION_STOP_SERVICE = "com.arifa.assistant.action.STOP_SERVICE"
    const val ACTION_TOGGLE_MIC = "com.arifa.assistant.action.TOGGLE_MIC"

    // Preferences File
    const val PREFS_NAME = "arifa_settings_prefs"
    const val CONVERSATIONS_PREFS = "arifa_conversations_prefs"
    const val LICENSE_PREFS = "arifa_license_prefs"
}
