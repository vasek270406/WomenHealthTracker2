package com.example.womenhealthtracker

import java.text.SimpleDateFormat
import java.util.*

/**
 * Анализатор задержки менструации
 * Определяет вероятные причины и генерирует рекомендации
 */
class DelayAnalyzer {
    
    /**
     * Анализирует задержку и определяет вероятные причины
     */
    fun analyzeDelay(
        delayDays: Int,
        context: DelayContext,
        cycleHistory: List<Int> = emptyList()  // История длин циклов
    ): List<Pair<DelayReason, Int>> {
        val reasons = mutableListOf<Pair<DelayReason, Int>>()
        
        // Беременность - высокая вероятность, если был ПА
        if (context.hadSexualActivity == true) {
            val probability = when {
                delayDays >= 7 -> 85
                delayDays >= 3 -> 70
                else -> 50
            }
            reasons.add(Pair(DelayReason.PREGNANCY, probability))
        }
        
        // Стресс
        if (context.stress) {
            val probability = when {
                delayDays >= 10 -> 60
                delayDays >= 5 -> 45
                else -> 30
            }
            reasons.add(Pair(DelayReason.STRESS, probability))
        }
        
        // Изменение образа жизни
        val lifestyleChanges = listOf(
            context.travel,
            context.dietChange,
            context.exerciseChange
        ).count { it }
        
        if (lifestyleChanges > 0) {
            val probability = lifestyleChanges * 20
            reasons.add(Pair(DelayReason.LIFESTYLE_CHANGE, minOf(probability, 60)))
        }
        
        // Заболевание
        if (context.illness) {
            reasons.add(Pair(DelayReason.ILLNESS, 50))
        }
        
        // Лекарства
        if (context.medication) {
            reasons.add(Pair(DelayReason.MEDICATION, 40))
        }
        
        // Гормональные колебания (если нет других причин или цикл нерегулярный)
        if (reasons.isEmpty() || isIrregularCycle(cycleHistory)) {
            val probability = when {
                delayDays >= 10 -> 50
                delayDays >= 5 -> 35
                else -> 20
            }
            reasons.add(Pair(DelayReason.HORMONAL_FLUCTUATION, probability))
        }
        
        // Сортируем по вероятности (убывание)
        return reasons.sortedByDescending { it.second }
    }
    
    /**
     * Генерирует рекомендации на основе анализа
     */
    fun generateRecommendations(
        delayDays: Int,
        reasons: List<Pair<DelayReason, Int>>,
        context: DelayContext
    ): List<DelayRecommendation> {
        val recommendations = mutableListOf<DelayRecommendation>()
        
        // Если высокая вероятность беременности
        val pregnancyReason = reasons.firstOrNull { it.first == DelayReason.PREGNANCY }
        if (pregnancyReason != null && pregnancyReason.second >= 50) {
            recommendations.add(
                DelayRecommendation(
                    title = "Сделайте тест на беременность",
                    description = "Сделайте тест на беременность утром. Для точного результата рекомендуем тест с чувствительностью 10 мМЕ/мл.",
                    actionType = RecommendationAction.PREGNANCY_TEST
                )
            )
            recommendations.add(
                DelayRecommendation(
                    title = "Ввести результат теста",
                    description = "После прохождения теста введите результат в приложение",
                    actionType = RecommendationAction.ENTER_TEST_RESULT
                )
            )
        }
        
        // Если стресс - основная причина
        val stressReason = reasons.firstOrNull { it.first == DelayReason.STRESS }
        if (stressReason != null && stressReason.second >= 40 && pregnancyReason == null) {
            recommendations.add(
                DelayRecommendation(
                    title = "Управление стрессом",
                    description = "Постарайтесь нормализовать сон и режим дня. Наш трекер настроения может помочь отследить динамику стресса.",
                    actionType = RecommendationAction.TRACK_MOOD
                )
            )
            recommendations.add(
                DelayRecommendation(
                    title = "Медитация для сна",
                    description = "Попробуйте техники релаксации для улучшения качества сна",
                    actionType = RecommendationAction.OPEN_MEDITATION,
                    actionData = "sleep"
                )
            )
        }
        
        // Если задержка более 7 дней - консультация врача
        if (delayDays >= 7) {
            recommendations.add(
                DelayRecommendation(
                    title = "Консультация гинеколога",
                    description = "При задержке более 7 дней рекомендуется записаться на консультацию к гинекологу.",
                    actionType = RecommendationAction.CONSULT_DOCTOR
                )
            )
            recommendations.add(
                DelayRecommendation(
                    title = "Сформировать отчет для врача",
                    description = "Создайте PDF отчет о вашем цикле для показа врачу",
                    actionType = RecommendationAction.GENERATE_REPORT
                )
            )
        }
        
        // Если задержка 5-7 дней и нет явных причин
        if (delayDays in 5..6 && reasons.isEmpty()) {
            recommendations.add(
                DelayRecommendation(
                    title = "Продолжайте отслеживать",
                    description = "Задержки время от времени случаются. Продолжайте отслеживать симптомы и обратитесь к врачу, если задержка превысит 7 дней.",
                    actionType = RecommendationAction.TRACK_MOOD
                )
            )
        }
        
        return recommendations
    }
    
    /**
     * Проверяет, является ли цикл нерегулярным
     */
    private fun isIrregularCycle(cycleHistory: List<Int>): Boolean {
        if (cycleHistory.size < 3) return false
        
        val avg = cycleHistory.average()
        val variance = cycleHistory.map { kotlin.math.abs(it - avg) }.average()
        
        // Если среднее отклонение больше 5 дней - цикл нерегулярный
        return variance > 5
    }
    
    /**
     * Вычисляет ожидаемую дату следующей менструации
     */
    fun calculateExpectedPeriodDate(
        lastPeriodStart: String,
        cycleLength: Int
    ): String? {
        if (lastPeriodStart.isEmpty() || cycleLength == 0) return null
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return null
            val calendar = Calendar.getInstance()
            calendar.time = lastPeriod
            calendar.add(Calendar.DAY_OF_YEAR, cycleLength)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Вычисляет количество дней задержки
     */
    fun calculateDelayDays(expectedDate: String): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        
        return try {
            val expected = dateFormat.parse(expectedDate) ?: return 0
            val expectedCalendar = Calendar.getInstance()
            expectedCalendar.time = expected
            
            val daysDiff = ((today.timeInMillis - expectedCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            maxOf(0, daysDiff)
        } catch (e: Exception) {
            0
        }
    }
}

