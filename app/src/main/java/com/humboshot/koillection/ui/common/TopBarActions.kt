package com.humboshot.koillection.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

class TopBarActionsState {
    private var owner by mutableStateOf<Any?>(null)
    private var content by mutableStateOf<(@Composable RowScope.() -> Unit)?>(null)

    fun actionsFor(owner: Any?): (@Composable RowScope.() -> Unit)? =
        if (owner != null && owner == this.owner) content else null

    fun set(owner: Any, content: @Composable RowScope.() -> Unit) {
        this.owner = owner
        this.content = content
    }

    fun clear(owner: Any) {
        if (this.owner == owner) {
            this.owner = null
            content = null
        }
    }
}

val LocalTopBarActions = staticCompositionLocalOf { TopBarActionsState() }

/**
 * Shows [content] in the shared top bar while this screen is the current destination.
 * The owner is the back stack entry, so a screen that leaves during a transition does not
 * remove the actions of the screen that replaces it.
 */
@Composable
fun TopBarActions(content: @Composable RowScope.() -> Unit) {
    val state = LocalTopBarActions.current
    val owner = checkNotNull(LocalViewModelStoreOwner.current)
    val currentContent by rememberUpdatedState(content)

    DisposableEffect(state, owner) {
        state.set(owner) { currentContent() }
        onDispose { state.clear(owner) }
    }
}
