package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        floatingActionButtonPosition = FabPosition.Center // or FabPosition.End
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
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = transaction.date,
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
    // Basic states for other fields
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }

    // For handling the date
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember {
        // Will produce a string in the format: dd/MM/yyyy
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }
    // The state that holds the chosen date string, defaulting to "today"
    var selectedDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }

    // Lambda that shows the native DatePickerDialog
    val showDatePicker = {
        val datePickerDialog = DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
                // Format the Calendar date as dd/MM/yyyy
                selectedDate = dateFormatter.format(calendar.time)
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
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Category
                CategoryDropdown(
                    categories = listOf("Groceries", "Food", "Transport", "Bills", "Entertainment"),
                    selectedCategory = category,
                    onCategorySelected = { newCategory -> category = newCategory }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Spacer(modifier = Modifier.height(8.dp))

                // Type (Income or Expense)
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Type (Income/Expense)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Date field with a trailing icon to open the DatePickerDialog
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { /* read-only, user picks date from dialog */ },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = showDatePicker) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val transaction = Transaction(
                        description = description.ifBlank { "No description" },
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = selectedDate, // Already in dd/MM/yyyy format
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

//Dropdown menu for selecting a category from a predefined list.
@Composable
fun CategoryDropdown(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // TextField that displays the selected category
    OutlinedTextField(
        value = selectedCategory,
        onValueChange = { /* no-op: we only pick from the dropdown */ },
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
