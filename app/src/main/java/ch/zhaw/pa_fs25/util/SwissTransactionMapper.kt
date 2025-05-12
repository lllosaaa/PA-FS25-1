package ch.zhaw.pa_fs25.util
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.model.SwissTransaction
import java.text.SimpleDateFormat
import java.util.*

object SwissTransactionMapper {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun map(tx: SwissTransaction, categories: List<Category>, defaultCategoryId: Int): Transaction {
        return Transaction(
            description = tx.remittanceInformationUnstructured ?: tx.creditorName ?: "No description",
            amount = tx.amount.amount.toDoubleOrNull() ?: 0.0,
            date = sdf.parse(tx.bookingDate) ?: Date(),
            categoryId = TransactionsCategorizer.detectCategoryId(
                tx.remittanceInformationUnstructured ?: "", categories, defaultCategoryId
            ),
            type = if ((tx.amount.amount.toDoubleOrNull() ?: 0.0) < 0) "Expense" else "Income"
        )
    }

    fun detectCategoryId(
        description: String,
        categoryList: List<Category>,
        defaultCategoryId: Int
    ): Int {

        val lowerDescription = description.lowercase()
        for (category in categoryList) {
            if (lowerDescription.contains(category.name.lowercase())) {
                return category.id
            }
        }
        return defaultCategoryId
    }
}
