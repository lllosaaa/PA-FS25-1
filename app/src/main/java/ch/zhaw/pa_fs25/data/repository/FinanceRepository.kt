package ch.zhaw.pa_fs25.data.repository

import ch.zhaw.pa_fs25.data.entity.Budget
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.local.dao.BudgetDao
import ch.zhaw.pa_fs25.data.local.dao.CategoryDao
import ch.zhaw.pa_fs25.data.local.dao.TransactionDao
import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao

) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category)
    }

    suspend fun deleteTransaction(lastTransaction: Transaction) {
        transactionDao.deleteTransaction(lastTransaction)
    }

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    suspend fun ensureDefaultMiscCategory() {
        val miscExists = categoryDao.getByName("Miscellaneous") != null
        if (!miscExists) {
            categoryDao.insert(Category(name = "Miscellaneous"))
        }
    }




    suspend fun deleteCategoryIfUnused(category: Category): Boolean {
        val count = categoryDao.countTransactionsWithCategory(category.id)
        return if (count == 0) {
            categoryDao.deleteCategory(category)
            true
        } else {
            false // Cannot delete if it's in use
        }
    }

    suspend fun updateBudgetLimit(categoryId: Int, newLimit: Double) {
        categoryDao.updateBudgetLimit(categoryId, newLimit)
    }

    suspend fun getSpentForCategory(categoryId: Int): Double {
        return categoryDao.getSpentForCategory(categoryId)
    }

    suspend fun getBudgetForCategory(categoryId: Int, month: Int, year: Int): Double {
        return budgetDao.getBudgetForCategory(categoryId, month, year)?.limitAmount ?: 0.0
    }

    suspend fun setBudgetForCategory(categoryId: Int, month: Int, year: Int, amount: Double) {
        budgetDao.insertBudget(Budget(categoryId = categoryId, month = month, year = year, limitAmount = amount))
    }

    suspend fun getBudgetsForMonth(month: Int, year: Int): List<Budget> {
        return budgetDao.getBudgetsForMonth(month, year)
    }

    suspend fun deleteBudget(categoryId: Int, month: Int, year: Int) {
        budgetDao.deleteBudget(categoryId, month, year)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun updateTransactionCategory(transactionId: Int, newCategoryId: Int) {
        transactionDao.updateTransactionCategory(transactionId, newCategoryId)
    }





}
