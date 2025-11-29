package com.example.womenhealthtracker

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserPreferences(context: Context) {
    
    private val appContext: Context = context.applicationContext
    
    val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "user_preferences"
        private const val KEY_NAME = "user_name"
        private const val KEY_AGE = "user_age"
        private const val KEY_CYCLE_LENGTH = "cycle_length"
        private const val KEY_MENSTRUATION_LENGTH = "menstruation_length"
        private const val KEY_GOALS = "user_goals"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PERIOD_DATES = "period_dates"
        private const val KEY_LAST_PERIOD_START = "last_period_start"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_NOTIFICATION_PERIOD = "notification_period"
        private const val KEY_NOTIFICATION_FERTILE = "notification_fertile"
        private const val KEY_NOTIFICATION_DAILY = "notification_daily"
        private const val KEY_NOTIFICATION_WATER = "notification_water"
        private const val KEY_DAY_DATA = "day_data_" // Префикс для данных по дням
        private const val KEY_SELECTED_GOAL = "selected_goal"
        private const val KEY_PREGNANCY_START_DATE = "pregnancy_start_date"
        private const val KEY_MENOPAUSE_START_DATE = "menopause_start_date"
        private const val KEY_HAS_IRREGULAR_CYCLES = "has_irregular_cycles"
        private const val KEY_PREGNANCY_DATA = "pregnancy_data"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MENOPAUSE_DAY_RECORD = "menopause_day_record_" // Префикс для записей менопаузы
        private const val KEY_NOTIFICATIONS = "smart_notifications" // Умные уведомления
        private const val KEY_DELAY_RECORDS = "delay_records" // Записи о задержках
        private const val KEY_MENOPAUSE_DOCTOR_VISITS = "menopause_doctor_visits" // Визиты к врачу в менопаузе
    }
    
    private val gson = Gson().newBuilder()
        .setLenient() // Разрешаем более гибкую десериализацию
        .create()
    
    // Сохранение имени
    fun saveName(name: String) {
        prefs.edit().putString(KEY_NAME, name).apply()
    }
    
    // Получение имени
    fun getName(): String {
        return prefs.getString(KEY_NAME, "") ?: ""
    }
    
    // Сохранение возраста
    fun saveAge(age: Int) {
        prefs.edit().putInt(KEY_AGE, age).apply()
    }
    
    // Получение возраста
    fun getAge(): Int {
        return prefs.getInt(KEY_AGE, 0)
    }
    
    // Сохранение длины цикла
    fun saveCycleLength(length: Int) {
        prefs.edit().putInt(KEY_CYCLE_LENGTH, length).apply()
    }
    
    // Получение длины цикла
    fun getCycleLength(): Int {
        return prefs.getInt(KEY_CYCLE_LENGTH, 0)
    }
    
    // Сохранение целей
    fun saveGoals(goals: String) {
        prefs.edit().putString(KEY_GOALS, goals).apply()
    }
    
    // Получение целей
    fun getGoals(): String {
        return prefs.getString(KEY_GOALS, "") ?: ""
    }
    
    // Сохранение статуса авторизации
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    // Проверка авторизации
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    // Сохранение даты начала месячных (сохраняет также все дни периода)
    fun savePeriodStartDate(dateString: String) {
        val existingDates = getPeriodDates().toMutableSet()
        
        // Добавляем все дни периода (обычно 5 дней)
        val periodDuration = 5
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        
        try {
            calendar.time = dateFormat.parse(dateString) ?: return
            for (i in 0 until periodDuration) {
                val periodDate = dateFormat.format(calendar.time)
                existingDates.add(periodDate)
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        } catch (e: Exception) {
            // Если не удалось распарсить дату, просто добавляем саму дату
            existingDates.add(dateString)
        }
        
        prefs.edit().putStringSet(KEY_PERIOD_DATES, existingDates).apply()
        prefs.edit().putString(KEY_LAST_PERIOD_START, dateString).apply()
    }
    
    // Получение всех дат месячных
    fun getPeriodDates(): Set<String> {
        return prefs.getStringSet(KEY_PERIOD_DATES, emptySet()) ?: emptySet()
    }
    
    // Сохранение набора дат месячных (для редактирования)
    fun savePeriodDates(dates: Set<String>) {
        prefs.edit().putStringSet(KEY_PERIOD_DATES, dates).apply()
    }
    
    // Получение даты последнего начала месячных
    fun getLastPeriodStart(): String {
        return prefs.getString(KEY_LAST_PERIOD_START, "") ?: ""
    }
    
    // Сохранение даты последнего начала месячных
    fun saveLastPeriodStart(dateString: String) {
        prefs.edit().putString(KEY_LAST_PERIOD_START, dateString).apply()
    }
    
    // Сохранение длины менструации
    fun saveMenstruationLength(length: Int) {
        prefs.edit().putInt(KEY_MENSTRUATION_LENGTH, length).apply()
    }
    
    // Получение длины менструации
    fun getMenstruationLength(): Int {
        return prefs.getInt(KEY_MENSTRUATION_LENGTH, 5)
    }
    
    // Сохранение статуса завершения онбординга
    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    // Проверка завершения онбординга
    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    // Настройки уведомлений
    fun setNotificationPeriod(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_PERIOD, enabled).apply()
    }
    
    fun isNotificationPeriodEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_PERIOD, true)
    }
    
    fun setNotificationFertile(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_FERTILE, enabled).apply()
    }
    
    fun isNotificationFertileEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_FERTILE, true)
    }
    
    fun setNotificationDaily(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_DAILY, enabled).apply()
    }
    
    fun isNotificationDailyEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_DAILY, false)
    }
    
    fun setNotificationWater(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_WATER, enabled).apply()
    }
    
    fun isNotificationWaterEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_WATER, false)
    }
    
    // Сохранение данных дня (локально и в Firestore)
    fun saveDayData(dayData: DayData) {
        // Сохраняем локально
        val json = gson.toJson(dayData)
        prefs.edit().putString("${KEY_DAY_DATA}${dayData.date}", json).apply()
        
        // Синхронизируем с Firestore (если пользователь авторизован)
        try {
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val firestoreHelper = FirestoreHelper(appContext)
                firestoreHelper.saveDayData(
                    userId = currentUser.uid,
                    dayData = dayData,
                    onSuccess = {
                        android.util.Log.d("UserPreferences", "Данные синхронизированы с Firestore: ${dayData.date}")
                    },
                    onError = { error ->
                        android.util.Log.e("UserPreferences", "Ошибка синхронизации с Firestore: $error")
                        // Не показываем ошибку пользователю, данные сохранены локально
                    }
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("UserPreferences", "Ошибка при попытке синхронизации: ${e.message}")
            // Продолжаем работу, данные сохранены локально
        }
    }
    
    // Получение данных дня
    fun getDayData(date: String): DayData? {
        val json = prefs.getString("${KEY_DAY_DATA}$date", null) ?: return null
        return try {
            // Парсим JSON в Map, чтобы удалить старые поля менопаузы
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val dataMap = gson.fromJson<Map<String, Any>>(json, type)
            
            // Удаляем старые поля менопаузы, если они есть
            val cleanedMap = dataMap.toMutableMap().apply {
                remove("hotFlashes")
                remove("brainFogLevel")
                remove("vaginalHealth")
                remove("jointPain")
                remove("triggers")
                remove("reliefMethods")
                remove("bleeding")
                remove("bleedingIntensity")
            }
            
            // Конвертируем очищенную Map обратно в JSON и десериализуем в DayData
            val cleanedJson = gson.toJson(cleanedMap)
            gson.fromJson(cleanedJson, DayData::class.java)
        } catch (e: Exception) {
            // Если не удалось распарсить, удаляем поврежденные данные
            prefs.edit().remove("${KEY_DAY_DATA}$date").apply()
            null
        }
    }
    
    // Получение всех дат с данными
    fun getAllDatesWithData(): List<String> {
        val allDates = mutableListOf<String>()
        val allPrefs = prefs.all
        for (key in allPrefs.keys) {
            if (key.startsWith(KEY_DAY_DATA)) {
                val date = key.removePrefix(KEY_DAY_DATA)
                allDates.add(date)
            }
        }
        return allDates.sorted()
    }
    
    // Получение всех данных за период
    fun getDayDataForPeriod(startDate: String, endDate: String): List<DayData> {
        val allDates = getAllDatesWithData()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val start = dateFormat.parse(startDate) ?: return emptyList()
        val end = dateFormat.parse(endDate) ?: return emptyList()
        
        return allDates.mapNotNull { date ->
            try {
                val dateObj = dateFormat.parse(date) ?: return@mapNotNull null
                if (dateObj >= start && dateObj <= end) {
                    getDayData(date)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()
    }
    
    // Очистка всех данных (для выхода)
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    // ========== Методы для работы с режимами ==========
    
    // Сохранение выбранного режима
    fun saveSelectedGoal(goal: UserGoal) {
        prefs.edit().putString(KEY_SELECTED_GOAL, goal.name).apply()
    }
    
    // Получение выбранного режима
    fun getSelectedGoal(): UserGoal {
        val goalString = prefs.getString(KEY_SELECTED_GOAL, "CYCLE_TRACKING") ?: "CYCLE_TRACKING"
        return try {
            // Если сохранен старый режим планирования беременности, заменяем на отслеживание цикла
            if (goalString == "PREGNANCY_PLANNING") {
                UserGoal.CYCLE_TRACKING
            } else {
                UserGoal.valueOf(goalString)
            }
        } catch (e: Exception) {
            UserGoal.CYCLE_TRACKING
        }
    }
    
    // Сохранение даты начала беременности
    fun savePregnancyStartDate(date: String) {
        prefs.edit().putString(KEY_PREGNANCY_START_DATE, date).apply()
    }
    
    // Получение даты начала беременности
    fun getPregnancyStartDate(): String {
        return prefs.getString(KEY_PREGNANCY_START_DATE, "") ?: ""
    }
    
    // Сохранение даты начала менопаузы
    fun saveMenopauseStartDate(date: String) {
        prefs.edit().putString(KEY_MENOPAUSE_START_DATE, date).apply()
    }
    
    // Получение даты начала менопаузы
    fun getMenopauseStartDate(): String {
        return prefs.getString(KEY_MENOPAUSE_START_DATE, "") ?: ""
    }
    
    // ========== Методы для работы с записями менопаузы ==========
    
    // Сохранение записи дня менопаузы
    fun saveMenopauseDayRecord(record: MenopauseDayRecord) {
        val json = gson.toJson(record)
        prefs.edit().putString("${KEY_MENOPAUSE_DAY_RECORD}${record.date}", json).apply()
    }
    
    // Получение записи дня менопаузы
    fun getMenopauseDayRecord(date: String): MenopauseDayRecord? {
        val json = prefs.getString("${KEY_MENOPAUSE_DAY_RECORD}$date", null) ?: return null
        return try {
            gson.fromJson(json, MenopauseDayRecord::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    // Получение всех дат с записями менопаузы
    fun getAllMenopauseDates(): List<String> {
        val allDates = mutableListOf<String>()
        val allPrefs = prefs.all
        for (key in allPrefs.keys) {
            if (key.startsWith(KEY_MENOPAUSE_DAY_RECORD)) {
                val date = key.removePrefix(KEY_MENOPAUSE_DAY_RECORD)
                allDates.add(date)
            }
        }
        return allDates.sorted()
    }
    
    // Получение всех записей менопаузы за период
    fun getMenopauseDayRecordsForPeriod(startDate: String, endDate: String): List<MenopauseDayRecord> {
        val allDates = getAllMenopauseDates()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val start = dateFormat.parse(startDate) ?: return emptyList()
        val end = dateFormat.parse(endDate) ?: return emptyList()
        
        return allDates.mapNotNull { date ->
            try {
                val dateObj = dateFormat.parse(date) ?: return@mapNotNull null
                if (dateObj >= start && dateObj <= end) {
                    getMenopauseDayRecord(date)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.filterNotNull()
    }
    
    // Сохранение флага нерегулярных циклов
    fun saveHasIrregularCycles(hasIrregular: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_IRREGULAR_CYCLES, hasIrregular).apply()
    }
    
    // Получение флага нерегулярных циклов
    fun hasIrregularCycles(): Boolean {
        return prefs.getBoolean(KEY_HAS_IRREGULAR_CYCLES, false)
    }
    
    // ========== Методы для работы с задержками ==========
    
    // Сохранение записи о задержке
    fun saveDelayRecord(record: DelayRecord) {
        val records = getDelayRecords().toMutableList()
        val existingIndex = records.indexOfFirst { it.id == record.id }
        if (existingIndex >= 0) {
            records[existingIndex] = record
        } else {
            records.add(record)
        }
        val json = gson.toJson(records.map { it.toMap() })
        prefs.edit().putString(KEY_DELAY_RECORDS, json).apply()
    }
    
    // Получение всех записей о задержках
    fun getDelayRecords(): List<DelayRecord> {
        val json = prefs.getString(KEY_DELAY_RECORDS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Map<String, Any>>>() {}.type
            val recordsList = gson.fromJson<List<Map<String, Any>>>(json, type)
            recordsList.map { DelayRecord.fromMap(it) }
        } catch (e: Exception) {
            android.util.Log.e("UserPreferences", "Ошибка десериализации DelayRecord: ${e.message}")
            emptyList()
        }
    }
    
    // Получение активной (нерешенной) задержки
    fun getActiveDelay(): DelayRecord? {
        return getDelayRecords().firstOrNull { !it.resolved }
    }
    
    // Получение истории циклов для анализа
    fun getCycleHistory(): List<Int> {
        // Получаем все даты начала месячных и вычисляем длину циклов
        val periodDates = getPeriodDates().sorted()
        if (periodDates.size < 2) return emptyList()
        
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cycles = mutableListOf<Int>()
        
        for (i in 1 until periodDates.size) {
            try {
                val date1 = dateFormat.parse(periodDates[i - 1]) ?: continue
                val date2 = dateFormat.parse(periodDates[i]) ?: continue
                val daysDiff = ((date2.time - date1.time) / (1000 * 60 * 60 * 24)).toInt()
                if (daysDiff in 20..45) { // Нормальный диапазон цикла
                    cycles.add(daysDiff)
                }
            } catch (e: Exception) {
                continue
            }
        }
        
        return cycles
    }
    
    // Сохранение данных беременности
    fun savePregnancyData(pregnancyData: PregnancyData) {
        val json = gson.toJson(pregnancyData)
        val success = prefs.edit().putString(KEY_PREGNANCY_DATA, json).commit()
        android.util.Log.d("UserPreferences", "Сохранение данных беременности: ${pregnancyData.visits.size} визитов, успех: $success")
        if (!success) {
            android.util.Log.e("UserPreferences", "ОШИБКА: Не удалось сохранить данные беременности!")
        }
    }
    
    // Получение данных беременности
    fun getPregnancyData(): PregnancyData {
        val json = prefs.getString(KEY_PREGNANCY_DATA, null)
        return if (json != null) {
            try {
                val data = gson.fromJson(json, PregnancyData::class.java)
                android.util.Log.d("UserPreferences", "Загружено данных беременности: ${data.visits.size} визитов")
                data
            } catch (e: Exception) {
                android.util.Log.e("UserPreferences", "Ошибка при загрузке данных беременности", e)
                // Возвращаем пустые данные при ошибке
                val startDate = getPregnancyStartDate()
                if (startDate.isNotEmpty()) {
                    PregnancyData(
                        pregnancyStartDate = startDate,
                        estimatedDueDate = ""
                    )
                } else {
                    PregnancyData(
                        pregnancyStartDate = "",
                        estimatedDueDate = ""
                    )
                }
            }
        } else {
            // Создаем новые данные, если их нет
            val startDate = getPregnancyStartDate()
            android.util.Log.d("UserPreferences", "Данных беременности нет, создаем новые")
            PregnancyData(
                pregnancyStartDate = startDate,
                estimatedDueDate = ""
            )
        }
    }
    
    // Сохранение ID пользователя
    fun saveUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }
    
    // Получение ID пользователя
    fun getUserId(): String {
        return prefs.getString(KEY_USER_ID, "") ?: ""
    }
    
    // Очистка старых данных с полями менопаузы
    fun cleanOldMenopauseData() {
        val allPrefs = prefs.all
        val editor = prefs.edit()
        
        for (key in allPrefs.keys) {
            if (key.startsWith(KEY_DAY_DATA)) {
                val json = prefs.getString(key, null)
                if (json != null && (json.contains("hotFlashes") || json.contains("brainFogLevel"))) {
                    // Пересохраняем данные без полей менопаузы
                    try {
                        val dayData = getDayData(key.removePrefix(KEY_DAY_DATA))
                        if (dayData != null) {
                            // Данные уже очищены в getDayData, просто пересохраняем
                            saveDayData(dayData)
                        } else {
                            // Если не удалось распарсить, удаляем
                            editor.remove(key)
                        }
                    } catch (e: Exception) {
                        // Удаляем поврежденные данные
                        editor.remove(key)
                    }
                }
            }
        }
        editor.apply()
    }
    
    // ========== Методы для работы с умными уведомлениями ==========
    
    // Сохранение списка уведомлений
    fun saveNotifications(notifications: List<SmartNotification>) {
        val notificationsList = notifications.map { it.toMap() }
        val json = gson.toJson(notificationsList)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }
    
    // Получение списка уведомлений
    fun getNotifications(): List<SmartNotification> {
        val json = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            val notificationsList = gson.fromJson<List<Map<String, Any>>>(json, type)
            notificationsList.map { SmartNotification.fromMap(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Получение уведомлений для конкретного режима
    fun getNotificationsForMode(mode: UserGoal): List<SmartNotification> {
        return getNotifications().filter { it.targetMode == mode }
    }
    
    // Сохранение состояния уведомления
    fun updateNotification(notification: SmartNotification) {
        val notifications = getNotifications().toMutableList()
        val index = notifications.indexOfFirst { it.id == notification.id }
        if (index >= 0) {
            notifications[index] = notification
        } else {
            notifications.add(notification)
        }
        saveNotifications(notifications)
    }
    
    // ========== Методы для работы с визитами к врачу в менопаузе ==========
    
    // Сохранение списка визитов к врачу
    fun saveMenopauseDoctorVisits(visits: List<MenopauseDoctorVisit>) {
        val visitsList = visits.map { it.toMap() }
        val json = gson.toJson(visitsList)
        val success = prefs.edit().putString(KEY_MENOPAUSE_DOCTOR_VISITS, json).commit()
        android.util.Log.d("UserPreferences", "Сохранение визитов менопаузы: ${visits.size} визитов, успех: $success")
        if (!success) {
            android.util.Log.e("UserPreferences", "ОШИБКА: Не удалось сохранить визиты менопаузы!")
        }
    }
    
    // Получение списка визитов к врачу
    fun getMenopauseDoctorVisits(): List<MenopauseDoctorVisit> {
        val json = prefs.getString(KEY_MENOPAUSE_DOCTOR_VISITS, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Map<String, Any>>>() {}.type
            val visitsList = gson.fromJson<List<Map<String, Any>>>(json, type)
            val visits = visitsList.map { MenopauseDoctorVisit.fromMap(it) }
            android.util.Log.d("UserPreferences", "Загружено визитов менопаузы: ${visits.size}")
            visits
        } catch (e: Exception) {
            android.util.Log.e("UserPreferences", "Ошибка при загрузке визитов менопаузы", e)
            emptyList()
        }
    }
}

