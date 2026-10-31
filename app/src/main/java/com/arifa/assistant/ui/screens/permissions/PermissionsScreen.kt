package com.arifa.assistant.ui.screens.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.arifa.assistant.core.flavor.AutomationProvider
import com.arifa.assistant.ui.components.GlassCard
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.SuccessGreen
import com.arifa.assistant.ui.theme.TextMuted

@Composable
fun PermissionsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasContactsPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasCallPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
    }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasAudioPermission = isGranted
    }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasContactsPermission = isGranted
    }
    val callLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasCallPermission = isGranted
    }

    val isAutomationSupported = remember { AutomationProvider.instance.isSupported() }
    val isAccessibilityEnabled = remember { AutomationProvider.instance.isServiceEnabled() }

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
                    text = "App Permissions",
                    color = PearlWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Microphone
            PermissionCard(
                title = "Microphone (Required)",
                subtitle = "Enables real-time full-duplex voice conversation with Arifa.",
                isGranted = hasAudioPermission,
                onRequest = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Contacts
            PermissionCard(
                title = "Contacts (Optional)",
                subtitle = "Allows Arifa to find phone numbers when you say 'Call Dad'.",
                isGranted = hasContactsPermission,
                onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Phone Calls
            PermissionCard(
                title = "Phone Calls (Optional)",
                subtitle = "Allows direct placing of phone calls via voice commands.",
                isGranted = hasCallPermission,
                onRequest = { callLauncher.launch(Manifest.permission.CALL_PHONE) }
            )

            // 4. Accessibility (Direct Only)
            if (isAutomationSupported) {
                Spacer(modifier = Modifier.height(12.dp))
                PermissionCard(
                    title = "Screen Automation (Direct Edition)",
                    subtitle = "Allows Arifa to visually navigate and assist on social apps.",
                    isGranted = isAccessibilityEnabled,
                    onRequest = { AutomationProvider.instance.openAccessibilitySettings() }
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isGranted) SuccessGreen else Marigold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = title, color = PearlWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(text = "Allow", color = NightBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(text = "Granted", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
