package com.humboshot.koillection

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

class AppState(
    val navController: NavController
) {
    private val userContext: UserContext = UserContext()

    var isAuthenticated by mutableStateOf(checkAuthenticatedState())
        private set

    private fun checkAuthenticatedState(): Boolean {
        return userContext.jwt.isNotEmpty()
    }

    fun singedIn() {
        isAuthenticated = true
    }

    fun signOut() {
        userContext.removeUserFromStorage()
        isAuthenticated = false
    }
}

@Composable
fun rememberAppState(
    navController: NavController,
    context: Context = LocalContext.current
) = remember(navController, context) {
    AppState(navController)
}