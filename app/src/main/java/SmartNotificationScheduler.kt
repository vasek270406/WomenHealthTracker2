package com.example.womenhealthtracker

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

/**
 * Умный планировщик уведомлений на основе прогнозов
 */
class SmartNotificationScheduler(private val context: Context) {
    
    private val notificationHelper = NotificationHelper(context)
    private val userPreferences = UserPreferences(context)
    private val calendarPredictor = CalendarPredictor(userPreferences)
    
    /**
     * Планирует все умные уведомления на основе прогнозов
     */
    fun scheduleSmartNotifications() {
        val goal = userPreferences.getSelectedGoal()
        
        // Планируем уведомления только для режима отслеживания цикла
        if (goal == UserGoal.CYCLE_TRACKING) {
            schedulePeriodPredictions()
            scheduleOvulationNotifications()
            schedulePMSNotifications()
        }
    }
    
    /**
     * Планирует уведомления о прогнозируемом начале цикла
     */
    private fun schedulePeriodPredictions() {
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return
        
        // Планируем на следующие 3 цикла
        for (cycleOffset in 0..2) {
            val predictedPeriodDate = Calendar.getInstance().apply {
                time = lastPeriod
                add(Calendar.DAY_OF_YEAR, cycleLength * (cycleOffset + 1))
            }
            
            val predictedDateString = dateFormat.format(predictedPeriodDate.time)
            val forecast = calendarPredictor.getForecast(predictedDateString)
            
            if (forecast.predictedPeriod && forecast.confidence >= 60) {
                // Уведомление за 2 дня до прогнозируемого начала
                val reminderDate = Calendar.getInstance().apply {
                    time = predictedPeriodDate.time
                    add(Calendar.DAY_OF_YEAR, -2)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                
                // Планируем только если дата в будущем
                if (reminderDate.timeInMillis > System.currentTimeMillis()) {
                    val notification = SmartNotification(
                        id = "period_prediction_${predictedDateString}",
                        type = NotificationType.CYCLE_PREDICTION,
                        title = "Скоро начало цикла",
                        body = "На основе ваших данных, менструация ожидается через 2 дня. Подготовьтесь заранее.",
                        scheduledHour = reminderDate.get(Calendar.HOUR_OF_DAY),
                        scheduledMinute = reminderDate.get(Calendar.MINUTE),
                        targetMode = userPreferences.getSelectedGoal(),
                        isEnabled = true,
                        repeatDaily = false
                    )
                    
                    // Планируем на конкретную дату
                    notificationHelper.scheduleNotificationAtDate(notification, reminderDate)
                }
            }
        }
    }
    
    /**
     * Планирует уведомления об овуляции
     */
    private fun scheduleOvulationNotifications() {
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return
        
        // Планируем на следующие 3 цикла
        for (cycleOffset in 0..2) {
            val cycleStart = Calendar.getInstance().apply {
                time = lastPeriod
                add(Calendar.DAY_OF_YEAR, cycleLength * cycleOffset)
            }
            
            // Овуляция обычно на 14 день цикла
            val ovulationDate = Calendar.getInstance().apply {
                time = cycleStart.time
                add(Calendar.DAY_OF_YEAR, 14)
            }
            
            val ovulationDateString = dateFormat.format(ovulationDate.time)
            val forecast = calendarPredictor.getForecast(ovulationDateString)
            
            if (forecast.predictedOvulation && forecast.confidence >= 60) {
                // Уведомление в первый день фертильного окна (за 3 дня до овуляции)
                val fertilityStart = Calendar.getInstance().apply {
                    time = ovulationDate.time
                    add(Calendar.DAY_OF_YEAR, -3)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                }
                
                if (fertilityStart.timeInMillis > System.currentTimeMillis()) {
                    val goal = userPreferences.getSelectedGoal()
                    val title = "Фертильное окно"
                    val body = "Началось фертильное окно. Овуляция ожидается через 3 дня."
                    
                    val notification = SmartNotification(
                        id = "fertility_window_${ovulationDateString}",
                        type = NotificationType.FERTILITY_WINDOW,
                        title = title,
                        body = body,
                        scheduledHour = fertilityStart.get(Calendar.HOUR_OF_DAY),
                        scheduledMinute = fertilityStart.get(Calendar.MINUTE),
                        targetMode = goal,
                        isEnabled = true,
                        repeatDaily = false
                    )
                    
                    notificationHelper.scheduleNotification(notification)
                }
                
                // Уведомление в день овуляции
                val ovulationReminder = Calendar.getInstance().apply {
                    time = ovulationDate.time
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                
                if (ovulationReminder.timeInMillis > System.currentTimeMillis()) {
                    val goal = userPreferences.getSelectedGoal()
                    val title = "День овуляции"
                    val body = "Сегодня день овуляции. Вероятность беременности максимальна."
                    
                    val notification = SmartNotification(
                        id = "ovulation_day_${ovulationDateString}",
                        type = NotificationType.FERTILITY_WINDOW,
                        title = title,
                        body = body,
                        scheduledHour = ovulationReminder.get(Calendar.HOUR_OF_DAY),
                        scheduledMinute = ovulationReminder.get(Calendar.MINUTE),
                        targetMode = goal,
                        isEnabled = true,
                        repeatDaily = false
                    )
                    
                    notificationHelper.scheduleNotification(notification)
                }
            }
        }
    }
    
    /**
     * Планирует уведомления о ПМС
     */
    private fun schedulePMSNotifications() {
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) return
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val lastPeriod = dateFormat.parse(lastPeriodStart) ?: return
        
        // Планируем на следующие 3 цикла
        for (cycleOffset in 0..2) {
            val cycleStart = Calendar.getInstance().apply {
                time = lastPeriod
                add(Calendar.DAY_OF_YEAR, cycleLength * cycleOffset)
            }
            
            // ПМС обычно начинается за 7 дней до начала цикла
            val pmsStartDate = Calendar.getInstance().apply {
                time = cycleStart.time
                add(Calendar.DAY_OF_YEAR, cycleLength - 7)
            }
            
            val pmsDateString = dateFormat.format(pmsStartDate.time)
            val forecast = calendarPredictor.getForecast(pmsDateString)
            
            if (forecast.predictedPMS && forecast.confidence >= 50) {
                // Уведомление за 1 день до ПМС
                val reminderDate = Calendar.getInstance().apply {
                    time = pmsStartDate.time
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 18)
                    set(Calendar.MINUTE, 0)
                }
                
                if (reminderDate.timeInMillis > System.currentTimeMillis()) {
                    val notification = SmartNotification(
                        id = "pms_prediction_${pmsDateString}",
                        type = NotificationType.CYCLE_PREDICTION,
                        title = "Завтра возможны симптомы ПМС",
                        body = "Завтра возможны перепады настроения. Хотите запланировать вечер релаксации?",
                        scheduledHour = reminderDate.get(Calendar.HOUR_OF_DAY),
                        scheduledMinute = reminderDate.get(Calendar.MINUTE),
                        targetMode = userPreferences.getSelectedGoal(),
                        isEnabled = true,
                        repeatDaily = false
                    )
                    
                    notificationHelper.scheduleNotification(notification)
                }
            }
        }
    }
    
    
    /**
     * Отменяет все умные уведомления
     */
    fun cancelSmartNotifications() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()
        
        // Отменяем уведомления на следующие 3 месяца
        for (monthOffset in 0..3) {
            val futureDate = Calendar.getInstance().apply {
                add(Calendar.MONTH, monthOffset)
            }
            
            val dateString = dateFormat.format(futureDate.time)
            
            // Отменяем все типы умных уведомлений
            notificationHelper.cancelNotification("period_prediction_$dateString")
            notificationHelper.cancelNotification("ovulation_day_$dateString")
            notificationHelper.cancelNotification("fertility_window_$dateString")
            notificationHelper.cancelNotification("fertility_window_start_$dateString")
            notificationHelper.cancelNotification("pms_prediction_$dateString")
        }
    }
    
    /**
     * Обновляет умные уведомления (отменяет старые и планирует новые)
     */
    fun updateSmartNotifications() {
        cancelSmartNotifications()
        scheduleSmartNotifications()
    }
}


