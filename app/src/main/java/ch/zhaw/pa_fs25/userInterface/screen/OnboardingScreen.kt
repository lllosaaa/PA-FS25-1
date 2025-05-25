package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val selectedCategories = remember { mutableStateListOf<String>() }
    val customCategory = remember { mutableStateOf("") }

    val suggestedCategories = listOf(
        "Groceries", "Transportation", "Dining Out", "Health", "Entertainment",
        "Education", "Clothing", "Utilities", "Insurance", "Travel",
        "Gifts", "Electronics", "Fees/Exchange", "Miscellaneous"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Set up your categories",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(12.dp))
            Text(
                "Suggested Categories:",
                color = MaterialTheme.colorScheme.onBackground
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedCategories.forEach { category ->
                    val isSelected = category in selectedCategories
                    AssistChip(
                        onClick = {
                            if (isSelected) selectedCategories.remove(category)
                            else selectedCategories.add(category)
                        },
                        label = {
                            Text(
                                category,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Add Custom Category:",
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = customCategory.value,
                onValueChange = { customCategory.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = {
                    Text("Custom Category", color = MaterialTheme.colorScheme.onBackground)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val name = customCategory.value.trim()
                    if (name.isNotEmpty() && name !in selectedCategories) {
                        selectedCategories.add(name)
                        customCategory.value = ""
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Add", color = MaterialTheme.colorScheme.onPrimary)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    selectedCategories.forEach {
                        if (it != "Miscellaneous") {
                            viewModel.addCategory(Category(name = it))
                        }
                    }
                   // viewModel.ensureMiscCategory() // Ensure Misc always exists
                    onFinish()
                },
                enabled = selectedCategories.isNotEmpty(),
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Continue", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
