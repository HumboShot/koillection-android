package com.humboshot.koillection.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// The web uses #9e9e9e (light) and #7d7f82 (dark) for secondary text. Those are too faint to read
// on a phone, so onSurfaceVariant is a little stronger. The web values stay for outlines.
private val LightColorScheme = lightColorScheme(
    primary = KoillectionTeal,
    onPrimary = Color.White,
    // Web buttons are solid teal with white text, so the floating buttons use the same.
    primaryContainer = KoillectionTeal,
    onPrimaryContainer = Color.White,
    secondary = KoillectionRed,
    onSecondary = Color.White,
    // Selected segments and chips use secondaryContainer. Teal tints match the web.
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = KoillectionTealDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF00201C),
    background = Color.White,
    onBackground = Color(0xDE000000),
    surface = Color.White,
    onSurface = Color(0xDE000000),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF616161),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFBFC),
    surfaceContainer = Color(0xFFF5F7FA),
    surfaceContainerHigh = Color(0xFFEEF1F5),
    surfaceContainerHighest = Color(0xFFE8EBF0),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFE0E3E8),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF0F2F5),
    inversePrimary = KoillectionTealLightest,
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFFE0E0E0),
    error = KoillectionWarning,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColorScheme = darkColorScheme(
    primary = KoillectionTeal,
    onPrimary = Color.White,
    primaryContainer = KoillectionTeal,
    onPrimaryContainer = Color.White,
    secondary = KoillectionTeal,
    onSecondary = Color.White,
    secondaryContainer = KoillectionTealDark,
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = KoillectionTealLight,
    onTertiary = Color.White,
    tertiaryContainer = KoillectionTealDark,
    onTertiaryContainer = Color(0xFFB2DFDB),
    background = Color(0xFF2A2A2A),
    onBackground = Color(0xFFCCCCCC),
    surface = Color(0xFF2A2A2A),
    onSurface = Color(0xFFCCCCCC),
    surfaceVariant = Color(0xFF3A3A3A),
    onSurfaceVariant = Color(0xFF9A9C9F),
    surfaceContainerLowest = Color(0xFF1E1E1E),
    surfaceContainerLow = Color(0xFF222222),
    surfaceContainer = Color(0xFF262626),
    surfaceContainerHigh = Color(0xFF303030),
    surfaceContainerHighest = Color(0xFF383838),
    surfaceBright = Color(0xFF3A3A3A),
    surfaceDim = Color(0xFF1E1E1E),
    inverseSurface = Color(0xFFE6E6E6),
    inverseOnSurface = Color(0xFF2A2A2A),
    inversePrimary = KoillectionTealDark,
    outline = Color(0xFF7D7F82),
    outlineVariant = Color(0xFF3F3F3F),
    error = KoillectionWarning,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

/** Uses the Koillection web colors. Light or dark follows the phone. Wallpaper colors are not used. */
@Composable
fun KoillectionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalKoillectionColors provides if (darkTheme) DarkKoillectionColors else LightKoillectionColors
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

object KoillectionTheme {
    val colors: KoillectionColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKoillectionColors.current
}
