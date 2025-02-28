package ch.zhaw.pa_fs25.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

// Extension function to get the name of a category by its id
fun List<Category>.getCategoryName(categoryId: Int): String {
    return this.find { it.id == categoryId }?.name ?: "Unknown"
}

