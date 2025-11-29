package com.example.womenhealthtracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import java.text.SimpleDateFormat
import java.util.*

class PregnancyStatisticsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregnancy_statistics)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        
        loadStatistics()
        setupBottomNavigation()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun loadStatistics() {
        val pregnancyData = userPreferences.getPregnancyData()
        
        if (pregnancyData.pregnancyStartDate.isEmpty()) {
            showEmptyState()
            return
        }
        
        // Заполняем карточки статистики
        updateStatisticsCards(pregnancyData)
    }
    
    private fun updateStatisticsCards(pregnancyData: PregnancyData) {
        // Показываем карточки статистики
        findViewById<androidx.cardview.widget.CardView>(R.id.weekCard)?.visibility = android.view.View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.visitsCard)?.visibility = android.view.View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.symptomsCard)?.visibility = android.view.View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.kicksCard)?.visibility = android.view.View.VISIBLE
        
        // Показываем детальные карточки
        findViewById<androidx.cardview.widget.CardView>(R.id.generalStatsCard)?.visibility = android.view.View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.pregnancyInfoCard)?.visibility = android.view.View.VISIBLE
        findViewById<androidx.cardview.widget.CardView>(R.id.doctorVisitsCard)?.visibility = android.view.View.VISIBLE
        
        // Заполняем общую статистику
        fillGeneralStatsCard(pregnancyData)
        
        // Неделя беременности
        val weekTextView = findViewById<TextView>(R.id.weekTextView)
        val currentWeek = pregnancyData.getCurrentWeek()
        weekTextView?.text = currentWeek.toString()
        
        // Визиты к врачу
        val visitsTextView = findViewById<TextView>(R.id.visitsTextView)
        val visitsCount = pregnancyData.visits.size
        visitsTextView?.text = visitsCount.toString()
        
        // Симптомы
        val symptomsStatTextView = findViewById<TextView>(R.id.symptomsStatTextView)
        val symptomsCount = pregnancyData.symptoms.size
        symptomsStatTextView?.text = symptomsCount.toString()
        
        // Шевеления
        val kicksTextView = findViewById<TextView>(R.id.kicksTextView)
        val kicksCount = pregnancyData.kicks.size
        kicksTextView?.text = kicksCount.toString()
        
        // Заполняем детальные карточки
        fillPregnancyInfoCard(pregnancyData)
        fillDoctorVisitsCard(pregnancyData)
        
        // Настраиваем кликабельность карточек
        setupCardClickListeners(pregnancyData)
    }
    
    private fun fillPregnancyInfoCard(pregnancyData: PregnancyData) {
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
        
        addDetailRow(container, "Текущая неделя", "$currentWeek недель")
        addDetailRow(container, "Триместр", trimesterText)
        
        // Дата начала
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        try {
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(pregnancyData.pregnancyStartDate)
            if (startDate != null) {
                addDetailRow(container, "Дата начала", dateFormat.format(startDate))
            }
        } catch (e: Exception) {
            addDetailRow(container, "Дата начала", pregnancyData.pregnancyStartDate)
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
                    addDetailRow(container, "Предполагаемая дата родов", dateFormat.format(dueDateObj))
                }
            } catch (e: Exception) {
                addDetailRow(container, "Предполагаемая дата родов", dueDate)
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
                        addDetailRow(container, "Дней до родов", "$daysDiff дней")
                    } else if (daysDiff == 0) {
                        addDetailRow(container, "Дней до родов", "Сегодня!")
                    } else {
                        addDetailRow(container, "Дней до родов", "Прошло ${-daysDiff} дней")
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
    }
    
    private fun fillDoctorVisitsCard(pregnancyData: PregnancyData) {
        val container = findViewById<LinearLayout>(R.id.doctorVisitsContainer)
        container?.removeAllViews()
        
        val visits = pregnancyData.visits
        if (visits.isEmpty()) {
            addDetailRow(container, "Всего визитов", "0")
            return
        }
        
        addDetailRow(container, "Всего визитов", visits.size.toString())
        
        // Статистика по типам визитов
        val visitTypes = visits.groupBy { it.type }
        visitTypes.forEach { (type, typeVisits) ->
            if (type.isNotEmpty()) {
                addDetailRow(container, type, "${typeVisits.size} раз(а)")
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
                    addDetailRow(container, "Последний визит", dateFormat.format(visitDate))
                }
            } catch (e: Exception) {
                addDetailRow(container, "Последний визит", lastVisit.date)
            }
        }
    }
    
    private fun addDetailRow(parent: LinearLayout, label: String, value: String) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.setPadding(0, 8, 0, 8)
        
        val labelView = TextView(this)
        labelView.text = label
        labelView.textSize = 14f
        labelView.setTextColor(android.graphics.Color.parseColor("#666666"))
        labelView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        row.addView(labelView)
        
        val valueView = TextView(this)
        valueView.text = value
        valueView.textSize = 14f
        valueView.setTextColor(android.graphics.Color.parseColor("#000000"))
        valueView.setTypeface(null, android.graphics.Typeface.BOLD)
        valueView.gravity = android.view.Gravity.END
        row.addView(valueView)
        
        parent.addView(row)
    }
    
    private fun setupCardClickListeners(pregnancyData: PregnancyData) {
        // Карточка недели - открывает детальную информацию (можно оставить как есть или добавить детальный экран)
        val weekCard = findViewById<androidx.cardview.widget.CardView>(R.id.weekCard)
        weekCard?.setOnClickListener {
            // Можно добавить переход на детальный экран или оставить пустым
        }
        
        // Карточка визитов - открывает экран визитов к врачу
        val visitsCard = findViewById<androidx.cardview.widget.CardView>(R.id.visitsCard)
        visitsCard?.setOnClickListener {
            val intent = Intent(this, DoctorVisitActivity::class.java)
            startActivity(intent)
        }
        
        // Карточка симптомов - открывает экран симптомов беременности
        val symptomsCard = findViewById<androidx.cardview.widget.CardView>(R.id.symptomsCard)
        symptomsCard?.setOnClickListener {
            val intent = Intent(this, PregnancySymptomsActivity::class.java)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            intent.putExtra("date", dateFormat.format(Date()))
            startActivity(intent)
        }
        
        // Карточка шевелений - открывает счетчик шевелений
        val kicksCard = findViewById<androidx.cardview.widget.CardView>(R.id.kicksCard)
        kicksCard?.setOnClickListener {
            val intent = Intent(this, KickCounterActivity::class.java)
            startActivity(intent)
        }
        
        // Детальная карточка "Информация о беременности" - кликабельна
        val pregnancyInfoCard = findViewById<androidx.cardview.widget.CardView>(R.id.pregnancyInfoCard)
        pregnancyInfoCard?.setOnClickListener {
            // Можно добавить переход на детальный экран или оставить пустым
        }
        
        // Детальная карточка "Визиты к врачу" - открывает экран визитов
        val doctorVisitsCard = findViewById<androidx.cardview.widget.CardView>(R.id.doctorVisitsCard)
        doctorVisitsCard?.setOnClickListener {
            val intent = Intent(this, DoctorVisitActivity::class.java)
            startActivity(intent)
        }
        
        // Карточка "Общая статистика" - открывает диалог со статистикой
        val generalStatsCard = findViewById<androidx.cardview.widget.CardView>(R.id.generalStatsCard)
        generalStatsCard?.setOnClickListener {
            showGeneralStatisticsDialog(pregnancyData)
        }
    }
    
    private fun showEmptyState() {
        // Скрываем карточки статистики
        findViewById<androidx.cardview.widget.CardView>(R.id.weekCard)?.visibility = android.view.View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.visitsCard)?.visibility = android.view.View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.symptomsCard)?.visibility = android.view.View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.kicksCard)?.visibility = android.view.View.GONE
        
        // Скрываем детальные карточки
        findViewById<androidx.cardview.widget.CardView>(R.id.generalStatsCard)?.visibility = android.view.View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.pregnancyInfoCard)?.visibility = android.view.View.GONE
        findViewById<androidx.cardview.widget.CardView>(R.id.doctorVisitsCard)?.visibility = android.view.View.GONE
    }
    
    private fun fillGeneralStatsCard(pregnancyData: PregnancyData) {
        val container = findViewById<LinearLayout>(R.id.generalStatsContainer)
        container?.removeAllViews()
        
        // Подсчитываем общую статистику
        val totalDays = calculateTotalDays(pregnancyData)
        val totalSymptoms = pregnancyData.symptoms.size
        val totalVisits = pregnancyData.visits.size
        val totalKicks = pregnancyData.kicks.size
        
        // Дней отслеживания
        addDetailRow(container, "Дней отслеживания", totalDays.toString())
        
        // Всего записей симптомов
        addDetailRow(container, "Всего записей симптомов", totalSymptoms.toString())
        
        // Всего визитов к врачу
        addDetailRow(container, "Всего визитов к врачу", totalVisits.toString())
        
        // Сессий подсчета шевелений
        addDetailRow(container, "Сессий подсчета шевелений", totalKicks.toString())
    }
    
    private fun calculateTotalDays(pregnancyData: PregnancyData): Int {
        if (pregnancyData.pregnancyStartDate.isEmpty()) {
            return 0
        }
        
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = dateFormat.parse(pregnancyData.pregnancyStartDate) ?: return 0
            val currentDate = Calendar.getInstance()
            val startCalendar = Calendar.getInstance()
            startCalendar.time = startDate
            
            val daysDiff = ((currentDate.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            return if (daysDiff >= 0) daysDiff + 1 else 1
        } catch (e: Exception) {
            return 0
        }
    }
    
    private fun setupBottomNavigation() {
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        val notificationsButton = findViewById<ImageButton>(R.id.notificationsButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        
        settingsButton?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        calendarButton?.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        
        notificationsButton?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        profileButton?.setOnClickListener {
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
    
    private fun showGeneralStatisticsDialog(pregnancyData: PregnancyData) {
        // Создаем диалог
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(createGeneralStatisticsView(pregnancyData))
            .setPositiveButton("Закрыть", null)
            .create()
        
        dialog.show()
    }
    
    private fun createGeneralStatisticsView(pregnancyData: PregnancyData): android.view.View {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(0, 0, 0, 0)
        layout.setBackgroundColor(android.graphics.Color.parseColor("#FFF0F5"))
        
        // Главный заголовок "Статистика беременности"
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.VERTICAL
        headerLayout.setPadding(24, 24, 24, 16)
        headerLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        val mainTitle = TextView(this)
        mainTitle.text = "Статистика беременности"
        mainTitle.textSize = 22f
        mainTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        mainTitle.setTextColor(android.graphics.Color.parseColor("#000000"))
        mainTitle.setPadding(0, 0, 0, 8)
        headerLayout.addView(mainTitle)
        
        val subtitle = TextView(this)
        subtitle.text = "Отслеживайте прогресс и заботьтесь о себе и малыше"
        subtitle.textSize = 14f
        subtitle.setTextColor(android.graphics.Color.parseColor("#666666"))
        subtitle.setPadding(0, 0, 0, 16)
        headerLayout.addView(subtitle)
        
        // Розовая полоска
        val pinkLine = View(this)
        pinkLine.setBackgroundColor(android.graphics.Color.parseColor("#FFB6C1"))
        val pinkLineParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        pinkLine.layoutParams = pinkLineParams
        headerLayout.addView(pinkLine)
        
        layout.addView(headerLayout)
        
        // Секция "Информация о беременности"
        val pregnancyInfoLayout = LinearLayout(this)
        pregnancyInfoLayout.orientation = LinearLayout.VERTICAL
        pregnancyInfoLayout.setPadding(24, 24, 24, 24)
        pregnancyInfoLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        val pregnancyInfoTitle = TextView(this)
        pregnancyInfoTitle.text = "Информация о беременности"
        pregnancyInfoTitle.textSize = 20f
        pregnancyInfoTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        pregnancyInfoTitle.setTextColor(android.graphics.Color.parseColor("#000000"))
        pregnancyInfoTitle.setPadding(0, 0, 0, 16)
        pregnancyInfoLayout.addView(pregnancyInfoTitle)
        
        // Розовая полоска под заголовком
        val pinkLine2 = View(this)
        pinkLine2.setBackgroundColor(android.graphics.Color.parseColor("#FFB6C1"))
        val pinkLineParams2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        pinkLineParams2.bottomMargin = 16
        pinkLine2.layoutParams = pinkLineParams2
        pregnancyInfoLayout.addView(pinkLine2)
        
        // Заполняем информацию о беременности
        val currentWeek = pregnancyData.getCurrentWeek()
        val trimester = pregnancyData.getTrimester()
        val trimesterText = when (trimester) {
            1 -> "Первый триместр"
            2 -> "Второй триместр"
            3 -> "Третий триместр"
            else -> ""
        }
        
        addDialogStatRow(pregnancyInfoLayout, "Текущая неделя", "$currentWeek недель")
        addDialogStatRow(pregnancyInfoLayout, "Триместр", trimesterText)
        
        // Дата начала
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        try {
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(pregnancyData.pregnancyStartDate)
            if (startDate != null) {
                addDialogStatRow(pregnancyInfoLayout, "Дата начала", dateFormat.format(startDate))
            }
        } catch (e: Exception) {
            addDialogStatRow(pregnancyInfoLayout, "Дата начала", pregnancyData.pregnancyStartDate)
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
                    addDialogStatRow(pregnancyInfoLayout, "Предполагаемая дата родов", dateFormat.format(dueDateObj))
                }
            } catch (e: Exception) {
                addDialogStatRow(pregnancyInfoLayout, "Предполагаемая дата родов", dueDate)
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
                        addDialogStatRow(pregnancyInfoLayout, "Дней до родов", "$daysDiff дней")
                    } else if (daysDiff == 0) {
                        addDialogStatRow(pregnancyInfoLayout, "Дней до родов", "Сегодня!")
                    } else {
                        addDialogStatRow(pregnancyInfoLayout, "Дней до родов", "Прошло ${-daysDiff} дней")
                    }
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
        
        layout.addView(pregnancyInfoLayout)
        
        // Разделитель (розовая линия)
        val divider = View(this)
        divider.setBackgroundColor(android.graphics.Color.parseColor("#FFB6C1"))
        val dividerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        divider.layoutParams = dividerParams
        layout.addView(divider)
        
        // Секция "Визиты к врачу"
        val visitsLayout = LinearLayout(this)
        visitsLayout.orientation = LinearLayout.VERTICAL
        visitsLayout.setPadding(24, 24, 24, 24)
        visitsLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        val visitsTitle = TextView(this)
        visitsTitle.text = "Визиты к врачу"
        visitsTitle.textSize = 20f
        visitsTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        visitsTitle.setTextColor(android.graphics.Color.parseColor("#000000"))
        visitsTitle.setPadding(0, 0, 0, 16)
        visitsLayout.addView(visitsTitle)
        
        // Розовая полоска под заголовком
        val pinkLine3 = View(this)
        pinkLine3.setBackgroundColor(android.graphics.Color.parseColor("#FFB6C1"))
        val pinkLineParams3 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        pinkLineParams3.bottomMargin = 16
        pinkLine3.layoutParams = pinkLineParams3
        visitsLayout.addView(pinkLine3)
        
        val visits = pregnancyData.visits
        if (visits.isEmpty()) {
            addDialogStatRow(visitsLayout, "Всего визитов", "0")
        } else {
            addDialogStatRow(visitsLayout, "Всего визитов", visits.size.toString())
            
            // Статистика по типам визитов
            val visitTypes = visits.groupBy { it.type }
            visitTypes.forEach { (type, typeVisits) ->
                if (type.isNotEmpty()) {
                    addDialogStatRow(visitsLayout, type, "${typeVisits.size} раз(а)")
                }
            }
            
            // Последний визит
            val sortedVisits = visits.sortedByDescending { it.date }
            if (sortedVisits.isNotEmpty()) {
                val lastVisit = sortedVisits.first()
                try {
                    val visitDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastVisit.date)
                    if (visitDate != null) {
                        addDialogStatRow(visitsLayout, "Последний визит", dateFormat.format(visitDate))
                    }
                } catch (e: Exception) {
                    addDialogStatRow(visitsLayout, "Последний визит", lastVisit.date)
                }
            }
        }
        
        layout.addView(visitsLayout)
        
        scrollView.addView(layout)
        return scrollView
    }
    
    private fun addDialogStatRow(parent: LinearLayout, label: String, value: String) {
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
}

