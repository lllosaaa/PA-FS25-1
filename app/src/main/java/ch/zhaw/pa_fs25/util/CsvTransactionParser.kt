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

/**
 * Interface for parsing transaction files (CSV, MT940, etc.).
 */
interface TransactionParser {
    /**
     * Parse the file at [uri] and return a list of Transaction objects.
     */
    suspend fun parse(context: Context, uri: Uri, defaultCategory: Category?): List<Transaction>
}

/**
 * CSV parser for the columns:
 *   0: Type
 *   1: Product
 *   2: Started Date
 *   3: Completed Date
 *   4: Description
 *   5: Amount
 *   6: Fee
 *   7: Currency
 *   8: State
 *   9: Balance
 *
 * This example uses the "Completed Date" (index 3) as the date,
 * the "Description" (index 4) for the transaction description,
 * the "Amount" (index 5) for the amount,
 * and the "Type" (index 0) for the transaction type.
 */
class CsvTransactionParser : TransactionParser {

    // Adjust this to match the actual date format in your CSV (e.g. "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", etc.)
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun parse(context: Context, uri: Uri, defaultCategory: Category?): List<Transaction> {
        val transactions = mutableListOf<Transaction>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = CSVReader(InputStreamReader(inputStream))
                val allRows = reader.readAll()
                reader.close()

                // If the first row contains "Type", treat it as a header row and skip it.
                val dataRows = if (allRows.isNotEmpty() && allRows[0].any { it.contains("Type", ignoreCase = true) }) {
                    allRows.drop(1)
                } else {
                    allRows
                }

                dataRows.forEach { row ->
                    // We expect at least 6 columns to parse date, description, amount, and type
                    if (row.size >= 6) {
                        try {
                            val completedDate = row[3].trim()
                            val date = dateFormatter.parse(completedDate)
                            val description = row[4].trim()
                            val amountStr = row[5].trim()
                            val type = row[0].trim()

                            if (date != null && amountStr.isNotEmpty()) {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                val categoryId = defaultCategory?.id ?: 1
                                val transaction = Transaction(
                                    description = description,
                                    amount = amount,
                                    date = date,
                                    categoryId = categoryId,
                                    type = type // Or transform this string into "Income"/"Expense" if needed
                                )
                                transactions.add(transaction)
                            } else {
                                Log.e("CsvTransactionParser", "Date or amount parse failed for row: ${row.contentToString()}")
                            }
                        } catch (e: Exception) {
                            Log.e("CsvTransactionParser", "Error parsing row: ${row.contentToString()}", e)
                        }
                    } else {
                        Log.w("CsvTransactionParser", "Skipping row (insufficient columns): ${row.contentToString()}")
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
