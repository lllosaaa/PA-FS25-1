package ch.zhaw.pa_fs25.data.parser

import android.content.Context
import android.net.Uri
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import java.io.BufferedReader
import java.io.InputStreamReader

class UniversalTransactionParser : TransactionParser {

    private val raiffeisenKeywords = listOf("IBAN", "Booked At", "Valuta Date")
    private val revolutKeywords = listOf("Type", "Product", "Completed Date")

    private val raiffeisen = RaiffeisenTransactionParser()
    private val revolut = CsvTransactionParser()

    override suspend fun parse(
        context: Context,
        uri: Uri,
        categories: List<Category>,
        defaultCategory: Category?
    ): List<Transaction> {
        val resolvedDefaultCategory = categories.find { it.name.equals("Miscellaneous", ignoreCase = true) }
            ?: defaultCategory
            ?: categories.firstOrNull()

        val header = readHeaderLine(context, uri)
        return when {
            raiffeisenKeywords.any { header.contains(it, ignoreCase = true) } -> {
                raiffeisen.parse(context, uri, categories, resolvedDefaultCategory)
            }
            revolutKeywords.any { header.contains(it, ignoreCase = true) } -> {
                revolut.parse(context, uri, categories, resolvedDefaultCategory)
            }
            else -> emptyList()
        }
    }

    private fun readHeaderLine(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readLine() ?: ""
            } ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
