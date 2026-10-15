package com.humboshot.koillection.ui.tag

import androidx.compose.ui.graphics.Color

/** Reads `#rrggbb` or `rrggbb`. Returns null for any other value. */
fun parseHexColor(value: String?): Color? {
    val hex = value?.trim()?.removePrefix("#") ?: return null
    if (hex.length != 6) return null
    val rgb = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000 or rgb)
}
