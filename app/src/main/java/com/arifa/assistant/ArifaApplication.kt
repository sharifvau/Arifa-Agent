package com.arifa.assistant

import android.app.Application
import com.arifa.assistant.core.storage.ConversationRepository
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.core.websiteforge.WebsiteForgeEngine
import com.arifa.assistant.flavor.FlavorInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ArifaApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize flavor-specific providers (Play or Direct)
        FlavorInitializer.init(this)

        // Initialize persistent storage and repositories in background
        applicationScope.launch {
            SettingsManager.getInstance(this@ArifaApplication)
            ConversationRepository.getInstance(this@ArifaApplication).initialize()
            WebsiteForgeEngine.getInstance(this@ArifaApplication).initialize()
        }
    }
}
