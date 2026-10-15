package com.humboshot.koillection.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.ui.common.ErrorState
import com.humboshot.koillection.ui.common.KoillectionImage
import com.humboshot.koillection.ui.common.TopBarActions

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemScreen(
    onItemClick: (Item) -> Unit,
    onEdit: (Item) -> Unit,
    onDeleted: () -> Unit,
    onTagClick: (Tag) -> Unit = {},
    showLoans: Boolean = true,
    onLoansChanged: () -> Unit = {},
    viewModel: ItemViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var loanToDelete by remember { mutableStateOf<Loan?>(null) }

    LaunchedEffect(state.loanChangeCount) {
        if (state.loanChangeCount > 0) onLoansChanged()
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onDeleted()
    }

    state.item?.let { item ->
        TopBarActions {
            IconButton(onClick = { onEdit(item) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit item")
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete item")
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            state.error?.let { error ->
                item { ErrorState(error, onRetry = { viewModel.refresh() }) }
            }

            val item = state.item ?: return@LazyColumn

            item {
                val largeImage = item.imageLargeThumbnail ?: item.image
                KoillectionImage(
                    path = largeImage,
                    contentDescription = item.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable(enabled = item.image != null) { fullScreenImage = item.image }
                )
            }

            item {
                Column(Modifier.padding(16.dp)) {
                    Text(item.name, style = MaterialTheme.typography.headlineSmall)
                    if (item.quantity > 1) {
                        Text(
                            text = "Quantity: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (state.tags.isNotEmpty()) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        state.tags.forEach { tag ->
                            AssistChip(onClick = { onTagClick(tag) }, label = { Text(tag.label) })
                        }
                    }
                }
            }

            items(state.data, key = { it.id }) { datum ->
                val editable = datum.type in EDITABLE_DATUM_TYPES
                Box(
                    Modifier
                        .clickable(enabled = editable) { viewModel.openDatumEditor(datum) }
                        .padding(horizontal = 16.dp)
                ) {
                    DatumRow(datum, onImageClick = { fullScreenImage = it })
                }
            }

            item {
                TextButton(
                    onClick = { viewModel.openDatumEditor(null) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text("Add field")
                }
            }

            if (showLoans) {
                item { LoansHeader(onLend = viewModel::openLendDialog) }
                if (state.loans.isEmpty()) {
                    item {
                        Text(
                            text = "This item was never lent.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                items(state.loans, key = { "loan-${it.id}" }) { loan ->
                    ItemLoanRow(
                        loan = loan,
                        enabled = !state.isChangingLoan,
                        onMarkReturned = { viewModel.markReturned(loan) },
                        onDelete = { loanToDelete = loan }
                    )
                }
            }

            if (state.relatedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Related items",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.relatedItems, key = { it.id }) { related ->
                            ItemCard(
                                item = related,
                                onClick = { onItemClick(related) },
                                modifier = Modifier.width(140.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    fullScreenImage?.let { path ->
        Dialog(
            onDismissRequest = { fullScreenImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenImage = null },
                contentAlignment = Alignment.Center
            ) {
                KoillectionImage(
                    path = path,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    state.datumEditor?.let { editor ->
        DatumDialog(
            editor = editor,
            onFieldsChange = viewModel::onDatumFieldsChange,
            onSave = viewModel::saveDatum,
            onDelete = viewModel::deleteDatum,
            onDismiss = viewModel::closeDatumEditor
        )
    }

    state.lendDialog?.let { dialog ->
        LendDialog(
            dialog = dialog,
            onChange = viewModel::onLendDialogChange,
            onConfirm = viewModel::lend,
            onDismiss = viewModel::closeLendDialog
        )
    }

    loanToDelete?.let { loan ->
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("Delete loan?") },
            text = { Text("This deletes the loan to ${loan.lentTo} from the history. You cannot undo this.") },
            confirmButton = {
                TextButton(onClick = {
                    loanToDelete = null
                    viewModel.deleteLoan(loan)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete item?") },
            text = { Text("This deletes \"${state.item?.name}\" and its data. You cannot undo this.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}
