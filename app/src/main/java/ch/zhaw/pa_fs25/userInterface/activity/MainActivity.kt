package ch.zhaw.pa_fs25.userInterface.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import ch.zhaw.pa_fs25.ui.theme.PAFS25Theme
import ch.zhaw.pa_fs25.userInterface.screen.OnboardingScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(
            database.transactionDao(),
            categoryDao = database.categoryDao(),
            budgetDao = database.budgetDao()
        )
        CoroutineScope(Dispatchers.IO).launch {
            repository.ensureDefaultMiscCategory()
        }

        val viewModelFactory = TransactionViewModel.Factory(repository)

        setContent {
            PAFS25Theme {
                val transactionViewModel: TransactionViewModel = viewModel(factory = viewModelFactory)
                val context = this
                val showOnboarding = remember { mutableStateOf(isFirstLaunch(context)) }

                if (showOnboarding.value) {
                    OnboardingScreen(
                        onFinish = {
                            setFirstLaunchDone(context)
                            showOnboarding.value = false
                        },
                        viewModel = transactionViewModel
                    )
                } else {
                    MainScreen(
                        viewModel = transactionViewModel,
                        repository = repository
                    )
                }
            }
        }
    }

    private fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_first_launch", true)
    }

    private fun setFirstLaunchDone(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_first_launch", false).apply()
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
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route ?: "dashboard"
                    Text(text = currentRoute.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    })
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
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
                BudgetScreen(viewModel)
            }
            composable("settings") {
                SettingsScreen(repository, viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: Painter,
    val label: String
)
