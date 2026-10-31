package com.arifa.assistant.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.Marigold

@Composable
fun WaveformView(
    modifier: Modifier = Modifier,
    amplitude: Float = 0.0f,
    barCount: Int = 7,
    maxHeight: Dp = 42.dp,
    barWidth: Dp = 4.dp,
    activeColor: Color = LagoonCyan
) {
    val multipliers = listOf(0.4f, 0.7f, 1.0f, 0.85f, 1.2f, 0.6f, 0.35f)

    Row(
        modifier = modifier.height(maxHeight),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val mult = multipliers[i % multipliers.size]
            val targetFraction = (0.12f + (amplitude * mult * 0.88f)).coerceIn(0.1f, 1.0f)

            val animatedHeightFraction by animateFloatAsState(
                targetValue = targetFraction,
                animationSpec = tween(durationMillis = 80),
                label = "bar_height"
            )

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(animatedHeightFraction)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                activeColor,
                                Marigold.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }
    }
}
