package ch.zhaw.pa_fs25.data.local.dao

import androidx.room.*
import ch.zhaw.pa_fs25.data.entity.Budget

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    suspend fun getBudgetsForMonth(month: Int, year: Int): List<Budget>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year LIMIT 1")
    suspend fun getBudgetForCategory(categoryId: Int, month: Int, year: Int): Budget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId AND month = :month AND year = :year")
    suspend fun deleteBudget(categoryId: Int, month: Int, year: Int)
}
