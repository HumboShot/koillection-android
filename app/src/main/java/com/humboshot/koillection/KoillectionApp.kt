package com.humboshot.koillection

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.humboshot.koillection.auth.AuthStore
import com.humboshot.koillection.destinations.MainCollection
import com.humboshot.koillection.data.user.enabledFeatures
import com.humboshot.koillection.destinations.breadcrumb
import com.humboshot.koillection.destinations.rootRoutes
import com.humboshot.koillection.theme.KoillectionTheme
import com.humboshot.koillection.ui.common.LocalTopBarActions
import com.humboshot.koillection.ui.common.TopBarActionsState
import com.humboshot.koillection.ui.common.clearImageCache
import com.humboshot.koillection.ui.navigation.AppDrawer
import com.humboshot.koillection.ui.navigation.SessionViewModel
import com.humboshot.koillection.ui.navigation.drawerEntries
import com.humboshot.koillection.ui.navigation.Breadcrumb
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoillectionApp(
    navHostController: NavHostController = rememberNavController(),
    appState: AppState = rememberAppState(navHostController)
) {
    val isAuthenticated by appState.isAuthenticated.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated == false) context.clearImageCache()
    }

    // The saved login loads on a background thread at start. Show only the window background until then.
    if (isAuthenticated ?: return) {
        val backStackEntry by navHostController.currentBackStackEntryAsState()
        val isRoot = backStackEntry?.destination?.route in rootRoutes
        val topBarActions = remember { TopBarActionsState() }
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val sessionViewModel: SessionViewModel = viewModel()
        val user by sessionViewModel.user.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { sessionViewModel.load() }

        BackHandler(enabled = drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            // A swipe from the left edge on an inner page would fight with the system back gesture.
            gesturesEnabled = isRoot || drawerState.isOpen,
            drawerContent = {
                val closeAndOpenRoot: (route: String?) -> Unit = { route ->
                    scope.launch { drawerState.close() }
                    navHostController.popBackStack(MainCollection.route, inclusive = false)
                    route?.let { navHostController.navigate(it) }
                }
                AppDrawer(
                    entries = drawerEntries(
                        features = user?.enabledFeatures(),
                        currentRoute = backStackEntry?.destination?.route,
                        open = closeAndOpenRoot,
                    ),
                    username = user?.username ?: AuthStore.username,
                    serverHost = AuthStore.serverUrl?.toHttpUrlOrNull()?.host,
                    onSignOut = {
                        scope.launch { drawerState.close() }
                        appState.signOut()
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val colors = KoillectionTheme.colors
                    TopAppBar(
                        title = {
                            val labels = backStackEntry?.breadcrumb().orEmpty()
                            Breadcrumb(
                                labels = labels,
                                onClick = { index -> repeat(labels.lastIndex - index) { navHostController.popBackStack() } }
                            )
                        },
                        navigationIcon = {
                            if (isRoot) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            } else {
                                IconButton(onClick = { navHostController.navigateUp() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        },
                        actions = { topBarActions.actionsFor(backStackEntry)?.invoke(this) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = colors.header,
                            titleContentColor = colors.onHeader,
                            navigationIconContentColor = colors.onHeader,
                            actionIconContentColor = colors.onHeader,
                        )
                    )
                }
            ) { innerPadding ->
                CompositionLocalProvider(LocalTopBarActions provides topBarActions) {
                    AppNavHost(
                        navHostController,
                        appState,
                        user?.enabledFeatures(),
                        Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
                    )
                }
            }
        }
    } else {
        Scaffold { innerPadding ->
            LoginNavHost(navHostController, Modifier.padding(innerPadding))
        }
    }
}
