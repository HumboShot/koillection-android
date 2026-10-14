package com.humboshot.koillection.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

private const val REFRESH_KEY = "refresh"

/** Calls [onRefresh] when a screen above this one reports a change with [popWithRefresh]. */
@Composable
fun RefreshOnResult(entry: NavBackStackEntry, onRefresh: () -> Unit) {
    val refresh by entry.savedStateHandle.getStateFlow(REFRESH_KEY, false).collectAsStateWithLifecycle()

    LaunchedEffect(refresh) {
        if (refresh) {
            entry.savedStateHandle[REFRESH_KEY] = false
            onRefresh()
        }
    }
}

fun NavHostController.popWithRefresh() {
    previousBackStackEntry?.savedStateHandle?.set(REFRESH_KEY, true)
    popBackStack()
}
