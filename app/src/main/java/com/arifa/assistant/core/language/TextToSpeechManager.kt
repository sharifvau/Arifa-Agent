package com.arifa.assistant.core.language

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.arifa.assistant.core.storage.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val settingsManager = SettingsManager.getInstance(context)

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoiceLocale()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    private fun setupVoiceLocale() {
        val t = tts ?: return
        // Prefer Bangladesh Bengali
        val bangladeshLocale = Locale("bn", "BD")
        val indiaLocale = Locale("bn", "IN")

        val resBD = t.isLanguageAvailable(bangladeshLocale)
        if (resBD >= TextToSpeech.LANG_AVAILABLE) {
            t.language = bangladeshLocale
            return
        }

        val resIN = t.isLanguageAvailable(indiaLocale)
        if (resIN >= TextToSpeech.LANG_AVAILABLE) {
            t.language = indiaLocale
            return
        }

        t.language = Locale.US
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) {
            onDone?.invoke()
            return
        }

        val t = tts ?: return
        val settings = settingsManager.settings.value

        t.setSpeechRate(settings.voiceSpeed)
        t.setPitch(settings.voicePitch)

        // Select language for TTS based on text content
        val detection = LanguageDetector.detect(text, settings.language)
        if (detection.language == DetectedLanguage.BANGLA) {
            val bnBD = Locale("bn", "BD")
            if (t.isLanguageAvailable(bnBD) >= TextToSpeech.LANG_AVAILABLE) {
                t.language = bnBD
            } else {
                t.language = Locale("bn", "IN")
            }
        } else {
            t.language = Locale.US
        }

        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, settings.volume)
        }

        t.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
