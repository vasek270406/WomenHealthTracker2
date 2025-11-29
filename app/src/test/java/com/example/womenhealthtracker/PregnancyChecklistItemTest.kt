package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для PregnancyChecklistItem
 */
class PregnancyChecklistItemTest {

    @Test
    fun `test PregnancyChecklistItem creation with all fields`() {
        val item = PregnancyChecklistItem(
            id = "test_1",
            title = "Test Item",
            trimester = 1,
            completed = true,
            completedDate = "2024-01-15"
        )

        assertEquals("test_1", item.id)
        assertEquals("Test Item", item.title)
        assertEquals(1, item.trimester)
        assertTrue(item.completed)
        assertEquals("2024-01-15", item.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem creation with default values`() {
        val item = PregnancyChecklistItem(
            id = "test_2",
            title = "Test Item",
            trimester = 2
        )

        assertEquals("test_2", item.id)
        assertEquals("Test Item", item.title)
        assertEquals(2, item.trimester)
        assertFalse(item.completed)
        assertEquals("", item.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem toMap`() {
        val item = PregnancyChecklistItem(
            id = "test_3",
            title = "Test Item",
            trimester = 3,
            completed = true,
            completedDate = "2024-01-15"
        )

        val map = item.toMap()

        assertEquals("test_3", map["id"])
        assertEquals("Test Item", map["title"])
        assertEquals(3, map["trimester"])
        assertEquals(true, map["completed"])
        assertEquals("2024-01-15", map["completedDate"])
    }

    @Test
    fun `test PregnancyChecklistItem toMap with default values`() {
        val item = PregnancyChecklistItem(
            id = "test_4",
            title = "Test Item",
            trimester = 1
        )

        val map = item.toMap()

        assertEquals("test_4", map["id"])
        assertEquals("Test Item", map["title"])
        assertEquals(1, map["trimester"])
        assertEquals(false, map["completed"])
        assertEquals("", map["completedDate"])
    }

    @Test
    fun `test PregnancyChecklistItem fromMap with all fields`() {
        val map = mapOf(
            "id" to "test_5",
            "title" to "Test Item",
            "trimester" to 2L,
            "completed" to true,
            "completedDate" to "2024-01-15"
        )

        val item = PregnancyChecklistItem.fromMap(map)

        assertEquals("test_5", item.id)
        assertEquals("Test Item", item.title)
        assertEquals(2, item.trimester)
        assertTrue(item.completed)
        assertEquals("2024-01-15", item.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem fromMap with missing fields`() {
        val map = mapOf(
            "id" to "test_6",
            "title" to "Test Item"
        )

        val item = PregnancyChecklistItem.fromMap(map)

        assertEquals("test_6", item.id)
        assertEquals("Test Item", item.title)
        assertEquals(1, item.trimester) // default value
        assertFalse(item.completed) // default value
        assertEquals("", item.completedDate) // default value
    }

    @Test
    fun `test PregnancyChecklistItem fromMap with null values`() {
        val map = mapOf<String, Any>(
            "id" to null as Any? ?: "",
            "title" to null as Any? ?: "",
            "trimester" to null as Any? ?: 1L,
            "completed" to null as Any? ?: false,
            "completedDate" to null as Any? ?: ""
        )

        val item = PregnancyChecklistItem.fromMap(map)

        assertEquals("", item.id)
        assertEquals("", item.title)
        assertEquals(1, item.trimester)
        assertFalse(item.completed)
        assertEquals("", item.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem roundtrip conversion`() {
        val original = PregnancyChecklistItem(
            id = "test_7",
            title = "Test Item",
            trimester = 3,
            completed = true,
            completedDate = "2024-01-15"
        )

        val map = original.toMap()
        val restored = PregnancyChecklistItem.fromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.title, restored.title)
        assertEquals(original.trimester, restored.trimester)
        assertEquals(original.completed, restored.completed)
        assertEquals(original.completedDate, restored.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem with different trimesters`() {
        val item1 = PregnancyChecklistItem("id1", "Title 1", 1)
        val item2 = PregnancyChecklistItem("id2", "Title 2", 2)
        val item3 = PregnancyChecklistItem("id3", "Title 3", 3)

        assertEquals(1, item1.trimester)
        assertEquals(2, item2.trimester)
        assertEquals(3, item3.trimester)
    }

    @Test
    fun `test PregnancyChecklistItem completed state`() {
        val incompleteItem = PregnancyChecklistItem("id1", "Title", 1, completed = false)
        val completeItem = PregnancyChecklistItem("id2", "Title", 1, completed = true, completedDate = "2024-01-15")

        assertFalse(incompleteItem.completed)
        assertTrue(incompleteItem.completedDate.isEmpty())
        
        assertTrue(completeItem.completed)
        assertEquals("2024-01-15", completeItem.completedDate)
    }

    @Test
    fun `test PregnancyChecklistItem fromMap with invalid trimester type`() {
        val map = mapOf(
            "id" to "test_8",
            "title" to "Test",
            "trimester" to "invalid" // should be Long/Int
        )

        val item = PregnancyChecklistItem.fromMap(map)

        assertEquals("test_8", item.id)
        assertEquals("Test", item.title)
        assertEquals(1, item.trimester) // fallback to default
    }
}

