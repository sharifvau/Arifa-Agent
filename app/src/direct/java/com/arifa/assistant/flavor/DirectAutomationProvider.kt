package com.arifa.assistant.flavor

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.core.flavor.SocialAction
import com.arifa.assistant.flavor.accessibility.ArifaAccessibilityService
import com.arifa.assistant.flavor.social.SocialMediaVisualAgent

class DirectAutomationProvider(private val context: Context) : AutomationProvider {

    private val socialAgent = SocialMediaVisualAgent(context)

    override fun isSupported(): Boolean = true

    override fun isServiceEnabled(): Boolean = ArifaAccessibilityService.isRunning()

    override fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override suspend fun executeSocialAction(action: SocialAction): Result<String> {
        return socialAgent.executeAction(action)
    }
}
