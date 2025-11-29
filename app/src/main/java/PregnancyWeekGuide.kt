package com.example.womenhealthtracker

/**
 * Класс для получения информации о неделе беременности
 */
object PregnancyWeekGuide {
    
    /**
     * Получить информацию о неделе беременности
     */
    fun getWeekInfo(week: Int): WeekInfo {
        return when {
            week <= 4 -> WeekInfo(
                week = week,
                babySize = "С маковое зернышко",
                development = "Формирование плаценты и пуповины",
                symptoms = listOf("Усталость", "Тошнота", "Изменение вкусовых предпочтений"),
                recommendations = "Принимайте фолиевую кислоту, избегайте алкоголя и курения"
            )
            week in 5..8 -> WeekInfo(
                week = week,
                babySize = "С малину",
                development = "Формирование основных органов, начало сердцебиения",
                symptoms = listOf("Тошнота", "Усталость", "Частое мочеиспускание"),
                recommendations = "Встаньте на учет в женской консультации, сдайте первые анализы"
            )
            week in 9..12 -> WeekInfo(
                week = week,
                babySize = "С сливу",
                development = "Формирование всех органов, начало движения",
                symptoms = listOf("Тошнота", "Усталость", "Изменение груди"),
                recommendations = "Первый скрининг, начните планировать декретный отпуск"
            )
            week in 13..16 -> WeekInfo(
                week = week,
                babySize = "С авокадо",
                development = "Активное развитие мозга, начало работы почек",
                symptoms = listOf("Улучшение самочувствия", "Рост живота", "Первые шевеления"),
                recommendations = "Второй триместр - время для активности и путешествий"
            )
            week in 17..20 -> WeekInfo(
                week = week,
                babySize = "С банан",
                development = "Развитие слуха, начало сосательного рефлекса",
                symptoms = listOf("Шевеления становятся заметнее", "Рост живота", "Изжога"),
                recommendations = "Второй скрининг, начните готовить детскую комнату"
            )
            week in 21..24 -> WeekInfo(
                week = week,
                babySize = "С кукурузу",
                development = "Развитие вкусовых рецепторов, начало открывания глаз",
                symptoms = listOf("Активные шевеления", "Отеки", "Одышка"),
                recommendations = "Глюкозотолерантный тест, курсы для будущих родителей"
            )
            week in 25..28 -> WeekInfo(
                week = week,
                babySize = "С баклажан",
                development = "Развитие легких, начало набора веса",
                symptoms = listOf("Частые шевеления", "Изжога", "Отеки"),
                recommendations = "Третий триместр - время для подготовки к родам"
            )
            week in 29..32 -> WeekInfo(
                week = week,
                babySize = "С тыкву",
                development = "Активный рост, развитие иммунной системы",
                symptoms = listOf("Сильные шевеления", "Одышка", "Боли в спине"),
                recommendations = "Соберите сумку в роддом, выберите имя для малыша"
            )
            week in 33..36 -> WeekInfo(
                week = week,
                babySize = "С ананас",
                development = "Окончательное формирование легких, подготовка к родам",
                symptoms = listOf("Меньше места для шевелений", "Отеки", "Усталость"),
                recommendations = "Последние визиты к врачу, подготовка к родам"
            )
            week >= 37 -> WeekInfo(
                week = week,
                babySize = "С арбуз",
                development = "Малыш готов к рождению, занимает правильное положение",
                symptoms = listOf("Ожидание родов", "Тренировочные схватки", "Опущение живота"),
                recommendations = "Будьте готовы к родам в любой момент, следите за предвестниками"
            )
            else -> WeekInfo(
                week = week,
                babySize = "Растет",
                development = "Активное развитие",
                symptoms = emptyList(),
                recommendations = "Следуйте рекомендациям врача"
            )
        }
    }
    
    /**
     * Получить информацию о триместре
     */
    fun getTrimesterInfo(trimester: Int): String {
        return when (trimester) {
            1 -> "Первый триместр (1-12 недели) - время формирования всех органов"
            2 -> "Второй триместр (13-27 недели) - период активного роста и развития"
            3 -> "Третий триместр (28-40+ недели) - подготовка к родам"
            else -> ""
        }
    }
}

/**
 * Информация о неделе беременности
 */
data class WeekInfo(
    val week: Int,
    val babySize: String,
    val development: String,
    val symptoms: List<String>,
    val recommendations: String
)

