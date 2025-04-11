package ch.zhaw.pa_fs25.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.zhaw.pa_fs25.data.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TransactionDao {

    /**
     * Retrieve all transactions, ordered by date descending.
     */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Insert a single transaction, replacing on conflict.
     * Returns the newly inserted row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    /**
     * Insert multiple transactions, replacing on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<Transaction>)

    /**
     * Delete a specific transaction.
     */
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    /**
     * Delete all transactions and return the number of rows deleted.
     */
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions(): Int

    /**
     * Retrieve transactions within a date range, ordered by date descending.
     * CHANGED: Now uses Date parameters instead of String.
     */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDate(startDate: Date, endDate: Date): Flow<List<Transaction>>

    /**
     * Retrieve transactions by category, ordered by date descending.
     */
    @Query("SELECT * FROM transactions WHERE categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(categoryId: Int): Flow<List<Transaction>>
    /**
     * Get total expenses (sum of all negative amounts).
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount < 0")
    fun getTotalExpenses(): Flow<Double>

    /**
     * Get total income (sum of all positive amounts).
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE amount > 0")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE categoryId = :categoryId AND amount < 0")
    suspend fun getSpentForCategory(categoryId: Int): Double

}
