package com.humboshot.koillection.ui.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.humboshot.koillection.data.loan.Loan
import com.humboshot.koillection.data.loan.loanDate
import com.humboshot.koillection.ui.common.formatApiDate
import com.humboshot.koillection.ui.loan.LoanRow

@Composable
fun LoansHeader(onLend: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 24.dp)
    ) {
        Text(
            text = "Loans",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onLend) { Text("Lend") }
    }
}

@Composable
fun ItemLoanRow(loan: Loan, enabled: Boolean, onMarkReturned: () -> Unit, onDelete: () -> Unit) {
    Column {
        LoanRow(loan, itemName = null, onClick = null)
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (!loan.isReturned) {
                TextButton(onClick = onMarkReturned, enabled = enabled) { Text("Mark returned") }
            }
            TextButton(onClick = onDelete, enabled = enabled) {
                Text("Delete loan", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LendDialog(
    dialog: LendDialogState,
    onChange: (LendDialogState) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var pickingDate by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lend item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dialog.lentTo,
                    onValueChange = { onChange(dialog.copy(lentTo = it)) },
                    label = { Text("Lent to") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Date: ${formatApiDate(loanDate(dialog.dateMillis))}")
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { pickingDate = true }) { Text("Change") }
                }
                dialog.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !dialog.isSaving) { Text("Lend") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (pickingDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dialog.dateMillis)
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onChange(dialog.copy(dateMillis = it)) }
                    pickingDate = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { pickingDate = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
