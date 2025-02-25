package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel

@Composable
fun TransactionsScreen(viewModel: TransactionViewModel) {
    // If you want to show all transactions here as well, you can reuse the same list:
    val transactions by viewModel.transactions.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "All Transactions",
            style = MaterialTheme.typography.titleLarge
        )
        LazyColumn {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}
