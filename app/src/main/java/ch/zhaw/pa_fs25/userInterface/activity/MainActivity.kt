package ch.zhaw.pa_fs25.userInterface.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import ch.zhaw.pa_fs25.data.local.database.AppDatabase
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.userInterface.screen.DashboardScreen
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the database and repository
        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(database.transactionDao())
        // Create a factory for the ViewModel
        val viewModelFactory = TransactionViewModel.Factory(repository)

        setContent {
            // Obtain the ViewModel using the factory
            val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
            DashboardScreen(viewModel = transactionViewModel)
        }
    }
}