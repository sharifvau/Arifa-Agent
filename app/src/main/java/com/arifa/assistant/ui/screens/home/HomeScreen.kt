package com.arifa.assistant.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.core.flavor.LicenseProvider
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.storage.ConversationRepository
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.core.voice.VoiceSessionManager
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.components.ArifaOrbView
import com.arifa.assistant.ui.components.EdgeGlowView
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.components.QuickActionChip
import com.arifa.assistant.ui.navigation.NavRoutes
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onQuickPrompt: (String) -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val personalityManager = remember { PersonalityManager.getInstance(context) }
    val voiceManager = remember { VoiceSessionManager.getInstance(context) }
    val conversationRepo = remember { ConversationRepository.getInstance(context) }

    val settings by settingsManager.settings.collectAsState()
    val personality by personalityManager.currentPersonality.collectAsState()
    val voiceState by voiceManager.voiceState.collectAsState()
    val waveLevel by voiceManager.waveLevel.collectAsState()
    val currentSession by conversationRepo.currentSession.collectAsState()

    val licenseInfo = remember { LicenseProvider.instance.getLicenseInfo() }
    val isAutomationSupported = remember { AutomationProvider.instance.isSupported() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
    ) {
        EdgeGlowView(state = voiceState, reducedMotion = settings.reducedMotion)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            // Top Status Bar: Personality & Language badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = settings.assistantName.uppercase(),
                        color = PearlWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "A Pearl of Light in the Night",
                        color = Marigold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Personality selector pill badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = NightSurfaceVariant,
                    modifier = Modifier.clickable { onNavigate(NavRoutes.PERSONALITY) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LotusPink)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = personality.title,
                            color = PearlWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Hero 3D Orb with Tilt & Touch interaction
            ArifaOrbView(
                size = 230.dp,
                state = voiceState,
                amplitude = if (voiceState == VoiceState.IDLE) 0.05f else waveLevel,
                orbStyle = settings.orbStyle,
                reducedMotion = settings.reducedMotion,
                onClick = { onNavigate(NavRoutes.VOICE) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = when (voiceState) {
                    VoiceState.IDLE -> "Tap orb or mic to start conversation"
                    VoiceState.CONNECTING -> "Connecting to Gemini Live..."
                    VoiceState.LISTENING -> "Listening to you..."
                    VoiceState.THINKING -> "Thinking..."
                    VoiceState.SPEAKING -> "Speaking..."
                    VoiceState.ERROR -> "Check API key in Settings"
                },
                color = if (voiceState == VoiceState.ERROR) LotusPink else TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Voice Action Button
            Button(
                onClick = { onNavigate(NavRoutes.VOICE) },
                colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(54.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = NightBackground,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Talk to ${settings.assistantName}",
                    color = NightBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Quick Actions Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Prompts",
                    color = PearlWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Forge 3D",
                    color = Marigold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onNavigate(NavRoutes.WEBSITE_FORGE) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Prompt Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    text = "Restaurant Website 🍽️",
                    icon = Icons.Default.Restaurant,
                    iconTint = Marigold,
                    onClick = {
                        onQuickPrompt("Arifa, amar jonno ekta restaurant er website banao.")
                    }
                )
                QuickActionChip(
                    text = "Cyberpunk Portfolio ⚡",
                    icon = Icons.Default.Web,
                    iconTint = LagoonCyan,
                    onClick = {
                        onQuickPrompt("Build a futuristic cyberpunk portfolio with Three.js.")
                    }
                )
                QuickActionChip(
                    text = "Kemon acho Arifa? ☕",
                    icon = Icons.Default.Chat,
                    iconTint = LotusPink,
                    onClick = {
                        onQuickPrompt("Kemon acho Arifa? Ajke ki khobor?")
                    }
                )
                QuickActionChip(
                    text = "Call Contact 📞",
                    icon = Icons.Default.Call,
                    iconTint = LagoonCyan,
                    onClick = {
                        onNavigate(NavRoutes.VOICE)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Website Forge Teaser Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(NavRoutes.WEBSITE_FORGE) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(listOf(Marigold, LotusPink))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PearlWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Website Forge 3D",
                            color = PearlWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create full 3D interactive websites using voice commands.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Direct Build Status Banner (If Direct Flavor)
            if (isAutomationSupported) {
                Spacer(modifier = Modifier.height(14.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Marigold.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Marigold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Direct Edition Active (${licenseInfo.tier})",
                                color = Marigold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Screen Automation & Social Visual Agent Enabled",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
        }
    }
}
