package ch.zhaw.pa_fs25.data.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryExtensionsTest {
    @Test
    fun `getCategoryName returns correct name`() {
        val categories = listOf(
            Category(id = 1, name = "Food"),
            Category(id = 2, name = "Travel")
        )

        assertEquals("Travel", categories.getCategoryName(2))
    }

    @Test
    fun `getCategoryName returns Unknown for missing id`() {
        val categories = listOf(Category(id = 1, name = "Food"))

        assertEquals("Unknown", categories.getCategoryName(99))
    }
}
