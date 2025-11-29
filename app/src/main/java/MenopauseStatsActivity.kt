package com.example.womenhealthtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class MenopauseStatsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var statsContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_stats)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        statsContainer = findViewById(R.id.statsContainer)
        
        loadStatistics()
        setupBottomNavigation()
    }
    
    private fun loadStatistics() {
        val records = userPreferences.getAllMenopauseDates()
            .mapNotNull { date -> userPreferences.getMenopauseDayRecord(date) }
            .sortedBy { it.date }
        
        if (records.isEmpty()) {
            showEmptyState()
            return
        }
        
        // Очищаем контейнер
        statsContainer.removeAllViews()
        
        // Заголовок
        addHeader()
        
        // Общая статистика
        addGeneralStats(records)
        
        // Анализ симптомов
        addSymptomAnalysis(records)
        
        // Анализ триггеров
        addTriggerAnalysis(records)
        
        // Корреляции
        addCorrelations(records)
        
        // Тренды
        addTrends(records)
        
        // Рекомендации
        addRecommendations(records)
    }
    
    private fun addHeader() {
        val headerCard = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        val title = TextView(this)
        title.text = "Ваше здоровье под контролем"
        title.textSize = 20f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.setPadding(0, 0, 0, 8)
        layout.addView(title)
        
        val description = TextView(this)
        description.text = "Анализ помогает лучше понимать свое тело и заботиться о себе"
        description.textSize = 14f
        description.setTextColor(Color.parseColor("#666666"))
        layout.addView(description)
        
        headerCard.addView(layout)
        statsContainer.addView(headerCard)
    }
    
    private fun addGeneralStats(records: List<MenopauseDayRecord>) {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Общая статистика"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        // Количество дней отслеживания
        val daysTracked = records.size
        val daysText = TextView(this)
        daysText.text = "Дней отслеживания: $daysTracked"
        daysText.textSize = 14f
        daysText.setPadding(0, 4, 0, 4)
        layout.addView(daysText)
        
        // Среднее настроение
        val avgMood = records.map { it.mood }.average()
        val moodText = TextView(this)
        moodText.text = "Среднее настроение: ${String.format("%.1f", avgMood)}/5"
        moodText.textSize = 14f
        moodText.setPadding(0, 4, 0, 4)
        layout.addView(moodText)
        
        // Средняя энергия
        val avgEnergy = records.map { it.energy }.average()
        val energyText = TextView(this)
        energyText.text = "Средняя энергия: ${String.format("%.1f", avgEnergy)}/5"
        energyText.textSize = 14f
        energyText.setPadding(0, 4, 0, 4)
        layout.addView(energyText)
        
        // Дней с симптомами
        val daysWithSymptoms = records.count { it.symptoms.isNotEmpty() }
        val symptomsDaysText = TextView(this)
        symptomsDaysText.text = "Дней с симптомами: $daysWithSymptoms (${(daysWithSymptoms * 100 / daysTracked)}%)"
        symptomsDaysText.textSize = 14f
        symptomsDaysText.setPadding(0, 4, 0, 4)
        layout.addView(symptomsDaysText)
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun addSymptomAnalysis(records: List<MenopauseDayRecord>) {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Анализ симптомов"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        // Подсчет частоты симптомов
        val symptomFrequency = mutableMapOf<String, SymptomStats>()
        records.forEach { record ->
            record.symptoms.forEach { (symptomType, detail) ->
                val stats = symptomFrequency.getOrPut(symptomType) {
                    SymptomStats(symptomType, 0, 0.0, 0)
                }
                stats.count++
                stats.totalIntensity += detail.intensity
                stats.maxIntensity = maxOf(stats.maxIntensity, detail.intensity)
            }
        }
        
        // Сортируем по частоте
        val sortedSymptoms = symptomFrequency.values.sortedByDescending { it.count }
        
        if (sortedSymptoms.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "Пока нет данных о симптомах"
            emptyText.textSize = 14f
            emptyText.setTextColor(Color.parseColor("#666666"))
            layout.addView(emptyText)
        } else {
            sortedSymptoms.take(10).forEach { stats ->
                val symptomType = try {
                    MenopauseSymptomType.valueOf(stats.symptomType)
                } catch (e: Exception) {
                    null
                }
                
                val symptomLayout = LinearLayout(this)
                symptomLayout.orientation = LinearLayout.VERTICAL
                symptomLayout.setPadding(0, 8, 0, 8)
                
                val symptomName = TextView(this)
                symptomName.text = "${symptomType?.displayName ?: stats.symptomType}"
                symptomName.textSize = 16f
                symptomName.setTypeface(null, android.graphics.Typeface.BOLD)
                symptomName.setPadding(0, 0, 0, 4)
                symptomLayout.addView(symptomName)
                
                val frequencyText = TextView(this)
                val percentage = (stats.count * 100 / records.size)
                frequencyText.text = "Частота: $percentage% (${stats.count} из ${records.size} дней)"
                frequencyText.textSize = 14f
                frequencyText.setTextColor(Color.parseColor("#666666"))
                frequencyText.setPadding(0, 0, 0, 2)
                symptomLayout.addView(frequencyText)
                
                val intensityText = TextView(this)
                val avgIntensity = stats.totalIntensity / stats.count
                intensityText.text = "Средняя интенсивность: ${String.format("%.1f", avgIntensity)}/5 (макс: ${stats.maxIntensity}/5)"
                intensityText.textSize = 14f
                intensityText.setTextColor(Color.parseColor("#666666"))
                symptomLayout.addView(intensityText)
                
                // Простая визуализация частоты
                val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
                progressBar.max = 100
                progressBar.progress = percentage
                progressBar.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 0)
                }
                progressBar.progressDrawable = getDrawable(R.drawable.progress_bar_pink)
                symptomLayout.addView(progressBar)
                
                layout.addView(symptomLayout)
            }
        }
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun addTriggerAnalysis(records: List<MenopauseDayRecord>) {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Анализ триггеров"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        val subtitle = TextView(this)
        subtitle.text = "Чаще всего симптомы вызывают:"
        subtitle.textSize = 14f
        subtitle.setTextColor(Color.parseColor("#666666"))
        subtitle.setPadding(0, 0, 0, 8)
        layout.addView(subtitle)
        
        // Подсчет триггеров
        val triggerCount = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.triggers.forEach { trigger ->
                triggerCount[trigger] = (triggerCount[trigger] ?: 0) + 1
            }
        }
        
        val total = triggerCount.values.sum()
        if (total == 0) {
            val emptyText = TextView(this)
            emptyText.text = "Недостаточно данных для анализа триггеров"
            emptyText.textSize = 14f
            emptyText.setTextColor(Color.parseColor("#666666"))
            layout.addView(emptyText)
        } else {
            triggerCount.toList().sortedByDescending { it.second }.take(8).forEach { (trigger, count) ->
                val percentage = (count * 100 / total)
                
                val triggerLayout = LinearLayout(this)
                triggerLayout.orientation = LinearLayout.VERTICAL
                triggerLayout.setPadding(0, 8, 0, 8)
                
                val triggerText = TextView(this)
                triggerText.text = "$trigger: $percentage% случаев ($count раз)"
                triggerText.textSize = 14f
                triggerText.setPadding(0, 0, 0, 4)
                layout.addView(triggerText)
                
                // Визуализация
                val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
                progressBar.max = 100
                progressBar.progress = percentage
                progressBar.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 4, 0, 0)
                }
                progressBar.progressDrawable = getDrawable(R.drawable.progress_bar_pink)
                triggerLayout.addView(progressBar)
                
                layout.addView(triggerLayout)
            }
        }
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun addCorrelations(records: List<MenopauseDayRecord>) {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Корреляции"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        // Корреляция между триггерами и симптомами
        val triggerSymptomMap = mutableMapOf<String, MutableList<String>>()
        records.forEach { record ->
            record.triggers.forEach { trigger ->
                record.symptoms.keys.forEach { symptom ->
                    triggerSymptomMap.getOrPut(trigger) { mutableListOf() }.add(symptom)
                }
            }
        }
        
        if (triggerSymptomMap.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "Недостаточно данных для анализа корреляций"
            emptyText.textSize = 14f
            emptyText.setTextColor(Color.parseColor("#666666"))
            layout.addView(emptyText)
        } else {
            val subtitle = TextView(this)
            subtitle.text = "Связь между триггерами и симптомами:"
            subtitle.textSize = 14f
            subtitle.setTextColor(Color.parseColor("#666666"))
            subtitle.setPadding(0, 0, 0, 8)
            layout.addView(subtitle)
            
            triggerSymptomMap.toList().sortedByDescending { it.second.size }.take(5).forEach { (trigger, symptoms) ->
                val symptomCounts = symptoms.groupingBy { it }.eachCount()
                val topSymptoms = symptomCounts.toList().sortedByDescending { it.second }.take(3)
                
                val correlationText = TextView(this)
                val symptomsText = topSymptoms.joinToString(", ") { (symptom, count) ->
                    val symptomType = try {
                        MenopauseSymptomType.valueOf(symptom)
                    } catch (e: Exception) {
                        null
                    }
                    "${symptomType?.displayName ?: symptom} ($count)"
                }
                correlationText.text = "$trigger → $symptomsText"
                correlationText.textSize = 14f
                correlationText.setPadding(0, 4, 0, 4)
                layout.addView(correlationText)
            }
        }
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun addTrends(records: List<MenopauseDayRecord>) {
        if (records.size < 7) return // Нужно минимум неделя данных
        
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Тренды"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        // Разбиваем на недели
        val weeklyData = records.chunked(7)
        val weeklyStats = weeklyData.mapIndexed { weekIndex, weekRecords ->
            val symptomCount = weekRecords.sumOf { it.symptoms.size }
            val avgMood = weekRecords.map { it.mood }.average()
            val avgEnergy = weekRecords.map { it.energy }.average()
            WeekStats(weekIndex + 1, symptomCount, avgMood, avgEnergy)
        }
        
        weeklyStats.forEach { weekStat ->
            val weekText = TextView(this)
            weekText.text = "Неделя ${weekStat.week}: Симптомов - ${weekStat.symptomCount}, Настроение - ${String.format("%.1f", weekStat.avgMood)}/5, Энергия - ${String.format("%.1f", weekStat.avgEnergy)}/5"
            weekText.textSize = 14f
            weekText.setPadding(0, 4, 0, 4)
            layout.addView(weekText)
        }
        
        // Анализ тренда
        if (weeklyStats.size >= 2) {
            val firstWeek = weeklyStats.first()
            val lastWeek = weeklyStats.last()
            
            val trendText = TextView(this)
            trendText.textSize = 14f
            trendText.setTypeface(null, android.graphics.Typeface.BOLD)
            trendText.setPadding(0, 12, 0, 4)
            
            val symptomTrend = if (lastWeek.symptomCount < firstWeek.symptomCount) {
                "Симптомы уменьшились"
            } else if (lastWeek.symptomCount > firstWeek.symptomCount) {
                "Симптомы увеличились"
            } else {
                "Симптомы стабильны"
            }
            
            val moodTrend = if (lastWeek.avgMood > firstWeek.avgMood) {
                "Настроение улучшилось"
            } else if (lastWeek.avgMood < firstWeek.avgMood) {
                "Настроение ухудшилось"
            } else {
                "Настроение стабильно"
            }
            
            trendText.text = "$symptomTrend\n$moodTrend"
            layout.addView(trendText)
        }
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun addRecommendations(records: List<MenopauseDayRecord>) {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Рекомендации"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        val recommendations = mutableListOf<String>()
        
        // Анализ данных для рекомендаций
        val avgMood = records.map { it.mood }.average()
        val avgEnergy = records.map { it.energy }.average()
        val daysWithSymptoms = records.count { it.symptoms.isNotEmpty() }
        val symptomPercentage = (daysWithSymptoms * 100 / records.size)
        
        // Рекомендации на основе данных
        if (avgMood < 3) {
            recommendations.add("Ваше настроение ниже среднего. Попробуйте техники релаксации, медитацию или обратитесь к специалисту.")
        }
        
        if (avgEnergy < 3) {
            recommendations.add("Энергия на низком уровне. Убедитесь, что вы достаточно спите и правильно питаетесь.")
        }
        
        if (symptomPercentage > 70) {
            recommendations.add("Симптомы проявляются очень часто. Рекомендуем проконсультироваться с врачом.")
        }
        
        // Анализ триггеров
        val triggerCount = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.triggers.forEach { trigger ->
                triggerCount[trigger] = (triggerCount[trigger] ?: 0) + 1
            }
        }
        
        val topTriggers = triggerCount.toList().sortedByDescending { it.second }.take(3)
        if (topTriggers.isNotEmpty()) {
            val triggersText = topTriggers.joinToString(", ") { it.first }
            recommendations.add("🎯 Частые триггеры: $triggersText. Попробуйте избегать их или уменьшить воздействие.")
        }
        
        // Рекомендации по симптомам
        val symptomFrequency = mutableMapOf<String, Int>()
        records.forEach { record ->
            record.symptoms.keys.forEach { symptom ->
                symptomFrequency[symptom] = (symptomFrequency[symptom] ?: 0) + 1
            }
        }
        
        val topSymptom = symptomFrequency.toList().maxByOrNull { it.second }
        if (topSymptom != null && topSymptom.second > records.size / 2) {
            val symptomType = try {
                MenopauseSymptomType.valueOf(topSymptom.first)
            } catch (e: Exception) {
                null
            }
            recommendations.add("Частый симптом: ${symptomType?.displayName ?: topSymptom.first}. Обсудите это с врачом.")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Вы хорошо отслеживаете свое здоровье! Продолжайте в том же духе.")
        }
        
        recommendations.forEach { recommendation ->
            val recText = TextView(this)
            recText.text = recommendation
            recText.textSize = 14f
            recText.setPadding(0, 8, 0, 8)
            recText.setTextColor(Color.parseColor("#333333"))
            layout.addView(recText)
        }
        
        card.addView(layout)
        statsContainer.addView(card)
    }
    
    private fun createCard(): CardView {
        val card = CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
        }
        card.radius = 12f
        card.setCardElevation(2f)
        card.setCardBackgroundColor(Color.WHITE)
        return card
    }
    
    private fun showEmptyState() {
        statsContainer.removeAllViews()
        
        val emptyCard = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 32, 32, 32)
        layout.gravity = android.view.Gravity.CENTER
        
        val emptyText = TextView(this)
        emptyText.text = "Пока нет данных для статистики.\n\nНачните отслеживать симптомы, чтобы увидеть детальную аналитику!"
        emptyText.textSize = 16f
        emptyText.gravity = android.view.Gravity.CENTER
        emptyText.setTextColor(Color.parseColor("#666666"))
        layout.addView(emptyText)
        
        emptyCard.addView(layout)
        statsContainer.addView(emptyCard)
    }
    
    private fun setupBottomNavigation() {
        val calendarButtonView = findViewById<View>(R.id.calendarButton)
        val notificationsButtonView = findViewById<View>(R.id.notificationsButton)
        val profileButtonView = findViewById<View>(R.id.profileButton)
        
        (calendarButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, MenopauseHomeActivity::class.java)
            startActivity(intent)
        }
        
        (notificationsButtonView as? ImageButton)?.setOnClickListener {
            // Уже на этом экране
        }
        
        (profileButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        loadStatistics()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// Вспомогательные классы для статистики
data class SymptomStats(
    val symptomType: String,
    var count: Int,
    var totalIntensity: Double,
    var maxIntensity: Int
)

data class WeekStats(
    val week: Int,
    val symptomCount: Int,
    val avgMood: Double,
    val avgEnergy: Double
)
