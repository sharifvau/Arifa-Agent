package com.arifa.assistant.ui.screens.websiteforge

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.arifa.assistant.core.websiteforge.WebsiteForgeEngine
import com.arifa.assistant.ui.theme.GlassBorder
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.NightBackground
import com.arifa.assistant.ui.theme.NightSurface
import com.arifa.assistant.ui.theme.NightSurfaceVariant
import com.arifa.assistant.ui.theme.PearlWhite
import com.arifa.assistant.ui.theme.TextMuted
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebsiteViewerScreen(
    projectId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { WebsiteForgeEngine.getInstance(context) }

    val projects by engine.projects.collectAsState()
    val project = projects.find { it.id == projectId }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: 3D Preview, 1: Source Code
    var editableCode by remember(project?.htmlCode) { mutableStateOf(project?.htmlCode ?: "") }
    var isDesktopMode by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NightBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = PearlWhite)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = project?.title ?: "Website Preview",
                            color = PearlWhite,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Three.js 3D Engine Live",
                            color = LagoonCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Share HTML intent
                    IconButton(
                        onClick = {
                            project?.let {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, it.htmlCode)
                                    putExtra(Intent.EXTRA_SUBJECT, "${it.title}.html")
                                    type = "text/html"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Website HTML"))
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = PearlWhite)
                    }
                }
            }

            // Tabs: 3D Live Preview vs Source Code
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = NightSurfaceVariant,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = LagoonCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, tint = if (selectedTab == 0) LagoonCyan else TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3D Live Preview", color = if (selectedTab == 0) LagoonCyan else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = if (selectedTab == 1) LagoonCyan else TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inspect Code", color = if (selectedTab == 1) LagoonCyan else TextMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                // Live Android WebView with Three.js rendering
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            webChromeClient = WebChromeClient()
                            webViewClient = WebViewClient()

                            val html = project?.htmlCode ?: "<html><body><h3>Loading 3D Scene...</h3></body></html>"
                            loadDataWithBaseURL("https://cdnjs.cloudflare.com/", html, "text/html", "UTF-8", null)
                        }
                    },
                    update = { webView ->
                        val html = project?.htmlCode ?: ""
                        webView.loadDataWithBaseURL("https://cdnjs.cloudflare.com/", html, "text/html", "UTF-8", null)
                    }
                )
            } else {
                // Source Code Editor / Inspector
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = editableCode,
                        onValueChange = { editableCode = it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LagoonCyan,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = PearlWhite,
                            unfocusedTextColor = PearlWhite,
                            focusedContainerColor = NightSurface,
                            unfocusedContainerColor = NightSurface
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            project?.let {
                                scope.launch {
                                    engine.updateProjectCode(it.id, editableCode)
                                    selectedTab = 0 // Switch back to live preview
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LagoonCyan),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Save & Apply Changes", color = NightBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
