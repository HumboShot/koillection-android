package com.humboshot.koillection.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Colors from the Koillection web themes (assets/styles/themes/light.css and dark.css).
val KoillectionTeal = Color(0xFF009688)
val KoillectionTealLight = Color(0xFF1AB0A2)
val KoillectionTealLightest = Color(0xFF80CBC4)
val KoillectionTealDark = Color(0xFF006355)
val KoillectionRed = Color(0xFFE74646)
val KoillectionWarning = Color(0xFFE53935)

/** Colors for the parts of the web layout that Material 3 has no role for. */
@Immutable
data class KoillectionColors(
    val header: Color,
    val onHeader: Color,
    val sidebar: Color,
    val onSidebar: Color,
    val sidebarLogo: Color,
    val sidebarSelected: Color,
    val sidebarDivider: Color,
)

val LightKoillectionColors = KoillectionColors(
    header = Color(0xFFF5F7FA),
    onHeader = Color(0xDE000000),
    sidebar = KoillectionTeal,
    onSidebar = Color(0xFFF0F2F5),
    sidebarLogo = KoillectionTealLight,
    sidebarSelected = KoillectionTealDark,
    sidebarDivider = KoillectionTealLight,
)

val DarkKoillectionColors = KoillectionColors(
    header = Color(0xFF222222),
    onHeader = Color(0xFFCCCCCC),
    sidebar = Color(0xFF222222),
    onSidebar = Color(0xFFCCCCCC),
    sidebarLogo = Color(0xFF2A2A2A),
    sidebarSelected = KoillectionTeal,
    sidebarDivider = Color(0xFF7D7F82),
)

val LocalKoillectionColors = staticCompositionLocalOf { LightKoillectionColors }
