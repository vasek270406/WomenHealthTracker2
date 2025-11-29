package com.example.womenhealthtracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

data class MenopauseDoctorVisit(
    val id: String = UUID.randomUUID().toString(),
    val specialistType: String, // Тип специалиста
    val doctorName: String,
    val date: String, // yyyy-MM-dd
    val time: String = "", // HH:mm
    val notes: String = "",
    val reminderDayBefore: Boolean = true, // Напоминание за день
    val reminderOnDay: Boolean = true // Напоминание в день приема
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "specialistType" to specialistType,
            "doctorName" to doctorName,
            "date" to date,
            "time" to time,
            "notes" to notes,
            "reminderDayBefore" to reminderDayBefore,
            "reminderOnDay" to reminderOnDay
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): MenopauseDoctorVisit {
            return MenopauseDoctorVisit(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                specialistType = map["specialistType"] as? String ?: "",
                doctorName = map["doctorName"] as? String ?: "",
                date = map["date"] as? String ?: "",
                time = map["time"] as? String ?: "",
                notes = map["notes"] as? String ?: "",
                reminderDayBefore = map["reminderDayBefore"] as? Boolean ?: true,
                reminderOnDay = map["reminderOnDay"] as? Boolean ?: true
            )
        }
    }
}

class MenopauseDoctorVisitBookingActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestoreHelper: FirestoreHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private var visitDate: Calendar = Calendar.getInstance()
    private var visitTime: Calendar = Calendar.getInstance()
    private var selectedSpecialistType: String = ""
    
    private lateinit var specialistSpinner: Spinner
    private lateinit var doctorNameEditText: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var notesEditText: EditText
    private lateinit var reminderDayBeforeSwitch: Switch
    private lateinit var reminderOnDaySwitch: Switch
    private lateinit var statisticsButton: Button
    private lateinit var saveButton: Button
    
    private val specialists = listOf(
        "Гинеколог",
        "Маммолог",
        "Эндокринолог",
        "Кардиолог",
        "Остеопат",
        "Дерматолог"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_doctor_visit_booking)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        firestoreHelper = FirestoreHelper(this)
        
        // Получаем выбранного специалиста из Intent (если есть)
        val intentSpecialist = intent.getStringExtra("selectedSpecialist")
        if (intentSpecialist != null && intentSpecialist in specialists) {
            selectedSpecialistType = intentSpecialist
        }
        
        initViews()
        setupSpecialistSpinner()
        setupDatePickers()
        setupStatisticsButton()
        setupSaveButton()
    }
    
    private fun initViews() {
        specialistSpinner = findViewById(R.id.specialistSpinner)
        doctorNameEditText = findViewById(R.id.doctorNameEditText)
        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        notesEditText = findViewById(R.id.notesEditText)
        reminderDayBeforeSwitch = findViewById(R.id.reminderDayBeforeSwitch)
        reminderOnDaySwitch = findViewById(R.id.reminderOnDaySwitch)
        statisticsButton = findViewById(R.id.statisticsButton)
        saveButton = findViewById(R.id.saveButton)
        
        // Установка текущей даты и времени
        updateDateButton()
        updateTimeButton()
    }
    
    private fun setupSpecialistSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialists)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        specialistSpinner.adapter = adapter
        
        specialistSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedSpecialistType = specialists[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Устанавливаем выбранного специалиста, если он был передан
        if (selectedSpecialistType.isEmpty()) {
            selectedSpecialistType = specialists[0]
        } else {
            val index = specialists.indexOf(selectedSpecialistType)
            if (index >= 0) {
                specialistSpinner.setSelection(index)
            }
        }
    }
    
    private fun setupDatePickers() {
        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    visitDate.set(year, month, dayOfMonth)
                    updateDateButton()
                },
                visitDate.get(Calendar.YEAR),
                visitDate.get(Calendar.MONTH),
                visitDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        
        timeButton.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    visitTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    visitTime.set(Calendar.MINUTE, minute)
                    updateTimeButton()
                },
                visitTime.get(Calendar.HOUR_OF_DAY),
                visitTime.get(Calendar.MINUTE),
                true
            ).show()
        }
    }
    
    private fun updateDateButton() {
        val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        dateButton.text = displayFormat.format(visitDate.time)
    }
    
    private fun updateTimeButton() {
        timeButton.text = timeFormat.format(visitTime.time)
    }
    
    private fun setupStatisticsButton() {
        statisticsButton.setOnClickListener {
            showStatisticsDialog()
        }
    }
    
    private fun showStatisticsDialog() {
        val records = userPreferences.getAllMenopauseDates()
            .mapNotNull { date -> userPreferences.getMenopauseDayRecord(date) }
        
        val visits = userPreferences.getMenopauseDoctorVisits()
        
        // Подсчет статистики
        val daysTracked = records.size
        val totalSymptomEntries = records.sumOf { it.symptoms.size }
        val totalVisits = visits.size
        
        // Статистика по типам специалистов
        val visitsBySpecialist = visits.groupBy { it.specialistType }
        
        // Последний визит
        val lastVisit = visits.sortedByDescending { it.date }.firstOrNull()
        
        // Создаем диалог
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(createStatisticsView(daysTracked, totalSymptomEntries, totalVisits, visitsBySpecialist, lastVisit))
            .setPositiveButton("Закрыть", null)
            .create()
        
        dialog.show()
    }
    
    private fun createStatisticsView(
        daysTracked: Int,
        totalSymptomEntries: Int,
        totalVisits: Int,
        visitsBySpecialist: Map<String, List<MenopauseDoctorVisit>>,
        lastVisit: MenopauseDoctorVisit?
    ): android.view.View {
        val scrollView = ScrollView(this)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(0, 0, 0, 0)
        layout.setBackgroundColor(android.graphics.Color.parseColor("#FFF0F5"))
        
        // Главный заголовок "Статистика менопаузы"
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.VERTICAL
        headerLayout.setPadding(24, 24, 24, 16)
        headerLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        val mainTitle = TextView(this)
        mainTitle.text = "Статистика менопаузы"
        mainTitle.textSize = 22f
        mainTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        mainTitle.setTextColor(android.graphics.Color.parseColor("#000000"))
        mainTitle.setPadding(0, 0, 0, 8)
        headerLayout.addView(mainTitle)
        
        val subtitle = TextView(this)
        subtitle.text = "Отслеживайте прогресс и заботьтесь о своем здоровье"
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
        
        // Секция "Общая статистика"
        val generalStatsLayout = LinearLayout(this)
        generalStatsLayout.orientation = LinearLayout.VERTICAL
        generalStatsLayout.setPadding(24, 24, 24, 24)
        generalStatsLayout.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
        
        val generalStatsTitle = TextView(this)
        generalStatsTitle.text = "Общая статистика"
        generalStatsTitle.textSize = 20f
        generalStatsTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        generalStatsTitle.setTextColor(android.graphics.Color.parseColor("#000000"))
        generalStatsTitle.setPadding(0, 0, 0, 16)
        generalStatsLayout.addView(generalStatsTitle)
        
        // Розовая полоска под заголовком
        val pinkLine2 = View(this)
        pinkLine2.setBackgroundColor(android.graphics.Color.parseColor("#FFB6C1"))
        val pinkLineParams2 = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            2
        )
        pinkLineParams2.bottomMargin = 16
        pinkLine2.layoutParams = pinkLineParams2
        generalStatsLayout.addView(pinkLine2)
        
        // Дней отслеживания
        addStatRow(generalStatsLayout, "Дней отслеживания", daysTracked.toString())
        
        // Всего записей симптомов
        addStatRow(generalStatsLayout, "Всего записей симптомов", totalSymptomEntries.toString())
        
        // Всего визитов к врачу
        addStatRow(generalStatsLayout, "Всего визитов к врачу", totalVisits.toString())
        
        layout.addView(generalStatsLayout)
        
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
        
        // Всего визитов
        addStatRow(visitsLayout, "Всего визитов", totalVisits.toString())
        
        // Статистика по типам специалистов
        visitsBySpecialist.forEach { (specialist, specialistVisits) ->
            addStatRow(visitsLayout, specialist, "${specialistVisits.size} раз(а)")
        }
        
        // Последний визит
        if (lastVisit != null) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                val visitDate = dateFormat.parse(lastVisit.date)
                if (visitDate != null) {
                    addStatRow(visitsLayout, "Последний визит", displayFormat.format(visitDate))
                } else {
                    addStatRow(visitsLayout, "Последний визит", lastVisit.date)
                }
            } catch (e: Exception) {
                addStatRow(visitsLayout, "Последний визит", lastVisit.date)
            }
        }
        
        layout.addView(visitsLayout)
        
        scrollView.addView(layout)
        return scrollView
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
    
    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveVisit()
        }
    }
    
    private fun saveVisit() {
        val doctorName = doctorNameEditText.text.toString().trim()
        
        if (doctorName.isEmpty()) {
            Toast.makeText(this, "Введите ФИО врача", Toast.LENGTH_SHORT).show()
            return
        }
        
        val visit = MenopauseDoctorVisit(
            specialistType = selectedSpecialistType,
            doctorName = doctorName,
            date = dateFormat.format(visitDate.time),
            time = timeFormat.format(visitTime.time),
            notes = notesEditText.text.toString().trim(),
            reminderDayBefore = reminderDayBeforeSwitch.isChecked,
            reminderOnDay = reminderOnDaySwitch.isChecked
        )
        
        // Сохранение визита локально
        val visits = userPreferences.getMenopauseDoctorVisits().toMutableList()
        android.util.Log.d("MenopauseVisit", "Текущее количество визитов: ${visits.size}")
        visits.add(visit)
        android.util.Log.d("MenopauseVisit", "Добавлен новый визит: ${visit.doctorName}, ${visit.date}, ${visit.time}")
        android.util.Log.d("MenopauseVisit", "Общее количество визитов после добавления: ${visits.size}")
        userPreferences.saveMenopauseDoctorVisits(visits)
        
        // Проверка сохранения
        val savedVisits = userPreferences.getMenopauseDoctorVisits()
        android.util.Log.d("MenopauseVisit", "Проверка: сохранено визитов: ${savedVisits.size}")
        
        // Создание уведомлений
        val notificationHelper = NotificationHelper(this)
        notificationHelper.scheduleDoctorVisitReminders(visit)
        
        // Синхронизация с Firestore (если есть userId)
        val userId = userPreferences.getUserId()
        if (userId.isNotEmpty()) {
            firestoreHelper.saveMenopauseDoctorVisits(
                userId,
                visits,
                onSuccess = {
                    Toast.makeText(this, "Визит к врачу сохранен. Напоминания установлены.", Toast.LENGTH_LONG).show()
                    finish()
                },
                onError = { error ->
                    // Данные уже сохранены локально
                    Toast.makeText(this, "Визит сохранен локально. Напоминания установлены.", Toast.LENGTH_LONG).show()
                    finish()
                }
            )
        } else {
            Toast.makeText(this, "Визит к врачу сохранен. Напоминания установлены.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

