package com.arifa.assistant.ui.screens.history

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.arifa.assistant.core.storage.ConversationRepository
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.ErrorRed
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onSelectSession: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { ConversationRepository.getInstance(context) }

    val sessions by repo.sessions.collectAsState()
    val currentSession by repo.currentSession.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) sessions else {
            sessions.filter { it.title.contains(searchQuery, ignoreCase = true) }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

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
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title and New Chat button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Conversation History",
                    color = PearlWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = CircleShape,
                    color = LagoonCyan,
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                repo.createNewSession("New Conversation")
                                onSelectSession()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Session",
                            tint = NightBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search conversations...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LagoonCyan,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = PearlWhite,
                    unfocusedTextColor = PearlWhite,
                    focusedContainerColor = NightSurfaceVariant,
                    unfocusedContainerColor = NightSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Sessions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filtered.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No conversation history found.",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                items(filtered, key = { it.id }) { session ->
                    val isSelected = session.id == currentSession?.id
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isSelected) LagoonCyan else GlassBorder,
                        onClick = {
                            scope.launch {
                                repo.selectSession(session.id)
                                onSelectSession()
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) LagoonCyan else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = session.title,
                                        color = PearlWhite,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${session.messages.size} messages • ${dateFormatter.format(Date(session.updatedAt))}",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    scope.launch {
                                        repo.deleteSession(session.id)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = ErrorRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
