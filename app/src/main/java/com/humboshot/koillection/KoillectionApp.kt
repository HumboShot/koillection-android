package com.humboshot.koillection

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.humboshot.koillection.destinations.MainCollection
import com.humboshot.koillection.destinations.TITLE_ARG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoillectionApp(
    navHostController: NavHostController = rememberNavController(),
    appState: AppState = rememberAppState(navHostController)
) {
    if (appState.isAuthenticated) {
        val backStackEntry by navHostController.currentBackStackEntryAsState()
        val isRoot = backStackEntry?.destination?.route == MainCollection.route
        val title = backStackEntry?.arguments?.getString(TITLE_ARG) ?: MainCollection.title

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    navigationIcon = {
                        if (!isRoot) {
                            IconButton(onClick = { navHostController.navigateUp() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    actions = {
                        if (isRoot) {
                            IconButton(onClick = { appState.signOut() }) {
                                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Log out")
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            AppNavHost(navHostController, appState, Modifier.padding(innerPadding))
        }
    } else {
        Scaffold { innerPadding ->
            LoginNavHost(navHostController, appState, Modifier.padding(innerPadding))
        }
    }
}
