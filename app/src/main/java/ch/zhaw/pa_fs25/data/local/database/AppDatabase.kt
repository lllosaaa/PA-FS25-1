package ch.zhaw.pa_fs25.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.local.dao.TransactionDao

@Database(
    entities = [Transaction::class],
    version = 2, // CHANGED: increment version from 1 to 2
    exportSchema = false
)
@TypeConverters(Converters::class) // CHANGED: register the Converters
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
                    // For a real app with existing data, implement a migration.
                    // For demo purposes, fallbackToDestructiveMigration will DROP the old table.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
