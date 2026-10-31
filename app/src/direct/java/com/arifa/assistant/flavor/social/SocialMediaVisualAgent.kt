package com.arifa.assistant.flavor.social

import android.content.Context
import android.content.Intent
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.SocialAction
import com.arifa.assistant.flavor.accessibility.ArifaAccessibilityService
import com.arifa.assistant.flavor.accessibility.ScreenNodeFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SocialMediaVisualAgent(private val context: Context) {

    suspend fun executeAction(action: SocialAction): Result<String> = withContext(Dispatchers.IO) {
        val service = ArifaAccessibilityService.instance
            ?: return@withContext Result.Error(IllegalStateException("Accessibility Service is not enabled. Please enable ARIFA AI in Android Accessibility Settings."))

        // Check for security screens
        val rootNode = service.rootInActiveWindow
        if (ScreenNodeFinder.isSecurityScreen(rootNode)) {
            return@withContext Result.Error(SecurityException("Security challenge, login, or 2FA detected. Automation stopped for safety."))
        }

        when (action.platform.lowercase()) {
            "instagram" -> handleInstagram(service, action)
            "facebook" -> handleFacebook(service, action)
            "whatsapp" -> handleWhatsApp(service, action)
            else -> Result.Error(IllegalArgumentException("Unsupported platform: ${action.platform}"))
        }
    }

    private suspend fun handleInstagram(service: ArifaAccessibilityService, action: SocialAction): Result<String> {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.instagram.android")
            ?: return Result.Error(IllegalStateException("Instagram app is not installed."))

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        delay(1500) // Wait for UI transition

        val root = service.rootInActiveWindow
        if (ScreenNodeFinder.isSecurityScreen(root)) {
            return Result.Error(SecurityException("Instagram login/security screen detected. Operation aborted."))
        }

        // Navigate or find caption input
        val editable = ScreenNodeFinder.findEditableNode(root)
        if (editable != null && action.content.isNotBlank()) {
            service.setNodeText(editable, action.content)
            return Result.Success("Caption populated in Instagram. Please review before publishing.")
        }

        return Result.Success("Instagram launched. Ready for user confirmation.")
    }

    private suspend fun handleFacebook(service: ArifaAccessibilityService, action: SocialAction): Result<String> {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.facebook.katana")
            ?: return Result.Error(IllegalStateException("Facebook app is not installed."))

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        delay(1500)

        val root = service.rootInActiveWindow
        if (ScreenNodeFinder.isSecurityScreen(root)) {
            return Result.Error(SecurityException("Facebook security/login challenge detected."))
        }

        val editable = ScreenNodeFinder.findEditableNode(root)
        if (editable != null && action.content.isNotBlank()) {
            service.setNodeText(editable, action.content)
            return Result.Success("Facebook post draft populated. Awaiting manual confirmation.")
        }

        return Result.Success("Facebook opened successfully.")
    }

    private suspend fun handleWhatsApp(service: ArifaAccessibilityService, action: SocialAction): Result<String> {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            ?: return Result.Error(IllegalStateException("WhatsApp is not installed."))

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        delay(1200)

        val root = service.rootInActiveWindow
        val editable = ScreenNodeFinder.findEditableNode(root)
        if (editable != null && action.content.isNotBlank()) {
            service.setNodeText(editable, action.content)
            return Result.Success("WhatsApp message prepared in text field. Press send to confirm.")
        }

        return Result.Success("WhatsApp opened.")
    }
}
