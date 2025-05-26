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
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: TransactionViewModel
) {
    val selectedCategories = remember { mutableStateListOf<String>() }
    val customCategory = remember { mutableStateOf("") }

    val suggestedCategories = listOf(
        "Groceries", "Transportation", "Dining Out", "Health", "Entertainment",
        "Education", "Clothing", "Utilities", "Insurance", "Travel",
        "Gifts", "Electronics", "Fees", "Savings"
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
            Text(
                text = "Customize Your Budget",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose categories that reflect your spending habits.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Suggested Categories",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

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
                            if (isSelected) selectedCategories.remove(category)
                            else selectedCategories.add(category)
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
                        }
                        ,
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

            Text(
                text = "Create Your Own Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customCategory.value,
                    onValueChange = { customCategory.value = it },
                    label = { Text("e.g. Coffee", color = MaterialTheme.colorScheme.onBackground) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = {
                        val name = customCategory.value.trim()
                        if (name.isNotEmpty() && name !in selectedCategories) {
                            selectedCategories.add(name)
                            customCategory.value = ""
                        }
                    },
                    enabled = customCategory.value.trim().isNotEmpty()
                ) {
                    Text("Add")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    selectedCategories.forEach { viewModel.addCategory(Category(name = it)) }
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
