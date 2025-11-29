package com.example.womenhealthtracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.*

class MenopauseHomeActivity : AppCompatActivity() {
    
    private lateinit var supportMessageCard: CardView
    private lateinit var supportMessageText: TextView
    private lateinit var supportIcon: TextView
    private lateinit var tapHintText: TextView
    private lateinit var addSymptomsButton: Button
    private lateinit var doctorVisitButton: Button
    private lateinit var statsButton: Button
    private lateinit var currentDateTextView: TextView
    private lateinit var daysContainer: LinearLayout
    private lateinit var userPreferences: UserPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_home)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        initViews()
        setupSupportWidget()
        setupButtons()
        setupCalendar()
        setupBottomNavigation()
    }
    
    private fun initViews() {
        supportMessageCard = findViewById(R.id.supportMessageCard)
        supportMessageText = findViewById(R.id.supportMessageText)
        supportIcon = findViewById(R.id.supportIcon)
        tapHintText = findViewById(R.id.tapHintText)
        addSymptomsButton = findViewById(R.id.addSymptomsButton)
        doctorVisitButton = findViewById(R.id.doctorVisitButton)
        statsButton = findViewById(R.id.statsButton)
        currentDateTextView = findViewById(R.id.currentDateTextView)
        daysContainer = findViewById(R.id.daysContainer)
    }
    
    private fun setupSupportWidget() {
        // Устанавливаем случайное сообщение поддержки
        updateSupportMessage()
        
        // При нажатии на карточку меняем сообщение
        supportMessageCard.setOnClickListener {
            updateSupportMessage()
        }
    }
    
    private fun updateSupportMessage() {
        val message = SupportMessageHelper.getRandomMessage()
        supportMessageText.text = message
        
        // Меняем эмодзи в зависимости от сообщения
        val emoji = when {
            message.contains("💖") -> "💖"
            message.contains("🌺") -> "🌺"
            message.contains("🌟") -> "🌟"
            message.contains("💪") -> "💪"
            message.contains("🌼") -> "🌼"
            message.contains("✨") -> "✨"
            message.contains("🌷") -> "🌷"
            message.contains("💝") -> "💝"
            message.contains("🦋") -> "🦋"
            message.contains("🌞") -> "🌞"
            message.contains("🎀") -> "🎀"
            message.contains("🌹") -> "🌹"
            message.contains("🌸") -> "🌸"
            message.contains("💐") -> "💐"
            message.contains("🌻") -> "🌻"
            message.contains("🌿") -> "🌿"
            else -> "💖"
        }
        supportIcon.text = emoji
    }
    
    private fun setupButtons() {
        addSymptomsButton.setOnClickListener {
            val intent = Intent(this, MenopauseSymptomsActivity::class.java)
            startActivity(intent)
        }
        
        doctorVisitButton.setOnClickListener {
            val intent = Intent(this, MenopauseDoctorVisitBookingActivity::class.java)
            startActivity(intent)
        }
        
        statsButton.setOnClickListener {
            val intent = Intent(this, MenopauseStatisticsFullActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun setupCalendar() {
        // Установка текущей даты
        val dateFormat = SimpleDateFormat("d MMMM", Locale("ru"))
        val currentDate = dateFormat.format(Date())
        currentDateTextView.text = currentDate
        
        // Очистка контейнера дней
        daysContainer.removeAllViews()
        
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Добавляем дни текущего месяца (начиная с текущего дня)
        val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in currentDay..daysInCurrentMonth) {
            val dayView = createDayView(day, currentMonth, currentYear, day == currentDay)
            daysContainer.addView(dayView)
        }
        
        // Добавляем дни следующих 2 месяцев
        for (monthOffset in 1..2) {
            val nextMonthCalendar = Calendar.getInstance()
            nextMonthCalendar.set(currentYear, currentMonth, 1)
            nextMonthCalendar.add(Calendar.MONTH, monthOffset)
            
            val nextMonth = nextMonthCalendar.get(Calendar.MONTH)
            val nextYear = nextMonthCalendar.get(Calendar.YEAR)
            val daysInNextMonth = nextMonthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // Добавляем заголовок месяца
            val monthHeader = TextView(this)
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
            monthHeader.text = monthFormat.format(nextMonthCalendar.time)
            monthHeader.textSize = 18f
            monthHeader.setTypeface(null, Typeface.BOLD)
            monthHeader.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            monthHeader.setPadding(16, 24, 16, 8)
            monthHeader.gravity = android.view.Gravity.CENTER
            daysContainer.addView(monthHeader)
            
            // Добавляем все дни следующего месяца
            for (day in 1..daysInNextMonth) {
                val dayView = createDayView(day, nextMonth, nextYear, false)
                daysContainer.addView(dayView)
            }
        }
    }
    
    private fun createDayView(day: Int, month: Int, year: Int, isSelected: Boolean): TextView {
        val dayTextView = TextView(this)
        
        // Формируем дату для проверки наличия симптомов
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateString = dateFormat.format(calendar.time)
        
        // Проверяем, есть ли симптомы в этот день
        val record = userPreferences.getMenopauseDayRecord(dateString)
        val hasSymptoms = record != null && record.symptoms.isNotEmpty()
        
        // Создаем круглый фон
        val backgroundDrawable = when {
            hasSymptoms -> ContextCompat.getDrawable(this, R.drawable.day_circle_with_symptoms)
            isSelected -> ContextCompat.getDrawable(this, R.drawable.day_circle_selected)
            else -> ContextCompat.getDrawable(this, R.drawable.day_circle_pink) ?: 
                    ContextCompat.getDrawable(this, R.drawable.day_circle_selected)
        }
        
        dayTextView.background = backgroundDrawable
        dayTextView.text = day.toString()
        dayTextView.textSize = 18f
        dayTextView.gravity = android.view.Gravity.CENTER
        dayTextView.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
        
        val size = (72 * resources.displayMetrics.density).toInt()
        val layoutParams = LinearLayout.LayoutParams(size, size)
        layoutParams.setMargins(12, 0, 12, 0)
        dayTextView.layoutParams = layoutParams
        
        // При нажатии открываем экран симптомов
        dayTextView.setOnClickListener {
            val intent = Intent(this, MenopauseSymptomsActivity::class.java)
            intent.putExtra("selected_date", dateString)
            startActivity(intent)
        }
        
        return dayTextView
    }
    
    private fun setupBottomNavigation() {
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val calendarButton = findViewById<ImageButton>(R.id.calendarButton)
        val notificationsButton = findViewById<ImageButton>(R.id.notificationsButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        
        settingsButton?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        calendarButton?.setOnClickListener {
            // Уже на главном экране менопаузы
        }
        
        notificationsButton?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        profileButton?.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем сообщение поддержки при возврате на экран
        updateSupportMessage()
        // Обновляем календарь, чтобы показать новые данные
        setupCalendar()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

