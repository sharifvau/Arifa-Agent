package com.arifa.assistant.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arifa.assistant.core.common.Result
import com.arifa.assistant.core.gemini.rest.GeminiRestClient
import com.arifa.assistant.core.language.LanguageDetector
import com.arifa.assistant.core.language.TextToSpeechManager
import com.arifa.assistant.core.personality.PersonalityManager
import com.arifa.assistant.core.storage.ChatMessage
import com.arifa.assistant.core.storage.ConversationRepository
import com.arifa.assistant.core.storage.SettingsManager
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.navigation.NavRoutes
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurface
import com.arifa.assistant.ui.theme.NightSurfaceHighlight
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    onNavigateToVoice: () -> Unit,
    onNavigateToForge: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settingsManager = remember { SettingsManager.getInstance(context) }
    val personalityManager = remember { PersonalityManager.getInstance(context) }
    val conversationRepo = remember { ConversationRepository.getInstance(context) }
    val restClient = remember { GeminiRestClient() }
    val ttsManager = remember { TextToSpeechManager(context) }

    val settings by settingsManager.settings.collectAsState()
    val personality by personalityManager.currentPersonality.collectAsState()
    val currentSession by conversationRepo.currentSession.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val messages = currentSession?.messages ?: emptyList()

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp) // Leave space for FloatingBottomNav
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = settings.assistantName,
                        color = PearlWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${personality.title} • ${settings.language.displayName}",
                        color = Marigold,
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onNavigateToVoice) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Mode",
                        tint = LagoonCyan
                    )
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a conversation in বাংলা, Banglish or English.\nAsk a question or request a 3D website!",
                                color = TextMuted,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Arifa Message", message.content))
                        },
                        onSpeak = {
                            ttsManager.speak(message.content)
                        },
                        onForgeClick = {
                            onNavigateToForge(message.content)
                        }
                    )
                }

                if (isSending) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = LagoonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${settings.assistantName} is thinking...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = { Text("Message ${settings.assistantName}...", color = TextMuted, fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LagoonCyan,
                        unfocusedBorderColor = GlassBorder,
                        focusedTextColor = PearlWhite,
                        unfocusedTextColor = PearlWhite,
                        focusedContainerColor = NightSurfaceVariant,
                        unfocusedContainerColor = NightSurfaceVariant
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = CircleShape,
                    color = if (inputMessage.isNotBlank()) LagoonCyan else NightSurfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            val text = inputMessage.trim()
                            if (text.isNotBlank() && !isSending) {
                                inputMessage = ""
                                val isForge = text.contains("website banao", ignoreCase = true) ||
                                        text.contains("restaurant", ignoreCase = true) && text.contains("website", ignoreCase = true) ||
                                        text.contains("make a website", ignoreCase = true)

                                val userMsg = ChatMessage(
                                    role = "user",
                                    content = text,
                                    isForgeCommand = isForge
                                )

                                scope.launch {
                                    conversationRepo.addMessageToCurrentSession(userMsg)
                                    isSending = true

                                    val instruction = personalityManager.getSystemInstruction()
                                    val currentHistory = conversationRepo.currentSession.value?.messages ?: emptyList()
                                    val result = restClient.generateChatResponse(
                                        apiKey = settings.apiKey,
                                        systemInstruction = instruction,
                                        history = currentHistory,
                                        userMessage = text
                                    )

                                    val replyText = when (result) {
                                        is Result.Success -> result.data
                                        is Result.Error -> "I couldn't reach Gemini right now. ${result.message}"
                                        else -> "No response received."
                                    }

                                    val modelMsg = ChatMessage(
                                        role = "model",
                                        content = replyText,
                                        isForgeCommand = isForge
                                    )
                                    conversationRepo.addMessageToCurrentSession(modelMsg)
                                    isSending = false

                                    if (settings.autoSpeakReplies) {
                                        ttsManager.speak(replyText)
                                    }
                                }
                            }
                        },
                        enabled = inputMessage.isNotBlank() && !isSending
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputMessage.isNotBlank()) NightBackground else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onForgeClick: () -> Unit
) {
    val isUser = message.role == "user"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) NightSurfaceHighlight else NightSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.content,
                    color = PearlWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )

                // Website Forge Command Detection Banner
                if (message.isForgeCommand && !isUser) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Marigold.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Marigold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onForgeClick)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Marigold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Open 3D Website Forge",
                                color = Marigold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Action Bar for assistant responses
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = onSpeak, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak Aloud",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Text",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
