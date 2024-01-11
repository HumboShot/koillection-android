package com.humboshot.koillection

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.humboshot.koillection.destinations.Login
import com.humboshot.koillection.ui.login.LoginScreen

@Composable
fun AppNavHost(
    navHostController: NavHostController,
    appState: AppState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navHostController,
        startDestination = Login.route,
        modifier
    ) {
        composable(route = Login.route) {
            LoginScreen()
        }
    }
}