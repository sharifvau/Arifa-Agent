package com.arifa.assistant.core.personality

import android.content.Context
import com.arifa.assistant.core.storage.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PersonalityManager private constructor(private val context: Context) {

    private val settingsManager = SettingsManager.getInstance(context)

    private val _currentPersonality = MutableStateFlow(resolvePersonality(settingsManager.settings.value.personalityId))
    val currentPersonality: StateFlow<PersonalityType> = _currentPersonality.asStateFlow()

    init {
        // Observe settings changes
        val currentId = settingsManager.settings.value.personalityId
        _currentPersonality.value = resolvePersonality(currentId)
    }

    private fun resolvePersonality(id: String): PersonalityType {
        return try {
            PersonalityType.valueOf(id)
        } catch (e: Exception) {
            PersonalityType.FRIENDLY
        }
    }

    fun setPersonality(personality: PersonalityType) {
        settingsManager.updatePersonality(personality.id)
        _currentPersonality.value = personality
    }

    fun getSystemInstruction(): String {
        val settings = settingsManager.settings.value
        val personality = _currentPersonality.value
        return SystemInstructions.buildInstruction(settings.assistantName, personality)
    }

    fun getAllPersonalities(): List<PersonalityType> {
        return PersonalityType.values().toList()
    }

    companion object {
        @Volatile
        private var instance: PersonalityManager? = null

        fun getInstance(context: Context): PersonalityManager {
            return instance ?: synchronized(this) {
                instance ?: PersonalityManager(context).also { instance = it }
            }
        }
    }
}
