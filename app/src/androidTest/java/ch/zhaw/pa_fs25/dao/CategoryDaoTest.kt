package ch.zhaw.pa_fs25.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import ch.zhaw.pa_fs25.data.entity.Category
import ch.zhaw.pa_fs25.data.entity.Transaction
import ch.zhaw.pa_fs25.data.local.dao.CategoryDao
import ch.zhaw.pa_fs25.data.local.dao.TransactionDao
import ch.zhaw.pa_fs25.data.local.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
@SmallTest
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        categoryDao = db.categoryDao()
        transactionDao = db.transactionDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndGetCategory_byName() = runBlocking {
        val cat = Category(name = "TestCategory", budgetLimit = 50.0)
        categoryDao.insertCategory(cat)

        val categories = categoryDao.getAllCategories().first()
        assertEquals(1, categories.size)
        assertEquals("TestCategory", categories[0].name)

        val fetched = categoryDao.getByName("TestCategory")
        assertNotNull(fetched)
        assertEquals("TestCategory", fetched!!.name)
        assertEquals(50.0, fetched.budgetLimit, 0.0)
    }


}

