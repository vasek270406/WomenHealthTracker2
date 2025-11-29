package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class KickCounterActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestoreHelper: FirestoreHelper
    private var kickCount = 0
    private var startTime: Long = 0
    private var timer: CountDownTimer? = null
    private var isCounting = false
    
    private lateinit var kickCountTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var kickButton: Button
    private lateinit var stopButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kick_counter)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        firestoreHelper = FirestoreHelper(this)
        
        kickCountTextView = findViewById(R.id.kickCountTextView)
        timerTextView = findViewById(R.id.timerTextView)
        startButton = findViewById(R.id.startButton)
        kickButton = findViewById(R.id.kickButton)
        stopButton = findViewById(R.id.stopButton)
        
        kickButton.isEnabled = false
        stopButton.isEnabled = false
        
        startButton.setOnClickListener {
            startCounting()
        }
        
        kickButton.setOnClickListener {
            incrementKick()
        }
        
        stopButton.setOnClickListener {
            stopCounting()
        }
        
        // Кнопка назад
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun startCounting() {
        kickCount = 0
        startTime = System.currentTimeMillis()
        isCounting = true
        
        startButton.isEnabled = false
        kickButton.isEnabled = true
        stopButton.isEnabled = true
        
        // Обновляем таймер каждую секунду
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimer()
            }
            
            override fun onFinish() {
                // Не вызывается
            }
        }
        timer?.start()
        
        updateKickCount()
        Toast.makeText(this, "Начните считать шевеления", Toast.LENGTH_SHORT).show()
    }
    
    private fun incrementKick() {
        if (!isCounting) return
        
        kickCount++
        updateKickCount()
        
        // Если достигли 10 шевелений, автоматически останавливаем
        if (kickCount >= 10) {
            stopCounting()
            Toast.makeText(this, "Отлично! 10 шевелений зафиксировано", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopCounting() {
        isCounting = false
        timer?.cancel()
        
        startButton.isEnabled = true
        kickButton.isEnabled = false
        stopButton.isEnabled = false
        
        if (kickCount > 0) {
            saveKickSession()
        }
    }
    
    private fun updateKickCount() {
        kickCountTextView.text = "$kickCount"
    }
    
    private fun updateTimer() {
        val elapsed = System.currentTimeMillis() - startTime
        val seconds = (elapsed / 1000).toInt()
        val minutes = seconds / 60
        val secs = seconds % 60
        
        timerTextView.text = String.format("%02d:%02d", minutes, secs)
    }
    
    private fun saveKickSession() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        val today = Calendar.getInstance()
        val dateString = dateFormat.format(today.time)
        val startTimeString = timeFormat.format(Date(startTime))
        val endTimeString = timeFormat.format(Date())
        
        val elapsed = System.currentTimeMillis() - startTime
        val minutes = (elapsed / 60000).toInt()
        
        val babyKick = BabyKick(
            date = dateString,
            startTime = startTimeString,
            endTime = endTimeString,
            kickCount = kickCount
        )
        
        // Сохраняем локально (можно расширить UserPreferences для этого)
        // И в Firestore
        try {
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Получаем текущие данные беременности
                firestoreHelper.getPregnancyData(
                    userId = currentUser.uid,
                    onSuccess = { pregnancyData ->
                        val updatedKicks = pregnancyData?.kicks?.toMutableList() ?: mutableListOf()
                        updatedKicks.add(babyKick)
                        
                        val updatedPregnancyData = pregnancyData?.copy(kicks = updatedKicks) 
                            ?: PregnancyData(
                                pregnancyStartDate = userPreferences.getPregnancyStartDate(),
                                kicks = listOf(babyKick)
                            )
                        
                        firestoreHelper.savePregnancyData(
                            userId = currentUser.uid,
                            pregnancyData = updatedPregnancyData,
                            onSuccess = {
                                Toast.makeText(this, "Шевеления сохранены: $kickCount за $minutes минут", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(this, "Ошибка сохранения: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    onError = {
                        // Создаем новые данные беременности
                        val newPregnancyData = PregnancyData(
                            pregnancyStartDate = userPreferences.getPregnancyStartDate(),
                            kicks = listOf(babyKick)
                        )
                        firestoreHelper.savePregnancyData(
                            userId = currentUser.uid,
                            pregnancyData = newPregnancyData,
                            onSuccess = {
                                Toast.makeText(this, "Шевеления сохранены: $kickCount за $minutes минут", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(this, "Ошибка сохранения: $error", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}


