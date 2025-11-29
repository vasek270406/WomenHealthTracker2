package com.example.womenhealthtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class SignUpActivity : AppCompatActivity() {
    
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val signInLink = findViewById<TextView>(R.id.signInLink)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        
        // Убеждаемся, что поля ввода активны и фокусируемы
        nameEditText.isEnabled = true
        nameEditText.isFocusable = true
        nameEditText.isFocusableInTouchMode = true
        nameEditText.isClickable = true
        
        emailEditText.isEnabled = true
        emailEditText.isFocusable = true
        emailEditText.isFocusableInTouchMode = true
        emailEditText.isClickable = true
        
        passwordEditText.isEnabled = true
        passwordEditText.isFocusable = true
        passwordEditText.isFocusableInTouchMode = true
        passwordEditText.isClickable = true

        createAccountButton.setOnClickListener {
            // Валидация полей
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show()
                nameEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (email.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите email", Toast.LENGTH_SHORT).show()
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Пожалуйста, введите корректный email", Toast.LENGTH_SHORT).show()
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите пароль", Toast.LENGTH_SHORT).show()
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            
            if (password.length < 6) {
                Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show()
                passwordEditText.requestFocus()
                return@setOnClickListener
            }
            
            // Регистрация через Firebase Authentication
            val authHelper = FirebaseAuthHelper(this)
            val firestoreHelper = FirestoreHelper(this)
            
            authHelper.signUp(
                email = email,
                password = password,
                name = name,
                onSuccess = { user ->
                    // Очищаем все локальные данные перед регистрацией нового пользователя
                    val userPreferences = UserPreferences(this)
                    userPreferences.clearAll()
                    
                    // Сохранение данных пользователя в SharedPreferences (для обратной совместимости)
                    userPreferences.saveName(name)
                    userPreferences.setLoggedIn(true)
                    userPreferences.saveUserId(user.uid) // Сохраняем userId
                    
                    // Сохранение профиля в Firestore
                    val profile = UserProfile(
                        name = name,
                        onboardingCompleted = false
                    )
                    firestoreHelper.saveUserProfile(
                        userId = user.uid,
                        profile = profile,
                        onSuccess = {
                            // После сохранения загружаем профиль обратно для синхронизации всех данных
                            firestoreHelper.getUserProfile(
                                userId = user.uid,
                                onSuccess = { savedProfile ->
                                    if (savedProfile != null) {
                                        // Синхронизация всех данных из Firestore в SharedPreferences
                                        userPreferences.saveName(savedProfile.name)
                                        userPreferences.saveAge(savedProfile.age)
                                        userPreferences.saveCycleLength(savedProfile.cycleLength)
                                        userPreferences.saveMenstruationLength(savedProfile.menstruationLength)
                                        userPreferences.saveGoals(savedProfile.goals)
                                        userPreferences.setOnboardingCompleted(savedProfile.onboardingCompleted)
                                        userPreferences.saveSelectedGoal(savedProfile.selectedGoal)
                                        userPreferences.savePregnancyStartDate(savedProfile.pregnancyStartDate)
                                        userPreferences.saveMenopauseStartDate(savedProfile.menopauseStartDate)
                                        userPreferences.saveHasIrregularCycles(savedProfile.hasIrregularCycles)
                                        
                                        // Сохраняем даты месячных
                                        if (savedProfile.periodDates.isNotEmpty()) {
                                            userPreferences.savePeriodDates(savedProfile.periodDates.toSet())
                                        }
                                        if (savedProfile.lastPeriodStart.isNotEmpty()) {
                                            userPreferences.savePeriodStartDate(savedProfile.lastPeriodStart)
                                        }
                                        
                                        // Сохраняем настройки уведомлений
                                        userPreferences.setNotificationPeriod(savedProfile.notificationPeriod)
                                        userPreferences.setNotificationFertile(savedProfile.notificationFertile)
                                        userPreferences.setNotificationDaily(savedProfile.notificationDaily)
                                        userPreferences.setNotificationWater(savedProfile.notificationWater)
                                    }
                                    
                                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                                    
                                    // Запрашиваем разрешение на уведомления после регистрации
                                    requestNotificationPermission()
                                    
                                    // Переход на экран аккаунта (настройка профиля)
                                    val intent = Intent(this, AccountActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                },
                                onError = { error ->
                                    // Даже если не удалось загрузить, продолжаем работу
                                    Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                                    
                                    // Запрашиваем разрешение на уведомления после регистрации
                                    requestNotificationPermission()
                                    
                                    val intent = Intent(this, AccountActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        },
                        onError = { error ->
                            // Даже если Firestore не сохранился, продолжаем работу
                            Toast.makeText(this, "Регистрация успешна! Профиль сохранен локально.", Toast.LENGTH_SHORT).show()
                            
                            // Запрашиваем разрешение на уведомления после регистрации
                            requestNotificationPermission()
                            
                            val intent = Intent(this, AccountActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                },
                onError = { error ->
                    Toast.makeText(this, "Ошибка регистрации: $error", Toast.LENGTH_SHORT).show()
                }
            )
        }

        signInLink.setOnClickListener {
            // Переход на экран авторизации
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    
    /**
     * Запрос разрешения на уведомления
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Вы можете включить уведомления позже в настройках", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}