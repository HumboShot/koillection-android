package com.humboshot.koillection

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.humboshot.koillection.theme.KoillectionTheme

@Composable
fun KoillectionApp(
    navHostController: NavHostController = rememberNavController(),
    appState: AppState = rememberAppState(navHostController)
) {
    if (appState.isAuthenticated) {
        Scaffold { innerPadding ->
            AppNavHost(navHostController, appState, Modifier.padding(innerPadding))
        }
    } else {
        Scaffold { innerPadding ->
            LoginNavHost(navHostController, appState, Modifier.padding(innerPadding))
        }
    }
}