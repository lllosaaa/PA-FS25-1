package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.material.icons.filled.Add
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // optional padding around the content
    ) {
        // Top text label
        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleLarge
        )

        // DELETE button in top-right corner
        IconButton(
            onClick = { viewModel.deleteFirstTransaction() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete First Transaction"
            )
        }

        // List of transactions
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp) // leave space for the top row
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }

        // Floating Add button in bottom-center
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 1.dp) // adjust if needed to avoid bottom nav overlap
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction"
            )
        }
    }

    // Show dialog for new transactions
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

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateString = remember(transaction.date) {
        // Or create a static formatter at the top of the file for efficiency
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormatter.format(transaction.date)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleSmall
                )


                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.titleSmall
                )
            }
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
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
// Store a Date, not a String
    var selectedDate by remember { mutableStateOf(calendar.time) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val showDatePicker = {
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.time
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

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

                CategoryDropdown(
                    categories = listOf("Groceries", "Food", "Transport", "Bills", "Entertainment"),
                    selectedCategory = category,
                    onCategorySelected = { newCategory -> category = newCategory }
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (Income/Expense)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateFormatter.format(selectedDate),
                    onValueChange = { },
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = showDatePicker) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val transaction = Transaction(
                        description = description.ifBlank { "No description" },
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = selectedDate,  // now a proper Date
                        category = category.ifBlank { "General" },
                        type = if (type.equals("income", ignoreCase = true)) "Income" else "Expense"
                    )
                    onAddTransaction(transaction)

                }
            ) {
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

@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedCategory,
        onValueChange = { },
        label = { Text("Category") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        categories.forEach { categoryOption ->
            DropdownMenuItem(
                text = { Text(categoryOption) },
                onClick = {
                    onCategorySelected(categoryOption)
                    expanded = false
                }
            )
        }
    }
}
