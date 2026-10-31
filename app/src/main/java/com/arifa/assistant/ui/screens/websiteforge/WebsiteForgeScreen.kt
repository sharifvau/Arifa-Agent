package com.arifa.assistant.ui.screens.websiteforge

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.websiteforge.BuildProgressStep
import com.arifa.assistant.core.websiteforge.WebsiteForgeEngine
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.SuccessGreen
import com.arifa.assistant.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun WebsiteForgeScreen(
    initialPrompt: String = "",
    onNavigateBack: () -> Unit,
    onOpenViewer: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { WebsiteForgeEngine.getInstance(context) }

    val projects by engine.projects.collectAsState()
    val isBuilding by engine.isBuilding.collectAsState()
    val currentStep by engine.currentStep.collectAsState()

    var promptInput by remember { mutableStateOf(initialPrompt.ifBlank { "Arifa, amar jonno ekta restaurant er website banao." }) }

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

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PearlWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Website Forge 3D",
                        color = PearlWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI-Powered Three.js & CSS 3D Generator",
                        color = Marigold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prompt Input Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(text = "Describe Your 3D Website", color = PearlWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { promptInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LagoonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = PearlWhite,
                            unfocusedTextColor = PearlWhite,
                            focusedContainerColor = NightSurfaceVariant,
                            unfocusedContainerColor = NightSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (promptInput.isNotBlank() && !isBuilding) {
                                scope.launch {
                                    val project = engine.generateWebsite(promptInput)
                                    onOpenViewer(project.id)
                                }
                            }
                        },
                        enabled = !isBuilding && promptInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isBuilding) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NightBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Synthesizing 3D Code...", color = NightBackground, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NightBackground, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Forge Website Now", color = NightBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live Building Step Progress Indicator
            AnimatedVisibility(visible = isBuilding && currentStep != null) {
                currentStep?.let { step ->
                    Spacer(modifier = Modifier.height(14.dp))
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = Marigold
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = step.title, color = Marigold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${(step.progress * 100).toInt()}%", color = LagoonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = step.subtitle, color = TextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { step.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LagoonCyan,
                                trackColor = NightSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Generated Projects (${projects.size})",
                color = PearlWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Projects List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(projects, key = { it.id }) { proj ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenViewer(proj.id) }
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Web, contentDescription = null, tint = LagoonCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(text = proj.title, color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }

                                IconButton(
                                    onClick = { scope.launch { engine.deleteProject(proj.id) } },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = proj.prompt, color = TextMuted, fontSize = 12.sp, maxLines = 2)

                            // Detected Sections
                            if (proj.detectedSections.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(proj.detectedSections) { sec ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = NightSurfaceVariant
                                        ) {
                                            Text(
                                                text = sec,
                                                color = LagoonCyan,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
    }
}
