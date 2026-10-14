package com.humboshot.koillection.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.extension.normalizeServerUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(val passwordFieldState: TextFieldState = TextFieldState()) : ViewModel() {
    private val loggingIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val errorMessage: MutableStateFlow<String> = MutableStateFlow("")

    private val _state = MutableStateFlow(LoginViewState())
    val state: StateFlow<LoginViewState>
        get() = _state

    init {
        viewModelScope.launch {
            combine(
                loggingIn,
                errorMessage
            ) { loggingIn, errorMessage ->
                LoginViewState(
                    loggingIn,
                    errorMessage
                )
            }.catch { throwable ->
                throw throwable
            }.collect {
                _state.value = it
            }
        }
    }

    fun login(username: String, domain: String) {
        val validatedDomain = domain.normalizeServerUrl()
        if (validatedDomain == null) {
            errorMessage.value = "Invalid server address"
            return
        }

        viewModelScope.launch {
            loggingIn.value = true
            errorMessage.value = ""

            val password: String = passwordFieldState.text.toString()

            ApiClient.loginService(validatedDomain).login(AuthenticationRequest(username, password))
                .mapCatching {
                    withContext(Dispatchers.IO) { AuthStore.signIn(validatedDomain, username, password, it.token) }
                }
                .onSuccess {
                    loggingIn.value = false
                }
                .onFailure {
                    errorMessage.value = it.message ?: "Login failed"
                    loggingIn.value = false
                }
        }
    }
}

data class LoginViewState(
    val loggingIn: Boolean = false,
    val errorMessage: String = ""
)