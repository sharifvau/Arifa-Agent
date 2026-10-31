package com.arifa.assistant.flavor

import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.core.flavor.SocialAction

class PlayAutomationProvider : AutomationProvider {
    override fun isSupported(): Boolean = false
    override fun isServiceEnabled(): Boolean = false
    override fun openAccessibilitySettings() {}
    override suspend fun executeSocialAction(action: SocialAction): Result<String> {
        return Result.Error(UnsupportedOperationException("Accessibility automation is disabled in Google Play edition."))
    }
}
