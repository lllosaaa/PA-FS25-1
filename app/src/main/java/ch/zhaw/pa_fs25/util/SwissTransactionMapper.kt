package ch.zhaw.pa_fs25.util

import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.model.SwissTransaction
import java.text.SimpleDateFormat
import java.util.*

object SwissTransactionMapper {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun map(tx: SwissTransaction, categories: List<Category>, defaultCategoryId: Int): Transaction {
        val description = tx.remittanceInformationUnstructured ?: tx.creditorName ?: "No description"
        val amount = tx.amount.amount.toDoubleOrNull() ?: 0.0

        return Transaction(
            description = description,
            amount = amount,
            date = sdf.parse(tx.bookingDate) ?: Date(),
            categoryId = TransactionsCategorizer.detectCategoryId(description, categories, defaultCategoryId),
            type = if (amount < 0) "Expense" else "Income"
        )
    }
}
