package com.humboshot.koillection.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
) {
    Column(
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

        Text("Sign in", Modifier.fillMaxWidth())

        if (uiState.loggingIn) {
            CircularProgressIndicator()
        } else {
            TextField(value = username, onValueChange = { username = it })
            TextField(value = password, onValueChange = { password = it })
            TextField(value = domain, onValueChange = { domain = it })

            Button(
                onClick = {
                    viewModel.login(username, password, domain)
                }
            ) {

            }
        }

    }
}