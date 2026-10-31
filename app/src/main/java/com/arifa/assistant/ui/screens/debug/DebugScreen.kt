package com.arifa.assistant.ui.screens.debug

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.arifa.assistant.BuildConfig
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.voice.VoiceSessionManager
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun DebugScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val voiceManager = remember { VoiceSessionManager.getInstance(context) }
    val personalityManager = remember { PersonalityManager.getInstance(context) }

    val voiceState by voiceManager.voiceState.collectAsState()
    val metrics by voiceManager.metrics.collectAsState()
    val personality by personalityManager.currentPersonality.collectAsState()

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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PearlWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Diagnostics & Latency",
                    color = PearlWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metrics Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Live Telemetry", color = LagoonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    MetricRow("Round-Trip Latency", "${metrics.lastLatencyMs} ms")
                    MetricRow("Active Voice State", voiceState.name)
                    MetricRow("Audio In (Microphone)", "${metrics.totalAudioSentBytes / 1024} KB")
                    MetricRow("Audio Out (Speaker)", "${metrics.totalAudioReceivedBytes / 1024} KB")
                    MetricRow("Active Personality", personality.name)
                    MetricRow("Build Flavor", BuildConfig.FLAVOR_TYPE)
                    MetricRow("Accessibility Support", if (AutomationProvider.instance.isSupported()) "ENABLED (Direct)" else "DISABLED (Play)")
                    MetricRow("Service Active", if (AutomationProvider.instance.isServiceEnabled()) "YES" else "NO")
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label:", color = TextMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = PearlWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
