package com.example.womenhealthtracker

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.*

class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        const val CHANNEL_ID = "women_health_tracker_channel"
        private const val CHANNEL_NAME = "Women Health Tracker"
        private const val CHANNEL_DESCRIPTION = "Уведомления о здоровье"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Создание канала уведомлений (для Android 8.0+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                AndroidNotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Планирование уведомления
     */
    fun scheduleNotification(notification: SmartNotification) {
        if (!notification.isEnabled) {
            cancelNotification(notification.id)
            return
        }
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notification.id)
            putExtra("notification_title", notification.title)
            putExtra("notification_body", notification.body)
            putExtra("notification_type", notification.type.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notification.scheduledHour)
            set(Calendar.MINUTE, notification.scheduledMinute)
            set(Calendar.SECOND, 0)
            
            // Если время уже прошло сегодня, планируем на завтра
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        scheduleNotificationAtTime(calendar, pendingIntent, notification.repeatDaily)
    }
    
    /**
     * Планирование уведомления на конкретную дату и время
     */
    fun scheduleNotificationAtDate(notification: SmartNotification, targetDate: Calendar) {
        if (!notification.isEnabled) {
            cancelNotification(notification.id)
            return
        }
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notification.id)
            putExtra("notification_title", notification.title)
            putExtra("notification_body", notification.body)
            putExtra("notification_type", notification.type.name)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notification.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val calendar = targetDate.clone() as Calendar
        calendar.set(Calendar.HOUR_OF_DAY, notification.scheduledHour)
        calendar.set(Calendar.MINUTE, notification.scheduledMinute)
        calendar.set(Calendar.SECOND, 0)
        
        scheduleNotificationAtTime(calendar, pendingIntent, notification.repeatDaily)
    }
    
    /**
     * Внутренний метод для планирования уведомления на конкретное время
     */
    private fun scheduleNotificationAtTime(calendar: Calendar, pendingIntent: PendingIntent, repeatDaily: Boolean) {
        if (repeatDaily) {
            // Ежедневное повторение
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        } else {
            // Однократное уведомление
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                @Suppress("DEPRECATION")
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
    
    /**
     * Отмена уведомления
     */
    fun cancelNotification(notificationId: String) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
    
    /**
     * Отмена всех уведомлений
     */
    fun cancelAllNotifications() {
        val userPreferences = UserPreferences(context)
        val notifications = userPreferences.getNotifications()
        notifications.forEach { notification: SmartNotification ->
            cancelNotification(notification.id)
        }
    }
    
    /**
     * Активация уведомлений для режима
     */
    fun activateModeNotifications(mode: UserGoal) {
        // Отменяем все старые уведомления
        cancelAllNotifications()
        
        // Получаем уведомления для нового режима
        val defaultNotificationsMap = com.example.womenhealthtracker.NotificationManager.getDefaultNotifications()
        val defaultNotifications = defaultNotificationsMap[mode] ?: emptyList()
        val userPreferences = UserPreferences(context)
        
        // Загружаем сохраненные настройки пользователя
        val savedNotifications = userPreferences.getNotificationsForMode(mode)
        val notificationMap = savedNotifications.associateBy { it.id }
        
        // Объединяем: используем сохраненные настройки, если есть, иначе - по умолчанию
        val notificationsToSchedule = defaultNotifications.map { default: SmartNotification ->
            notificationMap[default.id] ?: default
        }
        
        // Планируем уведомления
        notificationsToSchedule.forEach { notification: SmartNotification ->
            scheduleNotification(notification)
        }
        
        // Сохраняем уведомления
        val allNotifications = userPreferences.getNotifications().filter { it.targetMode != mode } + notificationsToSchedule
        userPreferences.saveNotifications(allNotifications)
    }
    
    /**
     * Планирование напоминаний о визите к врачу
     */
    fun scheduleDoctorVisitReminders(visit: MenopauseDoctorVisit) {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("HH:mm", Locale.getDefault())
        
        try {
            val visitDateParsed = dateFormat.parse(visit.date)
            if (visitDateParsed == null) return
            val visitDate = visitDateParsed
            val visitCalendar = Calendar.getInstance()
            visitCalendar.time = visitDate
            
            // Парсим время визита
            if (visit.time.isNotEmpty()) {
                try {
                    val timeParsed = timeFormat.parse(visit.time)
                    if (timeParsed != null) {
                        val timeCalendar = Calendar.getInstance()
                        timeCalendar.time = timeParsed
                        visitCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                        visitCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                    }
                } catch (e: Exception) {
                    // Если время не указано, используем 10:00 по умолчанию
                    visitCalendar.set(Calendar.HOUR_OF_DAY, 10)
                    visitCalendar.set(Calendar.MINUTE, 0)
                }
            } else {
                visitCalendar.set(Calendar.HOUR_OF_DAY, 10)
                visitCalendar.set(Calendar.MINUTE, 0)
            }
            
            // Напоминание за день до визита (в 9:00)
            if (visit.reminderDayBefore) {
                val dayBeforeCalendar = visitCalendar.clone() as Calendar
                dayBeforeCalendar.add(Calendar.DAY_OF_YEAR, -1)
                dayBeforeCalendar.set(Calendar.HOUR_OF_DAY, 9)
                dayBeforeCalendar.set(Calendar.MINUTE, 0)
                
                val notification = SmartNotification(
                    id = "doctor_visit_reminder_day_before_${visit.id}",
                    type = NotificationType.MEDICAL_REMINDER,
                    title = "Напоминание о визите к врачу",
                    body = "Завтра визит к ${visit.specialistType} (${visit.doctorName})",
                    scheduledHour = dayBeforeCalendar.get(Calendar.HOUR_OF_DAY),
                    scheduledMinute = dayBeforeCalendar.get(Calendar.MINUTE),
                    targetMode = UserGoal.MENOPAUSE,
                    isEnabled = true,
                    repeatDaily = false
                )
                
                scheduleNotification(notification)
            }
            
            // Напоминание в день визита (за час до времени визита)
            if (visit.reminderOnDay) {
                val onDayCalendar = visitCalendar.clone() as Calendar
                onDayCalendar.add(Calendar.HOUR_OF_DAY, -1)
                
                // Если время напоминания уже прошло, устанавливаем на 8:00 в день визита
                if (onDayCalendar.timeInMillis <= System.currentTimeMillis()) {
                    onDayCalendar.time = visitDate
                    onDayCalendar.set(Calendar.HOUR_OF_DAY, 8)
                    onDayCalendar.set(Calendar.MINUTE, 0)
                }
                
                val notification = SmartNotification(
                    id = "doctor_visit_reminder_on_day_${visit.id}",
                    type = NotificationType.MEDICAL_REMINDER,
                    title = "Визит к врачу сегодня",
                    body = "Сегодня в ${visit.time} визит к ${visit.specialistType} (${visit.doctorName})",
                    scheduledHour = onDayCalendar.get(Calendar.HOUR_OF_DAY),
                    scheduledMinute = onDayCalendar.get(Calendar.MINUTE),
                    targetMode = UserGoal.MENOPAUSE,
                    isEnabled = true,
                    repeatDaily = false
                )
                
                scheduleNotification(notification)
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "Ошибка планирования напоминаний: ${e.message}")
        }
    }
}

