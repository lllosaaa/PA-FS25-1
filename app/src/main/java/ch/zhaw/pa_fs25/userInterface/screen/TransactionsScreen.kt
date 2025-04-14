package ch.zhaw.pa_fs25.userInterface.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import ch.zhaw.pa_fs25.data.parser.CsvTransactionParser
import kotlinx.coroutines.launch


@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // File picker for CSV (using "*/*" so that CSV files are visible regardless of MIME type).
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            coroutineScope.launch {
                val defaultCategory = categories.firstOrNull()
                val parser = CsvTransactionParser()
                val importedTransactions: List<Transaction> =
                    parser.parse(context, fileUri, categories, defaultCategory)

                if (importedTransactions.isEmpty()) {
                    Toast.makeText(
                        context,
                        "No transactions imported. Check CSV format.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    importedTransactions.forEach { transaction ->
                        viewModel.addTransaction(transaction)
                    }
                    Toast.makeText(
                        context,
                        "${importedTransactions.size} transactions imported.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "All Transactions",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                // Launch file picker with "*/*" to ensure CSV files appear.
                csvLauncher.launch("*/*")
            }) {
                Text(text = "Import CSV")
            }
            //Delete all transaction button
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                viewModel.deleteAllTransactions()
            }) {
                Text(text = "Delete all transactions")
            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    categories = categories,
                )
            }
        }
    }
}
