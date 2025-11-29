package com.example.womenhealthtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

/**
 * Вид недели с детальным планированием и энергетическим прогнозом
 */
class WeekViewActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var calendarPredictor: CalendarPredictor
    private lateinit var weekContainer: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_week_view)
        
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        calendarPredictor = CalendarPredictor(userPreferences)
        weekContainer = findViewById(R.id.weekContainer)
        
        loadWeekView()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun loadWeekView() {
        weekContainer.removeAllViews()
        
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Начинаем с понедельника текущей недели
        val daysFromMonday = (today - Calendar.MONDAY + 7) % 7
        calendar.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("d MMMM, EEEE", Locale("ru"))
        
        // Создаем карточки для каждого дня недели
        for (i in 0..6) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.time = calendar.time
            dayCalendar.add(Calendar.DAY_OF_YEAR, i)
            
            val dateString = dateFormat.format(dayCalendar.time)
            val displayDate = displayFormat.format(dayCalendar.time)
            val forecast = calendarPredictor.getForecast(dateString)
            val dayData = userPreferences.getDayData(dateString)
            
            val dayCard = createDayCard(displayDate, dateString, forecast, dayData, i == daysFromMonday)
            weekContainer.addView(dayCard)
        }
    }
    
    private fun createDayCard(
        displayDate: String,
        dateString: String,
        forecast: CalendarPredictor.DayForecast,
        dayData: DayData?,
        isToday: Boolean
    ): CardView {
        val card = CardView(this)
        card.radius = 12f
        card.setCardElevation(2f)
        card.setCardBackgroundColor(if (isToday) Color.parseColor("#FFF0F0") else Color.parseColor("#FFFFFF"))
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
        }
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        // Дата
        val dateTextView = TextView(this)
        dateTextView.text = displayDate
        dateTextView.textSize = 16f
        dateTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        dateTextView.setTextColor(Color.parseColor("#000000"))
        dateTextView.setPadding(0, 0, 0, 12)
        layout.addView(dateTextView)
        
        // Энергетический прогноз
        val energy = forecast.predictedEnergy ?: dayData?.energy
        if (energy != null) {
            val energyLayout = LinearLayout(this)
            energyLayout.orientation = LinearLayout.VERTICAL
            energyLayout.setPadding(0, 0, 0, 12)
            
            val energyLabel = TextView(this)
            energyLabel.text = "Энергия: $energy%"
            energyLabel.textSize = 14f
            energyLabel.setTextColor(Color.parseColor("#666666"))
            energyLayout.addView(energyLabel)
            
            val energyBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            energyBar.max = 100
            energyBar.progress = energy
            energyBar.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 4
            )
            energyBar.progressDrawable = getDrawable(R.drawable.progress_bar_pink)
            energyLayout.addView(energyBar)
            
            layout.addView(energyLayout)
        }
        
        // Прогнозы
        if (forecast.predictedPeriod || forecast.predictedOvulation || forecast.predictedPMS) {
            val forecastLayout = LinearLayout(this)
            forecastLayout.orientation = LinearLayout.VERTICAL
            forecastLayout.setPadding(0, 0, 0, 12)
            
            val forecastLabel = TextView(this)
            forecastLabel.text = "Прогнозы:"
            forecastLabel.textSize = 12f
            forecastLabel.setTextColor(Color.parseColor("#666666"))
            forecastLayout.addView(forecastLabel)
            
            if (forecast.predictedPeriod) {
                val periodText = TextView(this)
                periodText.text = "🔴 Возможное начало цикла"
                periodText.textSize = 12f
                periodText.setTextColor(Color.parseColor("#FF6B6B"))
                forecastLayout.addView(periodText)
            }
            if (forecast.predictedOvulation) {
                val ovText = TextView(this)
                ovText.text = "🥚 Овуляция"
                ovText.textSize = 12f
                ovText.setTextColor(Color.parseColor("#FFA500"))
                forecastLayout.addView(ovText)
            }
            if (forecast.predictedPMS) {
                val pmsText = TextView(this)
                pmsText.text = "💙 Возможен ПМС"
                pmsText.textSize = 12f
                pmsText.setTextColor(Color.parseColor("#4A90E2"))
                forecastLayout.addView(pmsText)
            }
            
            layout.addView(forecastLayout)
        }
        
        // Симптомы
        val symptoms = dayData?.symptoms ?: emptyList()
        if (symptoms.isNotEmpty()) {
            val symptomsText = TextView(this)
            symptomsText.text = "Симптомы: ${symptoms.joinToString(", ") { it.name }}"
            symptomsText.textSize = 12f
            symptomsText.setTextColor(Color.parseColor("#666666"))
            symptomsText.setPadding(0, 0, 0, 12)
            layout.addView(symptomsText)
        }
        
        // Кнопка редактирования
        val editButton = Button(this)
        editButton.text = "Редактировать"
        // textAllCaps устарело, используем трансформацию текста
        editButton.transformationMethod = null
        editButton.setBackgroundColor(Color.parseColor("#FFB6C1"))
        editButton.setTextColor(Color.parseColor("#000000"))
        editButton.setOnClickListener {
            val intent = Intent(this, FullCalendarActivity::class.java)
            intent.putExtra("selectedDate", dateString)
            startActivity(intent)
        }
        layout.addView(editButton)
        
        card.addView(layout)
        return card
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

