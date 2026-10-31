package com.arifa.assistant.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.storage.AssistantLanguage
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.components.ArifaOrbView
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val settings by settingsManager.settings.collectAsState()

    var currentStep by remember { mutableIntStateOf(0) }
    var inputApiKey by remember { mutableStateOf(settings.apiKey) }
    var selectedLang by remember { mutableStateOf(settings.language) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Step Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 0..3) {
                    val isActive = i == currentStep
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(6.dp)
                            .width(if (isActive) 28.dp else 12.dp)
                            .clip(CircleShape)
                            .background(if (isActive) LagoonCyan else TextMuted.copy(alpha = 0.3f))
                    )
                }
            }

            // Step Content
            when (currentStep) {
                0 -> {
                    // Step 0: Welcome
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ArifaOrbView(
                            size = 180.dp,
                            state = VoiceState.SPEAKING,
                            amplitude = 0.25f,
                            orbStyle = settings.orbStyle
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Meet ARIFA",
                            color = PearlWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Your futuristic multilingual personal AI companion with real-time voice, emotion, phone control and 3D web creation.",
                            color = TextMuted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                1 -> {
                    // Step 1: Language Selection
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = LagoonCyan,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Choose Your Language",
                            color = PearlWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Arifa naturally understands Bangla, Banglish & English with auto-detection.",
                            color = TextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        AssistantLanguage.values().forEach { lang ->
                            val isSelected = selectedLang == lang
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                borderColor = if (isSelected) LagoonCyan else GlassBorder,
                                onClick = { selectedLang = lang }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        color = if (isSelected) LagoonCyan else PearlWhite,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = LagoonCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Step 2: Gemini API Key
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = Marigold,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Enter Gemini API Key",
                            color = PearlWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your API key is stored securely in private local storage and never transmitted elsewhere.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = inputApiKey,
                            onValueChange = { inputApiKey = it },
                            label = { Text("Gemini API Key", color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LagoonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = PearlWhite,
                                unfocusedTextColor = PearlWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Get a free key from Google AI Studio (aistudio.google.com). You can also configure this later in Settings.",
                            color = Marigold.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                3 -> {
                    // Step 3: Complete
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = LotusPink,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "All Set! Pera Nai Chill!",
                            color = PearlWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Arifa is ready to listen, talk, assist and create 3D websites with you in Bangla, Banglish & English.",
                            color = TextMuted,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    Text(
                        text = "Back",
                        color = TextMuted,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .clickable { currentStep-- }
                            .padding(8.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            if (currentStep == 1) {
                                settingsManager.updateLanguage(selectedLang)
                            }
                            if (currentStep == 2) {
                                if (inputApiKey.isNotBlank()) {
                                    settingsManager.updateApiKey(inputApiKey)
                                }
                            }
                            currentStep++
                        } else {
                            settingsManager.setOnboardingCompleted(true)
                            onFinishOnboarding()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (currentStep == 3) "Get Started" else "Next",
                        color = NightBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (currentStep == 3) Icons.Default.Check else Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = NightBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
