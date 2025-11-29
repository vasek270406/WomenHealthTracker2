package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для BabySizeHelper
 */
class BabySizeHelperTest {

    @Test
    fun `test getBabySizeForWeek returns correct size for exact week`() {
        val size = BabySizeHelper.getBabySizeForWeek(12)
        
        assertEquals(12, size.week)
        assertEquals("Лимон", size.fruit)
        assertEquals("🍋", size.emoji)
        assertEquals(6.0, size.sizeCm, 0.01)
    }

    @Test
    fun `test getBabySizeForWeek returns nearest size for week between milestones`() {
        // Неделя 10 находится между 8 и 12, должна вернуть размер для недели 12
        val size = BabySizeHelper.getBabySizeForWeek(10)
        
        assertEquals(12, size.week)
        assertEquals("Лимон", size.fruit)
    }

    @Test
    fun `test getBabySizeForWeek returns first size for early weeks`() {
        val size = BabySizeHelper.getBabySizeForWeek(1)
        
        assertEquals(4, size.week)
        assertEquals("Мак", size.fruit)
    }

    @Test
    fun `test getBabySizeForWeek returns last size for weeks beyond 40`() {
        val size = BabySizeHelper.getBabySizeForWeek(45)
        
        assertEquals(40, size.week)
        assertEquals("Арбуз", size.fruit)
        assertEquals("🍉", size.emoji)
    }

    @Test
    fun `test getBabySizeForWeek returns correct size for week 40`() {
        val size = BabySizeHelper.getBabySizeForWeek(40)
        
        assertEquals(40, size.week)
        assertEquals("Арбуз", size.fruit)
        assertEquals("Готов к встрече!", size.description)
        assertEquals(51.0, size.sizeCm, 0.01)
    }

    @Test
    fun `test getBabySizeForWeek for all milestone weeks`() {
        val weeks = listOf(4, 6, 8, 12, 16, 20, 24, 28, 32, 36, 40)
        
        weeks.forEach { week ->
            val size = BabySizeHelper.getBabySizeForWeek(week)
            assertEquals(week, size.week)
        }
    }

    @Test
    fun `test getNextBabySize returns next size when available`() {
        val next = BabySizeHelper.getNextBabySize(12)
        
        assertNotNull(next)
        assertEquals(16, next!!.week)
        assertEquals("Авокадо", next.fruit)
    }

    @Test
    fun `test getNextBabySize returns null for last week`() {
        val next = BabySizeHelper.getNextBabySize(40)
        
        assertNull(next)
    }

    @Test
    fun `test getNextBabySize returns null for weeks beyond 40`() {
        val next = BabySizeHelper.getNextBabySize(45)
        
        assertNull(next)
    }

    @Test
    fun `test getNextBabySize for first week`() {
        val next = BabySizeHelper.getNextBabySize(4)
        
        assertNotNull(next)
        assertEquals(6, next!!.week)
        assertEquals("Горошинка", next.fruit)
    }

    @Test
    fun `test getProgressToNext returns 1f for last week`() {
        val progress = BabySizeHelper.getProgressToNext(40)
        
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun `test getProgressToNext returns 1f for weeks beyond 40`() {
        val progress = BabySizeHelper.getProgressToNext(45)
        
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun `test getProgressToNext returns value between 0 and 1`() {
        val progress = BabySizeHelper.getProgressToNext(10)
        
        assertTrue("Progress should be between 0 and 1", progress >= 0f && progress <= 1f)
    }

    @Test
    fun `test getProgressToNext for exact milestone week`() {
        // Для недели 12 (точная веха), прогресс должен быть минимальным
        val progress = BabySizeHelper.getProgressToNext(12)
        
        assertTrue("Progress should be >= 0", progress >= 0f)
        assertTrue("Progress should be <= 1", progress <= 1f)
    }

    @Test
    fun `test getProgressToNext increases as week approaches next milestone`() {
        val progress1 = BabySizeHelper.getProgressToNext(10)
        val progress2 = BabySizeHelper.getProgressToNext(11)
        
        // Прогресс должен увеличиваться по мере приближения к следующей вехе
        assertTrue("Progress should increase", progress2 >= progress1)
    }

    @Test
    fun `test BabySize data class properties`() {
        val babySize = BabySize(
            week = 20,
            fruit = "Банан",
            emoji = "🍌",
            description = "Слышит ваш голос",
            sizeCm = 16.0
        )
        
        assertEquals(20, babySize.week)
        assertEquals("Банан", babySize.fruit)
        assertEquals("🍌", babySize.emoji)
        assertEquals("Слышит ваш голос", babySize.description)
        assertEquals(16.0, babySize.sizeCm, 0.01)
    }

    @Test
    fun `test getBabySizeForWeek for week 20`() {
        val size = BabySizeHelper.getBabySizeForWeek(20)
        
        assertEquals(20, size.week)
        assertEquals("Банан", size.fruit)
        assertEquals("🍌", size.emoji)
        assertEquals("Слышит ваш голос", size.description)
        assertEquals(16.0, size.sizeCm, 0.01)
    }

    @Test
    fun `test getBabySizeForWeek for week 28`() {
        val size = BabySizeHelper.getBabySizeForWeek(28)
        
        assertEquals(28, size.week)
        assertEquals("Баклажан", size.fruit)
        assertEquals("🍆", size.emoji)
        assertEquals("Различает свет", size.description)
        assertEquals(25.0, size.sizeCm, 0.01)
    }

    @Test
    fun `test getNextBabySize for week 28`() {
        val next = BabySizeHelper.getNextBabySize(28)
        
        assertNotNull(next)
        assertEquals(32, next!!.week)
        assertEquals("Капуста", next.fruit)
    }

    @Test
    fun `test getProgressToNext for week 15`() {
        // Неделя 15 находится между 12 и 16
        val progress = BabySizeHelper.getProgressToNext(15)
        
        assertTrue("Progress should be between 0 and 1", progress >= 0f && progress <= 1f)
    }
}

