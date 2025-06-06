package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.userInterface.component.CategoryBudgetChart
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel

import java.util.*

@Composable
fun DashboardScreen(viewModel: TransactionViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val calendar = remember { Calendar.getInstance() }
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    val visibleCategories = categories.filter {
        viewModel.rememberBudget(it.id, selectedMonth, selectedYear) > 0
    }

    Column {
        Row {
            Text(text = "Budget Overview", style = MaterialTheme.typography.titleLarge)
        }
        Row {
            MonthYearPicker(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChange = { selectedMonth = it },
                onYearChange = { selectedYear = it }
            )
        }
        CategoryBudgetOverview(
            categories = visibleCategories,
            transactions = transactions,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            viewModel = viewModel
        )

    }


}

@Composable
fun CategoryBudgetOverview(
    categories: List<Category>,
    transactions: List<Transaction>,
    selectedMonth: Int,
    selectedYear: Int,
    viewModel: TransactionViewModel
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val budgetLimit by viewModel.rememberBudgetState(category.id, selectedMonth, selectedYear)
            val spent = transactions
                .filter { it.categoryId == category.id && it.amount < 0 }
                .filter {
                    val cal = Calendar.getInstance().apply { time = it.date }
                    cal.get(Calendar.MONTH) == selectedMonth && cal.get(Calendar.YEAR) == selectedYear
                }
                .sumOf { it.amount }


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CategoryBudgetChart(
                        categoryName = category.name,
                        budgetLimit = budgetLimit,
                        spentAmount = spent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Remaining: ${"%.2f".format(budgetLimit + spent)} CHF",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (budgetLimit + spent < 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
