package ch.zhaw.pa_fs25.util

import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.model.SwissTransaction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class SwissTransactionMapperTest {
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Test
    fun `map converts swiss transaction correctly`() {
        val tx = SwissTransaction(
            transactionId = "1",
            bookingDate = "2024-05-01",
            transactionAmount = SwissTransaction.Amount(amount = "12.50", currency = "CHF"),
            creditorName = "Coop",
            remittanceInformationUnstructured = null
        )

        val categories = listOf(
            Category(id = 1, name = "Miscellaneous"),
            Category(id = 2, name = "Groceries/Supermarkets", keywords = "coop")
        )

        val mapped = SwissTransactionMapper.map(tx, categories, 1)

        assertEquals("Coop", mapped.description)
        assertEquals(12.50, mapped.amount, 0.001)
        assertEquals(sdf.parse("2024-05-01"), mapped.date)
        assertEquals(2, mapped.categoryId)
        assertEquals("Income", mapped.type)
    }

    @Test
    fun `map uses remittance info and sets expense type`() {
        val tx = SwissTransaction(
            transactionId = "2",
            bookingDate = "2024-05-02",
            transactionAmount = SwissTransaction.Amount(amount = "-5.00", currency = "CHF"),
            creditorName = "ignored",
            remittanceInformationUnstructured = "Coffee Shop"
        )

        val categories = listOf(
            Category(id = 1, name = "Miscellaneous"),
            Category(id = 3, name = "Restaurants/Dining", keywords = "coffee")
        )

        val mapped = SwissTransactionMapper.map(tx, categories, 1)

        assertEquals("Coffee Shop", mapped.description)
        assertEquals(-5.00, mapped.amount, 0.001)
        assertEquals(sdf.parse("2024-05-02"), mapped.date)
        assertEquals(3, mapped.categoryId)
        assertEquals("Expense", mapped.type)
    }
}
