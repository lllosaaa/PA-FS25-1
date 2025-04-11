package ch.zhaw.pa_fs25.userInterface.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.util.saveCsvExport
import ch.zhaw.pa_fs25.util.saveDatabaseBackup
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun SettingsScreen(repository: FinanceRepository) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("HH:mm_dd-MM-yyy")
    val currentDateTime = LocalDateTime.now().format(dateFormatter)

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            // Button to backup the database. User will choose where to save the backup.
            Button(onClick = { backupLauncher.launch("budget_database_backup_"+ currentDateTime.toString()+".db") }) {
                Text("Backup Data")
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Button to export the data as CSV. User will choose where to save the CSV file.

            Button(onClick = { exportLauncher.launch("transactions_backup_"+ currentDateTime.toString()+".csv") }) {
                Text("Export CSV")
            }

        }

    }
}
