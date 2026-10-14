package com.humboshot.koillection.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.SecureTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.AppState
import com.humboshot.koillection.R

@Composable
fun LoginScreen(
    appState: AppState,
    viewModel: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val uiState by viewModel.state.collectAsStateWithLifecycle()

        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var domain by remember { mutableStateOf("") }

        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding()
        ) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_foreground), contentDescription = null)
        }

        Text(
            stringResource(R.string.login_sign_in),
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium
        )

        if (uiState.loggingIn) {
            CircularProgressIndicator()
        } else {
            TextField(
                value = username,
                modifier = Modifier.fillMaxWidth(),
                label = {
                        Text(text = "Username")
                },
                onValueChange = { username = it }
            )

            SecureTextField(
                state = viewModel.passwordFieldState,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Password")
                },
            )

            TextField(
                value = domain,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(text = "Domain")
                },
                onValueChange = { domain = it },
                singleLine = true,
                placeholder = { Text("collection.example.com") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Uri
                ),
                isError = uiState.errorMessage.isNotEmpty()
            )

            if (uiState.errorMessage.isNotEmpty()) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    viewModel.login(username, domain)
                }
            ) {
                Text(stringResource(R.string.login_login_button))
            }
        }

    }
}