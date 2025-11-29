package com.example.womenhealthtracker

import android.content.Context
import java.util.*

object NotificationManager {
    
    /**
     * Получить уведомления по умолчанию для каждого режима
     */
    fun getDefaultNotifications(): Map<UserGoal, List<SmartNotification>> {
        return mapOf(
            // РЕЖИМ ОТСЛЕЖИВАНИЯ ЦИКЛА
            UserGoal.CYCLE_TRACKING to listOf(
                SmartNotification(
                    id = "daily_reminder_cycle",
                    type = NotificationType.DAILY_REMINDER,
                    title = "Время записать данные",
                    body = "Как ваше настроение и симптомы сегодня?",
                    scheduledHour = 20,
                    scheduledMinute = 0,
                    targetMode = UserGoal.CYCLE_TRACKING,
                    isEnabled = true,
                    repeatDaily = true
                ),
                SmartNotification(
                    id = "period_prediction",
                    type = NotificationType.CYCLE_PREDICTION,
                    title = "Скоро начало цикла",
                    body = "Менструация ожидается через несколько дней",
                    scheduledHour = 9,
                    scheduledMinute = 0,
                    targetMode = UserGoal.CYCLE_TRACKING,
                    isEnabled = true,
                    repeatDaily = false,
                    repeatMonthly = true
                )
            ),
            
            // РЕЖИМ БЕРЕМЕННОСТИ
            UserGoal.PREGNANCY to listOf(
                SmartNotification(
                    id = "pregnancy_week_update",
                    type = NotificationType.PREGNANCY_CHECKUP,
                    title = "Новая неделя!",
                    body = "У вас началась новая неделя беременности",
                    scheduledHour = 9,
                    scheduledMinute = 0,
                    targetMode = UserGoal.PREGNANCY,
                    isEnabled = true,
                    repeatDaily = false,
                    repeatWeekly = true
                ),
                SmartNotification(
                    id = "kick_counter",
                    type = NotificationType.SYMPTOM_TRACKING,
                    title = "Посчитайте шевеления",
                    body = "Время посчитать движения малыша",
                    scheduledHour = 21,
                    scheduledMinute = 0,
                    targetMode = UserGoal.PREGNANCY,
                    isEnabled = true,
                    repeatDaily = true
                ),
                SmartNotification(
                    id = "doctor_visit_reminder",
                    type = NotificationType.MEDICAL_REMINDER,
                    title = "Напоминание о визите",
                    body = "Не забудьте о визите к врачу",
                    scheduledHour = 10,
                    scheduledMinute = 0,
                    targetMode = UserGoal.PREGNANCY,
                    isEnabled = true,
                    repeatDaily = false
                )
            ),
            
            // РЕЖИМ МЕНОПАУЗЫ
            UserGoal.MENOPAUSE to listOf(
                SmartNotification(
                    id = "menopause_daily_check",
                    type = NotificationType.DAILY_REMINDER,
                    title = "Как самочувствие?",
                    body = "Отметьте симптомы менопаузы сегодня",
                    scheduledHour = 19,
                    scheduledMinute = 0,
                    targetMode = UserGoal.MENOPAUSE,
                    isEnabled = true,
                    repeatDaily = true
                ),
                SmartNotification(
                    id = "medical_checkup",
                    type = NotificationType.MEDICAL_REMINDER,
                    title = "Профилактический осмотр",
                    body = "Напоминание о ежегодном осмотре",
                    scheduledHour = 10,
                    scheduledMinute = 0,
                    targetMode = UserGoal.MENOPAUSE,
                    isEnabled = true,
                    repeatDaily = false,
                    repeatMonthly = true
                )
            )
        )
    }
    
    /**
     * Получить уведомления для конкретного режима
     */
    fun getNotificationsForMode(mode: UserGoal): List<SmartNotification> {
        return getDefaultNotifications()[mode] ?: emptyList()
    }
    
    /**
     * Получить сообщения уведомлений по режимам
     */
    fun getNotificationMessage(mode: UserGoal, type: NotificationType, vararg params: String): String {
        val messages = mapOf(
            UserGoal.CYCLE_TRACKING to mapOf(
                NotificationType.DAILY_REMINDER to "Как ваше настроение сегодня?",
                NotificationType.CYCLE_PREDICTION to if (params.isNotEmpty()) 
                    "Цикл начнется через ${params[0]} дней" 
                else "Скоро начало цикла"
            ),
            UserGoal.PREGNANCY to mapOf(
                NotificationType.DAILY_REMINDER to "Посчитайте шевеления малыша",
                NotificationType.PREGNANCY_CHECKUP to if (params.isNotEmpty()) 
                    "Началась ${params[0]} неделя!" 
                else "Новая неделя беременности!"
            ),
            UserGoal.MENOPAUSE to mapOf(
                NotificationType.DAILY_REMINDER to "Как прошёл день? Отметьте приливы",
                NotificationType.MEDICAL_REMINDER to "Напоминание о ежегодном осмотре"
            )
        )
        
        return messages[mode]?.get(type) ?: "Напоминание"
    }
}



