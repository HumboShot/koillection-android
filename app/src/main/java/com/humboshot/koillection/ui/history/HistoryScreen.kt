package com.humboshot.koillection.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.LogService
import com.humboshot.koillection.data.log.Log
import com.humboshot.koillection.data.log.filterLogs
import com.humboshot.koillection.data.repository.getAllPages
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.formatApiDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val service: LogService
        get() = ApiClient.getInstance().create(LogService::class.java)

    private val _state = MutableStateFlow(HistoryViewState())
    val state: StateFlow<HistoryViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** `/api/logs` has no order or filter parameters, so the app loads all pages and sorts them. */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllPages { page -> service.getLogs(page) }
                .onSuccess { logs -> _state.update { it.copy(isLoading = false, logs = logs) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load history") }
                }
        }
    }

    fun onTypeChange(type: String?) = _state.update { it.copy(type = type) }
}

data class HistoryViewState(
    val isLoading: Boolean = false,
    val logs: List<Log> = emptyList(),
    /** "create", "delete", or null for all. */
    val type: String? = null,
    val error: String? = null,
) {
    val visibleLogs: List<Log> get() = filterLogs(logs, type)
}

/** Only items and collections that still exist have a screen in the app. */
fun Log.canOpen(): Boolean = !objectDeleted && shortClass in setOf("Item", "Collection")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onOpen: (Log) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    listOf(null to "All", "create" to "Created", "delete" to "Deleted").forEach { (type, label) ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.onTypeChange(type) },
                            label = { Text(label) }
                        )
                    }
                }
            }
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            val logs = state.visibleLogs
            if (logs.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No history found") }
            }
            items(logs, key = { it.id }) { log ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (log.canOpen()) Modifier.clickable { onOpen(log) } else Modifier)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(log.objectLabel, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = listOf(
                            when (log.type) {
                                "create" -> "Created"
                                "delete" -> "Deleted"
                                else -> log.type.orEmpty().replaceFirstChar { it.uppercase() }
                            },
                            log.shortClass,
                            formatApiDate(log.loggedAt),
                        ).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (log.type == "delete") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
