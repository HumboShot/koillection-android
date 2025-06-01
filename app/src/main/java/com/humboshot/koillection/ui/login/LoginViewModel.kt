package com.humboshot.koillection.ui.login

import android.util.Log
import android.util.Patterns
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

            val validatedDomain = validateWebsite(domain)
            // Temporary set the domain in the UserContext
            UserContext().setDomain(validatedDomain)

            val result = ApiClient.getInstance().create(LoginService::class.java).login(AuthenticationRequest(username, password))
            if (result.isSuccessful) {
                result.body()?.let {
                    if (it.token.isEmpty()) {
                        //TODO: Handle empty string

                    } else {
                        UserContext.instance.setUser(username, password, validatedDomain, it.token)
                    }
                }
            } else {
                loggingIn.value = false
            }

            didLogin(result.isSuccessful)
        }
    }

    private fun validateWebsite(website: String): String {
        var validWebsite = website
        if (!Patterns.WEB_URL.matcher(website).matches()) {
            // If the URL is not valid, set an error message.
            // You might want to handle this error more explicitly,
            // perhaps by returning a null or throwing an exception,
            // depending on your desired error handling strategy.
            errorMessage.value = "Invalid URL format"
        } else {
            // If the URL is valid, ensure it starts with https://
            if (!website.startsWith("https://")) {
                Log.d(this.toString(), "Website must start with https://")
                validWebsite = "https://$website"
            }
            // Clear any previous error message if the URL is valid
            errorMessage.value = ""
        }
        return validWebsite
    }
}

data class LoginViewState(
    val loggingIn: Boolean = false,
    val errorMessage: String = ""
)