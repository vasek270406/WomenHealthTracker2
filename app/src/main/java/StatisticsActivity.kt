package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class StatisticsActivity : AppCompatActivity() {
    
    private lateinit var firestoreHelper: FirestoreHelper
    private val authHelper = FirebaseAuthHelper(this)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_statistics)
        
        // Проверяем режим пользователя
        val userPreferences = UserPreferences(this)
        val selectedGoal = userPreferences.getSelectedGoal()
        
        firestoreHelper = FirestoreHelper(this)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        // Добавляем кнопку статистики для беременности или менопаузы
        setupModeSpecificButton(selectedGoal)
        
        loadStatistics()
    }
    
    private fun setupModeSpecificButton(selectedGoal: UserGoal) {
        val buttonsContainer = findViewById<LinearLayout>(R.id.modeSpecificButtonsContainer)
        buttonsContainer?.removeAllViews()
        
        when (selectedGoal) {
            UserGoal.PREGNANCY -> {
                // Кнопка общей статистики беременности
                val fullStatsButton = android.widget.Button(this)
                fullStatsButton.text = "📊 Статистика беременности"
                fullStatsButton.textSize = 18f
                fullStatsButton.setTextColor(android.graphics.Color.parseColor("#000000"))
                fullStatsButton.setTypeface(null, android.graphics.Typeface.BOLD)
                fullStatsButton.setPadding(0, 16, 0, 16)
                fullStatsButton.background = getDrawable(R.drawable.button_pink_rounded)
                fullStatsButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(24, 0, 24, 12)
                }
                fullStatsButton.setOnClickListener {
                    val intent = Intent(this, PregnancyStatisticsFullActivity::class.java)
                    startActivity(intent)
                }
                buttonsContainer?.addView(fullStatsButton)
                
                // Кнопка детальной статистики беременности
                val detailedStatsButton = android.widget.Button(this)
                detailedStatsButton.text = "📈 Детальная статистика"
                detailedStatsButton.textSize = 18f
                detailedStatsButton.setTextColor(android.graphics.Color.parseColor("#000000"))
                detailedStatsButton.setTypeface(null, android.graphics.Typeface.BOLD)
                detailedStatsButton.setPadding(0, 16, 0, 16)
                detailedStatsButton.background = getDrawable(R.drawable.button_pink_rounded)
                detailedStatsButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(24, 0, 24, 16)
                }
                detailedStatsButton.setOnClickListener {
                    val intent = Intent(this, PregnancyStatisticsActivity::class.java)
                    startActivity(intent)
                }
                buttonsContainer?.addView(detailedStatsButton)
            }
            UserGoal.MENOPAUSE -> {
                // Кнопка общей статистики менопаузы
                val fullStatsButton = android.widget.Button(this)
                fullStatsButton.text = "📊 Статистика менопаузы"
                fullStatsButton.textSize = 18f
                fullStatsButton.setTextColor(android.graphics.Color.parseColor("#000000"))
                fullStatsButton.setTypeface(null, android.graphics.Typeface.BOLD)
                fullStatsButton.setPadding(0, 16, 0, 16)
                fullStatsButton.background = getDrawable(R.drawable.button_pink_rounded)
                fullStatsButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(24, 0, 24, 12)
                }
                fullStatsButton.setOnClickListener {
                    val intent = Intent(this, MenopauseStatisticsFullActivity::class.java)
                    startActivity(intent)
                }
                buttonsContainer?.addView(fullStatsButton)
                
                // Кнопка детальной аналитики менопаузы
                val detailedStatsButton = android.widget.Button(this)
                detailedStatsButton.text = "📈 Детальная аналитика"
                detailedStatsButton.textSize = 18f
                detailedStatsButton.setTextColor(android.graphics.Color.parseColor("#000000"))
                detailedStatsButton.setTypeface(null, android.graphics.Typeface.BOLD)
                detailedStatsButton.setPadding(0, 16, 0, 16)
                detailedStatsButton.background = getDrawable(R.drawable.button_pink_rounded)
                detailedStatsButton.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(24, 0, 24, 16)
                }
                detailedStatsButton.setOnClickListener {
                    val intent = Intent(this, MenopauseStatsActivity::class.java)
                    startActivity(intent)
                }
                buttonsContainer?.addView(detailedStatsButton)
            }
            else -> {
                // Для других режимов ничего не добавляем
            }
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем статистику при возврате на экран
        syncAndReloadStatistics()
    }
    
    /**
     * Синхронизация данных из Firestore и обновление статистики
     */
    private fun syncAndReloadStatistics() {
        val userId = authHelper.getCurrentUserId() ?: return
        val userPreferences = UserPreferences(this)
        
        // Получаем все даты с данными из Firestore
        firestoreHelper.getAllDatesWithData(
            userId = userId,
            onSuccess = { dates ->
                if (dates.isEmpty()) {
                    loadStatistics() // Просто обновляем статистику
                    return@getAllDatesWithData
                }
                
                // Загружаем данные для каждой даты
                var loadedCount = 0
                val totalDates = dates.size
                
                dates.forEach { date ->
                    firestoreHelper.getDayData(
                        userId = userId,
                        date = date,
                        onSuccess = { dayData ->
                            if (dayData != null) {
                                // Сохраняем данные дня в SharedPreferences
                                userPreferences.saveDayData(dayData)
                            }
                            
                            loadedCount++
                            // Когда все данные загружены, обновляем статистику
                            if (loadedCount == totalDates) {
                                loadStatistics()
                            }
                        },
                        onError = { error ->
                            android.util.Log.e("StatisticsActivity", "Ошибка загрузки данных дня $date: $error")
                            loadedCount++
                            if (loadedCount == totalDates) {
                                loadStatistics()
                            }
                        }
                    )
                }
            },
            onError = { error ->
                android.util.Log.e("StatisticsActivity", "Ошибка получения списка дат: $error")
                // Даже если не удалось загрузить данные, обновляем статистику
                loadStatistics()
            }
        )
    }
    
    private fun loadStatistics() {
        val userPreferences = UserPreferences(this)
        val selectedGoal = userPreferences.getSelectedGoal()
        
        val cycleDaysTextView = findViewById<TextView>(R.id.cycleDaysTextView)
        val energyTextView = findViewById<TextView>(R.id.energyTextView)
        val symptomsTextView = findViewById<TextView>(R.id.symptomsTextView)
        val moodStatTextView = findViewById<TextView>(R.id.moodStatTextView)
        
        // Показываем карточки статистики
        val statisticsContainer = findViewById<LinearLayout>(R.id.statisticsContainer)
        statisticsContainer?.visibility = android.view.View.VISIBLE
        
        // Меняем текст карточки "Цикл" на "Дней отслеживания" для менопаузы
        val cycleLabel = findViewById<TextView>(R.id.cycleLabelTextView)
        
        if (selectedGoal == UserGoal.PREGNANCY) {
            // Режим беременности
            if (cycleLabel != null) {
                cycleLabel.text = "Неделя"
            }
            
            val pregnancyData = userPreferences.getPregnancyData()
            
            if (pregnancyData.pregnancyStartDate.isNotEmpty()) {
                // Неделя беременности
                val currentWeek = pregnancyData.getCurrentWeek()
                cycleDaysTextView.text = currentWeek.toString()
                
                // Энергия - средняя энергия из данных дня за период беременности
                val allDates = userPreferences.getAllDatesWithData()
                val pregnancyStartDate = pregnancyData.pregnancyStartDate
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                
                val pregnancyDayData = allDates.mapNotNull { date ->
                    try {
                        val dateObj = dateFormat.parse(date)
                        val startDateObj = dateFormat.parse(pregnancyStartDate)
                        if (dateObj != null && startDateObj != null && dateObj >= startDateObj) {
                            userPreferences.getDayData(date)
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
                
                val avgEnergy = pregnancyDayData.mapNotNull { it.energy }.average()
                if (avgEnergy.isNaN()) {
                    energyTextView.text = "-"
                } else {
                    energyTextView.text = "${avgEnergy.toInt()}%"
                }
                
                // Симптомы беременности
                val symptomsCount = pregnancyData.symptoms.size
                symptomsTextView.text = when {
                    symptomsCount == 0 -> "Нет"
                    symptomsCount < 5 -> "Мало"
                    symptomsCount < 10 -> "Средне"
                    else -> "Много"
                }
                
                // Настроение - среднее настроение за период беременности
                val moods = pregnancyDayData.map { it.mood }.filter { it >= 0 && it <= 4 }
                if (moods.isEmpty()) {
                    moodStatTextView.text = "-"
                } else {
                    val avgMood = moods.average()
                    val avgMoodFormatted = String.format("%.1f", avgMood)
                    val moodValueForDisplay = kotlin.math.round(avgMood).toInt().coerceIn(0, 4)
                    val stars = "★".repeat(moodValueForDisplay + 1)
                    moodStatTextView.text = "$avgMoodFormatted$stars"
                }
            } else {
                cycleDaysTextView.text = "-"
                energyTextView.text = "-"
                symptomsTextView.text = "-"
                moodStatTextView.text = "-"
            }
        } else if (selectedGoal == UserGoal.MENOPAUSE) {
            // Режим менопаузы
            if (cycleLabel != null) {
                cycleLabel.text = "Дней отслеживания"
            }
            
            val menopauseRecords = userPreferences.getAllMenopauseDates()
                .mapNotNull { date -> userPreferences.getMenopauseDayRecord(date) }
            
            if (menopauseRecords.isNotEmpty()) {
                // Дней отслеживания
                val daysTracked = menopauseRecords.size
                cycleDaysTextView.text = daysTracked.toString()
                
                // Средняя энергия (1-5 -> процент)
                val avgEnergy = menopauseRecords.map { it.energy }.average()
                if (!avgEnergy.isNaN()) {
                    // Преобразуем из шкалы 1-5 в проценты (1=20%, 5=100%)
                    val energyPercent = ((avgEnergy - 1) / 4.0 * 100).toInt()
                    energyTextView.text = "${energyPercent}%"
                } else {
                    energyTextView.text = "-"
                }
                
                // Частота симптомов
                val totalSymptoms = menopauseRecords.sumOf { it.symptoms.size }
                val avgSymptomsPerDay = totalSymptoms.toDouble() / menopauseRecords.size
                symptomsTextView.text = when {
                    avgSymptomsPerDay < 1 -> "Редко"
                    avgSymptomsPerDay < 2 -> "Иногда"
                    else -> "Часто"
                }
                
                // Среднее настроение (1-5)
                val avgMood = menopauseRecords.map { it.mood }.average()
                if (!avgMood.isNaN()) {
                    val moodValue = avgMood.toInt()
                    val stars = "★".repeat(moodValue)
                    moodStatTextView.text = "${moodValue}$stars"
                } else {
                    moodStatTextView.text = "-"
                }
            } else {
                cycleDaysTextView.text = "0"
                energyTextView.text = "-"
                symptomsTextView.text = "-"
                moodStatTextView.text = "-"
            }
        } else {
            // Обычный режим (цикл)
            if (cycleLabel != null) {
                cycleLabel.text = "Цикл"
            }
            
            val cycleLength = userPreferences.getCycleLength()
            if (cycleLength > 0) {
                cycleDaysTextView.text = cycleLength.toString()
            } else {
                cycleDaysTextView.text = "28"
            }
            
            // Расчет статистики из сохраненных данных
            val allDates = userPreferences.getAllDatesWithData()
            if (allDates.isNotEmpty()) {
                val allDayData = allDates.mapNotNull { userPreferences.getDayData(it) }
                
                // Средний уровень энергии
                val avgEnergy = allDayData.mapNotNull { it.energy }.average()
                if (avgEnergy.isNaN()) {
                    energyTextView.text = "-"
                } else {
                    energyTextView.text = "${avgEnergy.toInt()}%"
                }
                
                // Частота симптомов
                val totalSymptoms = allDayData.sumOf { it.symptoms.size }
                val avgSymptomsPerDay = if (allDayData.isNotEmpty()) totalSymptoms.toDouble() / allDayData.size else 0.0
                symptomsTextView.text = when {
                    avgSymptomsPerDay < 1 -> "Редко"
                    avgSymptomsPerDay < 2 -> "Иногда"
                    else -> "Часто"
                }
                
                // Среднее настроение (точная статистика)
                val moods = allDayData.map { it.mood }.filter { it >= 0 && it <= 4 }
                if (moods.isEmpty()) {
                    moodStatTextView.text = "-"
                } else {
                    val avgMood = moods.average()
                    // Показываем точное среднее значение с одной десятичной
                    val avgMoodFormatted = String.format("%.1f", avgMood)
                    // Для отображения звездочек используем округленное значение (0-4 -> 1-5 для отображения)
                    val moodValueForDisplay = kotlin.math.round(avgMood).toInt().coerceIn(0, 4)
                    val stars = "★".repeat(moodValueForDisplay + 1) // +1 для отображения (0-4 -> 1-5)
                    moodStatTextView.text = "$avgMoodFormatted$stars"
                }
            } else {
                energyTextView.text = "-"
                symptomsTextView.text = "-"
                moodStatTextView.text = "-"
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

