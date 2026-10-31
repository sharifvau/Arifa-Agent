package com.arifa.assistant.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.arifa.assistant.core.common.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AssistantLanguage(val displayName: String, val code: String) {
    AUTO("Auto-Detect (বাংলা / Banglish / English)", "auto"),
    BANGLA("বাংলা (Bangla)", "bn"),
    BANGLISH("Banglish (বাংলা in English script)", "banglish"),
    ENGLISH("English", "en")
}

enum class AppTheme(val displayName: String) {
    DARK("Pearl in the Night (Dark)"),
    LIGHT("Daylight Pearl (Light)"),
    SYSTEM("System Default")
}

enum class OrbStyle(val displayName: String, val description: String) {
    PEARL_CLASSIC("Pearl Classic", "A luminous pearl floating in cosmic indigo"),
    NEON_CYBER("Neon Cyber", "Vibrant cyan & electric amber futuristic energy"),
    SOLAR_FLARE("Solar Flare", "Radiant marigold and fiery gold plasma"),
    QUANTUM_LOTUS("Quantum Lotus", "Deep magenta & glowing lotus pink petals"),
    CELESTIAL_AURORA("Celestial Aurora", "Northern lights cyan, teal and violet wave"),
    DARK_MATTER("Dark Matter", "Subtle gravitational lens with shimmering rim"),
    BIO_PULSE("Bio-Pulse", "Living neural pulse reacting to bio-signals")
}

data class UserSettings(
    val apiKey: String = "",
    val assistantName: String = Constants.DEFAULT_ASSISTANT_NAME,
    val personalityId: String = "FRIENDLY",
    val language: AssistantLanguage = AssistantLanguage.AUTO,
    val voicePitch: Float = 1.0f,
    val voiceSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val theme: AppTheme = AppTheme.DARK,
    val orbStyle: OrbStyle = OrbStyle.PEARL_CLASSIC,
    val reducedMotion: Boolean = false,
    val wakeWordEnabled: Boolean = false,
    val autoSpeakReplies: Boolean = true,
    val hapticFeedback: Boolean = true,
    val onboardingCompleted: Boolean = false
)

class SettingsManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        return UserSettings(
            apiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            assistantName = prefs.getString(KEY_ASSISTANT_NAME, Constants.DEFAULT_ASSISTANT_NAME) ?: Constants.DEFAULT_ASSISTANT_NAME,
            personalityId = prefs.getString(KEY_PERSONALITY_ID, "FRIENDLY") ?: "FRIENDLY",
            language = try {
                AssistantLanguage.valueOf(prefs.getString(KEY_LANGUAGE, AssistantLanguage.AUTO.name) ?: AssistantLanguage.AUTO.name)
            } catch (e: Exception) {
                AssistantLanguage.AUTO
            },
            voicePitch = prefs.getFloat(KEY_VOICE_PITCH, 1.0f),
            voiceSpeed = prefs.getFloat(KEY_VOICE_SPEED, 1.0f),
            volume = prefs.getFloat(KEY_VOLUME, 1.0f),
            theme = try {
                AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.DARK.name) ?: AppTheme.DARK.name)
            } catch (e: Exception) {
                AppTheme.DARK
            },
            orbStyle = try {
                OrbStyle.valueOf(prefs.getString(KEY_ORB_STYLE, OrbStyle.PEARL_CLASSIC.name) ?: OrbStyle.PEARL_CLASSIC.name)
            } catch (e: Exception) {
                OrbStyle.PEARL_CLASSIC
            },
            reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false),
            wakeWordEnabled = prefs.getBoolean(KEY_WAKE_WORD_ENABLED, false),
            autoSpeakReplies = prefs.getBoolean(KEY_AUTO_SPEAK, true),
            hapticFeedback = prefs.getBoolean(KEY_HAPTIC, true),
            onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        )
    }

    fun updateApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
        _settings.value = _settings.value.copy(apiKey = apiKey.trim())
    }

    fun updateAssistantName(name: String) {
        val validName = if (name.isBlank()) Constants.DEFAULT_ASSISTANT_NAME else name.trim()
        prefs.edit().putString(KEY_ASSISTANT_NAME, validName).apply()
        _settings.value = _settings.value.copy(assistantName = validName)
    }

    fun updatePersonality(personalityId: String) {
        prefs.edit().putString(KEY_PERSONALITY_ID, personalityId).apply()
        _settings.value = _settings.value.copy(personalityId = personalityId)
    }

    fun updateLanguage(language: AssistantLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun updateVoicePitch(pitch: Float) {
        val coerced = pitch.coerceIn(0.5f, 1.5f)
        prefs.edit().putFloat(KEY_VOICE_PITCH, coerced).apply()
        _settings.value = _settings.value.copy(voicePitch = coerced)
    }

    fun updateVoiceSpeed(speed: Float) {
        val coerced = speed.coerceIn(0.5f, 2.0f)
        prefs.edit().putFloat(KEY_VOICE_SPEED, coerced).apply()
        _settings.value = _settings.value.copy(voiceSpeed = coerced)
    }

    fun updateVolume(volume: Float) {
        val coerced = volume.coerceIn(0.0f, 1.0f)
        prefs.edit().putFloat(KEY_VOLUME, coerced).apply()
        _settings.value = _settings.value.copy(volume = coerced)
    }

    fun updateTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun updateOrbStyle(orbStyle: OrbStyle) {
        prefs.edit().putString(KEY_ORB_STYLE, orbStyle.name).apply()
        _settings.value = _settings.value.copy(orbStyle = orbStyle)
    }

    fun updateReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _settings.value = _settings.value.copy(reducedMotion = enabled)
    }

    fun updateWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(wakeWordEnabled = enabled)
    }

    fun updateAutoSpeakReplies(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
        _settings.value = _settings.value.copy(autoSpeakReplies = enabled)
    }

    fun updateHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.value = _settings.value.copy(hapticFeedback = enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _settings.value = _settings.value.copy(onboardingCompleted = completed)
    }

    fun isApiKeyConfigured(): Boolean {
        return _settings.value.apiKey.isNotBlank()
    }

    companion object {
        private const val KEY_API_KEY = "key_gemini_api"
        private const val KEY_ASSISTANT_NAME = "key_assistant_name"
        private const val KEY_PERSONALITY_ID = "key_personality_id"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_VOICE_PITCH = "key_voice_pitch"
        private const val KEY_VOICE_SPEED = "key_voice_speed"
        private const val KEY_VOLUME = "key_volume"
        private const val KEY_THEME = "key_theme"
        private const val KEY_ORB_STYLE = "key_orb_style"
        private const val KEY_REDUCED_MOTION = "key_reduced_motion"
        private const val KEY_WAKE_WORD_ENABLED = "key_wake_word_enabled"
        private const val KEY_AUTO_SPEAK = "key_auto_speak"
        private const val KEY_HAPTIC = "key_haptic"
        private const val KEY_ONBOARDING_COMPLETED = "key_onboarding_completed"

        @Volatile
        private var instance: SettingsManager? = null

        fun getInstance(context: Context): SettingsManager {
            return instance ?: synchronized(this) {
                instance ?: SettingsManager(context).also { instance = it }
            }
        }
    }
}
