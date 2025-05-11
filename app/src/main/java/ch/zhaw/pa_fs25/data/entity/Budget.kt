package ch.zhaw.pa_fs25.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int,
    val month: Int, // 0 = Jan, ..., 11 = Dec
    val year: Int,
    val limitAmount: Double
)
