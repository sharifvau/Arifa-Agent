package com.arifa.assistant.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.Marigold

@Composable
fun EdgeGlowView(
    state: VoiceState,
    reducedMotion: Boolean = false
) {
    val isGlowActive = state == VoiceState.LISTENING || state == VoiceState.SPEAKING || state == VoiceState.CONNECTING

    val infiniteTransition = rememberInfiniteTransition(label = "edge_glow")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 2400 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    AnimatedVisibility(
        visible = isGlowActive,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400))
    ) {
        val glowColor = if (state == VoiceState.SPEAKING) Marigold else LagoonCyan

        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowWidth = 32f
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Top edge glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(glowColor.copy(alpha = alphaAnim), Color.Transparent),
                    startY = 0f,
                    endY = glowWidth
                ),
                topLeft = Offset(0f, 0f),
                size = Size(canvasWidth, glowWidth)
            )

            // Bottom edge glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, glowColor.copy(alpha = alphaAnim)),
                    startY = canvasHeight - glowWidth,
                    endY = canvasHeight
                ),
                topLeft = Offset(0f, canvasHeight - glowWidth),
                size = Size(canvasWidth, glowWidth)
            )

            // Left edge glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(glowColor.copy(alpha = alphaAnim), Color.Transparent),
                    startX = 0f,
                    endX = glowWidth
                ),
                topLeft = Offset(0f, 0f),
                size = Size(glowWidth, canvasHeight)
            )

            // Right edge glow
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, glowColor.copy(alpha = alphaAnim)),
                    startX = canvasWidth - glowWidth,
                    endX = canvasWidth
                ),
                topLeft = Offset(canvasWidth - glowWidth, 0f),
                size = Size(glowWidth, canvasHeight)
            )
        }
    }
}
