package ch.zhaw.pa_fs25.util

import android.content.Context
import android.net.Uri
import android.util.Log
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.parser.TransactionParser
import ch.zhaw.pa_fs25.util.TransactionsCategorizer
import com.opencsv.CSVReaderBuilder
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class RaiffeisenTransactionParser : TransactionParser {

    private val dateFormat = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())

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

            val parser = com.opencsv.CSVParserBuilder().withSeparator(';').build()
            val reader = com.opencsv.CSVReaderBuilder(InputStreamReader(inputStream))
                .withSkipLines(1)
                .withCSVParser(parser)
                .build()


            reader.forEach { row ->
                if (row.size < 6) {
                    Log.w("RaiffeisenParser", "Skipping incomplete row: ${row.contentToString()}")
                    return@forEach
                }

                try {
                    val date = dateFormat.parse(row[1].trim()) ?: return@forEach
                    val description = row[2].trim()
                    val amount = row[3].trim().replace(",", ".").toDoubleOrNull() ?: return@forEach
                    val type = if (amount < 0) "Expense" else "Income"

                    val categoryId = TransactionsCategorizer.detectCategoryId(
                        description, categories, defaultCategory?.id ?: 1
                    )

                    val transaction = Transaction(
                        description = description,
                        amount = amount,
                        date = date,
                        categoryId = categoryId,
                        type = type
                    )
                    transactions.add(transaction)
                } catch (e: Exception) {
                    Log.e("RaiffeisenParser", "Failed to parse row: ${row.contentToString()}", e)
                }
            }

        } catch (e: Exception) {
            Log.e("RaiffeisenParser", "Failed to parse file", e)
        }

        Log.d("RaiffeisenParser", "Parsed ${transactions.size} transactions.")
        return transactions
    }

    private fun logError(message: String): List<Transaction> {
        Log.e("RaiffeisenParser", message)
        return emptyList()
    }
}
