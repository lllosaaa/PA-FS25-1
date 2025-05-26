package ch.zhaw.pa_fs25.userInterface.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.R
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.userInterface.activity.MainActivity
import ch.zhaw.pa_fs25.util.saveCsvExport
import ch.zhaw.pa_fs25.util.saveDatabaseBackup
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(repository: FinanceRepository, viewModel: TransactionViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm_dd-MM-yyy")
    val currentDateTime = LocalDateTime.now().format(dateFormatter)
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val categories by viewModel.categories.collectAsState()

    // Launcher to create a document for the database backup.
    val backupLauncher = rememberLauncherForActivityResult(CreateDocument("application/octet-stream")) { uri: Uri? ->
        uri?.let {
            val success = saveDatabaseBackup(context, it)
            Toast.makeText(
                context,
                if (success) "Database backup saved successfully." else "Database backup failed.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launcher to create a document for the CSV export.
    val exportLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val success = saveCsvExport(context, repository, it)
                Toast.makeText(
                    context,
                    if (success) "CSV export saved successfully." else "CSV export failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row {
                // Button to backup the database. User will choose where to save the backup.
                Button(onClick = { backupLauncher.launch("budget_database_backup_" + currentDateTime.toString() + ".db") }) {
                    Text("Backup Data")
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Button to export the data as CSV. User will choose where to save the CSV file.

                Button(onClick = { exportLauncher.launch("transactions_backup_" + currentDateTime.toString() + ".csv") }) {
                    Text("Export CSV")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        // Delete all transactions
                        repository.deleteAllTransactions()

                        // Delete all categories
                        val categories = repository.getAllCategories().first()
                        categories.forEach { repository.deleteCategoryIfUnused(it) }

                        // Reset onboarding flag
                        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_first_launch", true).apply()

                        //relaunch the app
                        (context as MainActivity).recreate()
                    }
                }) {
                    Text("🔄 Reset App (Demo)")
                }

            }

        }

        FloatingActionButton(
            onClick = { showCategoryDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                painter = painterResource(id = R.drawable.category_24px),
                contentDescription = "Add Category"
            )
        }


        if (showCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showCategoryDialog = false },
                onAddCategory = {
                    viewModel.addCategory(it)
                    showCategoryDialog = false
                }
            )
        }

    }




}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onAddCategory: (Category) -> Unit) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name", color = MaterialTheme.colorScheme.onBackground) },
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
        },
        confirmButton = {
            Button(onClick = {
                val category = Category(name = categoryName.ifBlank { "Uncategorized" })
                onAddCategory(category)
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,  // override the pale violet!
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )

}

