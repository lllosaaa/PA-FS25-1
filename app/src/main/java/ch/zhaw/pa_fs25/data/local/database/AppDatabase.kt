package ch.zhaw.pa_fs25.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.local.dao.TransactionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.transactionDao()


                                    dao.insertTransaction(
                                        Transaction(
                                            description = "Spesa Supermercato",
                                            amount = -45.50,
                                            date = "2025-01-01",
                                            category = "Groceries",
                                            type = "Expense"
                                        )
                                    )
                                    dao.insertTransaction(
                                        Transaction(
                                            description = "Abbonamento Palestra",
                                            amount = -30.00,
                                            date = "2025-01-02",
                                            category = "Fitness",
                                            type = "Expense"
                                        )
                                    )
                                    dao.insertTransaction(
                                        Transaction(
                                            description = "Stipendio",
                                            amount = 2500.00,
                                            date = "2025-01-03",
                                            category = "Salary",
                                            type = "Income"
                                        )
                                    )
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
