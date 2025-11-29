package com.example.womenhealthtracker

import java.io.Serializable

// Модель размера малыша с фруктом
data class BabySize(
    val week: Int,
    val fruit: String,
    val emoji: String,
    val description: String,
    val sizeCm: Double
) : Serializable

// Хелпер для получения размера малыша по неделям
object BabySizeHelper {
    
    // База данных размеров по неделям
    private val babySizes = listOf(
        BabySize(week = 4, fruit = "Мак", emoji = "🌱", description = "Формируется нервная трубка", sizeCm = 0.1),
        BabySize(week = 6, fruit = "Горошинка", emoji = "🟢", description = "Появляется сердцебиение", sizeCm = 0.6),
        BabySize(week = 8, fruit = "Малина", emoji = "🍓", description = "Формируются пальчики", sizeCm = 1.6),
        BabySize(week = 12, fruit = "Лимон", emoji = "🍋", description = "Все органы на месте", sizeCm = 6.0),
        BabySize(week = 16, fruit = "Авокадо", emoji = "🥑", description = "Малыш двигается", sizeCm = 11.0),
        BabySize(week = 20, fruit = "Банан", emoji = "🍌", description = "Слышит ваш голос", sizeCm = 16.0),
        BabySize(week = 24, fruit = "Кукуруза", emoji = "🌽", description = "Открывает глазки", sizeCm = 21.0),
        BabySize(week = 28, fruit = "Баклажан", emoji = "🍆", description = "Различает свет", sizeCm = 25.0),
        BabySize(week = 32, fruit = "Капуста", emoji = "🥬", description = "Поворачивает голову", sizeCm = 29.0),
        BabySize(week = 36, fruit = "Дыня", emoji = "🍈", description = "Занимает финальную позицию", sizeCm = 34.0),
        BabySize(week = 40, fruit = "Арбуз", emoji = "🍉", description = "Готов к встрече!", sizeCm = 51.0)
    )
    
    /**
     * Получить размер малыша для указанной недели
     */
    fun getBabySizeForWeek(week: Int): BabySize {
        // Найти ближайший размер (неделя >= указанной)
        return babySizes.firstOrNull { it.week >= week } 
            ?: babySizes.last()
    }
    
    /**
     * Получить следующий размер (для прогресса)
     */
    fun getNextBabySize(week: Int): BabySize? {
        val current = getBabySizeForWeek(week)
        val currentIndex = babySizes.indexOf(current)
        return if (currentIndex < babySizes.size - 1) {
            babySizes[currentIndex + 1]
        } else {
            null
        }
    }
    
    /**
     * Получить прогресс до следующего фрукта (0.0 - 1.0)
     */
    fun getProgressToNext(week: Int): Float {
        val current = getBabySizeForWeek(week)
        val next = getNextBabySize(week) ?: return 1.0f
        
        val weeksInCurrent = if (babySizes.indexOf(current) > 0) {
            val prev = babySizes[babySizes.indexOf(current) - 1]
            current.week - prev.week
        } else {
            current.week
        }
        
        val weeksPassed = week - current.week + 1
        return (weeksPassed.toFloat() / weeksInCurrent.toFloat()).coerceIn(0f, 1f)
    }
}

