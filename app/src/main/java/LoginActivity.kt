package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    
    /**
     * Синхронизация всех данных дня из Firestore в SharedPreferences
     */
    private fun syncAllDayDataFromFirestore(
        userId: String,
        userPreferences: UserPreferences,
        firestoreHelper: FirestoreHelper
    ) {
        // Получаем все даты с данными из Firestore
        firestoreHelper.getAllDatesWithData(
            userId = userId,
            onSuccess = { dates ->
                if (dates.isEmpty()) {
                    // Если данных нет, просто переходим на нужный экран
                    navigateAfterSync(userPreferences)
                    return@getAllDatesWithData
                }
                
                // Загружаем данные для каждой даты
                var loadedCount = 0
                val totalDates = dates.size
                
                dates.forEach { date ->
                    firestoreHelper.getDayData(
                        userId = userId,
                        date = date,
                        onSuccess = { dayData ->
                            if (dayData != null) {
                                // Сохраняем данные дня в SharedPreferences
                                userPreferences.saveDayData(dayData)
                            }
                            
                            loadedCount++
                            // Когда все данные загружены, переходим на нужный экран
                            if (loadedCount == totalDates) {
                                navigateAfterSync(userPreferences)
                            }
                        },
                        onError = { error ->
                            android.util.Log.e("LoginActivity", "Ошибка загрузки данных дня $date: $error")
                            loadedCount++
                            if (loadedCount == totalDates) {
                                navigateAfterSync(userPreferences)
                            }
                        }
                    )
                }
            },
            onError = { error ->
                android.util.Log.e("LoginActivity", "Ошибка получения списка дат: $error")
                // Даже если не удалось загрузить данные, продолжаем работу
                navigateAfterSync(userPreferences)
            }
        )
    }
    
    /**
     * Навигация после синхронизации данных
     */
    private fun navigateAfterSync(userPreferences: UserPreferences) {
        // Проверяем, завершен ли онбординг
        if (userPreferences.isOnboardingCompleted()) {
            val goal = userPreferences.getSelectedGoal()
            val intent = if (goal == UserGoal.MENOPAUSE) {
                Intent(this, MenopauseHomeActivity::class.java)
            } else {
                Intent(this, CalendarActivity::class.java)
            }
            startActivity(intent)
        } else {
            // Переход на экран аккаунта для настройки
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
        finish()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val signInButton = findViewById<Button>(R.id.signInButton)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите email", Toast.LENGTH_SHORT).show()
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите пароль", Toast.LENGTH_SHORT).show()
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            
            // Вход через Firebase Authentication
            val authHelper = FirebaseAuthHelper(this)
            val firestoreHelper = FirestoreHelper(this)
            
            authHelper.signIn(
                email = email,
                password = password,
                onSuccess = { user ->
                    // Очищаем все локальные данные перед входом нового пользователя
                    val userPreferences = UserPreferences(this)
                    userPreferences.clearAll()
                    
                    // Сохранение статуса авторизации в SharedPreferences (для обратной совместимости)
                    userPreferences.setLoggedIn(true)
                    userPreferences.saveUserId(user.uid) // Сохраняем userId
                    
                    // Загрузка профиля из Firestore
                    firestoreHelper.getUserProfile(
                        userId = user.uid,
                        onSuccess = { profile ->
                            if (profile != null) {
                                // Синхронизация данных из Firestore в SharedPreferences
                                userPreferences.saveName(profile.name)
                                userPreferences.saveAge(profile.age)
                                userPreferences.saveCycleLength(profile.cycleLength)
                                userPreferences.saveMenstruationLength(profile.menstruationLength)
                                userPreferences.saveGoals(profile.goals)
                                userPreferences.setOnboardingCompleted(profile.onboardingCompleted)
                                userPreferences.saveSelectedGoal(profile.selectedGoal)
                                userPreferences.savePregnancyStartDate(profile.pregnancyStartDate)
                                userPreferences.saveMenopauseStartDate(profile.menopauseStartDate)
                                userPreferences.saveHasIrregularCycles(profile.hasIrregularCycles)
                                
                                // Сохраняем даты месячных
                                if (profile.periodDates.isNotEmpty()) {
                                    userPreferences.savePeriodDates(profile.periodDates.toSet())
                                }
                                if (profile.lastPeriodStart.isNotEmpty()) {
                                    userPreferences.savePeriodStartDate(profile.lastPeriodStart)
                                }
                                
                                // Сохраняем настройки уведомлений
                                userPreferences.setNotificationPeriod(profile.notificationPeriod)
                                userPreferences.setNotificationFertile(profile.notificationFertile)
                                userPreferences.setNotificationDaily(profile.notificationDaily)
                                userPreferences.setNotificationWater(profile.notificationWater)
                                
                                // Синхронизация всех данных дня из Firestore
                                syncAllDayDataFromFirestore(user.uid, userPreferences, firestoreHelper)
                            } else {
                                // Если профиля нет, переходим на экран аккаунта
                                val intent = Intent(this, AccountActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        },
                        onError = { error ->
                            Toast.makeText(this, "Ошибка загрузки профиля: $error", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onError = { error ->
                    Toast.makeText(this, "Ошибка входа: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }

        signUpLink.setOnClickListener {
            // Переход на экран регистрации
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

