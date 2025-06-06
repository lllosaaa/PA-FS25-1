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
    fun `map uses creditor name and sets income type`() {
        val tx = SwissTransaction(
            transactionId = "1",
            bookingDate = "2024-05-01",
            transactionAmount = SwissTransaction.Amount(amount = "100.00", currency = "CHF"),
            creditorName = "Salary Payment",
            remittanceInformationUnstructured = null
        )

        val categories = listOf(
            Category(id = 1, name = "Income"),
            Category(id = 2, name = "Expenses")
        )

        val mapped = SwissTransactionMapper.map(tx, categories, 1)

        assertEquals("Salary Payment", mapped.description)
        assertEquals(100.00, mapped.amount, 0.001)
        assertEquals(sdf.parse("2024-05-01"), mapped.date)
        assertEquals(1, mapped.categoryId)
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

    @Test
    fun `map uses default category when no match found`() {
        val tx = SwissTransaction(
            transactionId = "3",
            bookingDate = "2024-05-03",
            transactionAmount = SwissTransaction.Amount(amount = "-10.00", currency = "CHF"),
            creditorName = "Unknown Transaction",
            remittanceInformationUnstructured = null
        )

        val categories = listOf(
            Category(id = 1, name = "Income"),
            Category(id = 2, name = "Expenses")
        )

        val mapped = SwissTransactionMapper.map(tx, categories, 1)

        assertEquals("Unknown Transaction", mapped.description)
        assertEquals(-10.00, mapped.amount, 0.001)
        assertEquals(sdf.parse("2024-05-03"), mapped.date)
        assertEquals(1, mapped.categoryId) // Default category
        assertEquals("Expense", mapped.type)
    }
}
