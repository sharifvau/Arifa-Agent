package com.arifa.assistant.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.arifa.assistant.core.storage.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Marigold,
    onPrimary = NightBackground,
    primaryContainer = NightSurfaceVariant,
    onPrimaryContainer = PearlWhite,
    secondary = LagoonCyan,
    onSecondary = NightBackground,
    tertiary = LotusPink,
    onTertiary = NightBackground,
    background = NightBackground,
    onBackground = PearlWhite,
    surface = NightSurface,
    onSurface = PearlWhite,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = TextMuted,
    outline = GlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = MarigoldDark,
    onPrimary = PearlWhite,
    primaryContainer = DaySurfaceVariant,
    onPrimaryContainer = NightBackground,
    secondary = LagoonTeal,
    onSecondary = PearlWhite,
    tertiary = LotusPinkDark,
    onTertiary = PearlWhite,
    background = DayBackground,
    onBackground = NightBackground,
    surface = DaySurface,
    onSurface = NightBackground,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = NightSurfaceHighlight,
    outline = GlassBorderSubtle
)

@Composable
fun ArifaAITheme(
    appTheme: AppTheme = AppTheme.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
