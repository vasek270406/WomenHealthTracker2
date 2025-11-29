package com.example.womenhealthtracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class DoctorVisitActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestoreHelper: FirestoreHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    private var visitDate: Calendar = Calendar.getInstance()
    private var visitTime: Calendar = Calendar.getInstance()
    private var nextVisitDate: Calendar? = null
    private var existingVisit: DoctorVisit? = null
    
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var typeSpinner: Spinner
    private lateinit var doctorNameEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var nextVisitButton: Button
    private lateinit var saveButton: Button
    
    private val visitTypes = arrayOf(
        "Консультация",
        "УЗИ",
        "Анализы",
        "Скрининг",
        "Плановый осмотр",
        "Экстренный визит"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_visit)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        firestoreHelper = FirestoreHelper(this)
        
        // Получаем существующий визит для редактирования (если есть)
        val visitId = intent.getStringExtra("visitId")
        if (visitId != null) {
            loadVisit(visitId)
        }
        
        setupViews()
        setupDatePickers()
        setupSaveButton()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun setupViews() {
        dateButton = findViewById(R.id.dateButton)
        timeButton = findViewById(R.id.timeButton)
        typeSpinner = findViewById(R.id.typeSpinner)
        doctorNameEditText = findViewById(R.id.doctorNameEditText)
        notesEditText = findViewById(R.id.notesEditText)
        nextVisitButton = findViewById(R.id.nextVisitButton)
        saveButton = findViewById(R.id.saveButton)
        
        // Настройка спиннера типов визита
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, visitTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = adapter
        
        // Установка текущей даты и времени
        updateDateButton()
        updateTimeButton()
        
        // Загрузка данных существующего визита
        existingVisit?.let { visit ->
            try {
                val visitCalendar = Calendar.getInstance()
                visitCalendar.time = dateFormat.parse(visit.date) ?: Date()
                visitDate = visitCalendar
                updateDateButton()
                
                // Загрузка времени визита
                if (visit.time.isNotEmpty()) {
                    try {
                        val timeParsed = timeFormat.parse(visit.time)
                        if (timeParsed != null) {
                            val timeCalendar = Calendar.getInstance()
                            timeCalendar.time = timeParsed
                            visitTime.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                            visitTime.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                        }
                    } catch (e: Exception) {
                        // Используем текущее время по умолчанию
                    }
                }
                updateTimeButton()
                
                val typeIndex = visitTypes.indexOf(visit.type)
                if (typeIndex >= 0) {
                    typeSpinner.setSelection(typeIndex)
                }
                
                doctorNameEditText.setText(visit.doctorName)
                notesEditText.setText(visit.notes)
                
                if (visit.photos.isNotEmpty()) {
                    // Показать фото, если есть
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
    }
    
    private fun setupDatePickers() {
        dateButton.setOnClickListener {
            showDatePicker(visitDate) { calendar ->
                visitDate = calendar
                updateDateButton()
            }
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
        
        nextVisitButton.setOnClickListener {
            val initialDate = nextVisitDate ?: Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
            }
            showDatePicker(initialDate) { calendar ->
                nextVisitDate = calendar
                updateNextVisitButton()
            }
        }
    }
    
    private fun showDatePicker(initialDate: Calendar, onDateSelected: (Calendar) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val calendar = Calendar.getInstance()
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar)
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun updateDateButton() {
        val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        dateButton.text = displayFormat.format(visitDate.time)
    }
    
    private fun updateTimeButton() {
        timeButton.text = timeFormat.format(visitTime.time)
    }
    
    private fun updateNextVisitButton() {
        nextVisitButton.text = if (nextVisitDate != null) {
            val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            displayFormat.format(nextVisitDate!!.time)
        } else {
            "Не назначен"
        }
    }
    
    private fun loadVisit(visitId: String) {
        // Загрузка визита из локального хранилища
        val pregnancyData = userPreferences.getPregnancyData()
        existingVisit = pregnancyData.visits.firstOrNull { 
            it.date == visitId // Используем date как идентификатор
        }
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
        
        val visit = DoctorVisit(
            date = dateFormat.format(visitDate.time),
            time = timeFormat.format(visitTime.time),
            type = visitTypes[typeSpinner.selectedItemPosition],
            doctorName = doctorName,
            notes = notesEditText.text.toString().trim(),
            photos = emptyList(), // TODO: добавить загрузку фото
            // nextVisitDate будет сохранен отдельно
        )
        
        // Сохранение в PregnancyData
        val pregnancyData = userPreferences.getPregnancyData()
        android.util.Log.d("DoctorVisit", "Текущее количество визитов: ${pregnancyData.visits.size}")
        
        val updatedVisits = if (existingVisit != null) {
            // Обновление существующего визита
            android.util.Log.d("DoctorVisit", "Обновление существующего визита: ${existingVisit!!.date}")
            pregnancyData.visits.map { if (it.date == existingVisit!!.date) visit else it }
        } else {
            // Добавление нового визита
            android.util.Log.d("DoctorVisit", "Добавление нового визита: ${visit.date}, ${visit.doctorName}, ${visit.time}")
            pregnancyData.visits + visit
        }
        
        android.util.Log.d("DoctorVisit", "Общее количество визитов после изменения: ${updatedVisits.size}")
        
        val updatedPregnancyData = pregnancyData.copy(visits = updatedVisits)
        // Сохранение в локальное хранилище
        userPreferences.savePregnancyData(updatedPregnancyData)
        
        // Проверка сохранения
        val savedData = userPreferences.getPregnancyData()
        android.util.Log.d("DoctorVisit", "Проверка: сохранено визитов: ${savedData.visits.size}")
        
        // Сохранение в Firestore (если есть userId)
        val userId = userPreferences.getUserId()
        if (userId.isNotEmpty()) {
            firestoreHelper.savePregnancyData(
                userId,
                updatedPregnancyData,
                onSuccess = {
                    Toast.makeText(this, "Визит сохранен", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    // Сохраняем локально даже при ошибке Firestore
                    Toast.makeText(this, "Визит сохранен локально", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        } else {
            Toast.makeText(this, "Визит сохранен", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

