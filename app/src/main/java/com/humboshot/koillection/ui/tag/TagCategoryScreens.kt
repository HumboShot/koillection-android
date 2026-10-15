package com.humboshot.koillection.ui.tag

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.repository.TagRepository
import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.destinations.TagCategoryForm
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.TopBarActions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TagCategoryListViewModel : ViewModel() {
    private val _state = MutableStateFlow(TagCategoryListViewState())
    val state: StateFlow<TagCategoryListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            TagRepository.getCategories()
                .onSuccess { categories -> _state.update { it.copy(isLoading = false, categories = categories) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load categories") }
                }
        }
    }
}

data class TagCategoryListViewState(
    val isLoading: Boolean = false,
    val categories: List<TagCategory> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagCategoryListScreen(
    onCategoryClick: (TagCategory) -> Unit,
    onAdd: () -> Unit,
    viewModel: TagCategoryListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }
            if (state.categories.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No tag categories found") }
            }
            items(state.categories, key = { it.id }) { category ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCategoryClick(category) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    CategoryLabel(category)
                    category.description?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        FloatingActionButton(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add tag category")
        }
    }
}

class TagCategoryFormViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val categoryId: String? = savedStateHandle[TagCategoryForm.CATEGORY_ID_ARG]
    private var original: TagCategory? = null

    private val _state = MutableStateFlow(TagCategoryFormViewState(isEditRoute = categoryId != null))
    val state: StateFlow<TagCategoryFormViewState> = _state.asStateFlow()

    init {
        if (categoryId != null) load(categoryId)
    }

    private fun load(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            TagRepository.getCategory(id)
                .onSuccess { category ->
                    original = category
                    _state.update {
                        it.copy(isLoading = false, isEditing = true, fields = TagCategoryFormFields.from(category))
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load category") }
                }
        }
    }

    fun onFieldsChange(fields: TagCategoryFormFields) = _state.update { it.copy(fields = fields, error = null) }

    fun save() {
        val current = _state.value
        if (current.isSaving || !current.canSave) return
        current.fields.validate()?.let { error ->
            _state.update { it.copy(error = error) }
            return
        }

        val category = original
        val patch = category?.let { current.fields.toPatch(it) }
        if (category != null && patch == null) {
            _state.update { it.copy(isSaved = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            val result = if (category != null) {
                TagRepository.updateCategory(category.id, checkNotNull(patch))
            } else {
                TagRepository.createCategory(current.fields.toCreate())
            }
            result
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to save category") }
                }
        }
    }

    fun delete() {
        val category = original ?: return
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            TagRepository.deleteCategory(category.id)
                .onSuccess { _state.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isSaving = false, error = throwable.message ?: "Failed to delete category") }
                }
        }
    }
}

data class TagCategoryFormViewState(
    val fields: TagCategoryFormFields = TagCategoryFormFields(),
    val isLoading: Boolean = false,
    val isEditRoute: Boolean = false,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    /** True after a save or a delete. The screen then goes back. */
    val isSaved: Boolean = false,
    val error: String? = null,
) {
    val canSave: Boolean get() = !isEditRoute || isEditing
}

@Composable
fun TagCategoryFormScreen(
    onDone: () -> Unit,
    viewModel: TagCategoryFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val fields = state.fields
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onDone()
    }

    if (state.isEditing) {
        TopBarActions {
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete tag category")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = fields.label,
            onValueChange = { viewModel.onFieldsChange(fields.copy(label = it)) },
            label = { Text("Label") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fields.description,
            onValueChange = { viewModel.onFieldsChange(fields.copy(description = it)) },
            label = { Text("Description") },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = fields.color,
                onValueChange = { viewModel.onFieldsChange(fields.copy(color = it)) },
                label = { Text("Color") },
                placeholder = { Text("#009688") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            val preview = parseHexColor(fields.color.trim().removePrefix("#").let { "#$it" })
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .then(if (preview != null) Modifier.background(preview) else Modifier)
            )
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = viewModel::save,
            enabled = !state.isSaving && !state.isLoading && state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSaving) "Saving…" else "Save")
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete tag category?") },
            text = { Text("This deletes \"${fields.label}\". The tags stay, without a category. You cannot undo this.") },
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
