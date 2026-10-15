package com.humboshot.koillection.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.api.core.ApiClient
import com.humboshot.koillection.api.services.CountService
import com.humboshot.koillection.data.repository.LoanRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StatisticsViewModel : ViewModel() {
    private val service: CountService
        get() = ApiClient.getInstance().create(CountService::class.java)

    private val _state = MutableStateFlow(StatisticsViewState())
    val state: StateFlow<StatisticsViewState> = _state.asStateFlow()
    private var statistics: List<Statistic> = emptyList()

    /** Loads the counts of [visible] when they change. Hidden tiles cost no request. */
    fun load(visible: List<Statistic>) {
        if (visible == statistics) return
        statistics = visible
        refresh()
    }

    /** Loads every count at the same time. A count that fails stays unknown and shows "–". */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val counts = statistics.map { stat ->
                async {
                    stat to when (val resource = stat.resource) {
                        null -> LoanRepository.getLoans().getOrNull()?.count { !it.isReturned }
                        else -> service.count(resource).getOrNull()?.totalItems
                    }
                }
            }.awaitAll().toMap()
            _state.update { it.copy(isLoading = false, counts = counts) }
        }
    }
}

data class StatisticsViewState(
    val isLoading: Boolean = false,
    val counts: Map<Statistic, Int?> = emptyMap(),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    statistics: List<Statistic>,
    viewModel: StatisticsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(statistics) { viewModel.load(statistics) }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(statistics, key = { it.name }) { stat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = state.counts[stat]?.toString() ?: if (state.isLoading) "…" else "–",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stat.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
