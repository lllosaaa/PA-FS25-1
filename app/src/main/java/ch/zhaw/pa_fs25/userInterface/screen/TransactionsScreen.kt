package ch.zhaw.pa_fs25.userInterface.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    val swissApi = remember { NetworkModule.provideSwissNextGenApi() }

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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DropdownMenuSelector(
                label = "Month",
                options = listOf("All") + (0..11).map { month ->
                    SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, month) }.time)
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

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                csvLauncher.launch("*/*")
            }) {
                Text(text = "Import CSV")
            }

            Button(onClick = {
                viewModel.importSwissMockTransactions(swissApi) {
                    Toast.makeText(context, "Imported $it transactions from API", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Import API")
            }

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

@Composable
fun DropdownMenuSelector(label: String, options: List<String>, selectedIndex: Int, onSelectIndex: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = options.getOrElse(selectedIndex) { "" },
            onValueChange = {},
            modifier = Modifier.width(150.dp),
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectIndex(index)
                        expanded = false
                    }
                )
            }
        }
    }
}
