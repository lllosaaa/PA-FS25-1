package ch.zhaw.pa_fs25.data.parser

import android.content.Context
import android.net.Uri
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction

interface TransactionParser {
    suspend fun parse(
        context: Context,
        uri: Uri,
        categories: List<Category>,
        defaultCategory: Category?
    ): List<Transaction>
}