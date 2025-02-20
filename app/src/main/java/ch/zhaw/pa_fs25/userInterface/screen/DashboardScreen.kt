package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TransactionViewModel) {
    // Collect transactions from the ViewModel
    val transactions by viewModel.transactions.collectAsState()

    // State for controlling the AddTransactionDialog
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction"
                    )
                }
                //this button should delete the last transaction added
                FloatingActionButton(
                    onClick = {
                        viewModel.deleteLastTransaction()
                    }

                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Last Transaction"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }

        // Show dialog when user presses the + button
        if (showDialog) {
            AddTransactionDialog(
                onDismiss = { showDialog = false },
                onAddTransaction = { transaction ->
                    viewModel.addTransaction(transaction)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = transaction.category,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${transaction.amount} CHF",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAddTransaction: (Transaction) -> Unit
) {
    // Simple form fields for demonstration; adapt as needed
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Transaction") },
        text = {
            Column {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (Income/Expense)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val transaction = Transaction(
                    description = description.ifBlank { "No description" },
                    amount = amount.toDoubleOrNull() ?: 0.0,
                    date = "2025-01-01", // Replace with a date picker or dynamic date
                    category = category.ifBlank { "General" },
                    type = if (type.equals("Income", true)) "Income" else "Expense"
                )
                onAddTransaction(transaction)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
