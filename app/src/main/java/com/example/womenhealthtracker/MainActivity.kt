package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Очищаем старые данные с полями менопаузы (однократно)
        val userPreferences = UserPreferences(this)
        val cleanedKey = "menopause_data_cleaned"
        if (!userPreferences.prefs.getBoolean(cleanedKey, false)) {
            userPreferences.cleanOldMenopauseData()
            userPreferences.prefs.edit().putBoolean(cleanedKey, true).apply()
        }
        
        // Проверяем, авторизован ли пользователь через Firebase
        val authHelper = FirebaseAuthHelper(this)
        
        if (authHelper.isUserLoggedIn() || userPreferences.isLoggedIn()) {
            // Если авторизован, переходим на главный экран или онбординг
            if (userPreferences.isOnboardingCompleted()) {
                // Активируем уведомления для текущего режима
                val goal = userPreferences.getSelectedGoal()
                val notificationHelper = NotificationHelper(this)
                notificationHelper.activateModeNotifications(goal)
                
                // Планируем умные уведомления на основе прогнозов
                val smartScheduler = SmartNotificationScheduler(this)
                smartScheduler.scheduleSmartNotifications()
                
                // Проверяем режим менопаузы
                val intent = if (goal == UserGoal.MENOPAUSE) {
                    Intent(this, MenopauseHomeActivity::class.java)
                } else {
                    Intent(this, CalendarActivity::class.java)
                }
                startActivity(intent)
                finish()
                return
            } else {
                val intent = Intent(this, AccountActivity::class.java)
                startActivity(intent)
                finish()
                return
            }
        }
        
        setContentView(R.layout.activity_main)

        // Находим кнопку по ID
        val getStartedButton = findViewById<Button>(R.id.getStartedButton)

        // Устанавливаем обработчик нажатия на кнопку
        getStartedButton.setOnClickListener {
            // Переход на экран регистрации
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}