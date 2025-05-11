package ch.zhaw.pa_fs25.data.parser

import android.content.Context
import android.net.Uri
import android.util.Log
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.util.TransactionsCategorizer
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class CsvTransactionParser : TransactionParser {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun parse(
        context: Context,
        uri: Uri,
        categories: List<Category>,
        defaultCategory: Category?
    ): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return logError("Could not open InputStream for URI: $uri")

            CSVReader(InputStreamReader(inputStream)).use { reader ->
                val allRows = reader.readAll()
                val dataRows = if (allRows.isNotEmpty() && allRows[0].any { it.contains("Type", ignoreCase = true) }) {
                    allRows.drop(1)
                } else {
                    allRows
                }

                for (row in dataRows) {
                    if (row.size < 6) {
                        Log.w("CsvTransactionParser", "Skipping incomplete row: ${row.contentToString()}")
                        continue
                    }

                    val transaction = parseRow(row, categories, defaultCategory)
                    if (transaction != null) transactions.add(transaction)
                }
            }
        } catch (e: Exception) {
            Log.e("CsvTransactionParser", "Error reading CSV file", e)
        }

        Log.d("CsvTransactionParser", "Parsed ${transactions.size} transactions.")
        return transactions
    }

    private fun parseRow(
        row: Array<String>,
        categories: List<Category>,
        defaultCategory: Category?
    ): Transaction? {
        return try {
            val date = dateFormatter.parse(row[3].trim()) ?: return null
            val description = row[4].trim()
            val amount = row[5].trim().toDoubleOrNull() ?: 0.0
            val type = if (amount < 0) "Expense" else "Income"
            val categoryId = TransactionsCategorizer.detectCategoryId(description, categories, defaultCategory?.id ?: 1)

            Transaction(
                description = description,
                amount = amount,
                date = date,
                categoryId = categoryId,
                type = type
            )
        } catch (e: Exception) {
            Log.e("CsvTransactionParser", "Error parsing row: ${row.contentToString()}", e)
            null
        }
    }

    private fun logError(message: String): List<Transaction> {
        Log.e("CsvTransactionParser", message)
        return emptyList()
    }
}
