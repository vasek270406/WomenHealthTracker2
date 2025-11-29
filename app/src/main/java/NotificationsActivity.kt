package com.example.womenhealthtracker

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class NotificationsActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var notificationsContainer: LinearLayout
    private lateinit var currentMode: UserGoal
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        
        // Настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        currentMode = userPreferences.getSelectedGoal()
        
        initViews()
        loadNotifications()
        setupBottomNavigation()
    }
    
    private fun initViews() {
        notificationsContainer = findViewById(R.id.notificationsContainer)
    }
    
    private fun loadNotifications() {
        notificationsContainer.removeAllViews()
        
        // Заголовок режима
        val modeHeader = createModeHeader()
        notificationsContainer.addView(modeHeader)
        
        // Получаем уведомления для текущего режима
        val defaultNotificationsMap = NotificationManager.getDefaultNotifications()
        val notifications = defaultNotificationsMap[currentMode] ?: emptyList()
        
        if (notifications.isEmpty()) {
            val emptyText = TextView(this)
            emptyText.text = "Нет уведомлений для текущего режима"
            emptyText.textSize = 16f
            emptyText.gravity = android.view.Gravity.CENTER
            emptyText.setPadding(32, 32, 32, 32)
            notificationsContainer.addView(emptyText)
            return
        }
        
        // Загружаем сохраненные настройки пользователя
        val savedNotifications = userPreferences.getNotificationsForMode(currentMode)
        val notificationMap = savedNotifications.associateBy { it.id }
        
        // Создаем карточки для каждого уведомления
        notifications.forEach { defaultNotification ->
            val savedNotification = notificationMap[defaultNotification.id]
            val notification = savedNotification ?: defaultNotification
            val card = createNotificationCard(notification)
            notificationsContainer.addView(card)
        }
    }
    
    private fun createModeHeader(): CardView {
        val card = CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 8)
        }
        card.radius = 12f
        card.setCardElevation(2f)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        val title = TextView(this)
        title.text = "Уведомления для режима: ${GoalHelper.getGoalTitle(currentMode)}"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        layout.addView(title)
        
        val description = TextView(this)
        description.text = GoalHelper.getGoalDescription(currentMode)
        description.textSize = 14f
        description.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        description.setPadding(0, 8, 0, 0)
        layout.addView(description)
        
        card.addView(layout)
        return card
    }
    
    private fun createNotificationCard(notification: SmartNotification): CardView {
        val card = CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
        }
        card.radius = 12f
        card.setCardElevation(2f)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        
        // Заголовок и переключатель
        val headerLayout = LinearLayout(this)
        headerLayout.orientation = LinearLayout.HORIZONTAL
        headerLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        
        val titleLayout = LinearLayout(this)
        titleLayout.orientation = LinearLayout.VERTICAL
        titleLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        
        val title = TextView(this)
        title.text = notification.title
        title.textSize = 16f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        titleLayout.addView(title)
        
        val body = TextView(this)
        body.text = notification.body
        body.textSize = 14f
        body.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        body.setPadding(0, 4, 0, 0)
        titleLayout.addView(body)
        
        val timeText = TextView(this)
        val timeString = String.format("%02d:%02d", notification.scheduledHour, notification.scheduledMinute)
        timeText.text = "Время: $timeString"
        timeText.textSize = 12f
        timeText.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        timeText.setPadding(0, 4, 0, 0)
        titleLayout.addView(timeText)
        
        headerLayout.addView(titleLayout)
        
        val switch = Switch(this)
        switch.isChecked = notification.isEnabled
        switch.setOnCheckedChangeListener { _, isChecked ->
            val updatedNotification = notification.copy(isEnabled = isChecked)
            userPreferences.updateNotification(updatedNotification)
            
            // Обновляем уведомление в системе
            val notificationHelper = NotificationHelper(this)
            if (isChecked) {
                notificationHelper.scheduleNotification(updatedNotification)
            } else {
                notificationHelper.cancelNotification(notification.id)
            }
        }
        headerLayout.addView(switch)
        
        layout.addView(headerLayout)
        card.addView(layout)
        
        return card
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
            val goal = userPreferences.getSelectedGoal()
            val intent = if (goal == UserGoal.MENOPAUSE) {
                Intent(this, MenopauseHomeActivity::class.java)
            } else {
                Intent(this, CalendarActivity::class.java)
            }
            startActivity(intent)
        }
        
        notificationsButton.setOnClickListener {
            // Уже на экране уведомлений
        }
        
        profileButton.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем список уведомлений при возврате на экран
        val newMode = userPreferences.getSelectedGoal()
        if (newMode != currentMode) {
            currentMode = newMode
            loadNotifications()
        }
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
