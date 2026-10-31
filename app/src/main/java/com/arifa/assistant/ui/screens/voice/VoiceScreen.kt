package com.arifa.assistant.ui.screens.voice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.core.voice.VoiceSessionManager
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.components.ArifaOrbView
import com.arifa.assistant.ui.components.EdgeGlowView
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.components.WaveformView
import com.arifa.assistant.ui.navigation.NavRoutes
import com.arifa.assistant.ui.theme.ErrorRed
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun VoiceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceSessionManager.getInstance(context) }
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val personalityManager = remember { PersonalityManager.getInstance(context) }

    val settings by settingsManager.settings.collectAsState()
    val personality by personalityManager.currentPersonality.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()
    val waveLevel by voiceManager.waveLevel.collectAsState()
    val activeTranscript by voiceManager.activeTranscript.collectAsState()
    val metrics by voiceManager.metrics.collectAsState()

    // Start voice session upon entering screen if idle
    LaunchedEffect(Unit) {
        if (voiceState == VoiceState.IDLE) {
            voiceManager.startSession()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
    ) {
        EdgeGlowView(state = voiceState, reducedMotion = settings.reducedMotion)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        voiceManager.stopSession()
                        onNavigateBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Voice Mode",
                        tint = PearlWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NightSurfaceVariant
                ) {
                    Text(
                        text = "${personality.title} Mode",
                        color = Marigold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                if (metrics.lastLatencyMs > 0) {
                    Text(
                        text = "${metrics.lastLatencyMs}ms",
                        color = LagoonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.width(36.dp))
                }
            }

            // Center: 3D Orb and Live Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ArifaOrbView(
                    size = 260.dp,
                    state = voiceState,
                    amplitude = waveLevel,
                    orbStyle = settings.orbStyle,
                    reducedMotion = settings.reducedMotion
                )

                Spacer(modifier = Modifier.height(24.dp))

                WaveformView(
                    amplitude = waveLevel,
                    barCount = 9,
                    activeColor = if (voiceState == VoiceState.SPEAKING) Marigold else LagoonCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (voiceState) {
                        VoiceState.IDLE -> "Tap to start conversation"
                        VoiceState.CONNECTING -> "Connecting to Gemini Live..."
                        VoiceState.LISTENING -> "Arifa is listening..."
                        VoiceState.THINKING -> "Thinking..."
                        VoiceState.SPEAKING -> "Arifa is speaking..."
                        VoiceState.ERROR -> "Connection error"
                    },
                    color = when (voiceState) {
                        VoiceState.SPEAKING -> Marigold
                        VoiceState.LISTENING -> LagoonCyan
                        VoiceState.ERROR -> ErrorRed
                        else -> TextMuted
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Real-time transcript bubble
                if (activeTranscript.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .height(84.dp)
                    ) {
                        Text(
                            text = activeTranscript,
                            color = PearlWhite,
                            fontSize = 14.sp,
                            maxLines = 3,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Controls: Mic, Interrupt (Barge-in), and Stop
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                if (voiceState == VoiceState.ERROR) {
                    Button(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = LotusPink),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = PearlWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configure API Key in Settings", color = PearlWhite)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Barge-in / Interrupt Button
                        if (voiceState == VoiceState.SPEAKING) {
                            Surface(
                                shape = CircleShape,
                                color = NightSurfaceVariant,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                            ) {
                                IconButton(
                                    onClick = {
                                        // Trigger barge in interruption
                                        voiceManager.stopSession()
                                        voiceManager.startSession()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Hearing,
                                        contentDescription = "Interrupt",
                                        tint = LagoonCyan
                                    )
                                }
                            }
                        }

                        // Main Start / Stop Voice Session Toggle
                        Surface(
                            shape = CircleShape,
                            color = if (voiceState == VoiceState.IDLE) LagoonCyan else ErrorRed,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                        ) {
                            IconButton(
                                onClick = {
                                    if (voiceState == VoiceState.IDLE) {
                                        voiceManager.startSession()
                                    } else {
                                        voiceManager.stopSession()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (voiceState == VoiceState.IDLE) Icons.Default.Mic else Icons.Default.Stop,
                                    contentDescription = "Voice Control",
                                    tint = NightBackground,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
