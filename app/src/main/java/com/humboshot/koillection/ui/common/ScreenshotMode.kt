package com.humboshot.koillection.ui.common

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.humboshot.koillection.BuildConfig
import com.humboshot.koillection.R
import com.humboshot.koillection.data.user.User

/**
 * Shows sample account details instead of the real ones, and placeholders instead of the
 * server images, for store screenshots. Item images are often covers that belong to publishers.
 * Only debug builds read the flag: `adb shell am start -n <package>/.MainActivity --ez screenshot_mode true`.
 * docs/play-store/screenshots.py starts the app this way.
 */
object ScreenshotMode {
    private const val EXTRA = "screenshot_mode"
    private const val USERNAME = "collector"
    private const val HOST = "collection.example.com"

    var enabled: Boolean = false
        private set

    fun enableFrom(intent: Intent?) {
        if (BuildConfig.DEBUG && intent?.getBooleanExtra(EXTRA, false) == true) enabled = true
    }

    fun user(user: User?): User? = if (!enabled) user else user?.copy(
        username = USERNAME,
        email = "$USERNAME@example.com",
        avatar = null,
        currency = "EUR",
        timezone = "UTC",
    )

    fun username(username: String?): String? = if (enabled) USERNAME else username

    fun host(host: String?): String? = if (enabled) HOST else host

    private val placeholderColors = listOf(
        Color(0xFF26A69A), Color(0xFF5C6BC0), Color(0xFFEF8A62), Color(0xFF8D6E63),
        Color(0xFF7E57C2), Color(0xFF66BB6A), Color(0xFFEC407A), Color(0xFF42A5F5),
    )

    /** A colored tile with the app symbol. The same image path always gets the same color. */
    @Composable
    fun ImagePlaceholder(path: String, modifier: Modifier) {
        // Fibonacci hashing spreads similar paths over the 8 colors better than the plain hash code.
        val color = placeholderColors[(path.hashCode() * -0x61c88647) ushr 29]
        Box(contentAlignment = Alignment.Center, modifier = modifier.background(color)) {
            Icon(
                painter = painterResource(R.drawable.ic_collections),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxSize(0.35f)
            )
        }
    }
}
