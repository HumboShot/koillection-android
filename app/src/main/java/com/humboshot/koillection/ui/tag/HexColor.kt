package com.humboshot.koillection.ui.tag

import androidx.compose.ui.graphics.Color

private val HEX = Regex("^[0-9a-fA-F]{6}$")

/** Reads `#rrggbb` or `rrggbb`. Returns null for any other value. */
fun parseHexColor(value: String?): Color? {
    val hex = value?.trim()?.removePrefix("#") ?: return null
    if (!HEX.matches(hex)) return null
    return Color(0xFF000000 or hex.toLong(16))
}
