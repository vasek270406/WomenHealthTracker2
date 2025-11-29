package com.example.womenhealthtracker

import java.text.SimpleDateFormat
import java.util.*

/**
 * Интеллектуальный предсказатель для календаря
 * Анализирует исторические данные и делает прогнозы
 */
class CalendarPredictor(private val userPreferences: UserPreferences) {
    
    /**
     * Прогноз для дня
     */
    data class DayForecast(
        val date: String,
        val predictedPeriod: Boolean = false,      // Прогноз начала цикла
        val predictedOvulation: Boolean = false,   // Прогноз овуляции
        val predictedPMS: Boolean = false,        // Прогноз ПМС
        val predictedEnergy: Int? = null,         // Прогнозируемая энергия (0-100)
        val confidence: Int = 0,                   // Уверенность в прогнозе (0-100)
        val hasData: Boolean = false,              // Есть ли данные за этот день
        val symptoms: List<String> = emptyList()   // Прогнозируемые симптомы
    )
    
    /**
     * Получить прогноз для конкретного дня
     */
    fun getForecast(date: String): DayForecast {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        val forecastDate = dateFormat.parse(date) ?: return DayForecast(date)
        
        val forecastCalendar = Calendar.getInstance()
        forecastCalendar.time = forecastDate
        
        // Проверяем, есть ли данные за этот день
        val dayData = userPreferences.getDayData(date)
        val hasData = dayData != null
        
        // Если день в прошлом, возвращаем фактические данные
        if (forecastCalendar.before(today)) {
            return DayForecast(
                date = date,
                hasData = hasData,
                predictedEnergy = dayData?.energy,
                symptoms = dayData?.symptoms?.map { it.name } ?: emptyList()
            )
        }
        
        // Для будущих дней делаем прогноз
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            return DayForecast(date, hasData = hasData)
        }
        
        // Вычисляем день цикла
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return DayForecast(date, hasData = hasData)
        val daysDiff = ((forecastCalendar.timeInMillis - lastPeriod.time) / (1000 * 60 * 60 * 24)).toInt()
        val dayOfCycle = ((daysDiff % cycleLength) + cycleLength) % cycleLength
        
        // Прогноз начала цикла
        val predictedPeriod = dayOfCycle >= cycleLength - 2 && dayOfCycle < cycleLength + 3
        
        // Прогноз овуляции (обычно на 14 день, ±2 дня)
        val ovulationDay = 14
        val predictedOvulation = dayOfCycle in (ovulationDay - 2)..(ovulationDay + 2)
        
        // Прогноз ПМС (за 3-7 дней до начала цикла)
        val predictedPMS = dayOfCycle >= cycleLength - 7 && dayOfCycle < cycleLength - 1
        
        // Прогноз энергии на основе исторических данных
        val predictedEnergy = predictEnergy(dayOfCycle, cycleLength)
        
        // Прогноз симптомов на основе истории
        val predictedSymptoms = predictSymptoms(dayOfCycle, cycleLength)
        
        // Уверенность в прогнозе (выше для регулярных циклов)
        val cycleHistory = userPreferences.getCycleHistory()
        val confidence = calculateConfidence(cycleHistory, cycleLength)
        
        return DayForecast(
            date = date,
            predictedPeriod = predictedPeriod,
            predictedOvulation = predictedOvulation,
            predictedPMS = predictedPMS,
            predictedEnergy = predictedEnergy,
            confidence = confidence,
            hasData = hasData,
            symptoms = predictedSymptoms
        )
    }
    
    /**
     * Прогнозирует уровень энергии на основе исторических данных
     */
    private fun predictEnergy(dayOfCycle: Int, cycleLength: Int): Int? {
        val allDates = userPreferences.getAllDatesWithData()
        if (allDates.isEmpty()) return null
        
        // Собираем данные об энергии для похожих дней цикла
        val energyData = mutableListOf<Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return null
        
        allDates.forEach { date ->
            val dayData = userPreferences.getDayData(date) ?: return@forEach
            if (dayData.energy == null) return@forEach
            
            try {
                val dayDate = dateFormat.parse(date) ?: return@forEach
                val daysDiff = ((dayDate.time - lastPeriod.time) / (1000 * 60 * 60 * 24)).toInt()
                val dayOfCycleForDate = ((daysDiff % cycleLength) + cycleLength) % cycleLength
                
                // Если день цикла близок к текущему (±2 дня)
                if (kotlin.math.abs(dayOfCycleForDate - dayOfCycle) <= 2) {
                    energyData.add(dayData.energy)
                }
            } catch (e: Exception) {
                // Игнорируем ошибки
            }
        }
        
        return if (energyData.isNotEmpty()) {
            energyData.average().toInt()
        } else {
            // Если нет исторических данных, используем общие паттерны
            when {
                dayOfCycle < 5 -> 40  // Низкая энергия во время месячных
                dayOfCycle in 10..16 -> 80  // Высокая энергия во время овуляции
                dayOfCycle >= cycleLength - 7 -> 50  // Средняя энергия перед ПМС
                else -> 65  // Нормальная энергия
            }
        }
    }
    
    /**
     * Прогнозирует симптомы на основе истории
     */
    private fun predictSymptoms(dayOfCycle: Int, cycleLength: Int): List<String> {
        val allDates = userPreferences.getAllDatesWithData()
        if (allDates.isEmpty()) return emptyList()
        
        val symptomCounts = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return emptyList()
        
        allDates.forEach { date ->
            val dayData = userPreferences.getDayData(date) ?: return@forEach
            
            try {
                val dayDate = dateFormat.parse(date) ?: return@forEach
                val daysDiff = ((dayDate.time - lastPeriod.time) / (1000 * 60 * 60 * 24)).toInt()
                val dayOfCycleForDate = ((daysDiff % cycleLength) + cycleLength) % cycleLength
                
                // Если день цикла близок к текущему (±2 дня)
                if (kotlin.math.abs(dayOfCycleForDate - dayOfCycle) <= 2) {
                    dayData.symptoms.forEach { symptom ->
                        symptomCounts[symptom.name] = (symptomCounts[symptom.name] ?: 0) + 1
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибки
            }
        }
        
        // Возвращаем симптомы, которые встречались чаще всего (минимум 2 раза)
        return symptomCounts.filter { it.value >= 2 }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
    
    /**
     * Вычисляет уверенность в прогнозе
     */
    private fun calculateConfidence(cycleHistory: List<Int>, currentCycleLength: Int): Int {
        if (cycleHistory.isEmpty()) return 50
        
        // Если цикл регулярный (отклонение < 3 дня), уверенность выше
        val avg = cycleHistory.average()
        val variance = cycleHistory.map { kotlin.math.abs(it - avg) }.average()
        
        return when {
            variance < 2 -> 90
            variance < 4 -> 75
            variance < 6 -> 60
            else -> 45
        }
    }
    
    /**
     * Автоматическое распознавание начала цикла
     * Проверяет, не начался ли цикл на основе симптомов
     */
    fun detectPeriodStart(): String? {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dayData = userPreferences.getDayData(today) ?: return null
        
        // Проверяем симптомы, указывающие на начало цикла
        val periodSymptoms = listOf(
            "Кровянистые выделения",
            "Кровотечение",
            "Боли внизу живота",
            "Спазмы"
        )
        
        val hasPeriodSymptoms = dayData.symptoms.any { symptom ->
            periodSymptoms.any { periodSymptom ->
                symptom.name.contains(periodSymptom, ignoreCase = true)
            }
        }
        
        // Проверяем последние 3 дня
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        var consecutiveDays = 0
        
        for (i in 0..2) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)
            val data = userPreferences.getDayData(date)
            
            if (data != null && data.symptoms.any { symptom ->
                periodSymptoms.any { periodSymptom ->
                    symptom.name.contains(periodSymptom, ignoreCase = true)
                }
            }) {
                consecutiveDays++
            }
        }
        
        // Если есть симптомы в течение 2+ дней подряд, вероятно начался цикл
        if (hasPeriodSymptoms && consecutiveDays >= 2) {
            return today
        }
        
        return null
    }
}

