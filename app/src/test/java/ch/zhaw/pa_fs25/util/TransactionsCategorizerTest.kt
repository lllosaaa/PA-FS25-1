package ch.zhaw.pa_fs25.util

import ch.zhaw.pa_fs25.data.entity.Category
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionsCategorizerTest {
    @Test
    fun `categorizeTransaction matches keywords`() {
        val category = TransactionsCategorizer.categorizeTransaction("Spent at Coop store")
        assertEquals("Groceries/Supermarkets", category)
    }

    @Test
    fun `categorizeTransaction returns Miscellaneous when no match`() {
        val category = TransactionsCategorizer.categorizeTransaction("Random thing")
        assertEquals("Miscellaneous", category)
    }

    @Test
    fun `categorizeTransactions groups by category`() {
        val result = TransactionsCategorizer.categorizeTransactions(listOf("Coop purchase", "Bus ticket"))
        assertEquals(2, result.size)
        assertEquals(listOf("Coop purchase"), result["Groceries/Supermarkets"])
    }

    @Test
    fun `detectCategoryId returns category id for keyword`() {
        val categories = listOf(
            Category(id = 1, name = "Miscellaneous"),
            Category(id = 2, name = "Groceries/Supermarkets", keywords = "coop")
        )
        val id = TransactionsCategorizer.detectCategoryId("Coop City", categories, 1, type = "Expense")
        assertEquals(2, id)
    }

    @Test
    fun `detectCategoryId falls back to default for income`() {
        val categories = listOf(Category(id = 1, name = "Miscellaneous"))
        val id = TransactionsCategorizer.detectCategoryId("Salary", categories, 1, type = "Income")
        assertEquals(1, id)
    }
}
