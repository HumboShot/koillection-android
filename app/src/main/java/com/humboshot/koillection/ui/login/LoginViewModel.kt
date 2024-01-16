package com.humboshot.koillection.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humboshot.koillection.UserContext
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.LoginService
import com.humboshot.koillection.data.login.AuthenticationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import retrofit2.http.Url

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

    fun login(username: String, password: String, domain: String, didLogin: (Boolean) -> Unit) {
        viewModelScope.launch {
            loggingIn.value = true

            // Temporary set the domain in the UserContext
            UserContext().setDomain(domain)

            val result = ApiClient.getInstance().create(LoginService::class.java).login(AuthenticationRequest(username, password))
            if (result.isSuccessful) {
                result.body()?.let {
                    UserContext.instance.setUser(username, password, domain, it.token)
                }
            } else {
                loggingIn.value = false
            }

            didLogin(result.isSuccessful)
        }
    }
}

data class LoginViewState(
    val loggingIn: Boolean = false,
    val errorMessage: String = ""
)