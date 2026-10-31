package com.arifa.assistant.ui.screens.orb

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
import com.arifa.assistant.core.storage.OrbStyle
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.components.ArifaOrbView
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun OrbStyleScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val settings by settingsManager.settings.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                Text(
                    text = "3D Orb Themes",
                    color = PearlWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Interactive Preview of Currently Selected Orb
            ArifaOrbView(
                size = 190.dp,
                state = VoiceState.SPEAKING,
                amplitude = 0.3f,
                orbStyle = settings.orbStyle,
                reducedMotion = settings.reducedMotion
            )

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(OrbStyle.values(), key = { it.name }) { style ->
                    val isSelected = style == settings.orbStyle
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isSelected) LagoonCyan else GlassBorder,
                        borderWidth = if (isSelected) 2.dp else 1.dp,
                        onClick = { settingsManager.updateOrbStyle(style) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.displayName,
                                    color = if (isSelected) LagoonCyan else PearlWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = style.description,
                                    color = TextMuted,
                                    fontSize = 12.sp
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
                }
            }
        }
    }
}
