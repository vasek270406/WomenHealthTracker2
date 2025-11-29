package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast

class SymptomTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_tracker)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        setupButtons()
        setupBottomNavigation()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun setupButtons() {
        val physicalButton = findViewById<Button>(R.id.physicalButton)
        val emotionalButton = findViewById<Button>(R.id.emotionalButton)
        val activityButton = findViewById<Button>(R.id.activityButton)
        val habitsButton = findViewById<Button>(R.id.habitsButton)
        
        physicalButton.setOnClickListener {
            val intent = Intent(this, PhysicalSymptomsActivity::class.java)
            startActivity(intent)
        }
        
        emotionalButton.setOnClickListener {
            val intent = Intent(this, EmotionalSymptomsActivity::class.java)
            startActivity(intent)
        }
        
        activityButton.setOnClickListener {
            val intent = Intent(this, ActivityTrackerActivity::class.java)
            startActivity(intent)
        }
        
        habitsButton.setOnClickListener {
            val intent = Intent(this, HabitsTrackerActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupBottomNavigation() {
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        val notificationsButton = findViewById<ImageButton>(R.id.notificationsButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        calendarButton.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        
        notificationsButton.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        profileButton.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}


