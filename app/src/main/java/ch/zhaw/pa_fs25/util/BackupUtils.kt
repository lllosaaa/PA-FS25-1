package ch.zhaw.pa_fs25.util

import android.content.Context
import android.net.Uri
import ch.zhaw.pa_fs25.data.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import java.io.FileInputStream
import java.io.OutputStreamWriter

/**
 * Writes the entire Room database to the provided URI.
 * Returns true if the backup was successful.
 */
fun saveDatabaseBackup(context: Context, uri: Uri): Boolean {
    return try {
        val dbName = "budget_database" // Use your Room DB name here
        val dbFile = context.getDatabasePath(dbName)
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(dbFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

/**
 * Exports the transaction data as a CSV file to the provided URI.
 * Returns true if the export was successful.
 */
suspend fun saveCsvExport(context: Context, repository: FinanceRepository, uri: Uri): Boolean {
    return try {
        val transactions = repository.getAllTransactions().first()
        val categories = repository.getAllCategories().first()

        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                // Write CSV header
                writer.append("Date,Description,Amount,Type,Category\n")
                // Write each transaction row (adjust date formatting as needed)
                transactions.forEach { t ->
                    val categoryName = categories.find { it.id == t.categoryId }?.name ?: "Unknown"
                    writer.append("${t.date},${t.description},${t.amount},${t.type},${categoryName}\n")
                }
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
