package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class QuickAddActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedDate: String = ""
    private var selectedMood: Int = 2
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_add)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        selectedDate = intent.getStringExtra("date") ?: dateFormat.format(Date())
        
        setupMoodSelector()
        setupQuickSymptoms()
        setupSaveButton()
    }
    
    private fun setupMoodSelector() {
        val moodContainer = findViewById<LinearLayout>(R.id.moodContainer)
        moodContainer?.removeAllViews()
        
        val dayData = userPreferences.getDayData(selectedDate)
        selectedMood = if (dayData?.mood != null && dayData.mood >= 0) dayData.mood else 2
        
        val moodLabels = listOf("Плохое", "Нейтральное", "Хорошее", "Отличное", "Превосходное")
        
        moodLabels.forEachIndexed { index, label ->
            val moodButton = Button(this)
            moodButton.text = label
            moodButton.textSize = 14f
            moodButton.setPadding(8, 16, 8, 16)
            
            val layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            layoutParams.setMargins(4, 0, 4, 0)
            moodButton.layoutParams = layoutParams
            
            if (index == selectedMood) {
                moodButton.background = ContextCompat.getDrawable(this, R.drawable.button_pink_rounded)
            } else {
                moodButton.background = ContextCompat.getDrawable(this, R.drawable.button_white_rounded)
            }
            
            moodButton.setOnClickListener {
                selectedMood = index
                // Обновляем визуальное состояние
                for (i in 0 until (moodContainer?.childCount ?: 0)) {
                    val btn = moodContainer?.getChildAt(i) as? Button
                    if (i == index) {
                        btn?.background = ContextCompat.getDrawable(this, R.drawable.button_pink_rounded)
                    } else {
                        btn?.background = ContextCompat.getDrawable(this, R.drawable.button_white_rounded)
                    }
                }
            }
            
            moodContainer?.addView(moodButton)
        }
    }
    
    private fun setupQuickSymptoms() {
        val symptomsContainer = findViewById<LinearLayout>(R.id.quickSymptomsContainer)
        symptomsContainer?.removeAllViews()
        
        val dayData = userPreferences.getDayData(selectedDate)
        val existingSymptoms = dayData?.symptoms?.map { it.name }?.toSet() ?: emptySet()
        
        // Определяем, какие симптомы показывать в зависимости от режима
        val goal = userPreferences.getSelectedGoal()
        val quickSymptoms = if (goal == UserGoal.PREGNANCY) {
            listOf("Токсикоз", "Изжога", "Отеки", "Усталость", "Шевеления")
        } else {
            listOf("Головная боль", "Вздутие", "Усталость", "Спазмы", "Тошнота")
        }
        
        quickSymptoms.forEach { symptomName ->
            val checkBox = CheckBox(this)
            checkBox.text = symptomName
            checkBox.textSize = 16f
            checkBox.isChecked = existingSymptoms.contains(symptomName)
            checkBox.setPadding(8, 8, 8, 8)
            symptomsContainer?.addView(checkBox)
        }
    }
    
    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton?.setOnClickListener {
            saveQuickData()
        }
    }
    
    private fun saveQuickData() {
        val quickSymptomsContainer = findViewById<LinearLayout>(R.id.quickSymptomsContainer)
        
        // Собираем выбранные симптомы
        val selectedSymptoms = mutableListOf<SymptomData>()
        for (i in 0 until (quickSymptomsContainer?.childCount ?: 0)) {
            val checkBox = quickSymptomsContainer?.getChildAt(i) as? CheckBox
            if (checkBox?.isChecked == true) {
                selectedSymptoms.add(SymptomData(checkBox.text.toString(), SymptomCategory.PHYSICAL, 2))
            }
        }
        
        val dayData = userPreferences.getDayData(selectedDate) ?: DayData(selectedDate)
        // Удаляем старые физические симптомы из быстрого списка
        val quickSymptoms = listOf("Головная боль", "Вздутие", "Усталость", "Спазмы", "Тошнота")
        val updatedSymptoms = dayData.symptoms.filter { 
            it.category != SymptomCategory.PHYSICAL || !quickSymptoms.contains(it.name)
        }.toMutableList()
        updatedSymptoms.addAll(selectedSymptoms)
        
        val updatedDayData = dayData.copy(
            mood = selectedMood,
            symptoms = updatedSymptoms
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

