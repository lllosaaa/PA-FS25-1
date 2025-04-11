package ch.zhaw.pa_fs25.userInterface.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.zhaw.pa_fs25.data.local.database.AppDatabase
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import ch.zhaw.pa_fs25.userInterface.screen.BudgetScreen
import ch.zhaw.pa_fs25.userInterface.screen.DashboardScreen
import ch.zhaw.pa_fs25.userInterface.screen.SettingsScreen
import ch.zhaw.pa_fs25.userInterface.screen.TransactionsScreen
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import androidx.compose.ui.res.painterResource
import ch.zhaw.pa_fs25.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Database + Repository
        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(
            database.transactionDao(),
            categoryDao = database.categoryDao()
        )

        // ViewModel factory
        val viewModelFactory = TransactionViewModel.Factory(repository)

        setContent {
            // Grab the TransactionViewModel
            val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)

            // Show our main screen with bottom nav
            MainScreen(
                transactionViewModel,
                repository = repository
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TransactionViewModel, repository: FinanceRepository) {
    val navController = rememberNavController()

    // Bottom nav items: route, icon, label
    val items = listOf(
        BottomNavItem(
            route = "dashboard",
            icon = painterResource(id = R.drawable.home_24px),
            label = "Dashboard"
        ),
        BottomNavItem(
            route = "transactions",
            //use icon in res/xml
            icon = painterResource(id = R.drawable.sync_alt_24px),
            label = "Transactions"
        ),
        BottomNavItem(
            route = "budget",
            icon = painterResource(id = R.drawable.data_usage_24px),
            label = "Budget"
        ),
        BottomNavItem(
            route = "settings",
            icon = painterResource(id = R.drawable.settings_24px),
            label = "Settings"
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // You can automatically update the title based on the current route:
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route ?: "dashboard"
                    Text(text = currentRoute.capitalize())
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination to avoid building back stack up
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(viewModel)
            }
            composable("transactions") {
                TransactionsScreen(viewModel)
            }
            composable("budget") {
                BudgetScreen()
            }
            composable("settings") {
                SettingsScreen(repository)
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: Painter,
    val label: String
)
