package com.humboshot.koillection.ui.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.repository.TemplateRepository
import com.humboshot.koillection.data.template.Template
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.NameDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TemplateListViewModel : ViewModel() {
    private val _state = MutableStateFlow(TemplateListViewState())
    val state: StateFlow<TemplateListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            TemplateRepository.getTemplates()
                .onSuccess { templates -> _state.update { it.copy(isLoading = false, templates = templates) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load templates") }
                }
        }
    }

    fun openCreateDialog() = _state.update { it.copy(isCreating = true, createError = null) }

    fun closeCreateDialog() = _state.update { it.copy(isCreating = false, createError = null) }

    fun create(name: String) {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, createError = null) }
            TemplateRepository.createTemplate(name)
                .onSuccess { template ->
                    _state.update { it.copy(isSaving = false, isCreating = false, created = template) }
                    refresh()
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, createError = throwable.message ?: "Failed to create template") }
                }
        }
    }

    fun onCreatedHandled() = _state.update { it.copy(created = null) }
}

data class TemplateListViewState(
    val isLoading: Boolean = false,
    val templates: List<Template> = emptyList(),
    val error: String? = null,
    val isCreating: Boolean = false,
    val isSaving: Boolean = false,
    val createError: String? = null,
    /** A new template. The screen opens it, so the user can add fields at once. */
    val created: Template? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    onTemplateClick: (Template) -> Unit,
    viewModel: TemplateListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.created) {
        state.created?.let { template ->
            viewModel.onCreatedHandled()
            onTemplateClick(template)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            if (state.templates.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No templates found") }
            }
            items(state.templates, key = { it.id }) { template ->
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTemplateClick(template) }
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        FloatingActionButton(
            onClick = viewModel::openCreateDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add template")
        }
    }

    if (state.isCreating) {
        NameDialog(
            title = "New template",
            initialName = "",
            isSaving = state.isSaving,
            error = state.createError,
            onConfirm = viewModel::create,
            onDismiss = viewModel::closeCreateDialog
        )
    }
}
