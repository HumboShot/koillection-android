package com.humboshot.koillection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.humboshot.koillection.auth.AuthStore
import kotlinx.coroutines.flow.StateFlow

class AppState(
    val navController: NavController
) {
    /** Also turns false when the server rejects the saved password during a token refresh. */
    val isAuthenticated: StateFlow<Boolean> = AuthStore.isLoggedIn

    fun signOut() = AuthStore.signOut()
}

@Composable
fun rememberAppState(navController: NavController) = remember(navController) {
    AppState(navController)
}
