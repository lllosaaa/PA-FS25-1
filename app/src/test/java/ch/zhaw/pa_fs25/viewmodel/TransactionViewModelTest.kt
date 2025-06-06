package ch.zhaw.pa_fs25.viewmodel
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.remote.SwissNextGenApi
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest


@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelTest {

    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: TransactionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getAllTransactions() } returns MutableStateFlow(emptyList())
        every { repository.getAllCategories() } returns MutableStateFlow(emptyList())
        viewModel = TransactionViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteLastTransaction skips if list is empty`() = runTest(testDispatcher) {
        viewModel.deleteLastTransaction()
        coVerify(exactly = 0) { repository.deleteTransaction(any()) }
    }

    @Test
    fun `deleteCategory returns result via callback`() = runTest(testDispatcher) {
        val category = Category(id = 5, name = "Health")
        coEvery { repository.deleteCategoryIfUnused(category) } returns true

        var result = false
        viewModel.deleteCategory(category) { success -> result = success }
        advanceUntilIdle()

        assertTrue(result)
    }

    @Test
    fun `setBudgetForCategory with 0 deletes budget`() = runTest(testDispatcher) {
        coEvery { repository.deleteBudget(1, 3, 2025) } just Runs
        viewModel.setBudgetForCategory(1, 3, 2025, 0.0)
        advanceUntilIdle()
        coVerify { repository.deleteBudget(1, 3, 2025) }
    }

    @Test
    fun `setBudgetForCategory with positive inserts budget`() = runTest(testDispatcher) {
        coEvery { repository.setBudgetForCategory(1, 3, 2025, 200.0) } just Runs
        viewModel.setBudgetForCategory(1, 3, 2025, 200.0)
        advanceUntilIdle()
        coVerify { repository.setBudgetForCategory(1, 3, 2025, 200.0) }
    }

    @Test
    fun `updateCategoryBudget calls repository`() = runTest(testDispatcher) {
        coEvery { repository.updateBudgetLimit(1, 300.0) } just Runs
        viewModel.updateCategoryBudget(1, 300.0)
        advanceUntilIdle()
        coVerify { repository.updateBudgetLimit(1, 300.0) }
    }

    @Test
    fun `getSpentForCategory returns correct value`() = runTest(testDispatcher) {
        coEvery { repository.getSpentForCategory(1) } returns 42.0
        val spent = viewModel.getSpentForCategory(1)
        assertEquals(42.0, spent)
    }

    @Test
    fun `importSwissMockTransactions returns 0 if no account`() = runTest(testDispatcher) {
        val api = mockk<SwissNextGenApi>()
        coEvery { api.getAccounts().accounts } returns emptyList()

        var count = -1
        viewModel.importSwissMockTransactions(api) { imported -> count = imported }
        advanceUntilIdle()

        assertEquals(0, count)
    }
}