package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView

class GoalsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val selectedGoals = mutableSetOf<String>()
    
    private val goals = listOf(
        "Следить за циклом" to "Следить за циклом",
        "Улучшить общее здоровье" to "Улучшить общее здоровье",
        "Управлять симптомами ПМС" to "Управлять симптомами ПМС"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        setupGoals()
        setupContinueButton()
    }
    
    private fun setupGoals() {
        val goalsContainer = findViewById<LinearLayout>(R.id.goalsContainer)
        goalsContainer.removeAllViews()
        
        // Загрузка сохраненных целей
        val savedGoals = userPreferences.getGoals()
        if (savedGoals.isNotEmpty()) {
            selectedGoals.addAll(savedGoals.split(", "))
        }
        
        goals.forEach { (_, goal) ->
            val checkBox = CheckBox(this)
            checkBox.text = goal
            checkBox.textSize = 18f
            checkBox.setPadding(16, 24, 16, 24)
            checkBox.isChecked = selectedGoals.contains(goal)
            
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGoals.add(goal)
                } else {
                    selectedGoals.remove(goal)
                }
            }
            
            goalsContainer.addView(checkBox)
        }
    }
    
    private fun setupContinueButton() {
        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            // Сохранение выбранных целей
            val goalsText = selectedGoals.joinToString(", ")
            userPreferences.saveGoals(goalsText)
            
            // Переход на следующий экран онбординга
            val intent = Intent(this, CycleSetupActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
