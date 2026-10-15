package com.humboshot.koillection.ui.login

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.auth.UnlockResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Asks for the fingerprint or the screen lock, so the app can use the saved password. */
@Composable
fun UnlockScreen(onSignOut: () -> Unit) {
    val activity = LocalActivity.current as FragmentActivity
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var prompted by rememberSaveable { mutableStateOf(false) }

    val prompt = remember(activity) {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    scope.launch {
                        when (withContext(Dispatchers.IO) { AuthStore.unlock() }) {
                            UnlockResult.Unlocked, UnlockResult.LoggedOut -> error = null
                            UnlockResult.Failed -> error = "The app could not unlock the saved login. Try again, or use your PIN, pattern or password."
                        }
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    error = errString.toString()
                }
            }
        )
    }
    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Koillection")
            .setSubtitle("Confirm that it is you to use the saved login")
            .setAllowedAuthenticators(allowedAuthenticators())
            .build()
    }

    LaunchedEffect(Unit) {
        if (!prompted) {
            prompted = true
            prompt.authenticate(promptInfo)
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text("Koillection is locked", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Use your fingerprint or screen lock to open the app.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Button(onClick = { prompt.authenticate(promptInfo) }, modifier = Modifier.fillMaxWidth()) {
            Text("Unlock")
        }
        TextButton(onClick = onSignOut) { Text("Log out") }
    }
}

/** Android 9 and 10 do not support a strong biometric together with the screen lock in one prompt. */
private fun allowedAuthenticators(): Int =
    if (Build.VERSION.SDK_INT in Build.VERSION_CODES.P..Build.VERSION_CODES.Q) BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    else BIOMETRIC_STRONG or DEVICE_CREDENTIAL
