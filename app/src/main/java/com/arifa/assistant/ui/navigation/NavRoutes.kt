package com.arifa.assistant.ui.navigation

object NavRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val VOICE = "voice"
    const val CHAT = "chat"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val PERSONALITY = "personality"
    const val ORB_STYLE = "orb_style"
    const val PERMISSIONS = "permissions"
    const val PROFILE = "profile"
    const val DEBUG = "debug"
    const val WEBSITE_FORGE = "website_forge"
    const val WEBSITE_VIEWER = "website_viewer/{projectId}"

    fun buildViewerRoute(projectId: String): String = "website_viewer/$projectId"
}
