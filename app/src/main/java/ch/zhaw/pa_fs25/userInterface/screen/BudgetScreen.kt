package ch.zhaw.pa_fs25.userInterface.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel

@Composable
fun BudgetScreen(viewModel: TransactionViewModel) {
    val categories by viewModel.categories.collectAsState()
    val editMode = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Edit button
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
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Budgets"
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { category ->
                BudgetCategoryCard(category, viewModel, editMode = editMode.value)
            }
        }
    }
}


@Composable
fun BudgetCategoryCard(
    category: Category,
    viewModel: TransactionViewModel,
    editMode: Boolean
) {
    val spent = remember { mutableStateOf(0.0) }
    val showDialog = remember { mutableStateOf(false) }

    LaunchedEffect(category.id) {
        spent.value = viewModel.getSpentForCategory(category.id)
    }

    val remaining = category.budgetLimit + spent.value
    val progress = if (category.budgetLimit > 0)
        (-spent.value / category.budgetLimit).toFloat().coerceIn(0f, 1f)
    else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${"%.2f".format(-spent.value)} / ${"%.2f".format(category.budgetLimit)} CHF",
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
                Button(
                    onClick = { showDialog.value = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Set Budget")
                }
            }
        }
    }

    if (showDialog.value) {
        SetBudgetDialog(
            category = category,
            onDismiss = { showDialog.value = false },
            onSave = { newLimit ->
                viewModel.updateCategoryBudget(category.id, newLimit)
                showDialog.value = false
            }
        )
    }
}




@Composable
fun SetBudgetDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var budgetText by remember { mutableStateOf(category.budgetLimit.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Budget for ${category.name}") },
        text = {
            OutlinedTextField(
                value = budgetText,
                onValueChange = { budgetText = it },
                label = { Text("Budget (CHF)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = {
                val parsed = budgetText.toDoubleOrNull()
                if (parsed != null) onSave(parsed)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// list with all categories
/*
@Composable
fun CategoryListScreen(viewModel: TransactionViewModel) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(categories) { category ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                ) {
                    Text(
                        text = category.name,
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = {
                        viewModel.deleteCategory(category) { success ->
                            val msg = if (success) "Category deleted" else "Category in use, cannot delete"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
*/


