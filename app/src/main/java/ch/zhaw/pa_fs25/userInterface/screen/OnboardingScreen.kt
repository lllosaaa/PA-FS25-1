package ch.zhaw.pa_fs25.userInterface.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.viewmodel.TransactionViewModel
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: TransactionViewModel
) {
    var showWelcomeDialog by remember { mutableStateOf(true) }
    val selectedCategories = remember { mutableStateListOf<String>() }
    val categoryKeywords = remember { mutableStateMapOf<String, String>() }
    val customCategory = remember { mutableStateOf("") }

    val suggestedCategories = listOf(
        "Groceries/Supermarkets", "Transportation", "Restaurants/Dining", "Health/Medical", "Entertainment",
        "Education", "Clothing/Apparel", "Utilities", "Insurance", "Travel",
        "Gifts", "Electronics", "ATM fees/Bank charges"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            if (showWelcomeDialog) {
                AlertDialog(
                    onDismissRequest = { showWelcomeDialog = false },
                    confirmButton = {
                        Button(onClick = { showWelcomeDialog = false }) {
                            Text("Got it")
                        }
                    },
                    title = {
                        Text("Welcome!", style = MaterialTheme.typography.titleLarge)
                    },
                    text = {
                        Text("Let’s help you set up your budget. Select the categories you usually spend on, or add your own.")
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    textContentColor = MaterialTheme.colorScheme.onSurface
                )
            }

            Text("Customize Your Budget", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Suggested Categories", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedCategories.forEach { category ->
                    val isSelected = category in selectedCategories
                    AssistChip(
                        onClick = {
                            if (isSelected) {
                                selectedCategories.remove(category)
                                categoryKeywords.remove(category)
                            } else {
                                selectedCategories.add(category)
                                categoryKeywords[category] = ""
                            }
                        },
                        label = {
                            Text(
                                text = category,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.Transparent
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

            Spacer(modifier = Modifier.height(24.dp))
            Text("Create Your Own Category", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customCategory.value,
                    onValueChange = { customCategory.value = it },
                    label = { Text("e.g. Subscriptions") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val name = customCategory.value.trim()
                        if (name.isNotEmpty() && name !in selectedCategories) {
                            selectedCategories.add(name)
                            categoryKeywords[name] = ""
                            customCategory.value = ""
                        }
                    },
                    enabled = customCategory.value.trim().isNotEmpty()
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(selectedCategories) { category ->
                    if (!suggestedCategories.contains(category)) {
                        Column {
                            Text(text = "$category Keywords", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = categoryKeywords[category] ?: "",
                                onValueChange = { categoryKeywords[category] = it },
                                label = { Text("Comma-separated keywords (e.g. a, b, c)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }


            Button(
                onClick = {
                    selectedCategories.forEach {
                        val keywords = categoryKeywords[it]?.trim() ?: ""
                        viewModel.addCategory(Category(name = it, keywords = keywords))
                    }
                    onFinish()
                },
                enabled = selectedCategories.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
