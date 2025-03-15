package ch.zhaw.pa_fs25.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import androidx.room.ForeignKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val amount: Double,
    val date: Date,
    val categoryId: Int, // Now references the `id` in categories
    val type: String
)



