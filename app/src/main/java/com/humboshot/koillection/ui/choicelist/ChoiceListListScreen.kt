package com.humboshot.koillection.ui.choicelist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.choicelist.ChoiceList
import com.humboshot.koillection.data.repository.ChoiceListRepository
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChoiceListListViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChoiceListListViewState())
    val state: StateFlow<ChoiceListListViewState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ChoiceListRepository.getChoiceLists()
                .onSuccess { lists -> _state.update { it.copy(isLoading = false, choiceLists = lists) } }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.message ?: "Failed to load choice lists") }
                }
        }
    }
}

data class ChoiceListListViewState(
    val isLoading: Boolean = false,
    val choiceLists: List<ChoiceList> = emptyList(),
    val error: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceListListScreen(
    onChoiceListClick: (ChoiceList) -> Unit,
    onAdd: () -> Unit,
    viewModel: ChoiceListListViewModel = viewModel()
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
            if (state.choiceLists.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState("No choice lists found") }
            }
            items(state.choiceLists, key = { it.id }) { choiceList ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChoiceListClick(choiceList) }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(choiceList.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = choiceList.choices.filterNotNull().joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
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
            Icon(Icons.Default.Add, contentDescription = "Add choice list")
        }
    }
}
