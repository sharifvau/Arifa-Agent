package com.arifa.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arifa.assistant.core.storage.OrbStyle
import com.arifa.assistant.core.voice.VoiceState
import com.arifa.assistant.ui.theme.LagoonCyan
import com.arifa.assistant.ui.theme.LotusPink
import com.arifa.assistant.ui.theme.Marigold
import com.arifa.assistant.ui.theme.PearlWhite
import kotlin.math.cos
import kotlin.math.sin

data class OrbPalette(
    val coreColor: Color,
    val middleColor: Color,
    val outerGlow: Color,
    val ringColor: Color,
    val highlightColor: Color
)

object OrbThemes {
    fun getPalette(style: OrbStyle): OrbPalette = when (style) {
        OrbStyle.PEARL_CLASSIC -> OrbPalette(
            coreColor = PearlWhite,
            middleColor = LotusPink.copy(alpha = 0.85f),
            outerGlow = LagoonCyan.copy(alpha = 0.45f),
            ringColor = LagoonCyan.copy(alpha = 0.6f),
            highlightColor = Color.White
        )
        OrbStyle.NEON_CYBER -> OrbPalette(
            coreColor = LagoonCyan,
            middleColor = Color(0xFF00B0FF),
            outerGlow = Marigold.copy(alpha = 0.6f),
            ringColor = Marigold,
            highlightColor = Color(0xFFE0F7FA)
        )
        OrbStyle.SOLAR_FLARE -> OrbPalette(
            coreColor = Color(0xFFFFF176),
            middleColor = Marigold,
            outerGlow = Color(0xFFFF5722).copy(alpha = 0.65f),
            ringColor = Color(0xFFFF9100),
            highlightColor = Color.White
        )
        OrbStyle.QUANTUM_LOTUS -> OrbPalette(
            coreColor = Color(0xFFFF80AB),
            middleColor = LotusPink,
            outerGlow = Color(0xFF8E24AA).copy(alpha = 0.6f),
            ringColor = Color(0xFFFF4081),
            highlightColor = Color.White
        )
        OrbStyle.CELESTIAL_AURORA -> OrbPalette(
            coreColor = Color(0xFF80DEEA),
            middleColor = Color(0xFF26A69A),
            outerGlow = Color(0xFF7E57C2).copy(alpha = 0.6f),
            ringColor = Color(0xFF80CBC4),
            highlightColor = Color.White
        )
        OrbStyle.DARK_MATTER -> OrbPalette(
            coreColor = Color(0xFF212121),
            middleColor = Color(0xFF37474F),
            outerGlow = Color(0xFF00E5FF).copy(alpha = 0.5f),
            ringColor = Color(0xFF00E5FF),
            highlightColor = Color(0xFFB0BEC5)
        )
        OrbStyle.BIO_PULSE -> OrbPalette(
            coreColor = Color(0xFF69F0AE),
            middleColor = Color(0xFF00E676),
            outerGlow = Color(0xFF00B0FF).copy(alpha = 0.6f),
            ringColor = Color(0xFF00E5FF),
            highlightColor = Color.White
        )
    }
}

@Composable
fun ArifaOrbView(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    state: VoiceState = VoiceState.IDLE,
    amplitude: Float = 0.0f,
    orbStyle: OrbStyle = OrbStyle.PEARL_CLASSIC,
    reducedMotion: Boolean = false,
    onClick: () -> Unit = {}
) {
    val palette = remember(orbStyle) { OrbThemes.getPalette(orbStyle) }

    // Interactive 3D tilt offset from touch drag
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 3000 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (reducedMotion) 18000 else 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_rotation"
    )

    val thinkingSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_spin"
    )

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        tiltX = (tiltX + dragAmount.x * 0.005f).coerceIn(-0.35f, 0.35f)
                        tiltY = (tiltY + dragAmount.y * 0.005f).coerceIn(-0.35f, 0.35f)
                    },
                    onDragEnd = {
                        tiltX = 0f
                        tiltY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.58f

            // Dynamic reactive radius influenced by speech amplitude
            val reactiveAmp = if (reducedMotion) amplitude * 0.3f else amplitude
            val dynamicRadius = baseRadius * (pulseScale + reactiveAmp * 0.45f)

            // 1. Outer Diffuse Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.outerGlow.copy(alpha = if (state == VoiceState.SPEAKING || state == VoiceState.LISTENING) 0.8f else 0.4f),
                        palette.outerGlow.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 1.6f
                ),
                radius = dynamicRadius * 1.6f,
                center = center
            )

            // 2. Orbital Quantum Rings
            val ringAngle = if (state == VoiceState.THINKING) thinkingSpin else rotationAngle
            drawOrbitalRings(center, dynamicRadius, ringAngle, palette.ringColor, state)

            // 3. 3D Main Sphere Body with Specular Lighting Highlight
            val lightSource = Offset(
                center.x - dynamicRadius * (0.35f - tiltX),
                center.y - dynamicRadius * (0.35f - tiltY)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette.highlightColor.copy(alpha = 0.95f),
                        palette.coreColor,
                        palette.middleColor,
                        palette.coreColor.copy(alpha = 0.7f)
                    ),
                    center = lightSource,
                    radius = dynamicRadius * 1.25f
                ),
                radius = dynamicRadius,
                center = center
            )

            // 4. Subtle Inner Shadow Rim for 3D Depth
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.35f)
                    ),
                    center = center,
                    radius = dynamicRadius
                ),
                radius = dynamicRadius,
                center = center
            )

            // 5. Specular Sheen Reflection
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = dynamicRadius * 0.22f,
                center = Offset(
                    center.x - dynamicRadius * (0.38f - tiltX),
                    center.y - dynamicRadius * (0.38f - tiltY)
                )
            )
        }
    }
}

private fun DrawScope.drawOrbitalRings(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color,
    state: VoiceState
) {
    val ringCount = if (state == VoiceState.SPEAKING) 3 else 2
    for (i in 0 until ringCount) {
        val radOffset = (i * 45f + rotation) * (Math.PI / 180.0)
        val rx = (radius * (1.15f + i * 0.18f))
        val ry = rx * 0.38f

        // Draw ring particles
        for (j in 0 until 4) {
            val particleAngle = radOffset + (j * Math.PI / 2.0)
            val px = center.x + (rx * cos(particleAngle)).toFloat()
            val py = center.y + (ry * sin(particleAngle)).toFloat()
            drawCircle(
                color = color.copy(alpha = 0.75f),
                radius = 3.5f + (i * 1.0f),
                center = Offset(px, py)
            )
        }

        // Draw elliptical orbit path
        drawCircle(
            color = color.copy(alpha = 0.22f),
            radius = rx,
            center = center,
            style = Stroke(width = 1.5f)
        )
    }
}
