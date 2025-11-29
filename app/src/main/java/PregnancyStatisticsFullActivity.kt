package com.example.womenhealthtracker

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

class PregnancyStatisticsFullActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregnancy_statistics_full)
        
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
        val pregnancyData = userPreferences.getPregnancyData()
        
        if (pregnancyData.pregnancyStartDate.isEmpty()) {
            showEmptyState()
            return
        }
        
        // Заполняем статистику
        fillPregnancyInfo(pregnancyData)
        fillDoctorVisitsStatistics(pregnancyData)
    }
    
    private fun fillPregnancyInfo(pregnancyData: PregnancyData) {
        val container = findViewById<LinearLayout>(R.id.pregnancyInfoContainer)
        container?.removeAllViews()
        
        // Неделя беременности
        val currentWeek = pregnancyData.getCurrentWeek()
        val trimester = pregnancyData.getTrimester()
        val trimesterText = when (trimester) {
            1 -> "Первый триместр"
            2 -> "Второй триместр"
            3 -> "Третий триместр"
            else -> ""
        }
        
        addStatRow(container, "Текущая неделя", "$currentWeek недель")
        addStatRow(container, "Триместр", trimesterText)
        
        // Дата начала
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        try {
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(pregnancyData.pregnancyStartDate)
            if (startDate != null) {
                addStatRow(container, "Дата начала", dateFormat.format(startDate))
            }
        } catch (e: Exception) {
            addStatRow(container, "Дата начала", pregnancyData.pregnancyStartDate)
        }
        
        // Предполагаемая дата родов
        val dueDate = if (pregnancyData.estimatedDueDate.isNotEmpty()) {
            pregnancyData.estimatedDueDate
        } else {
            pregnancyData.calculateDueDate()
        }
        if (dueDate.isNotEmpty()) {
            try {
                val dueDateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dueDate)
                if (dueDateObj != null) {
                    addStatRow(container, "Предполагаемая дата родов", dateFormat.format(dueDateObj))
                }
            } catch (e: Exception) {
                addStatRow(container, "Предполагаемая дата родов", dueDate)
            }
        }
        
        // Дней до родов
        if (dueDate.isNotEmpty()) {
            try {
                val dueDateObj = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dueDate)
                if (dueDateObj != null) {
                    val currentDate = Calendar.getInstance()
                    val dueCalendar = Calendar.getInstance()
                    dueCalendar.time = dueDateObj
                    val daysDiff = ((dueCalendar.timeInMillis - currentDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    if (daysDiff > 0) {
                        addStatRow(container, "Дней до родов", "$daysDiff дней")
                    } else if (daysDiff == 0) {
                        addStatRow(container, "Дней до родов", "Сегодня!")
                    } else {
                        addStatRow(container, "Дней до родов", "Прошло ${-daysDiff} дней")
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
    }
    
    private fun fillDoctorVisitsStatistics(pregnancyData: PregnancyData) {
        val container = findViewById<LinearLayout>(R.id.doctorVisitsContainer)
        container?.removeAllViews()
        
        val visits = pregnancyData.visits
        if (visits.isEmpty()) {
            addStatRow(container, "Всего визитов", "0")
            return
        }
        
        addStatRow(container, "Всего визитов", visits.size.toString())
        
        // Статистика по типам визитов
        val visitTypes = visits.groupBy { it.type }
        
        // Список всех возможных типов визитов в порядке отображения
        val visitTypeOrder = listOf(
            "Плановый осмотр",
            "Консультация",
            "УЗИ",
            "Экстренный визит",
            "Анализы"
        )
        
        // Добавляем типы в заданном порядке
        visitTypeOrder.forEach { type ->
            val typeVisits = visitTypes[type]
            if (typeVisits != null && typeVisits.isNotEmpty()) {
                addStatRow(container, type, "${typeVisits.size} раз(а)")
            }
        }
        
        // Добавляем остальные типы, которых нет в списке
        visitTypes.forEach { (type, typeVisits) ->
            if (!visitTypeOrder.contains(type) && type.isNotEmpty()) {
                addStatRow(container, type, "${typeVisits.size} раз(а)")
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
        val container = findViewById<LinearLayout>(R.id.pregnancyInfoContainer)
        container?.removeAllViews()
        
        val emptyText = TextView(this)
        emptyText.text = "Данные о беременности не найдены"
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





