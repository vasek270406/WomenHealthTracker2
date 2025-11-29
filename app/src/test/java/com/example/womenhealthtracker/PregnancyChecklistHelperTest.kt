package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для PregnancyChecklistHelper
 */
class PregnancyChecklistHelperTest {

    @Test
    fun `test getChecklistForTrimester returns first trimester checklist`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(1)

        assertNotNull(checklist)
        assertTrue(checklist.isNotEmpty())
        assertEquals(7, checklist.size)
        
        // Проверяем, что все элементы относятся к первому триместру
        checklist.forEach { item ->
            assertEquals(1, item.trimester)
        }
        
        // Проверяем наличие ключевых элементов
        val titles = checklist.map { it.title }
        assertTrue(titles.contains("Встать на учет в женской консультации"))
        assertTrue(titles.contains("Первый скрининг (11-13 недель)"))
        assertTrue(titles.contains("Начать принимать витамины для беременных"))
    }

    @Test
    fun `test getChecklistForTrimester returns second trimester checklist`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(2)

        assertNotNull(checklist)
        assertTrue(checklist.isNotEmpty())
        assertEquals(7, checklist.size)
        
        // Проверяем, что все элементы относятся ко второму триместру
        checklist.forEach { item ->
            assertEquals(2, item.trimester)
        }
        
        // Проверяем наличие ключевых элементов
        val titles = checklist.map { it.title }
        assertTrue(titles.contains("Второй скрининг (18-21 неделя)"))
        assertTrue(titles.contains("Глюкозотолерантный тест (24-28 недель)"))
        assertTrue(titles.contains("Выбрать роддом"))
    }

    @Test
    fun `test getChecklistForTrimester returns third trimester checklist`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(3)

        assertNotNull(checklist)
        assertTrue(checklist.isNotEmpty())
        assertEquals(8, checklist.size)
        
        // Проверяем, что все элементы относятся к третьему триместру
        checklist.forEach { item ->
            assertEquals(3, item.trimester)
        }
        
        // Проверяем наличие ключевых элементов
        val titles = checklist.map { it.title }
        assertTrue(titles.contains("Третий скрининг (30-34 недели)"))
        assertTrue(titles.contains("Собрать сумку в роддом"))
        assertTrue(titles.contains("Обсудить план родов с врачом"))
    }

    @Test
    fun `test getChecklistForTrimester returns empty list for invalid trimester`() {
        val checklist0 = PregnancyChecklistHelper.getChecklistForTrimester(0)
        val checklist4 = PregnancyChecklistHelper.getChecklistForTrimester(4)
        val checklistNegative = PregnancyChecklistHelper.getChecklistForTrimester(-1)

        assertTrue(checklist0.isEmpty())
        assertTrue(checklist4.isEmpty())
        assertTrue(checklistNegative.isEmpty())
    }

    @Test
    fun `test getAllChecklistItems returns all items from all trimesters`() {
        val allItems = PregnancyChecklistHelper.getAllChecklistItems()

        assertNotNull(allItems)
        assertTrue(allItems.isNotEmpty())
        
        // Должно быть 7 + 7 + 8 = 22 элемента
        assertEquals(22, allItems.size)
        
        // Проверяем, что есть элементы из всех триместров
        val trimesters = allItems.map { it.trimester }.distinct().sorted()
        assertEquals(listOf(1, 2, 3), trimesters)
        
        // Проверяем количество элементов по триместрам
        val firstTrimesterCount = allItems.count { it.trimester == 1 }
        val secondTrimesterCount = allItems.count { it.trimester == 2 }
        val thirdTrimesterCount = allItems.count { it.trimester == 3 }
        
        assertEquals(7, firstTrimesterCount)
        assertEquals(7, secondTrimesterCount)
        assertEquals(8, thirdTrimesterCount)
    }

    @Test
    fun `test checklist items have unique IDs`() {
        val allItems = PregnancyChecklistHelper.getAllChecklistItems()
        val ids = allItems.map { it.id }
        
        // Проверяем, что все ID уникальны
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun `test checklist items have non-empty titles`() {
        val allItems = PregnancyChecklistHelper.getAllChecklistItems()
        
        allItems.forEach { item ->
            assertTrue("Title should not be empty for item ${item.id}", item.title.isNotBlank())
        }
    }

    @Test
    fun `test checklist items have valid trimester values`() {
        val allItems = PregnancyChecklistHelper.getAllChecklistItems()
        
        allItems.forEach { item ->
            assertTrue(
                "Trimester should be 1, 2, or 3 for item ${item.id}",
                item.trimester in 1..3
            )
        }
    }

    @Test
    fun `test first trimester checklist contains expected items`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(1)
        val itemIds = checklist.map { it.id }.toSet()
        
        assertTrue(itemIds.contains("1_1"))
        assertTrue(itemIds.contains("1_2"))
        assertTrue(itemIds.contains("1_3"))
        assertTrue(itemIds.contains("1_4"))
        assertTrue(itemIds.contains("1_5"))
        assertTrue(itemIds.contains("1_6"))
        assertTrue(itemIds.contains("1_7"))
    }

    @Test
    fun `test second trimester checklist contains expected items`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(2)
        val itemIds = checklist.map { it.id }.toSet()
        
        assertTrue(itemIds.contains("2_1"))
        assertTrue(itemIds.contains("2_2"))
        assertTrue(itemIds.contains("2_3"))
        assertTrue(itemIds.contains("2_4"))
        assertTrue(itemIds.contains("2_5"))
        assertTrue(itemIds.contains("2_6"))
        assertTrue(itemIds.contains("2_7"))
    }

    @Test
    fun `test third trimester checklist contains expected items`() {
        val checklist = PregnancyChecklistHelper.getChecklistForTrimester(3)
        val itemIds = checklist.map { it.id }.toSet()
        
        assertTrue(itemIds.contains("3_1"))
        assertTrue(itemIds.contains("3_2"))
        assertTrue(itemIds.contains("3_3"))
        assertTrue(itemIds.contains("3_4"))
        assertTrue(itemIds.contains("3_5"))
        assertTrue(itemIds.contains("3_6"))
        assertTrue(itemIds.contains("3_7"))
        assertTrue(itemIds.contains("3_8"))
    }

    @Test
    fun `test checklist items are not completed by default`() {
        val allItems = PregnancyChecklistHelper.getAllChecklistItems()
        
        allItems.forEach { item ->
            assertFalse("Item ${item.id} should not be completed by default", item.completed)
            assertTrue("Item ${item.id} should have empty completedDate by default", item.completedDate.isEmpty())
        }
    }
}

