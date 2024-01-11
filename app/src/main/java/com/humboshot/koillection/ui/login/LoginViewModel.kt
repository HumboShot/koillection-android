package com.humboshot.koillection.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
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

    fun login(username: String, password: String, domain: String) {
        viewModelScope.launch {
            loggingIn.value = true
            //TODO: Call API service
        }
    }
}

data class LoginViewState(
    val loggingIn: Boolean = false,
    val errorMessage: String = ""
)