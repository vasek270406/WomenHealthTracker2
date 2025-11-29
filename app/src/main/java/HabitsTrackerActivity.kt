package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class HabitsTrackerActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habits_tracker)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        setupFields()
        setupSaveButton()
    }
    
    private fun setupFields() {
        val dayData = userPreferences.getDayData(selectedDate)
        
        // Вода
        val waterSeekBar = findViewById<SeekBar>(R.id.waterSeekBar)
        val waterValue = findViewById<TextView>(R.id.waterValue)
        waterSeekBar?.max = 30 // 0-3 литра (0.1 шаг)
        val waterLiters = (dayData?.waterIntake ?: 0f) * 10
        waterSeekBar?.progress = waterLiters.toInt()
        waterValue?.text = String.format("%.1f л", dayData?.waterIntake ?: 0f)
        
        waterSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val liters = progress / 10f
                waterValue?.text = String.format("%.1f л", liters)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Витамины
        val vitaminsCheckBox = findViewById<CheckBox>(R.id.vitaminsCheckBox)
        vitaminsCheckBox?.isChecked = dayData?.vitamins ?: false
        
        // Вес
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        if (dayData?.weight != null) {
            weightEditText?.setText(dayData.weight.toString())
        }
        
        // Температура
        val temperatureEditText = findViewById<EditText>(R.id.temperatureEditText)
        if (dayData?.temperature != null) {
            temperatureEditText?.setText(dayData.temperature.toString())
        }
        
        // Заметки
        val notesEditText = findViewById<EditText>(R.id.notesEditText)
        notesEditText?.setText(dayData?.notes ?: "")
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton?.setOnClickListener {
            saveHabits()
        }
    }
    
    private fun saveHabits() {
        val waterSeekBar = findViewById<SeekBar>(R.id.waterSeekBar)
        val vitaminsCheckBox = findViewById<CheckBox>(R.id.vitaminsCheckBox)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val temperatureEditText = findViewById<EditText>(R.id.temperatureEditText)
        val notesEditText = findViewById<EditText>(R.id.notesEditText)
        
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(selectedDate)
        
        val waterLiters = (waterSeekBar?.progress ?: 0) / 10f
        val weight = weightEditText?.text?.toString()?.toFloatOrNull()
        val temperature = temperatureEditText?.text?.toString()?.toFloatOrNull()
        val notes = notesEditText?.text?.toString() ?: ""
        
        val updatedDayData = dayData.copy(
            waterIntake = waterLiters,
            vitamins = vitaminsCheckBox?.isChecked ?: false,
            weight = weight,
            temperature = temperature,
            notes = notes
        )
        
        userPreferences.saveDayData(updatedDayData)
        android.widget.Toast.makeText(this, "Данные сохранены", android.widget.Toast.LENGTH_SHORT).show()
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

