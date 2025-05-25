package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import java.util.*

@Composable
fun BudgetScreen(viewModel: TransactionViewModel) {
    val categories by viewModel.categories.collectAsState()
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val editMode = remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.setFilterMonthYear(selectedMonth, selectedYear)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Budgets",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { editMode.value = !editMode.value }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Budgets")
            }
        }

        MonthYearPicker(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthChange = { selectedMonth = it },
            onYearChange = { selectedYear = it }
        )

        val visibleCategories = if (editMode.value) {
            categories
        } else {
            categories.filter { viewModel.rememberBudget(it.id, selectedMonth, selectedYear) > 0 }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(visibleCategories) { category ->
                val budgetLimit by viewModel.rememberBudgetState(category.id, selectedMonth, selectedYear)
                val spent = filteredTransactions
                    .filter { it.categoryId == category.id && it.amount < 0 }
                    .sumOf { it.amount }

                BudgetCategoryCard(
                    category = category,
                    spentAmount = spent,
                    budgetLimit = budgetLimit,
                    onSetBudget = {
                        viewModel.setBudgetForCategory(category.id, selectedMonth, selectedYear, it)
                    },
                    editMode = editMode.value
                )
            }
        }
    }
}

@Composable
fun MonthYearPicker(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit
) {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                val newMonth = (selectedMonth - 1 + 12) % 12
                onMonthChange(newMonth)
            }) { Text("<") }

            Spacer(modifier = Modifier.width(8.dp))
            Text("${months[selectedMonth]} $selectedYear")
            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                val newMonth = (selectedMonth + 1) % 12
                onMonthChange(newMonth)
            }) { Text(">") }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { onYearChange(selectedYear - 1) }) { Text("-") }
            Spacer(modifier = Modifier.width(8.dp))
            Text("$selectedYear")
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onYearChange(selectedYear + 1) }) { Text("+") }
        }
    }
}

@Composable
fun BudgetCategoryCard(
    category: Category,
    spentAmount: Double,
    budgetLimit: Double,
    onSetBudget: (Double) -> Unit,
    editMode: Boolean
) {
    val remaining = budgetLimit + spentAmount
    val progress = if (budgetLimit > 0) (-spentAmount / budgetLimit).toFloat().coerceIn(0f, 1f) else 0f
    val showDialog = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    )
    {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = category.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${"%.2f".format(-spentAmount)} / ${"%.2f".format(budgetLimit)} CHF",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Remaining: ${"%.2f".format(remaining)} CHF",
                style = MaterialTheme.typography.bodySmall,
                color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )

            if (editMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { showDialog.value = true }, modifier = Modifier.align(Alignment.End)) {
                    Text("Set Budget")
                }
            }
        }
    }

    if (showDialog.value) {
        SetBudgetDialog(
            initialAmount = budgetLimit,
            categoryName = category.name,
            onDismiss = { showDialog.value = false },
            onSave = {
                onSetBudget(it)
                showDialog.value = false
            }
        )
    }
}

@Composable
fun SetBudgetDialog(
    initialAmount: Double,
    categoryName: String,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(initialAmount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                val parsed = budgetText.toDoubleOrNull()
                if (parsed != null) onSave(parsed)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = {
            Text("Set Budget for $categoryName")
        },
        text = {
            Surface(
                color = MaterialTheme.colorScheme.surface, // override pale violet
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Budget (CHF)", color = MaterialTheme.colorScheme.onBackground) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface, // ← override dialog background!
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

