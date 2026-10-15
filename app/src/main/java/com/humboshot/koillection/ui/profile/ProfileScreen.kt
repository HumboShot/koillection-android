package com.humboshot.koillection.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.humboshot.koillection.BuildConfig
import com.humboshot.koillection.R
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.data.user.User
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.common.ScreenshotMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

/** The account details from the server, and the settings of this app. */
@Composable
fun ProfileScreen(
    user: User?,
    serverHost: String?,
    onSignOut: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val fingerprintUnlock by AuthStore.fingerprintUnlock.collectAsStateWithLifecycle()
    val canUseFingerprint = AuthStore.isDeviceSecure

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val avatarModifier = Modifier.size(64.dp).clip(CircleShape)
            if (user?.avatar != null) {
                KoillectionImage(path = user.avatar, contentDescription = null, modifier = avatarModifier)
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = avatarModifier)
            }
            Column {
                Text(user?.username ?: ScreenshotMode.username(AuthStore.username).orEmpty(), style = MaterialTheme.typography.headlineSmall)
                user?.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Account", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            ProfileValue("Server", serverHost)
            ProfileValue("Currency", user?.currency)
            ProfileValue("Language", user?.locale?.let { Locale.forLanguageTag(it).displayName })
            ProfileValue("Time zone", user?.timezone)
            ProfileValue("Profile visibility", user?.visibility?.replaceFirstChar { it.uppercase() })
            Text(
                text = "Change these settings on the web.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("App settings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Unlock with fingerprint", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = if (canUseFingerprint) {
                            "Ask for the fingerprint or screen lock when the app starts. This protects the saved password."
                        } else {
                            "Set a screen lock (PIN, pattern or password) on this device to use this."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = fingerprintUnlock && canUseFingerprint,
                    enabled = canUseFingerprint,
                    onCheckedChange = { enabled -> scope.launch(Dispatchers.IO) { AuthStore.setFingerprintUnlock(enabled) } },
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        HorizontalDivider()

        AboutSection()

        HorizontalDivider()

        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Log out")
        }
    }
}

@Composable
private fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("About", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        ProfileValue("App version", BuildConfig.VERSION_NAME)
        Text(
            text = "${stringResource(R.string.app_name)} is an unofficial app for Koillection. It is not affiliated with the Koillection project.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Icons: Material Icons by Google, Apache License 2.0",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier.clickable { uriHandler.openUri(APACHE_LICENSE_URL) }
        )
    }
}

private const val APACHE_LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0"

@Composable
private fun ProfileValue(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
