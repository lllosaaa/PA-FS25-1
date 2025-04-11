package ch.zhaw.pa_fs25.data.parser

import android.content.Context
import android.net.Uri
import android.util.Log
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import ch.zhaw.pa_fs25.util.TransactionsCategorizer

interface TransactionParser {
    suspend fun parse(context: Context, uri: Uri, categories: List<Category>, defaultCategory: Category?): List<Transaction>
}

class CsvTransactionParser : TransactionParser {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun parse(context: Context, uri: Uri, categories: List<Category>, defaultCategory: Category?): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                val allRows = reader.readAll()
                reader.close()

                val dataRows = if (allRows.isNotEmpty() && allRows[0].any { it.contains("Type", ignoreCase = true) }) {
                    allRows.drop(1)
                } else {
                    allRows
                }

                dataRows.forEach { row ->
                    if (row.size >= 6) {
                        try {
                            val completedDate = row[3].trim()
                            val date = dateFormatter.parse(completedDate)
                            val description = row[4].trim()
                            val amountStr = row[5].trim()
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            val type = if (amount < 0) "Expense" else "Income"

                            if (date != null) {
                                val categoryId = TransactionsCategorizer.detectCategoryId(
                                    description,
                                    categories,
                                    defaultCategory?.id ?: 1
                                )

                                val transaction = Transaction(
                                    description = description,
                                    amount = amount,
                                    date = date,
                                    categoryId = categoryId,
                                    type = type
                                )
                                transactions.add(transaction)
                            } else {
                                Log.e("CsvTransactionParser", "Invalid date in row: ${row.contentToString()}")
                            }
                        } catch (e: Exception) {
                            Log.e("CsvTransactionParser", "Error parsing row: ${row.contentToString()}", e)
                        }
                    } else {
                        Log.w("CsvTransactionParser", "Skipping incomplete row: ${row.contentToString()}")
                    }
                }
            } ?: Log.e("CsvTransactionParser", "Could not open InputStream for URI: $uri")
        } catch (e: Exception) {
            Log.e("CsvTransactionParser", "Error reading CSV file", e)
        }

        Log.d("CsvTransactionParser", "Parsed ${transactions.size} transactions.")
        return transactions
    }
}
