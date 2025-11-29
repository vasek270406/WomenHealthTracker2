package com.example.womenhealthtracker

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class CycleSetupActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    private val dateFormatSave = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_setup)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        setupFields()
        setupContinueButton()
    }
    
    private fun setupFields() {
        val cycleLengthEditText = findViewById<EditText>(R.id.cycleLengthEditText)
        val menstruationLengthEditText = findViewById<EditText>(R.id.menstruationLengthEditText)
        val lastPeriodDateTextView = findViewById<TextView>(R.id.lastPeriodDateTextView)
        
        // Загрузка сохраненных данных
        val savedCycleLength = userPreferences.getCycleLength()
        if (savedCycleLength > 0) {
            cycleLengthEditText.setText(savedCycleLength.toString())
        } else {
            cycleLengthEditText.setText("28")
        }
        
        val savedMenstruationLength = userPreferences.getMenstruationLength()
        if (savedMenstruationLength > 0) {
            menstruationLengthEditText.setText(savedMenstruationLength.toString())
        } else {
            menstruationLengthEditText.setText("5")
        }
        
        val savedLastPeriod = userPreferences.getLastPeriodStart()
        if (savedLastPeriod.isNotEmpty()) {
            try {
                val date = dateFormatSave.parse(savedLastPeriod)
                if (date != null) {
                    lastPeriodDateTextView.text = dateFormat.format(date)
                }
            } catch (e: Exception) {
                // Игнорируем ошибку парсинга
            }
        }
        
        // Сохранение при изменении
        cycleLengthEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val length = cycleLengthEditText.text.toString().toIntOrNull() ?: 28
                userPreferences.saveCycleLength(length)
            }
        }
        
        menstruationLengthEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val length = menstruationLengthEditText.text.toString().toIntOrNull() ?: 5
                userPreferences.saveMenstruationLength(length)
            }
        }
        
        // Выбор даты последней менструации
        lastPeriodDateTextView.setOnClickListener {
            showDatePicker(lastPeriodDateTextView)
        }
    }
    
    private fun showDatePicker(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                textView.text = dateFormat.format(selectedDate.time)
                
                // Сохранение даты
                val dateString = dateFormatSave.format(selectedDate.time)
                userPreferences.savePeriodStartDate(dateString)
            },
            year,
            month,
            day
        ).show()
    }
    
    private fun setupContinueButton() {
        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            // Сохранение данных
            val cycleLengthEditText = findViewById<EditText>(R.id.cycleLengthEditText)
            val menstruationLengthEditText = findViewById<EditText>(R.id.menstruationLengthEditText)
            
            val cycleLength = cycleLengthEditText.text.toString().toIntOrNull() ?: 28
            val menstruationLength = menstruationLengthEditText.text.toString().toIntOrNull() ?: 5
            
            userPreferences.saveCycleLength(cycleLength)
            userPreferences.saveMenstruationLength(menstruationLength)
            
            // Переход на экран профиля
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}


