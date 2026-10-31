package com.arifa.assistant.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.ui.navigation.AppNavigation
import com.arifa.assistant.ui.theme.ArifaAITheme
import com.arifa.assistant.ui.theme.NightBackground

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager.getInstance(applicationContext)

        setContent {
            val settings by settingsManager.settings.collectAsState()

            ArifaAITheme(appTheme = settings.theme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = NightBackground
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
