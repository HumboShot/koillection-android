package com.humboshot.koillection.ui.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.UserContext
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.data.login.AuthenticationRequest
import com.humboshot.koillection.extension.normalizeServerUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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

    fun login(username: String, domain: String, didLogin: (Boolean) -> Unit) {
        val validatedDomain = domain.normalizeServerUrl()
        if (validatedDomain == null) {
            errorMessage.value = "Invalid server address"
            didLogin(false)
            return
        }

        viewModelScope.launch {
            loggingIn.value = true
            errorMessage.value = ""

            // Temporary set the domain in the UserContext
            UserContext().setDomain(validatedDomain)

            val password: String = passwordFieldState.text.toString()

            val apiClient = ApiClient.getInstance().create(LoginService::class.java)
            apiClient.login(AuthenticationRequest(username, password))
                .onSuccess {
                    UserContext.instance.setUser(username, password, validatedDomain, it.token)
                    loggingIn.value = false
                    didLogin(true)
                }
                .onFailure {
                    errorMessage.value = it.message ?: "Login failed"
                    loggingIn.value = false
                    didLogin(false)
                }
        }
    }
}

data class LoginViewState(
    val loggingIn: Boolean = false,
    val errorMessage: String = ""
)