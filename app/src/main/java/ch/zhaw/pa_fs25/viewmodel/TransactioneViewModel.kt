package ch.zhaw.pa_fs25.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.remote.SwissNextGenApi
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.util.SwissTransactionMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class TransactionViewModel(private val repository: FinanceRepository) : ViewModel() {

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val selectedMonthYear = MutableStateFlow<Pair<Int, Int>?>(null)

    fun setFilterMonthYear(month: Int, year: Int) {
        selectedMonthYear.value = month to year
    }

    val filteredTransactions: StateFlow<List<Transaction>> =
        combine(transactions, selectedMonthYear) { allTransactions, monthYear ->
            if (monthYear == null) return@combine allTransactions
            val (month, year) = monthYear
            allTransactions.filter {
                val cal = Calendar.getInstance().apply { time = it.date }
                cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setBudgetForCategory(categoryId: Int, month: Int, year: Int, amount: Double) {
        viewModelScope.launch {
            if (amount == 0.0) {
                repository.deleteBudget(categoryId, month, year)
            } else {
                repository.setBudgetForCategory(categoryId, month, year, amount)
            }
        }
    }

    @Composable
    fun rememberBudgetState(categoryId: Int, month: Int, year: Int): State<Double> {
        return produceState(initialValue = 0.0, categoryId, month, year) {
            value = repository.getBudgetForCategory(categoryId, month, year)
        }
    }

    @Composable
    fun rememberBudget(categoryId: Int, month: Int, year: Int): Double {
        val state = rememberBudgetState(categoryId, month, year)
        return state.value
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }

    fun addCategory(category: Category) {
        viewModelScope.launch {
            repository.insertCategory(category)
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

    fun deleteAllTransactions() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
        }
    }

    fun deleteCategory(category: Category, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteCategoryIfUnused(category)
            onResult(deleted)
        }
    }

    fun updateCategoryBudget(categoryId: Int, newLimit: Double) {
        viewModelScope.launch {
            repository.updateBudgetLimit(categoryId, newLimit)
        }
    }

    suspend fun getSpentForCategory(categoryId: Int): Double {
        return repository.getSpentForCategory(categoryId)
    }

    fun importSwissMockTransactions(api: SwissNextGenApi, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val accounts = api.getAccounts().accounts
                val firstAccount = accounts.firstOrNull() ?: return@launch

                // ✅ Use href exactly as provided by the mock server
                val rawHref = firstAccount._links.transactions.href
                val fullUrl = "http://10.0.2.2:3000$rawHref"

                val txResponse = api.getTransactionsByUrl(fullUrl)

                val categoryList = categories.value
                val defaultCategoryId = categoryList.firstOrNull()?.id ?: 1

                val converted = txResponse.transactions.booked.map {
                    val description = it.remittanceInformationUnstructured ?: it.creditorName ?: "No description"
                    val transaction = SwissTransactionMapper.map(it, categoryList, defaultCategoryId)
                    transaction.copy(
                        categoryId = SwissTransactionMapper.detectCategoryId(description, categoryList, defaultCategoryId)
                    )
                }

                converted.forEach { addTransaction(it) }
                onResult(converted.size)

            } catch (e: Exception) {
                e.printStackTrace()
                onResult(0)
            }
        }
    }


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
