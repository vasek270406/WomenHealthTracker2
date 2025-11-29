package com.example.womenhealthtracker

import java.text.SimpleDateFormat
import java.util.*

/**
 * Вспомогательный класс для адаптации календаря под разные режимы
 */
class CalendarModeHelper(private val userPreferences: UserPreferences) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Получить информацию о дне в зависимости от режима
     */
    fun getDayInfo(dateString: String, day: Int, month: Int, year: Int): DayInfo {
        val goal = userPreferences.getSelectedGoal()
        
        return when (goal) {
            UserGoal.PREGNANCY -> getPregnancyDayInfo(dateString)
            UserGoal.MENOPAUSE -> getMenopauseDayInfo(dateString)
            else -> getCycleDayInfo(dateString, day, month, year)
        }
    }
    
    /**
     * Информация о дне для режима отслеживания цикла
     */
    private fun getCycleDayInfo(dateString: String, day: Int, month: Int, year: Int): DayInfo {
        val periodDates = userPreferences.getPeriodDates()
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = calendar.get(Calendar.MONTH)
        val todayYear = calendar.get(Calendar.YEAR)
        val isToday = (day == today && month == todayMonth && year == todayYear)
        
        if (isToday) {
            return DayInfo(
                title = "Сегодня",
                subtitle = "Текущий день",
                type = CalendarDayType.TODAY
            )
        }
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            if (periodDates.contains(dateString)) {
                return DayInfo(
                    title = "День месячных",
                    subtitle = "",
                    type = CalendarDayType.CURRENT_PERIOD
                )
            }
            return DayInfo(
                title = "",
                subtitle = "",
                type = CalendarDayType.NORMAL
            )
        }
        
        try {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.set(year, month, day)
            val lastPeriodCalendar = Calendar.getInstance()
            lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return DayInfo("", "", CalendarDayType.NORMAL)
            
            val daysDiff = ((dayCalendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            val dayOfCycle = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1
            
            val periodDuration = 5
            val ovulationDay = 14
            val lutealStartDay = ovulationDay + 1
            val lutealEndDay = cycleLength - 1
            
            val isInSavedPeriod = periodDates.contains(dateString)
            val isCalculatedPeriod = dayOfCycle < periodDuration
            
            if (isInSavedPeriod || isCalculatedPeriod) {
                return DayInfo(
                    title = "$dayOfCycle-й день цикла",
                    subtitle = "Менструация",
                    type = if (daysDiff >= 0 && daysDiff < cycleLength) CalendarDayType.CURRENT_PERIOD else CalendarDayType.PREVIOUS_PERIOD
                )
            }
            
            when {
                dayOfCycle in (ovulationDay - 2)..(ovulationDay + 2) -> {
                    return DayInfo(
                        title = "$dayOfCycle-й день цикла",
                        subtitle = "Овуляция",
                        type = CalendarDayType.OVULATION
                    )
                }
                dayOfCycle in lutealStartDay..lutealEndDay -> {
                    return DayInfo(
                        title = "$dayOfCycle-й день цикла",
                        subtitle = "Лютеиновая фаза",
                        type = CalendarDayType.LUTEAL
                    )
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибку
        }
        
        return DayInfo("", "", CalendarDayType.NORMAL)
    }
    
    /**
     * Информация о дне для режима беременности
     */
    private fun getPregnancyDayInfo(dateString: String): DayInfo {
        val pregnancyStartDate = userPreferences.getPregnancyStartDate()
        if (pregnancyStartDate.isEmpty()) {
            return DayInfo("", "", CalendarDayType.NORMAL)
        }
        
        try {
            val calendar = Calendar.getInstance()
            calendar.time = dateFormat.parse(dateString) ?: return DayInfo("", "", CalendarDayType.NORMAL)
            val startCalendar = Calendar.getInstance()
            startCalendar.time = dateFormat.parse(pregnancyStartDate) ?: return DayInfo("", "", CalendarDayType.NORMAL)
            
            val daysDiff = ((calendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff < 0) {
                return DayInfo("", "", CalendarDayType.NORMAL)
            }
            
            val week = (daysDiff / 7) + 1
            val trimester = when {
                week <= 12 -> 1
                week <= 27 -> 2
                else -> 3
            }
            
            return DayInfo(
                title = "$week неделя",
                subtitle = "$trimester триместр",
                type = CalendarDayType.NORMAL
            )
        } catch (e: Exception) {
            return DayInfo("", "", CalendarDayType.NORMAL)
        }
    }
    
    /**
     * Информация о дне для режима менопаузы
     */
    private fun getMenopauseDayInfo(dateString: String): DayInfo {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = calendar.get(Calendar.MONTH)
        val todayYear = calendar.get(Calendar.YEAR)
        
        try {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.time = dateFormat.parse(dateString) ?: return DayInfo("", "", CalendarDayType.NORMAL)
            
            val day = dayCalendar.get(Calendar.DAY_OF_MONTH)
            val month = dayCalendar.get(Calendar.MONTH)
            val year = dayCalendar.get(Calendar.YEAR)
            val isToday = (day == today && month == todayMonth && year == todayYear)
            
            if (isToday) {
                return DayInfo(
                    title = "Сегодня",
                    subtitle = "Текущий день",
                    type = CalendarDayType.TODAY
                )
            }
            
            // Проверяем, есть ли запись для этого дня
            val record = userPreferences.getMenopauseDayRecord(dateString)
            
            if (record != null) {
                // Если есть симптомы
                if (record.symptoms.isNotEmpty()) {
                    val symptomsCount = record.symptoms.size
                    val avgIntensity = record.symptoms.values.map { it.intensity }.average().toInt()
                    val intensityText = when {
                        avgIntensity >= 4 -> "Сильные симптомы"
                        avgIntensity >= 2 -> "Умеренные симптомы"
                        else -> "Легкие симптомы"
                    }
                    return DayInfo(
                        title = "$symptomsCount симптом${if (symptomsCount == 1) "" else if (symptomsCount < 5) "а" else "ов"}",
                        subtitle = intensityText,
                        type = CalendarDayType.NORMAL
                    )
                }
                
                // Если есть заметки
                if (record.notes.isNotEmpty()) {
                    return DayInfo(
                        title = "Запись",
                        subtitle = if (record.mood > 0) "Настроение: ${record.mood}/5" else "",
                        type = CalendarDayType.NORMAL
                    )
                }
                
                // Если только настроение и энергия
                if (record.mood > 0 || record.energy > 0) {
                    return DayInfo(
                        title = "",
                        subtitle = "Настроение: ${record.mood}/5",
                        type = CalendarDayType.NORMAL
                    )
                }
            }
            
            return DayInfo("", "", CalendarDayType.NORMAL)
        } catch (e: Exception) {
            return DayInfo("", "", CalendarDayType.NORMAL)
        }
    }
    
    /**
     * Получить текст для отображения информации о цикле в зависимости от режима
     */
    fun getCycleInfoText(): String {
        val goal = userPreferences.getSelectedGoal()
        
        return when (goal) {
            UserGoal.PREGNANCY -> {
                val pregnancyStartDate = userPreferences.getPregnancyStartDate()
                if (pregnancyStartDate.isEmpty()) {
                    return "Настройте дату начала беременности"
                }
                try {
                    val startCalendar = Calendar.getInstance()
                    startCalendar.time = dateFormat.parse(pregnancyStartDate) ?: return "Настройте дату начала беременности"
                    val currentCalendar = Calendar.getInstance()
                    val daysDiff = ((currentCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val week = (daysDiff / 7) + 1
                    val trimester = when {
                        week <= 12 -> 1
                        week <= 27 -> 2
                        else -> 3
                    }
                    return "$week неделя беременности ($trimester триместр)"
                } catch (e: Exception) {
                    return "Настройте дату начала беременности"
                }
            }
            UserGoal.MENOPAUSE -> {
                return "Менопауза"
            }
            else -> {
                val cycleLength = userPreferences.getCycleLength()
                val lastPeriodStart = userPreferences.getLastPeriodStart()
                
                if (lastPeriodStart.isEmpty() || cycleLength == 0) {
                    return "Настройте цикл"
                }
                
                try {
                    val calendar = Calendar.getInstance()
                    val lastPeriodCalendar = Calendar.getInstance()
                    lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return "Настройте цикл"
                    val daysDiff = ((calendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val cycleDay = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1
                    return "$cycleDay-й день"
                } catch (e: Exception) {
                    return "Настройте цикл"
                }
            }
        }
    }
}

/**
 * Информация о дне для отображения
 */
data class DayInfo(
    val title: String,
    val subtitle: String,
    val type: CalendarDayType
)

/**
 * Тип дня календаря (используется из CalendarActivity)
 */
enum class CalendarDayType {
    CURRENT_PERIOD,
    PREVIOUS_PERIOD,
    OVULATION,
    LUTEAL,
    TODAY,
    NORMAL
}



