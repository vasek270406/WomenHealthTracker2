package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class PhysicalSymptomsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    private val selectedSymptoms = mutableMapOf<String, Int>() // symptom name -> intensity
    
    private val physicalSymptoms = listOf(
        "Головная боль",
        "Вздутие",
        "Акне",
        "Болезненность груди",
        "Спазмы",
        "Тяга к еде",
        "Тошнота",
        "Усталость",
        "Боль в спине",
        "Вздутие живота"
    )
    
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
        setContentView(R.layout.activity_physical_symptoms)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        
        // Получаем дату из интента или используем сегодняшнюю
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        setupSymptoms()
        setupSaveButton()
    }
    
    private fun setupSymptoms() {
        val symptomsContainer = findViewById<LinearLayout>(R.id.symptomsContainer)
        symptomsContainer.removeAllViews()
        
        // Загружаем сохраненные данные
        val dayData = userPreferences.getDayData(selectedDate)
        if (dayData != null) {
            for (symptom in dayData.symptoms) {
                if (symptom.category == SymptomCategory.PHYSICAL) {
                    selectedSymptoms[symptom.name] = symptom.intensity
                }
            }
        }
        
        // Определяем, какие симптомы показывать в зависимости от режима
        val goal = userPreferences.getSelectedGoal()
        val symptomsToShow = if (goal == UserGoal.PREGNANCY) {
            pregnancySymptoms
        } else {
            physicalSymptoms
        }
        
        symptomsToShow.forEach { symptomName ->
            val symptomLayout = LinearLayout(this)
            symptomLayout.orientation = LinearLayout.VERTICAL
            symptomLayout.setPadding(16, 16, 16, 16)
            
            val checkBox = CheckBox(this)
            checkBox.text = symptomName
            checkBox.textSize = 16f
            checkBox.isChecked = selectedSymptoms.containsKey(symptomName)
            
            val intensityLayout = LinearLayout(this)
            intensityLayout.orientation = LinearLayout.HORIZONTAL
            intensityLayout.setPadding(32, 8, 0, 0)
            intensityLayout.visibility = if (checkBox.isChecked) android.view.View.VISIBLE else android.view.View.GONE
            
            val intensityLabel = TextView(this)
            intensityLabel.text = "Интенсивность: "
            intensityLabel.textSize = 14f
            
            val intensityValue = TextView(this)
            intensityValue.text = selectedSymptoms[symptomName]?.toString() ?: "1"
            intensityValue.textSize = 14f
            intensityValue.setPadding(8, 0, 0, 0)
            
            val seekBar = SeekBar(this)
            seekBar.max = 2 // 0-2 (легкая=1, средняя=2, сильная=3)
            seekBar.progress = (selectedSymptoms[symptomName] ?: 1) - 1
            seekBar.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val intensity = progress + 1
                    intensityValue.text = intensity.toString()
                    if (checkBox.isChecked) {
                        selectedSymptoms[symptomName] = intensity
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                intensityLayout.visibility = if (isChecked) android.view.View.VISIBLE else android.view.View.GONE
                if (isChecked) {
                    selectedSymptoms[symptomName] = seekBar.progress + 1
                } else {
                    selectedSymptoms.remove(symptomName)
                }
            }
            
            intensityLayout.addView(intensityLabel)
            intensityLayout.addView(intensityValue)
            intensityLayout.addView(seekBar)
            
            symptomLayout.addView(checkBox)
            symptomLayout.addView(intensityLayout)
            symptomsContainer.addView(symptomLayout)
        }
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveSymptoms()
        }
    }
    
    private fun saveSymptoms() {
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(selectedDate)
        
        // Удаляем старые физические симптомы
        val updatedSymptoms = dayData.symptoms.filter { it.category != SymptomCategory.PHYSICAL }.toMutableList()
        
        // Добавляем новые выбранные симптомы
        selectedSymptoms.forEach { (name, intensity) ->
            updatedSymptoms.add(SymptomData(name, SymptomCategory.PHYSICAL, intensity))
        }
        
        val updatedDayData = dayData.copy(symptoms = updatedSymptoms)
        userPreferences.saveDayData(updatedDayData)
        
        android.widget.Toast.makeText(this, "Симптомы сохранены", android.widget.Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

