package com.humboshot.koillection.ui.loan

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.ui.common.EmptyState
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.formatApiDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanListScreen(
    onLoanClick: (loan: Loan, itemName: String) -> Unit,
    viewModel: LoanListViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    FilterChip(
                        selected = !state.showAll,
                        onClick = { viewModel.onShowAllChange(false) },
                        label = { Text("Lent") }
                    )
                    FilterChip(
                        selected = state.showAll,
                        onClick = { viewModel.onShowAllChange(true) },
                        label = { Text("All") }
                    )
                }
            }

            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }

            val loans = state.visibleLoans
            if (loans.isEmpty() && !state.isLoading && state.error == null) {
                item { EmptyState(if (state.showAll) "No loans found" else "No items are lent") }
            }

            items(loans, key = { it.id }) { loan ->
                val name = state.itemName(loan)
                LoanRow(loan, name, onClick = { onLoanClick(loan, name) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun LoanRow(loan: Loan, itemName: String?, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        itemName?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        Text("Lent to ${loan.lentTo}", style = MaterialTheme.typography.bodyLarge)
        Text(
            text = buildString {
                append("Lent ${formatApiDate(loan.lentAt)}")
                append(" · ")
                append(loan.returnedAt?.let { "Returned ${formatApiDate(it)}" } ?: "Not returned")
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (loan.isReturned) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
        )
    }
}
