package com.arifa.assistant.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.flavor.LicenseProvider
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.storage.AppTheme
import com.arifa.assistant.core.storage.AssistantLanguage
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.navigation.NavRoutes
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val personalityManager = remember { PersonalityManager.getInstance(context) }

    val settings by settingsManager.settings.collectAsState()
    val personality by personalityManager.currentPersonality.collectAsState()
    val licenseInfo = remember { LicenseProvider.instance.getLicenseInfo() }
    val isLicensingEnabled = remember { LicenseProvider.instance.isLicensingEnabled() }

    var apiKeyInput by remember { mutableStateOf(settings.apiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var showLicenseDialog by remember { mutableStateOf(false) }
    var licenseKeyInput by remember { mutableStateOf("") }
    var licenseFeedback by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
            .padding(bottom = 88.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Settings & Personalization",
                color = PearlWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 1. Gemini API Key Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Marigold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Gemini API Key", color = PearlWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Stored locally on device. Never shared or logged.", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            settingsManager.updateApiKey(it)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Visibility",
                                    tint = TextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LagoonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = PearlWhite,
                            unfocusedTextColor = PearlWhite,
                            focusedContainerColor = NightSurfaceVariant,
                            unfocusedContainerColor = NightSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Personality Selection Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.PERSONALITY) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = LotusPink, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Personality", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${personality.title} (${personality.titleBn})", color = Marigold, fontSize = 13.sp)
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Orb Style Selection Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.ORB_STYLE) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = LagoonCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "3D Orb Style", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = settings.orbStyle.displayName, color = LagoonCyan, fontSize = 13.sp)
                        }
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Voice Controls (Speed & Pitch)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Voice Synthesis", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Speaking Speed: ${String.format("%.1f", settings.voiceSpeed)}x", color = TextMuted, fontSize = 13.sp)
                    Slider(
                        value = settings.voiceSpeed,
                        onValueChange = { settingsManager.updateVoiceSpeed(it) },
                        valueRange = 0.5f..2.0f,
                        steps = 5,
                        colors = SliderDefaults.colors(thumbColor = LagoonCyan, activeTrackColor = LagoonCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Speaking Pitch: ${String.format("%.1f", settings.voicePitch)}x", color = TextMuted, fontSize = 13.sp)
                    Slider(
                        value = settings.voicePitch,
                        onValueChange = { settingsManager.updateVoicePitch(it) },
                        valueRange = 0.5f..1.5f,
                        steps = 4,
                        colors = SliderDefaults.colors(thumbColor = Marigold, activeTrackColor = Marigold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Switches (Reduced Motion & Auto Speak)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Reduced Motion", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Subdue 3D orb spinning and rapid screen transitions", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.reducedMotion,
                            onCheckedChange = { settingsManager.updateReducedMotion(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LagoonCyan, checkedTrackColor = LagoonCyan.copy(alpha = 0.4f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto-Speak Text Replies", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Synthesize natural Bengali/English voice in chat mode", color = TextMuted, fontSize = 12.sp)
                        }
                        Switch(
                            checked = settings.autoSpeakReplies,
                            onCheckedChange = { settingsManager.updateAutoSpeakReplies(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = LagoonCyan, checkedTrackColor = LagoonCyan.copy(alpha = 0.4f))
                        )
                    }
                }
            }

            // 6. Direct Edition Licensing Section (If Direct Build)
            if (isLicensingEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Marigold.copy(alpha = 0.5f),
                    onClick = { showLicenseDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = Marigold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Direct Access Key", color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (licenseInfo.isValid) "${licenseInfo.tier} (${licenseInfo.remainingDays} days left)" else "Expired / Inactive",
                                    color = if (licenseInfo.isValid) Marigold else LotusPink,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Text(text = "Manage", color = Marigold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Navigation links: Permissions, Debug, About
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.PERMISSIONS) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = LagoonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "App Permissions", color = PearlWhite, fontSize = 15.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.DEBUG) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = Marigold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Latency & Debug Metrics", color = PearlWhite, fontSize = 15.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.PROFILE) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PearlWhite, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "About ARIFA AI", color = PearlWhite, fontSize = 15.sp)
                    }
                    Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // Direct Access Key Activation Modal Dialog
        if (showLicenseDialog) {
            AlertDialog(
                onDismissRequest = {
                    showLicenseDialog = false
                    licenseFeedback = null
                },
                title = { Text(text = "Activate Direct Edition", color = PearlWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text(text = "Enter your license key (e.g., ARIFA-PRO-A1B2-C3D4):", color = TextMuted, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = licenseKeyInput,
                            onValueChange = { licenseKeyInput = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LagoonCyan,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = PearlWhite,
                                unfocusedTextColor = PearlWhite
                            )
                        )
                        if (licenseFeedback != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = licenseFeedback!!, color = LagoonCyan, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val result = LicenseProvider.instance.activateKey(licenseKeyInput)
                            when (result) {
                                is Result.Success -> {
                                    licenseFeedback = "Key activated! Status: ${result.data.tier}"
                                }
                                is Result.Error -> {
                                    licenseFeedback = result.message
                                }
                                else -> {}
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan)
                    ) {
                        Text("Activate", color = NightBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLicenseDialog = false }) {
                        Text("Close", color = TextMuted)
                    }
                },
                containerColor = NightSurfaceVariant
            )
        }
    }
}
