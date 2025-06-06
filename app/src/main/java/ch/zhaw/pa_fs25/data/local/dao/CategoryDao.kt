package ch.zhaw.pa_fs25.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.zhaw.pa_fs25.data.entity.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)
    suspend fun countByName(name: String): Int {
        return getAllCategories().first().count { it.name == name }
    }
    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("SELECT COUNT(*) FROM transactions WHERE categoryId = :categoryId")
    suspend fun countTransactionsWithCategory(categoryId: Int): Int

    @Query("UPDATE categories SET budgetLimit = :newLimit WHERE id = :categoryId")
    suspend fun updateBudgetLimit(categoryId: Int, newLimit: Double)

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE categoryId = :categoryId AND amount < 0")
    suspend fun getSpentForCategory(categoryId: Int): Double

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Category?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long


}
