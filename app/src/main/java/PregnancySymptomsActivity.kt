package com.example.womenhealthtracker

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class PregnancySymptomsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    
    private val selectedSymptoms = mutableMapOf<String, Int>() // symptom name -> intensity
    private val symptomNotes = mutableMapOf<String, String>() // symptom name -> notes
    
    private val pregnancySymptoms = listOf(
        "Токсикоз",
        "Изжога",
        "Отеки",
        "Боль в спине",
        "Усталость",
        "Тяга к еде",
        "Отвращение к еде",
        "Частое мочеиспускание",
        "Болезненность груди",
        "Шевеления",
        "Схватки",
        "Одышка"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregnancy_symptoms)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        // Получаем дату из интента или используем сегодняшнюю
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        setupDateDisplay()
        setupSymptoms()
        setupSaveButton()
    }
    
    private fun setupDateDisplay() {
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        try {
            val calendar = Calendar.getInstance()
            calendar.time = dateFormat.parse(selectedDate) ?: Date()
            val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            dateTextView?.text = displayFormat.format(calendar.time)
        } catch (e: Exception) {
            dateTextView?.text = selectedDate
        }
    }
    
    private fun setupSymptoms() {
        val symptomsContainer = findViewById<LinearLayout>(R.id.symptomsContainer)
        symptomsContainer?.removeAllViews()
        
        // Загружаем сохраненные данные
        val dayData = userPreferences.getDayData(selectedDate)
        if (dayData != null) {
            for (symptom in dayData.symptoms) {
                // Извлекаем название без эмодзи
                val symptomName = symptom.name.split(" ").drop(1).joinToString(" ")
                val fullName = pregnancySymptoms.find { it.contains(symptomName) }
                if (fullName != null) {
                    selectedSymptoms[fullName] = symptom.intensity
                    symptomNotes[fullName] = symptom.notes ?: ""
                }
            }
        }
        
        pregnancySymptoms.forEach { symptomName ->
            val symptomCard = createSymptomCard(symptomName)
            symptomsContainer?.addView(symptomCard)
        }
    }
    
    private fun createSymptomCard(symptomName: String): androidx.cardview.widget.CardView {
        val card = androidx.cardview.widget.CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }
        card.radius = 12f
        card.cardElevation = 2f
        card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.white))
        
        val cardContent = LinearLayout(this)
        cardContent.orientation = LinearLayout.VERTICAL
        cardContent.setPadding(16, 16, 16, 16)
        
        // Заголовок с переключателем
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.HORIZONTAL
        headerLayout.gravity = android.view.Gravity.CENTER_VERTICAL
        
        val nameText = TextView(this)
        nameText.text = symptomName
        nameText.textSize = 16f
        nameText.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.black))
        nameText.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        
        val switch = Switch(this)
        switch.isChecked = selectedSymptoms.containsKey(symptomName)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSymptoms[symptomName] = 1
            } else {
                selectedSymptoms.remove(symptomName)
                symptomNotes.remove(symptomName)
            }
            updateSymptomCard(card, symptomName, isChecked)
        }
        
        headerLayout.addView(nameText)
        headerLayout.addView(switch)
        
        cardContent.addView(headerLayout)
        
        // Контейнер для интенсивности и заметок
        val detailsContainer = LinearLayout(this)
        detailsContainer.orientation = LinearLayout.VERTICAL
        detailsContainer.id = View.generateViewId()
        detailsContainer.visibility = if (switch.isChecked) android.view.View.VISIBLE else android.view.View.GONE
        detailsContainer.setPadding(0, 16, 0, 0)
        
        // Интенсивность
        val intensityLabel = TextView(this)
        intensityLabel.text = "Интенсивность: ${selectedSymptoms[symptomName] ?: 1}/5"
        intensityLabel.textSize = 14f
        intensityLabel.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.darker_gray))
        intensityLabel.setPadding(0, 0, 0, 8)
        
        val seekBar = SeekBar(this)
        seekBar.max = 4 // 1-5 (0-4 в SeekBar)
        seekBar.progress = (selectedSymptoms[symptomName] ?: 1) - 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val intensity = progress + 1
                intensityLabel.text = "Интенсивность: $intensity/5"
                if (switch.isChecked) {
                    selectedSymptoms[symptomName] = intensity
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Заметки
        val notesLabel = TextView(this)
        notesLabel.text = "Заметки по ${symptomName.split(" ").drop(1).joinToString(" ").lowercase()}:"
        notesLabel.textSize = 14f
        notesLabel.setTextColor(androidx.core.content.ContextCompat.getColor(this, android.R.color.darker_gray))
        notesLabel.setPadding(0, 16, 0, 8)
        
        val notesEditText = EditText(this)
        notesEditText.hint = "Введите заметки..."
        notesEditText.textSize = 14f
        notesEditText.setText(symptomNotes[symptomName] ?: "")
        notesEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (switch.isChecked) {
                    symptomNotes[symptomName] = s?.toString() ?: ""
                }
            }
        })
        
        detailsContainer.addView(intensityLabel)
        detailsContainer.addView(seekBar)
        detailsContainer.addView(notesLabel)
        detailsContainer.addView(notesEditText)
        
        cardContent.addView(detailsContainer)
        card.addView(cardContent)
        
        return card
    }
    
    private fun updateSymptomCard(card: androidx.cardview.widget.CardView, symptomName: String, isChecked: Boolean) {
        val cardContent = card.getChildAt(0) as? LinearLayout ?: return
        val detailsContainer = cardContent.getChildAt(1) as? LinearLayout ?: return
        detailsContainer.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton?.setOnClickListener {
            saveSymptoms()
        }
    }
    
    private fun saveSymptoms() {
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(
            date = selectedDate,
            mood = -1,
            symptoms = emptyList(),
            sleepHours = null,
            libido = null,
            sexualActivity = false,
            energy = null,
            waterIntake = null,
            vitamins = false,
            weight = null,
            temperature = null,
            notes = ""
        )
        
        // Преобразуем выбранные симптомы в формат DayData
        val symptomsList = selectedSymptoms.map { (symptomName, intensity) ->
            SymptomData(
                name = symptomName,
                category = SymptomCategory.PHYSICAL,
                intensity = intensity,
                notes = symptomNotes[symptomName] ?: ""
            )
        }
        
        val updatedDayData = dayData.copy(
            symptoms = symptomsList
        )
        
        userPreferences.saveDayData(updatedDayData)
        
        // Сохранение в Firestore
        val userId = userPreferences.getUserId()
        if (userId.isNotEmpty()) {
            FirestoreHelper(this).saveDayData(
                userId,
                updatedDayData,
                onSuccess = {
                    Toast.makeText(this, "Симптомы сохранены", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, "Симптомы сохранены локально", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        } else {
            Toast.makeText(this, "Симптомы сохранены", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

