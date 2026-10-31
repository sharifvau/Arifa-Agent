package com.arifa.assistant.core.flavor

import com.arifa.assistant.core.common.Result

data class SocialAction(
    val platform: String, // "instagram", "facebook", "whatsapp"
    val actionType: String, // "post", "story", "caption", "message"
    val content: String,
    val targetRecipient: String? = null
)

interface AutomationProvider {
    fun isSupported(): Boolean
    fun isServiceEnabled(): Boolean
    fun openAccessibilitySettings()
    suspend fun executeSocialAction(action: SocialAction): Result<String>

    companion object {
        var instance: AutomationProvider = object : AutomationProvider {
            override fun isSupported(): Boolean = false
            override fun isServiceEnabled(): Boolean = false
            override fun openAccessibilitySettings() {}
            override suspend fun executeSocialAction(action: SocialAction): Result<String> =
                Result.Error(UnsupportedOperationException("Automation not available in this build"))
        }
    }
}
