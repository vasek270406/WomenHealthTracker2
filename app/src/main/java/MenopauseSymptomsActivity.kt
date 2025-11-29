package com.example.womenhealthtracker

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MenopauseSymptomsActivity : AppCompatActivity() {
    
    private lateinit var symptomsContainer: LinearLayout
    private lateinit var triggersContainer: LinearLayout
    private lateinit var moodContainer: LinearLayout
    private lateinit var energyContainer: LinearLayout
    private lateinit var notesEditText: EditText
    private lateinit var saveButton: Button
    
    private val selectedSymptoms = mutableMapOf<String, SymptomDetail>()
    private val selectedTriggers = mutableSetOf<String>()
    private var selectedMood = 3
    private var selectedEnergy = 3
    
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_symptoms)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        initViews()
        setupSymptoms()
        setupTriggers()
        setupMoodAndEnergy()
        setupSaveButton()
        
        // Загружаем данные за выбранную дату или сегодня
        val selectedDate = intent.getStringExtra("selected_date")
        if (selectedDate != null) {
            loadDateData(selectedDate)
        } else {
            loadTodayData()
        }
    }
    
    private fun initViews() {
        symptomsContainer = findViewById(R.id.symptomsContainer)
        triggersContainer = findViewById(R.id.triggersContainer)
        moodContainer = findViewById(R.id.moodContainer)
        energyContainer = findViewById(R.id.energyContainer)
        notesEditText = findViewById(R.id.notesEditText)
        saveButton = findViewById(R.id.saveButton)
    }
    
    private fun setupSymptoms() {
        MenopauseSymptomType.values().forEach { symptomType ->
            val symptomCard = createSymptomCard(symptomType)
            symptomsContainer.addView(symptomCard)
        }
    }
    
    private fun createSymptomCard(symptomType: MenopauseSymptomType): LinearLayout {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(16, 12, 16, 12)
        
        val checkBox = CheckBox(this)
        checkBox.text = symptomType.displayName
        checkBox.textSize = 16f
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSymptoms[symptomType.name] = SymptomDetail(intensity = 3)
            } else {
                selectedSymptoms.remove(symptomType.name)
            }
        }
        card.addView(checkBox)
        
        // Интенсивность (показывается только если симптом выбран)
        val intensityLayout = LinearLayout(this)
        intensityLayout.orientation = LinearLayout.HORIZONTAL
        intensityLayout.setPadding(32, 8, 0, 0)
        intensityLayout.visibility = View.GONE
        
        val intensityLabel = TextView(this)
        intensityLabel.text = "Интенсивность: "
        intensityLabel.textSize = 14f
        intensityLayout.addView(intensityLabel)
        
        val intensitySeekBar = SeekBar(this)
        intensitySeekBar.max = 4 // 1-5 (0-4)
        intensitySeekBar.progress = 2 // по умолчанию 3
        intensitySeekBar.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val intensity = progress + 1 // 1-5
                selectedSymptoms[symptomType.name]?.let {
                    selectedSymptoms[symptomType.name] = it.copy(intensity = intensity)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        intensityLayout.addView(intensitySeekBar)
        
        val intensityValue = TextView(this)
        intensityValue.text = "3"
        intensityValue.textSize = 14f
        intensityValue.setPadding(8, 0, 0, 0)
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val intensity = progress + 1
                intensityValue.text = intensity.toString()
                selectedSymptoms[symptomType.name]?.let {
                    selectedSymptoms[symptomType.name] = it.copy(intensity = intensity)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        intensityLayout.addView(intensityValue)
        
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedSymptoms[symptomType.name] = SymptomDetail(intensity = 3)
                intensityLayout.visibility = View.VISIBLE
            } else {
                selectedSymptoms.remove(symptomType.name)
                intensityLayout.visibility = View.GONE
            }
        }
        
        card.addView(intensityLayout)
        
        return card
    }
    
    private fun setupTriggers() {
        MenopauseTriggers.commonTriggers.forEach { trigger ->
            val checkBox = CheckBox(this)
            checkBox.text = trigger
            checkBox.textSize = 14f
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedTriggers.add(trigger)
                } else {
                    selectedTriggers.remove(trigger)
                }
            }
            triggersContainer.addView(checkBox)
        }
    }
    
    private fun setupMoodAndEnergy() {
        // Настроение (1-5)
        for (i in 1..5) {
            val button = Button(this)
            button.text = i.toString()
            button.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            button.setOnClickListener {
                selectedMood = i
                updateMoodButtons()
            }
            moodContainer.addView(button)
        }
        
        // Энергия (1-5)
        for (i in 1..5) {
            val button = Button(this)
            button.text = i.toString()
            button.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(4, 0, 4, 0)
            }
            button.setOnClickListener {
                selectedEnergy = i
                updateEnergyButtons()
            }
            energyContainer.addView(button)
        }
        
        updateMoodButtons()
        updateEnergyButtons()
    }
    
    private fun updateMoodButtons() {
        for (i in 0 until moodContainer.childCount) {
            val button = moodContainer.getChildAt(i) as Button
            val value = i + 1
            if (value == selectedMood) {
                button.background = getDrawable(R.drawable.button_pink_rounded)
                button.setTextColor(getColor(R.color.white))
            } else {
                button.background = getDrawable(R.drawable.button_white_rounded)
                button.setTextColor(getColor(android.R.color.black))
            }
        }
    }
    
    private fun updateEnergyButtons() {
        for (i in 0 until energyContainer.childCount) {
            val button = energyContainer.getChildAt(i) as Button
            val value = i + 1
            if (value == selectedEnergy) {
                button.background = getDrawable(R.drawable.button_pink_rounded)
                button.setTextColor(getColor(R.color.white))
            } else {
                button.background = getDrawable(R.drawable.button_white_rounded)
                button.setTextColor(getColor(android.R.color.black))
            }
        }
    }
    
    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveData()
        }
    }
    
    private fun loadTodayData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        loadDateData(today)
    }
    
    private fun loadDateData(date: String) {
        val record = userPreferences.getMenopauseDayRecord(date)
        if (record != null) {
            // Загружаем симптомы
            selectedSymptoms.clear()
            selectedSymptoms.putAll(record.symptoms)
            
            // Загружаем триггеры
            selectedTriggers.clear()
            selectedTriggers.addAll(record.triggers)
            
            // Загружаем настроение и энергию
            selectedMood = record.mood
            selectedEnergy = record.energy
            
            // Загружаем заметки
            notesEditText.setText(record.notes)
            
            // Обновляем UI (нужно будет добавить логику обновления чекбоксов и слайдеров)
            updateMoodButtons()
            updateEnergyButtons()
        }
    }
    
    private fun saveData() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        val record = MenopauseDayRecord(
            date = today,
            symptoms = selectedSymptoms,
            notes = notesEditText.text.toString(),
            mood = selectedMood,
            energy = selectedEnergy,
            triggers = selectedTriggers.toList()
        )
        
        // Сохраняем в UserPreferences
        userPreferences.saveMenopauseDayRecord(record)
        
        Toast.makeText(this, "Данные сохранены!", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

