package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class EmotionalSymptomsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    private val selectedSymptoms = mutableMapOf<String, Int>()
    
    private val emotionalSymptoms = listOf(
        "Тревога",
        "Раздражительность",
        "Грусть",
        "Прилив любви",
        "Мотивация",
        "Стресс",
        "Перепады настроения",
        "Плаксивость"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_physical_symptoms)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        // Изменяем заголовок
        val titleTextView = findViewById<TextView>(R.id.titleTextView)
        titleTextView?.text = "Эмоциональные симптомы"
        
        setupSymptoms()
        setupSaveButton()
    }
    
    private fun setupSymptoms() {
        val symptomsContainer = findViewById<LinearLayout>(R.id.symptomsContainer)
        symptomsContainer?.removeAllViews()
        
        val dayData = userPreferences.getDayData(selectedDate)
        if (dayData != null) {
            for (symptom in dayData.symptoms) {
                if (symptom.category == SymptomCategory.EMOTIONAL) {
                    selectedSymptoms[symptom.name] = symptom.intensity
                }
            }
        }
        
        emotionalSymptoms.forEach { symptomName ->
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
            seekBar.max = 2
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
            symptomsContainer?.addView(symptomLayout)
        }
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton?.setOnClickListener {
            saveSymptoms()
        }
    }
    
    private fun saveSymptoms() {
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(selectedDate)
        val updatedSymptoms = dayData.symptoms.filter { it.category != SymptomCategory.EMOTIONAL }.toMutableList()
        
        selectedSymptoms.forEach { (name, intensity) ->
            updatedSymptoms.add(SymptomData(name, SymptomCategory.EMOTIONAL, intensity))
        }
        
        val updatedDayData = dayData.copy(symptoms = updatedSymptoms)
        userPreferences.saveDayData(updatedDayData)
        
        android.widget.Toast.makeText(this, "Симптомы сохранены", android.widget.Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

