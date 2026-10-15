package com.humboshot.koillection.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.extension.normalizeServerUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class LoginViewModel : ViewModel() {
    // The password lives only in this ViewModel, not in the saved instance state.
    private val _state = MutableStateFlow(LoginViewState(server = AuthStore.lastServer?.toServerInput().orEmpty()))
    val state: StateFlow<LoginViewState> = _state.asStateFlow()

    fun onServerChange(server: String) = _state.update { it.copy(server = server, error = null) }

    fun onUsernameChange(username: String) = _state.update { it.copy(username = username, error = null) }

    fun onPasswordChange(password: String) = _state.update { it.copy(password = password, error = null) }

    fun togglePasswordVisible() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun login() {
        val current = _state.value
        if (current.isLoggingIn || !current.canLogin) return
        val serverUrl = current.server.normalizeServerUrl()
        if (serverUrl == null) {
            _state.update { it.copy(error = "Enter a valid server address, for example collection.example.com") }
            return
        }

        // Keyboards often add a space after a suggested word. The password stays as typed.
        val request = AuthenticationRequest(current.username.trim(), current.password)

        viewModelScope.launch {
            _state.update { it.copy(isLoggingIn = true, error = null) }
            withContext(Dispatchers.IO) {
                runCatching {
                    val response = ApiClient.loginService(serverUrl).loginCall(request).execute()
                    val token = response.body()?.token
                    when {
                        response.code() == 401 -> error("Wrong username or password")
                        !response.isSuccessful || token == null -> error("Login failed (HTTP ${response.code()})")
                        else -> AuthStore.signIn(serverUrl, request.username, request.password, token)
                    }
                }
            }.onFailure { throwable ->
                val message = if (throwable is IOException) "Cannot reach the server" else throwable.message ?: "Login failed"
                _state.update { it.copy(error = message) }
            }
            _state.update { it.copy(isLoggingIn = false) }
        }
    }
}

/** Shows a saved server URL the way the user types it: no "https://" and no "/" at the end. */
internal fun String.toServerInput(): String = removePrefix("https://").trimEnd('/')

data class LoginViewState(
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoggingIn: Boolean = false,
    val error: String? = null,
) {
    val canLogin: Boolean get() = server.isNotBlank() && username.isNotBlank() && password.isNotEmpty()
}
