package com.example.womenhealthtracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsActivity : AppCompatActivity() {
    
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val notificationsCard = findViewById<CardView>(R.id.notificationsCard)
        
        // Обработка клика на карточку уведомлений
        notificationsCard.setOnClickListener {
            requestNotificationPermission()
        }
        
        // Проверка текущих разрешений
        checkPermissions()
        
        // Кнопка "Продолжить" - переход на главный экран календаря
        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            navigateToCalendar()
        }
        
        setupBottomNavigation()
    }
    
    private fun checkPermissions() {
        // Проверка разрешения на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Разрешение уже предоставлено
                updateNotificationCardState(true)
            }
        } else {
            // Для версий ниже Android 13 разрешение на уведомления не требуется
            updateNotificationCardState(true)
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                // Запрашиваем разрешение
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            } else {
                // Если разрешение уже есть, открываем настройки приложения для его отключения
                openAppSettings()
            }
        } else {
            // Для версий ниже Android 13 разрешение на уведомления не требуется
            Toast.makeText(this, "Разрешение не требуется для этой версии Android", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Открывает настройки приложения для управления разрешениями
     */
    private fun openAppSettings() {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
            Toast.makeText(this, "Откройте 'Уведомления' для управления разрешением", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show()
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
                updateNotificationCardState(true)
                Toast.makeText(this, "Разрешение на уведомления предоставлено", Toast.LENGTH_SHORT).show()
            } else {
                updateNotificationCardState(false)
                Toast.makeText(this, "Разрешение на уведомления отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем состояние при возврате на экран (если пользователь изменил разрешение в настройках)
        checkPermissions()
    }
    
    private fun updateNotificationCardState(isGranted: Boolean) {
        val circleView = findViewById<View>(R.id.notificationCircle)
        
        // При выключении (isGranted = false) кружочек становится светлым/пустым
        if (isGranted) {
            circleView.background = ContextCompat.getDrawable(this, R.drawable.circle_green)
        } else {
            // Используем светлый/пустой кружок
            circleView.background = ContextCompat.getDrawable(this, R.drawable.circle_gray)
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
        
        // После настройки разрешений можно перейти на главный экран календаря
        // Это можно сделать автоматически или по кнопке "Продолжить"
    }
    
    // Метод для перехода на главный экран календаря
    private fun navigateToCalendar() {
        // Отмечаем завершение онбординга
        val userPreferences = UserPreferences(this)
        userPreferences.setOnboardingCompleted(true)
        
        val intent = Intent(this, CalendarActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

