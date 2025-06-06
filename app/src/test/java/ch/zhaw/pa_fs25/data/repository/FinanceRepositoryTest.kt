package ch.zhaw.pa_fs25.data.repository

import ch.zhaw.pa_fs25.data.entity.Budget
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.local.dao.BudgetDao
import ch.zhaw.pa_fs25.data.local.dao.CategoryDao
import ch.zhaw.pa_fs25.data.local.dao.TransactionDao
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class FinanceRepositoryTest {

    private lateinit var transactionDao: TransactionDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var budgetDao: BudgetDao
    private lateinit var repository: FinanceRepository

    @Before
    fun setUp() {
        transactionDao = mockk(relaxed = true)
        categoryDao = mockk(relaxed = true)
        budgetDao = mockk(relaxed = true)
        repository = FinanceRepository(transactionDao, categoryDao, budgetDao)
    }


    @Test
    fun `ensureDefaultMiscCategory does nothing if already exists`() = runTest {
        coEvery { categoryDao.getByName("Miscellaneous") } returns Category(id = 1, name = "Miscellaneous")

        repository.ensureDefaultMiscCategory()

        coVerify(exactly = 0) { categoryDao.insert(any()) }
    }

    @Test
    fun `deleteCategoryIfUnused deletes when count is zero`() = runTest {
        val category = Category(id = 2, name = "Test")
        coEvery { categoryDao.countTransactionsWithCategory(2) } returns 0
        coEvery { categoryDao.deleteCategory(category) } just Runs

        val result = repository.deleteCategoryIfUnused(category)

        assertTrue(result)
        coVerify { categoryDao.deleteCategory(category) }
    }

    @Test
    fun `deleteCategoryIfUnused does not delete when in use`() = runTest {
        val category = Category(id = 2, name = "Test")
        coEvery { categoryDao.countTransactionsWithCategory(2) } returns 3

        val result = repository.deleteCategoryIfUnused(category)

        assertFalse(result)
        coVerify(exactly = 0) { categoryDao.deleteCategory(any()) }
    }

    @Test
    fun `updateBudgetLimit calls DAO`() = runTest {
        repository.updateBudgetLimit(1, 150.0)
        coVerify { categoryDao.updateBudgetLimit(1, 150.0) }
    }

    @Test
    fun `getSpentForCategory returns value from DAO`() = runTest {
        coEvery { categoryDao.getSpentForCategory(1) } returns 123.45
        val spent = repository.getSpentForCategory(1)
        assertEquals(123.45, spent)
    }

    @Test
    fun `getBudgetForCategory returns budget amount`() = runTest {
        coEvery { budgetDao.getBudgetForCategory(1, 5, 2025) } returns Budget(id = 1, categoryId = 1, month = 5, year = 2025, limitAmount = 99.99)
        val result = repository.getBudgetForCategory(1, 5, 2025)
        assertEquals(99.99, result)
    }

    @Test
    fun `getBudgetForCategory returns zero if null`() = runTest {
        coEvery { budgetDao.getBudgetForCategory(1, 5, 2025) } returns null
        val result = repository.getBudgetForCategory(1, 5, 2025)
        assertEquals(0.0, result)
    }

    @Test
    fun `setBudgetForCategory inserts budget`() = runTest {
        coEvery { budgetDao.insertBudget(any()) } just Runs

        repository.setBudgetForCategory(2, 4, 2025, 55.5)

        coVerify {
            budgetDao.insertBudget(match {
                it.categoryId == 2 && it.month == 4 && it.year == 2025 && it.limitAmount == 55.5
            })
        }
    }

    @Test
    fun `getBudgetsForMonth returns list`() = runTest {
        val budgets = listOf(
            Budget(categoryId = 1, month = 4, year = 2025, limitAmount = 50.0),
            Budget(categoryId = 2, month = 4, year = 2025, limitAmount = 75.0)
        )
        coEvery { budgetDao.getBudgetsForMonth(4, 2025) } returns budgets
        val result = repository.getBudgetsForMonth(4, 2025)
        assertEquals(budgets, result)
    }

    @Test
    fun `deleteBudget delegates to DAO`() = runTest {
        coEvery { budgetDao.deleteBudget(3, 6, 2025) } just Runs
        repository.deleteBudget(3, 6, 2025)
        coVerify { budgetDao.deleteBudget(3, 6, 2025) }
    }
}
