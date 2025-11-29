package com.example.womenhealthtracker

import android.content.Context
import android.graphics.Color

/**
 * Вспомогательный класс для работы с режимами приложения (UserGoal)
 */
object GoalHelper {
    
    /**
     * Получить название режима
     */
    fun getGoalTitle(goal: UserGoal): String {
        return when (goal) {
            UserGoal.CYCLE_TRACKING -> "Отслеживание цикла"
            UserGoal.PREGNANCY -> "Беременность"
            UserGoal.MENOPAUSE -> "Менопауза"
        }
    }
    
    /**
     * Получить описание режима
     */
    fun getGoalDescription(goal: UserGoal): String {
        return when (goal) {
            UserGoal.CYCLE_TRACKING -> "Регулярные менструальные циклы"
            UserGoal.PREGNANCY -> "Отслеживаем беременность"
            UserGoal.MENOPAUSE -> "Нерегулярные циклы или менопауза"
        }
    }
    
    /**
     * Получить цвет для режима
     */
    fun getGoalColor(goal: UserGoal): Int {
        return when (goal) {
            UserGoal.CYCLE_TRACKING -> Color.parseColor("#2196F3") // Синий
            UserGoal.PREGNANCY -> Color.parseColor("#E91E63") // Розовый
            UserGoal.MENOPAUSE -> Color.parseColor("#FF9800") // Оранжевый
        }
    }
    
    /**
     * Проверить, активен ли режим беременности
     */
    fun isPregnancyModeActive(userPreferences: UserPreferences): Boolean {
        return userPreferences.getSelectedGoal() == UserGoal.PREGNANCY
    }
    
    /**
     * Получить информацию о текущей беременности
     */
    fun getPregnancyInfo(userPreferences: UserPreferences): String? {
        if (!isPregnancyModeActive(userPreferences)) {
            return null
        }
        
        val pregnancyStartDate = userPreferences.getPregnancyStartDate()
        if (pregnancyStartDate.isEmpty()) {
            return "Беременность (неделя не указана)"
        }
        
        try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val startCalendar = java.util.Calendar.getInstance()
            startCalendar.time = dateFormat.parse(pregnancyStartDate) ?: return null
            val currentCalendar = java.util.Calendar.getInstance()
            val daysDiff = ((currentCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            val week = if (daysDiff >= 0) (daysDiff / 7) + 1 else 1
            return "Беременность ($week неделя)"
        } catch (e: Exception) {
            return "Беременность"
        }
    }
}



