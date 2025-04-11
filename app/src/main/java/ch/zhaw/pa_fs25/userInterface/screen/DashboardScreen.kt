package ch.zhaw.pa_fs25.userInterface.screen

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.R
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.userInterface.component.CategoryBudgetChart
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun DashboardScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {

            Text(text = "Recent Transactions", style = MaterialTheme.typography.titleLarge)

            CategoryBudgetOverview(
                categories = categories,
                transactions = transactions
            )
        }
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
        }

        FloatingActionButton(
            onClick = { showCategoryDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {

            Icon(painter = painterResource(id = R.drawable.category_24px), contentDescription = "Add Category")
        }

        if (showDialog) {
            AddTransactionDialog(
                onDismiss = { showDialog = false },
                onAddTransaction = { transaction ->
                    viewModel.addTransaction(transaction)
                    showDialog = false
                },
                categories = categories.map { it.name }
            )
        }


        if (showCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showCategoryDialog = false },
                onAddCategory = { category ->
                    viewModel.addCategory(category)
                    showCategoryDialog = false
                }
            )
        }
    }

}
@Composable
fun CategoryBudgetOverview(
    categories: List<Category>,
    transactions: List<Transaction>
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories.filter { it.budgetLimit > 0 }) { category ->
            val spent = transactions
                .filter { it.categoryId == category.id && it.amount < 0 }
                .sumOf { it.amount }

            CategoryBudgetChart(
                categoryName = category.name,
                budgetLimit = category.budgetLimit,
                spentAmount = spent
            )
        }
    }
}



@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onAddCategory: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val category = Category(name = categoryName.ifBlank { "Uncategorized" })
                    onAddCategory(category)
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
fun TransactionItem(transaction: Transaction, categories: List<Category>) {
    val dateString = remember(transaction.date) {
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormatter.format(transaction.date)
    }

    // Find the category name based on the categoryId
    val categoryName = remember(transaction.categoryId, categories) {
        categories.find { it.id == transaction.categoryId }?.name ?: "General"
    }
    val type = remember(transaction.type){
        if(transaction.type.equals("Income", ignoreCase = true)){
            "Income"
        }else{
            "Expense"
        }
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
                    text = categoryName,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text= type,
                    style = MaterialTheme.typography.bodySmall

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
    onAddTransaction: (Transaction) -> Unit,
    categories: List<String> // <-- Accept categories as a parameter
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }

    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(dateFormatter.format(calendar.time)) }

    val showDatePicker = {
        val datePickerDialog = DatePickerDialog(
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
                    categories = categories, // <-- Use the provided categories list
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
                    value = selectedDate,
                    onValueChange = { },
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
                        date = calendar.time,
                        type = if (type.equals("income", ignoreCase = true)) "Income" else "Expense",
                        categoryId = categories.indexOf(category) + 1

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