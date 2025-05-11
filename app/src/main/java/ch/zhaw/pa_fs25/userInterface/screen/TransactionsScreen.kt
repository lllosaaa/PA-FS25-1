package ch.zhaw.pa_fs25.userInterface.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.parser.UniversalTransactionParser
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
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

    val filteredTransactions = transactions.filter { tx ->
        val matchCategory = categories.find { it.id == tx.categoryId }?.name?.contains(searchText, true) ?: false
        val matchDate = sdf.format(tx.date).contains(searchText, true)
        val matchAmount = tx.amount.toString().contains(searchText, true)
        val matchDescription = tx.description.contains(searchText, true)
        searchText.isBlank() || matchCategory || matchDate || matchAmount || matchDescription
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
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search by category, date, amount, or name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Button(onClick = {
                csvLauncher.launch("*/*")
            }) {
                Text(text = "Import CSV")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                viewModel.deleteAllTransactions()
            }) {
                Text(text = "Delete all transactions")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredTransactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    categories = categories,
                )
            }
        }
    }
}
