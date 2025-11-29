package com.example.womenhealthtracker

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

class MenopauseStatisticsFullActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_statistics_full)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        
        loadStatistics()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun loadStatistics() {
        val records = userPreferences.getAllMenopauseDates()
            .mapNotNull { date -> userPreferences.getMenopauseDayRecord(date) }
        
        val visits = userPreferences.getMenopauseDoctorVisits()
        
        if (records.isEmpty() && visits.isEmpty()) {
            showEmptyState()
            return
        }
        
        // Заполняем статистику
        fillGeneralInfo(records, visits)
        fillDoctorVisitsStatistics(visits)
    }
    
    private fun fillGeneralInfo(records: List<MenopauseDayRecord>, visits: List<MenopauseDoctorVisit>) {
        val container = findViewById<LinearLayout>(R.id.generalInfoContainer)
        container?.removeAllViews()
        
        // Дней отслеживания
        val daysTracked = records.size
        addStatRow(container, "Дней отслеживания", daysTracked.toString())
        
        // Всего записей симптомов
        val totalSymptomEntries = records.sumOf { it.symptoms.size }
        addStatRow(container, "Всего записей симптомов", totalSymptomEntries.toString())
        
        // Всего визитов к врачу
        val totalVisits = visits.size
        addStatRow(container, "Всего визитов к врачу", totalVisits.toString())
        
        // Среднее настроение (если есть данные)
        if (records.isNotEmpty()) {
            val avgMood = records.map { it.mood }.average()
            addStatRow(container, "Среднее настроение", String.format("%.1f/5", avgMood))
        }
        
        // Средняя энергия (если есть данные)
        if (records.isNotEmpty()) {
            val avgEnergy = records.map { it.energy }.average()
            addStatRow(container, "Средняя энергия", String.format("%.1f/5", avgEnergy))
        }
        
        // Дней с симптомами
        if (records.isNotEmpty()) {
            val daysWithSymptoms = records.count { it.symptoms.isNotEmpty() }
            val percentage = if (daysTracked > 0) (daysWithSymptoms * 100 / daysTracked) else 0
            addStatRow(container, "Дней с симптомами", "$daysWithSymptoms ($percentage%)")
        }
    }
    
    private fun fillDoctorVisitsStatistics(visits: List<MenopauseDoctorVisit>) {
        val container = findViewById<LinearLayout>(R.id.doctorVisitsContainer)
        container?.removeAllViews()
        
        if (visits.isEmpty()) {
            addStatRow(container, "Всего визитов", "0")
            return
        }
        
        addStatRow(container, "Всего визитов", visits.size.toString())
        
        // Статистика по типам специалистов
        val visitsBySpecialist = visits.groupBy { it.specialistType }
        
        // Добавляем статистику по каждому типу специалиста
        visitsBySpecialist.toList().sortedByDescending { it.second.size }.forEach { (specialist, specialistVisits) ->
            if (specialist.isNotEmpty()) {
                addStatRow(container, specialist, "${specialistVisits.size} раз(а)")
            }
        }
        
        // Последний визит
        val sortedVisits = visits.sortedByDescending { it.date }
        if (sortedVisits.isNotEmpty()) {
            val lastVisit = sortedVisits.first()
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            try {
                val visitDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastVisit.date)
                if (visitDate != null) {
                    addStatRow(container, "Последний визит", dateFormat.format(visitDate))
                }
            } catch (e: Exception) {
                addStatRow(container, "Последний визит", lastVisit.date)
            }
        }
    }
    
    private fun addStatRow(parent: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 8, 0, 8)
        
        val labelView = TextView(this)
        labelView.text = label
        labelView.textSize = 16f
        labelView.setTextColor(android.graphics.Color.parseColor("#666666"))
        labelView.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        
        val valueView = TextView(this)
        valueView.text = value
        valueView.textSize = 16f
        valueView.setTextColor(android.graphics.Color.parseColor("#000000"))
        valueView.setTypeface(null, android.graphics.Typeface.BOLD)
        valueView.gravity = android.view.Gravity.END
        
        row.addView(labelView)
        row.addView(valueView)
        parent.addView(row)
    }
    
    private fun showEmptyState() {
        val container = findViewById<LinearLayout>(R.id.generalInfoContainer)
        container?.removeAllViews()
        
        val emptyText = TextView(this)
        emptyText.text = "Данные о менопаузе не найдены"
        emptyText.textSize = 16f
        emptyText.setTextColor(android.graphics.Color.parseColor("#666666"))
        emptyText.gravity = android.view.Gravity.CENTER
        emptyText.setPadding(0, 32, 0, 32)
        container?.addView(emptyText)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}





