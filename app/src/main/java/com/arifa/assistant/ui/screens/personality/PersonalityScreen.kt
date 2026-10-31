package com.arifa.assistant.ui.screens.personality

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.language.TextToSpeechManager
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.personality.PersonalityType
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun PersonalityScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val manager = remember { PersonalityManager.getInstance(context) }
    val currentPersonality by manager.currentPersonality.collectAsState()
    val tts = remember { TextToSpeechManager(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = PearlWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "10 AI Personalities",
                        color = PearlWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant personality & behavioral switching",
                        color = Marigold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(manager.getAllPersonalities(), key = { it.id }) { p ->
                    val isSelected = p.id == currentPersonality.id
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isSelected) LagoonCyan else GlassBorder,
                        borderWidth = if (isSelected) 2.dp else 1.dp,
                        onClick = { manager.setPersonality(p) }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = p.title,
                                        color = if (isSelected) LagoonCyan else PearlWhite,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${p.titleBn})",
                                        color = Marigold,
                                        fontSize = 14.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { tts.speak(p.sampleQuoteBn) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Preview voice",
                                            tint = LagoonCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = LagoonCyan,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = NightBackground,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = p.tagline,
                                color = TextMuted,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "“${p.sampleQuoteBn}”",
                                color = PearlWhite.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }
            }
        }
    }
}
