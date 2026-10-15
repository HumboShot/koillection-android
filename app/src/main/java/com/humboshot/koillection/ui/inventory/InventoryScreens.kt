package com.humboshot.koillection.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.InventoryService
import com.humboshot.koillection.data.inventory.Inventory
import com.humboshot.koillection.data.inventory.InventorySection
import com.humboshot.koillection.data.inventory.parseInventory
import com.humboshot.koillection.data.repository.getAllPages
import com.humboshot.koillection.destinations.InventoryDetail
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.TopBarActions
import com.humboshot.koillection.ui.common.formatApiDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val service: InventoryService
    get() = ApiClient.getInstance().create(InventoryService::class.java)

class InventoryListViewModel : ViewModel() {
    private val _state = MutableStateFlow(InventoryListViewState())
    val state: StateFlow<InventoryListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getAllPages { page -> service.getInventories(page) }
                .onSuccess { inventories ->
                    _state.update { it.copy(isLoading = false, inventories = inventories.sortedByDescending { i -> i.createdAt }) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load inventories") }
                }
        }
    }
}

data class InventoryListViewState(
    val isLoading: Boolean = false,
    val inventories: List<Inventory> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onInventoryClick: (Inventory) -> Unit,
    viewModel: InventoryListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            if (state.inventories.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No inventories found. Create one on the web.") }
            }
            items(state.inventories, key = { it.id }) { inventory ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInventoryClick(inventory) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(inventory.name, style = MaterialTheme.typography.titleMedium)
                    inventory.createdAt?.let {
                        Text(formatApiDate(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

class InventoryDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val inventoryId: String = checkNotNull(savedStateHandle[InventoryDetail.ID_ARG])

    private val _state = MutableStateFlow(InventoryDetailViewState())
    val state: StateFlow<InventoryDetailViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            service.getInventory(inventoryId)
                .onSuccess { inventory ->
                    _state.update { it.copy(isLoading = false, inventory = inventory, sections = parseInventory(inventory.content)) }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load inventory") }
                }
        }
    }

    fun delete() {
        if (_state.value.isDeleting) return
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, error = null) }
            service.deleteInventory(inventoryId)
                .onSuccess { _state.update { it.copy(isDeleting = false, isDeleted = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isDeleting = false, error = throwable.message ?: "Failed to delete inventory") }
                }
        }
    }
}

data class InventoryDetailViewState(
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val inventory: Inventory? = null,
    val sections: List<InventorySection> = emptyList(),
    val error: String? = null,
    val isDeleted: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryDetailScreen(
    onDeleted: () -> Unit,
    viewModel: InventoryDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    if (state.inventory != null) {
        TopBarActions {
            IconButton(onClick = { confirmDelete = true }, enabled = !state.isDeleting) {
                Icon(Icons.Default.Delete, contentDescription = "Delete inventory")
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            val inventory = state.inventory ?: return@LazyColumn
            item {
                val total = state.sections.sumOf { it.totalItems }
                val checked = state.sections.sumOf { it.checkedItems }
                Column(Modifier.padding(16.dp)) {
                    Text(inventory.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "$checked of $total items checked. Check items on the web.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (state.sections.isEmpty() && !state.isLoading) {
                item { EmptyState("This inventory is empty") }
            }
            state.sections.forEach { section(it, depth = 0) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete inventory?") },
            text = { Text("This deletes \"${state.inventory?.name}\". Your items stay. You cannot undo this.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

private fun LazyListScope.section(section: InventorySection, depth: Int) {
    val indent = (16 + depth * 16).dp
    item {
        Text(
            text = "${section.title} (${section.checkedItems}/${section.totalItems})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = indent, end = 16.dp, top = 12.dp, bottom = 4.dp)
        )
    }
    items(section.items) { item ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = indent, end = 16.dp, top = 2.dp, bottom = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = if (item.checked) "Checked" else "Not checked",
                tint = if (item.checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(20.dp)
            )
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
    section.children.forEach { section(it, depth + 1) }
}
