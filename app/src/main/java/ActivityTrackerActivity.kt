package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class ActivityTrackerActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_tracker)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        setupFields()
        setupSaveButton()
    }
    
    private fun setupFields() {
        val dayData = userPreferences.getDayData(selectedDate)
        
        // Сон
        val sleepSeekBar = findViewById<SeekBar>(R.id.sleepSeekBar)
        val sleepValue = findViewById<TextView>(R.id.sleepValue)
        sleepSeekBar?.max = 12 // 0-12 часов
        sleepSeekBar?.progress = (dayData?.sleepHours ?: 7f).toInt()
        sleepValue?.text = "${sleepSeekBar?.progress}ч"
        
        sleepSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                sleepValue?.text = "${progress}ч"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Либидо
        val libidoSpinner = findViewById<Spinner>(R.id.libidoSpinner)
        val libidoOptions = arrayOf("Низкое", "Среднее", "Высокое")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, libidoOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        libidoSpinner?.adapter = adapter
        libidoSpinner?.setSelection(dayData?.libido ?: 1)
        
        // Половая активность
        val sexualActivityCheckBox = findViewById<CheckBox>(R.id.sexualActivityCheckBox)
        sexualActivityCheckBox?.isChecked = dayData?.sexualActivity ?: false
        
        // Энергия
        val energySeekBar = findViewById<SeekBar>(R.id.energySeekBar)
        val energyValue = findViewById<TextView>(R.id.energyValue)
        energySeekBar?.max = 100
        energySeekBar?.progress = dayData?.energy ?: 50
        energyValue?.text = "${energySeekBar?.progress}%"
        
        energySeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                energyValue?.text = "${progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton?.setOnClickListener {
            saveActivity()
        }
    }
    
    private fun saveActivity() {
        val sleepSeekBar = findViewById<SeekBar>(R.id.sleepSeekBar)
        val libidoSpinner = findViewById<Spinner>(R.id.libidoSpinner)
        val sexualActivityCheckBox = findViewById<CheckBox>(R.id.sexualActivityCheckBox)
        val energySeekBar = findViewById<SeekBar>(R.id.energySeekBar)
        
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(selectedDate)
        
        val updatedDayData = dayData.copy(
            sleepHours = sleepSeekBar?.progress?.toFloat(),
            libido = libidoSpinner?.selectedItemPosition,
            sexualActivity = sexualActivityCheckBox?.isChecked ?: false,
            energy = energySeekBar?.progress
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

