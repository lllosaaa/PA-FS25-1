package ch.zhaw.pa_fs25.userInterface.screen

import android.net.Uri
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.parser.UniversalTransactionParser
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import ch.zhaw.pa_fs25.di.NetworkModule
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    var searchText by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf(-1) }
    var selectedYear by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(false) }

    val filteredTransactions = transactions.filter { tx ->
        val cal = Calendar.getInstance().apply { time = tx.date }
        val matchCategory = categories.find { it.id == tx.categoryId }?.name?.contains(searchText, true) ?: false
        val matchDate = sdf.format(tx.date).contains(searchText, true)
        val matchAmount = tx.amount.toString().contains(searchText, true)
        val matchDescription = tx.description.contains(searchText, true)
        val matchMonth = selectedMonth == -1 || cal.get(Calendar.MONTH) == selectedMonth
        val matchYear = selectedYear == -1 || cal.get(Calendar.YEAR) == selectedYear

        (searchText.isBlank() || matchCategory || matchDate || matchAmount || matchDescription) && matchMonth && matchYear
    }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            coroutineScope.launch {
                val defaultCategory = categories.firstOrNull()
                val parser = UniversalTransactionParser()
                val importedTransactions: List<Transaction> =
                    parser.parse(context, fileUri, categories, defaultCategory)

                if (importedTransactions.isEmpty()) {
                    Toast.makeText(context, "No transactions imported. Check CSV format.", Toast.LENGTH_SHORT).show()
                } else {
                    importedTransactions.forEach { viewModel.addTransaction(it) }
                    Toast.makeText(context, "${importedTransactions.size} transactions imported.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val swissApi = remember { NetworkModule.provideSwissNextGenApi() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { csvLauncher.launch("*/*") }) {
                    Text("Import CSV", color = MaterialTheme.colorScheme.onPrimary)
                }
                Button(onClick = {
                    viewModel.importSwissMockTransactions(swissApi) {
                        Toast.makeText(context, "Imported $it transactions from API", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Import API", color = MaterialTheme.colorScheme.onPrimary)
                }
                Button(onClick = { viewModel.deleteAllTransactions() }) {
                    Text("Delete all transactions", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = {
                    Text("Search by category, date, amount, or name", color = MaterialTheme.colorScheme.onBackground)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DropdownMenuSelector(
                    label = "Month",
                    options = listOf("All") + (0..11).map {
                        SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, it) }.time)
                    },
                    selectedIndex = if (selectedMonth == -1) 0 else selectedMonth + 1,
                    onSelectIndex = { selectedMonth = it - 1 }
                )

                DropdownMenuSelector(
                    label = "Year",
                    options = listOf("All") + (2020..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() },
                    selectedIndex = if (selectedYear == -1) 0 else (selectedYear - 2019),
                    onSelectIndex = { selectedYear = if (it == 0) -1 else 2019 + it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            LazyColumn {
                items(filteredTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        categories = categories,
                        onCategoryChange = { newCategoryId ->
                            viewModel.updateTransactionCategory(transaction, newCategoryId)
                        }
                    )

                }
            }
        }


        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }

        if (showDialog) {
            AddTransactionDialog(
                onDismiss = { showDialog = false },
                onAddTransaction = {
                    viewModel.addTransaction(it)
                    viewModel.setFilterMonthYear(selectedMonth, selectedYear)
                    showDialog = false
                },
                categories = categories
            )


        }

    }

}

@Composable
fun DropdownMenuSelector(label: String, options: List<String>, selectedIndex: Int, onSelectIndex: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            modifier = Modifier.width(150.dp),
            readOnly = true,
            label = { Text(label, color = MaterialTheme.colorScheme.onBackground) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSelectIndex(index)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    categories: List<Category>,
    onCategoryChange: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val dateString = remember(transaction.date) {
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormatter.format(transaction.date)
    }

    val categoryName = remember(transaction.categoryId, categories) {
        categories.find { it.id == transaction.categoryId }?.name ?: "General"
    }

    val type = remember(transaction.type) {
        if (transaction.type.equals("Income", ignoreCase = true)) "Income" else "Expense"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(90.dp)
            .clickable { showDialog = true },
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(transaction.description, style = MaterialTheme.typography.titleSmall)
            }

            Spacer(modifier = Modifier.width(12.dp))


            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(categoryName, style = MaterialTheme.typography.bodySmall)
                Text(type, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.width(12.dp))


            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(dateString, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${transaction.amount} CHF",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Change Category") },
            text = {
                Column {
                    categories.forEach { category ->
                        TextButton(onClick = {
                            onCategoryChange(category.id)
                            showDialog = false
                        }) {
                            Text(category.name)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}



@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onAddTransaction: (Transaction) -> Unit,
    categories: List<Category>
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var type by remember { mutableStateOf("Expense") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }

    val showDatePicker = {
        val datePickerDialog = android.app.DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                calendar.set(year, month, dayOfMonth)
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
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
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
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
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
                    value = selectedDate,
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = showDatePicker) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val categoryId = selectedCategory?.id ?: return@Button
                    val transaction = Transaction(
                        description = description.ifBlank { "No description" },
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        date = calendar.time,
                        type = if (type.equals("income", ignoreCase = true)) "Income" else "Expense",
                        categoryId = categoryId
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
private fun textFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
)



@Composable
fun CategoryDropdown(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedCategory?.name ?: "",
        onValueChange = {},
        label = { Text("Category") },
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        categories.forEach { category ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = category.name,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                onClick = {
                    onCategorySelected(category)
                    expanded = false
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            )
        }
    }

}
