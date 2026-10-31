package com.arifa.assistant.flavor

import android.app.Application
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.core.flavor.LicenseProvider

object FlavorInitializer {
    fun init(application: Application) {
        AutomationProvider.instance = DirectAutomationProvider(application)
        LicenseProvider.instance = DirectLicenseProvider(application)
    }
}
