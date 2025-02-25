package ch.zhaw.pa_fs25.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: FinanceRepository) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun deleteLastTransaction() {
        viewModelScope.launch {
            val lastTransaction = transactions.value.lastOrNull()
            if (lastTransaction != null) {
                repository.deleteTransaction(lastTransaction)
            }
        }
    }
    fun deleteFirstTransaction() {
        viewModelScope.launch {
            val firstTransaction = transactions.value.firstOrNull()
            if (firstTransaction != null) {
                repository.deleteTransaction(firstTransaction)
            }
        }
    }

//    fun deleteSelectedTransaction(transaction: Transaction) {
//        viewModelScope.launch {
//            repository.deleteTransaction(transaction)
//        }
//    }

    class Factory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
