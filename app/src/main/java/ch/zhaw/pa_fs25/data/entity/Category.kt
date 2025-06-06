package ch.zhaw.pa_fs25.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "categories",indices = [Index(value = ["name"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val budgetLimit: Double = 0.0,
    val keywords: String = ""
)

// Extension function to get the name of a category by its id
fun List<Category>.getCategoryName(categoryId: Int): String {
    return this.find { it.id == categoryId }?.name ?: "Unknown"
}

