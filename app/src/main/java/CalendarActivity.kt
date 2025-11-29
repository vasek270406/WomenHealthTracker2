package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {
    
    private var selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var calendarPredictor: CalendarPredictor
    private lateinit var smartNotificationScheduler: SmartNotificationScheduler
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        
        // Создаем и настраиваем Toolbar с кнопкой назад
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        calendarPredictor = CalendarPredictor(userPreferences)
        smartNotificationScheduler = SmartNotificationScheduler(this)
        
        // Проверяем автоматическое распознавание начала цикла
        checkAutoPeriodDetection()
        
        // Планируем умные уведомления
        smartNotificationScheduler.scheduleSmartNotifications()
        
        // Проверяем, завершен ли онбординг
        if (!userPreferences.isOnboardingCompleted()) {
            // Если онбординг не завершен, переходим на экран аккаунта
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        // Проверяем режим менопаузы - перенаправляем на специальный экран
        val goal = userPreferences.getSelectedGoal()
        if (goal == UserGoal.MENOPAUSE) {
            val intent = Intent(this, MenopauseHomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        setupGreeting()
        setupCalendar()
        setupButtons()
        setupBottomNavigation()
        updateCycleInfo()
        updateButtonLabels()
        checkForDelay()
    }
    
    override fun onResume() {
        super.onResume()
        checkForDelay()
        // Обновляем умные уведомления при возврате на экран
        smartNotificationScheduler.updateSmartNotifications()
    }
    
    /**
     * Проверяет наличие задержки и показывает виджет/уведомление
     */
    private fun checkForDelay() {
        // Проверяем только для режима отслеживания цикла
        val goal = userPreferences.getSelectedGoal()
        if (goal != UserGoal.CYCLE_TRACKING) {
            return
        }
        
        // Проверяем, есть ли уже активная задержка
        val activeDelay = userPreferences.getActiveDelay()
        if (activeDelay != null) {
            showDelayWidget(activeDelay.delayDays)
            return
        }
        
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val cycleLength = userPreferences.getCycleLength()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            return
        }
        
        val delayAnalyzer = DelayAnalyzer()
        val expectedDate = delayAnalyzer.calculateExpectedPeriodDate(lastPeriodStart, cycleLength)
        
        if (expectedDate != null) {
            val delayDays = delayAnalyzer.calculateDelayDays(expectedDate)
            
            // Если задержка больше 1 дня (средняя длина цикла + 1 день)
            if (delayDays > 1) {
                showDelayWidget(delayDays)
            }
        }
    }
    
    /**
     * Показывает виджет задержки на главном экране
     */
    private fun showDelayWidget(delayDays: Int) {
        // Ищем контейнер для виджета задержки в layout
        val delayWidgetContainer = findViewById<LinearLayout>(R.id.delayWidgetContainer)
        if (delayWidgetContainer == null) {
            // Если контейнера нет, добавляем его программно
            // Но лучше добавить в layout
            return
        }
        
        delayWidgetContainer.removeAllViews()
        delayWidgetContainer.visibility = View.VISIBLE
        
        val card = androidx.cardview.widget.CardView(this)
        card.radius = 12f
        card.setCardElevation(2f)
        card.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF0F0"))
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(24, 16, 24, 16)
        }
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(20, 20, 20, 20)
        layout.gravity = android.view.Gravity.CENTER_VERTICAL
        
        val icon = TextView(this)
        icon.text = "⚠️"
        icon.textSize = 24f
        icon.setPadding(0, 0, 16, 0)
        layout.addView(icon)
        
        val textLayout = LinearLayout(this)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        
        val title = TextView(this)
        title.text = "Задержка: $delayDays ${getDayWord(delayDays)}"
        title.textSize = 16f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(android.graphics.Color.parseColor("#000000"))
        textLayout.addView(title)
        
        val subtitle = TextView(this)
        subtitle.text = "Нажмите для анализа"
        subtitle.textSize = 12f
        subtitle.setTextColor(android.graphics.Color.parseColor("#666666"))
        textLayout.addView(subtitle)
        
        layout.addView(textLayout)
        
        card.setOnClickListener {
            val intent = Intent(this, DelayAnalysisActivity::class.java)
            intent.putExtra("delayDays", delayDays)
            startActivity(intent)
        }
        
        card.addView(layout)
        delayWidgetContainer.addView(card)
    }
    
    private fun getDayWord(days: Int): String {
        return when {
            days % 10 == 1 && days % 100 != 11 -> "день"
            days % 10 in 2..4 && days % 100 !in 12..14 -> "дня"
            else -> "дней"
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    /**
     * Обновить тексты кнопок в зависимости от режима
     */
    private fun updateButtonLabels() {
        val goal = userPreferences.getSelectedGoal()
        val markStartButton = findViewById<Button>(R.id.markStartButton)
        val quickAddButton = findViewById<Button>(R.id.quickAddButton)
        
        when (goal) {
            UserGoal.PREGNANCY -> {
                markStartButton.text = "Шевеления 👶"
                quickAddButton.text = "Симптомы беременности"
            }
            else -> {
                markStartButton.text = "Отметить начало"
                quickAddButton.text = "Быстрое добавление"
            }
        }
    }
    
    private fun setupGreeting() {
        val greetingTextView = findViewById<TextView>(R.id.greetingTextView)
        val name = userPreferences.getName()
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when {
            hour in 5..11 -> "Доброе утро"
            hour in 12..17 -> "Добрый день"
            hour in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
        
        if (name.isNotEmpty()) {
            greetingTextView.text = "$greeting, $name!"
        } else {
            greetingTextView.text = "$greeting!"
        }
    }
    
    private fun setupCalendar() {
        val daysContainer = findViewById<LinearLayout>(R.id.daysContainer)
        val currentDateTextView = findViewById<TextView>(R.id.currentDateTextView)
        
        // Установка текущей даты
        val dateFormat = SimpleDateFormat("d MMMM", Locale("ru"))
        val currentDate = dateFormat.format(Date())
        currentDateTextView.text = currentDate
        
        // Получение текущего дня месяца
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        selectedDay = currentDay
        
        daysContainer.removeAllViews()
        
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
        
        // Фиксированные размеры для кружков
        val circleSize = 72
        val layoutParams = LinearLayout.LayoutParams(
            circleSize,
            circleSize
        )
        layoutParams.setMargins(12, 0, 12, 0)
        dayTextView.layoutParams = layoutParams
        
        dayTextView.text = day.toString()
        dayTextView.textSize = 18f
        dayTextView.setTypeface(null, Typeface.BOLD)
        dayTextView.gravity = android.view.Gravity.CENTER
        dayTextView.setPadding(0, 0, 0, 0)
        
        // Убеждаемся, что текст не выходит за границы
        dayTextView.maxLines = 1
        dayTextView.ellipsize = android.text.TextUtils.TruncateAt.END
        
        // Проверяем, является ли это сегодняшним днем
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val todayMonth = calendar.get(Calendar.MONTH)
        val todayYear = calendar.get(Calendar.YEAR)
        val isToday = (day == today && month == todayMonth && year == todayYear)
        
        // Получаем данные о циклах для цветовой индикации
        val periodDates = userPreferences.getPeriodDates()
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        // Формируем дату для проверки
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayCalendar = Calendar.getInstance()
        dayCalendar.set(year, month, day)
        val dateString = dateFormat.format(dayCalendar.time)
        
        // Получаем прогноз для дня
        val forecast = calendarPredictor.getForecast(dateString)
        
        // Определяем тип дня для цветовой индикации
        val dayType = getDayTypeForCalendar(dateString, periodDates, lastPeriodStart, cycleLength, isToday)
        
        // Улучшенное цветовое кодирование с учетом прогнозов
        when {
            // Подтвержденные дни менструации
            dayType == CalendarDayType.CURRENT_PERIOD -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_period_current)
            }
            dayType == CalendarDayType.PREVIOUS_PERIOD -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_period_previous)
            }
            // Прогнозируемое начало цикла (полупрозрачный индикатор)
            forecast.predictedPeriod && !isToday -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_period_previous)
                dayTextView.alpha = 0.5f
                // Добавляем иконку прогноза
                dayTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, android.R.drawable.ic_menu_recent_history)
            }
            // Овуляция
            dayType == CalendarDayType.OVULATION || forecast.predictedOvulation -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_ovulation)
                if (forecast.predictedOvulation && !isToday) {
                    dayTextView.alpha = 0.7f
                }
            }
            // ПМС
            forecast.predictedPMS -> {
                // Синий/голубой для ПМС (используем существующий drawable или создаем новый)
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_luteal)
                dayTextView.alpha = 0.6f
            }
            dayType == CalendarDayType.LUTEAL -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_luteal)
            }
            dayType == CalendarDayType.TODAY -> {
                dayTextView.background = ContextCompat.getDrawable(this, R.drawable.day_circle_selected)
            }
            // Дни с данными - зеленая рамка
            forecast.hasData -> {
                dayTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                // Добавляем зеленую рамку программно
                dayTextView.setPadding(4, 4, 4, 4)
                dayTextView.background = ContextCompat.getDrawable(this, android.R.drawable.dialog_holo_light_frame)
            }
            else -> {
                dayTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
        }
        
        // Сохраняем данные в tag для использования в обработчиках
        dayTextView.tag = mapOf(
            "date" to dateString,
            "day" to day,
            "month" to month,
            "year" to year,
            "forecast" to forecast,
            "dayType" to dayType
        )
        
        // Умный тап: короткое нажатие - быстрая карточка, долгое - детальное редактирование
        var longPressHandled = false
        dayTextView.setOnLongClickListener {
            longPressHandled = true
            showDayDetailEditor(day, month, year, dateString, forecast)
            true
        }
        
        dayTextView.setOnClickListener {
            // Небольшая задержка, чтобы отличить от долгого нажатия
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (!longPressHandled) {
                    showQuickDayCard(day, month, year, dateString, forecast, dayType)
                }
                longPressHandled = false
            }, 200)
        }
        
        return dayTextView
    }
    
    private enum class CalendarDayType {
        CURRENT_PERIOD,
        PREVIOUS_PERIOD,
        OVULATION,
        LUTEAL,
        TODAY,
        NORMAL
    }
    
    private fun getDayTypeForCalendar(
        dateString: String,
        periodDates: Set<String>,
        lastPeriodStart: String,
        cycleLength: Int,
        isToday: Boolean
    ): CalendarDayType {
        if (isToday) {
            return CalendarDayType.TODAY
        }
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            if (periodDates.contains(dateString)) {
                return CalendarDayType.CURRENT_PERIOD
            }
            return CalendarDayType.NORMAL
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDateCalendar = Calendar.getInstance()
        val lastPeriodCalendar = Calendar.getInstance()
        
        try {
            currentDateCalendar.time = dateFormat.parse(dateString) ?: return CalendarDayType.NORMAL
            lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return CalendarDayType.NORMAL
        } catch (e: Exception) {
            return CalendarDayType.NORMAL
        }
        
        val daysDiff = ((currentDateCalendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        val dayOfCycle = ((daysDiff % cycleLength) + cycleLength) % cycleLength
        
        val periodDuration = 5
        val ovulationDay = 14
        val lutealStartDay = ovulationDay + 1
        val lutealEndDay = cycleLength - 1
        
        val isInSavedPeriod = periodDates.contains(dateString)
        val isCalculatedPeriod = dayOfCycle < periodDuration
        
        if (isInSavedPeriod || isCalculatedPeriod) {
            if (daysDiff >= 0 && daysDiff < cycleLength) {
                return CalendarDayType.CURRENT_PERIOD
            } else {
                return CalendarDayType.PREVIOUS_PERIOD
            }
        }
        
        if (daysDiff >= 0) {
            when {
                dayOfCycle in (ovulationDay - 2)..(ovulationDay + 2) -> {
                    return CalendarDayType.OVULATION
                }
                dayOfCycle in lutealStartDay..lutealEndDay -> {
                    return CalendarDayType.LUTEAL
                }
            }
        }
        
        if (daysDiff < 0) {
            val prevCycleDay = ((daysDiff % cycleLength) + cycleLength) % cycleLength
            when {
                prevCycleDay in (ovulationDay - 2)..(ovulationDay + 2) -> {
                    return CalendarDayType.OVULATION
                }
                prevCycleDay in lutealStartDay..lutealEndDay -> {
                    return CalendarDayType.LUTEAL
                }
            }
        }
        
        return CalendarDayType.NORMAL
    }
    
    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)
    
    /**
     * Проверка автоматического распознавания начала цикла
     */
    private fun checkAutoPeriodDetection() {
        val detectedDate = calendarPredictor.detectPeriodStart()
        if (detectedDate != null) {
            // Показываем диалог с предложением отметить начало цикла
            android.app.AlertDialog.Builder(this)
                .setTitle("Обнаружено начало цикла")
                .setMessage("Похоже, у вас начался цикл. Хотите отметить его начало?")
                .setPositiveButton("Да") { _, _ ->
                    userPreferences.savePeriodStartDate(detectedDate)
                    Toast.makeText(this, "Начало цикла отмечено", Toast.LENGTH_SHORT).show()
                    // Обновляем календарь
                    setupCalendar()
                    // Обновляем умные уведомления
                    smartNotificationScheduler.updateSmartNotifications()
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }
    
    /**
     * Быстрая карточка дня (короткое нажатие)
     */
    private fun showQuickDayCard(day: Int, month: Int, year: Int, dateString: String, forecast: CalendarPredictor.DayForecast, dayType: CalendarDayType) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val dateDisplay = dayFormat.format(calendar.time)
        
        val dialogView = layoutInflater.inflate(R.layout.quick_day_card, null)
        
        val dateTextView = dialogView.findViewById<TextView>(R.id.dateTextView)
        val forecastTextView = dialogView.findViewById<TextView>(R.id.forecastTextView)
        val energyTextView = dialogView.findViewById<TextView>(R.id.energyTextView)
        val symptomsTextView = dialogView.findViewById<TextView>(R.id.symptomsTextView)
        val addSymptomsButton = dialogView.findViewById<Button>(R.id.addSymptomsButton)
        val addPeriodButton = dialogView.findViewById<Button>(R.id.addPeriodButton)
        val addSexButton = dialogView.findViewById<Button>(R.id.addSexButton)
        val viewDetailsButton = dialogView.findViewById<Button>(R.id.viewDetailsButton)
        
        dateTextView.text = dateDisplay
        
        // Прогнозы
        val forecastText = buildString {
            if (forecast.predictedPeriod) append("🔴 Прогноз начала цикла\n")
            if (forecast.predictedOvulation) append("🥚 Прогноз овуляции\n")
            if (forecast.predictedPMS) append("💙 Возможен ПМС\n")
            if (forecast.symptoms.isNotEmpty()) {
                append("Симптомы: ${forecast.symptoms.joinToString(", ")}\n")
            }
        }
        forecastTextView.text = forecastText.ifEmpty { "Нет прогнозов" }
        
        // Энергия
        energyTextView.text = if (forecast.predictedEnergy != null) {
            "Прогнозируемая энергия: ${forecast.predictedEnergy}%"
        } else {
            "Энергия не прогнозируется"
        }
        
        // Симптомы
        val dayData = userPreferences.getDayData(dateString)
        symptomsTextView.text = if (dayData?.symptoms?.isNotEmpty() == true) {
            dayData.symptoms.joinToString(", ") { it.name }
        } else {
            "Симптомы не отмечены"
        }
        
        // Кнопки быстрых действий
        addSymptomsButton.setOnClickListener {
            val intent = Intent(this, SymptomTrackerActivity::class.java)
            intent.putExtra("date", dateString)
            startActivity(intent)
        }
        
        addPeriodButton.setOnClickListener {
            userPreferences.savePeriodStartDate(dateString)
            Toast.makeText(this, "Начало цикла отмечено", Toast.LENGTH_SHORT).show()
            // Обновляем умные уведомления
            smartNotificationScheduler.updateSmartNotifications()
        }
        
        addSexButton.setOnClickListener {
            val dayData = userPreferences.getDayData(dateString) ?: DayData(dateString)
            val updatedDayData = dayData.copy(sexualActivity = true)
            userPreferences.saveDayData(updatedDayData)
            Toast.makeText(this, "Половой акт отмечен", Toast.LENGTH_SHORT).show()
        }
        
        viewDetailsButton.setOnClickListener {
            showDayDetailEditor(day, month, year, dateString, forecast)
        }
        
        android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Закрыть", null)
            .show()
    }
    
    /**
     * Детальный редактор дня (долгое нажатие)
     */
    private fun showDayDetailEditor(day: Int, month: Int, year: Int, dateString: String, forecast: CalendarPredictor.DayForecast) {
        val intent = Intent(this, FullCalendarActivity::class.java)
        intent.putExtra("selectedDate", dateString)
        startActivity(intent)
    }
    
    private fun showDayForecastDialog(day: Int, month: Int, year: Int, dateString: String, dayType: CalendarDayType) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dayFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("ru"))
        val dateDisplay = dayFormat.format(calendar.time)
        
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val periodDates = userPreferences.getPeriodDates()
        
        val message = buildString {
            append("$dateDisplay\n\n")
            
            if (lastPeriodStart.isEmpty() || cycleLength == 0) {
                append("Настройте цикл для получения прогноза")
            } else {
                try {
                    val lastPeriodCalendar = Calendar.getInstance()
                    lastPeriodCalendar.time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastPeriodStart) ?: return
                    val daysDiff = ((calendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val cycleDay = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1
                    
                    append("День цикла: $cycleDay\n\n")
                    
                    val (phase, probability, description) = when {
                        cycleDay in 1..5 -> Triple("Менструация", "Низкая", "Вероятность забеременеть очень низкая")
                        cycleDay in 6..14 -> Triple("Фолликулярная фаза", "Средняя", "Вероятность забеременеть средняя")
                        cycleDay in 15..17 -> Triple("Овуляция", "Высокая", "Вероятность забеременеть высокая")
                        cycleDay in 18..(cycleLength - 2) -> Triple("Лютеиновая фаза", "Низкая", "Вероятность забеременеть низкая")
                        else -> Triple("Предменструальная фаза", "Низкая", "Вероятность забеременеть очень низкая")
                    }
                    
                    append("Фаза: $phase\n")
                    append("Вероятность беременности: $probability\n")
                    append("$description\n\n")
                    
                    // Прогноз следующих событий
                    val nextPeriodCalendar = Calendar.getInstance()
                    nextPeriodCalendar.time = lastPeriodCalendar.time
                    nextPeriodCalendar.add(Calendar.DAY_OF_MONTH, cycleLength)
                    
                    val nextOvulationCalendar = Calendar.getInstance()
                    nextOvulationCalendar.time = lastPeriodCalendar.time
                    val ovulationDay = 14
                    nextOvulationCalendar.add(Calendar.DAY_OF_MONTH, cycleLength - cycleLength + ovulationDay)
                    
                    val dateFormatDisplay = SimpleDateFormat("d MMMM", Locale("ru"))
                    if (calendar.before(nextPeriodCalendar)) {
                        append("Следующая менструация: ${dateFormatDisplay.format(nextPeriodCalendar.time)}\n")
                    }
                    if (calendar.before(nextOvulationCalendar) && cycleDay < ovulationDay) {
                        append("Овуляция: ${dateFormatDisplay.format(nextOvulationCalendar.time)}\n")
                    }
                } catch (e: Exception) {
                    append("Ошибка расчета прогноза")
                }
            }
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("Прогноз на день")
            .setMessage(message)
            .setPositiveButton("ОК", null)
            .show()
    }
    
    private fun updateSelectedDay(day: Int, month: Int, year: Int) {
        val daysContainer = findViewById<LinearLayout>(R.id.daysContainer)
        selectedDay = day
        selectedMonth = month
        selectedYear = year
        
        // Обновление визуального состояния всех дней
        // Проходим по всем элементам контейнера
        for (i in 0 until daysContainer.childCount) {
            val view = daysContainer.getChildAt(i)
            if (view is TextView) {
                val dayNumber = view.text.toString().toIntOrNull()
                // Пропускаем заголовки месяцев (они не числа)
                if (dayNumber != null) {
                    // Проверяем, является ли это выбранным днем
                    // Для упрощения проверяем только номер дня, так как у нас нет сохранения месяца/года в view
                    // В реальном приложении можно использовать tag для хранения полной даты
                }
            }
        }
    }
    
    private fun setupButtons() {
        // Кнопка переключения на вид недели
        val weekViewButton = findViewById<Button>(R.id.weekViewButton)
        weekViewButton?.setOnClickListener {
            val intent = Intent(this, WeekViewActivity::class.java)
            startActivity(intent)
        }
        
        val markStartButton = findViewById<Button>(R.id.markStartButton)
        val quickAddButton = findViewById<Button>(R.id.quickAddButton)
        val moodButton = findViewById<Button>(R.id.moodButton)
        val sleepButton = findViewById<Button>(R.id.sleepButton)
        val waterButton = findViewById<Button>(R.id.waterButton)
        val symptomsButton = findViewById<Button>(R.id.symptomsButton)
        
        // Адаптируем кнопки в зависимости от режима
        val goal = userPreferences.getSelectedGoal()
        when (goal) {
            UserGoal.PREGNANCY -> {
                markStartButton.text = "Шевеления 👶"
                quickAddButton.text = "Симптомы беременности"
                // Скрываем кнопки быстрого доступа в режиме беременности
                moodButton?.visibility = android.view.View.GONE
                sleepButton?.visibility = android.view.View.GONE
                waterButton?.visibility = android.view.View.GONE
                symptomsButton?.visibility = android.view.View.GONE
            }
            else -> {
                markStartButton.text = "Отметить начало"
                quickAddButton.text = "Быстрое добавление"
                // Показываем кнопки быстрого доступа
                moodButton?.visibility = android.view.View.VISIBLE
                sleepButton?.visibility = android.view.View.VISIBLE
                waterButton?.visibility = android.view.View.VISIBLE
                symptomsButton?.visibility = android.view.View.VISIBLE
            }
        }
        
        markStartButton.setOnClickListener {
            val goal = userPreferences.getSelectedGoal()
            when (goal) {
                UserGoal.PREGNANCY -> {
                    // Для режима беременности - открыть счетчик шевелений
                    val intent = Intent(this, KickCounterActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    // Отметка начала цикла
                    val today = Calendar.getInstance()
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val todayString = dateFormat.format(today.time)
                    userPreferences.savePeriodStartDate(todayString)
                    android.widget.Toast.makeText(this, "Начало цикла отмечено", android.widget.Toast.LENGTH_SHORT).show()
                    updateCycleInfo()
                    // Обновляем умные уведомления
                    smartNotificationScheduler.updateSmartNotifications()
                }
            }
        }
        
        symptomsButton.setOnClickListener {
            // Переход на экран трекера симптомов
            val intent = Intent(this, SymptomTrackerActivity::class.java)
            startActivity(intent)
        }
        
        quickAddButton.setOnClickListener {
            val goal = userPreferences.getSelectedGoal()
            if (goal == UserGoal.PREGNANCY) {
                // Для режима беременности - открыть экран симптомов беременности
                val intent = Intent(this, PregnancySymptomsActivity::class.java)
                startActivity(intent)
            } else {
                // Переход на экран быстрого добавления
                val intent = Intent(this, QuickAddActivity::class.java)
                startActivity(intent)
            }
        }
        
        moodButton.setOnClickListener {
            // Переход на быстрое добавление с фокусом на настроение
            val intent = Intent(this, QuickAddActivity::class.java)
            startActivity(intent)
        }
        
        sleepButton.setOnClickListener {
            // Переход на трекер активности
            val intent = Intent(this, ActivityTrackerActivity::class.java)
            startActivity(intent)
        }
        
        waterButton.setOnClickListener {
            val goal = userPreferences.getSelectedGoal()
            if (goal == UserGoal.PREGNANCY) {
                // Для режима беременности - открыть экран фото УЗИ
                val intent = Intent(this, UltrasoundActivity::class.java)
                startActivity(intent)
            } else {
                // Переход на трекер привычек
                val intent = Intent(this, HabitsTrackerActivity::class.java)
                startActivity(intent)
            }
        }
        
        // Добавляем обработчик для кнопки настроения в режиме беременности
        moodButton.setOnClickListener {
            val goal = userPreferences.getSelectedGoal()
            if (goal == UserGoal.PREGNANCY) {
                // Для режима беременности - открыть экран визита к врачу
                val intent = Intent(this, DoctorVisitActivity::class.java)
                startActivity(intent)
            } else {
                // Переход на быстрое добавление с фокусом на настроение
                val intent = Intent(this, QuickAddActivity::class.java)
                startActivity(intent)
            }
        }
        
        // Настройка панели быстрых действий для беременности
        setupPregnancyQuickActions()
        
        // Установка информации о цикле (можно получать из сохраненных данных)
        updateCycleInfo()
    }
    
    private fun setupPregnancyQuickActions() {
        val quickActionsContainer = findViewById<LinearLayout>(R.id.pregnancyQuickActionsContainer)
        val doctorVisitButton = findViewById<Button>(R.id.doctorVisitCard)
        val symptomsButton = findViewById<Button>(R.id.symptomsCard)
        val ultrasoundButton = findViewById<Button>(R.id.ultrasoundCard)
        
        val goal = userPreferences.getSelectedGoal()
        
        // Показываем панель только в режиме беременности
        if (goal == UserGoal.PREGNANCY) {
            quickActionsContainer?.visibility = android.view.View.VISIBLE
            
            // Визит к врачу
            doctorVisitButton?.setOnClickListener {
                val intent = Intent(this, DoctorVisitActivity::class.java)
                startActivity(intent)
            }
            
            // Симптомы
            symptomsButton?.setOnClickListener {
                val intent = Intent(this, PregnancySymptomsActivity::class.java)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                intent.putExtra("date", dateFormat.format(Date()))
                startActivity(intent)
            }
            
            // УЗИ
            ultrasoundButton?.setOnClickListener {
                val intent = Intent(this, UltrasoundActivity::class.java)
                startActivity(intent)
            }
        } else {
            quickActionsContainer?.visibility = android.view.View.GONE
        }
    }
    
    private fun updateCycleInfo() {
        // Обновляем панель быстрых действий
        setupPregnancyQuickActions()
        
        val cycleDayTextView = findViewById<TextView>(R.id.cycleDayTextView)
        val pregnancyProbabilityTextView = findViewById<TextView>(R.id.pregnancyProbabilityTextView)
        val cycleDayLabelTextView = findViewById<TextView>(R.id.cycleDayLabelTextView)
        val babySizeCard = findViewById<CardView>(R.id.babySizeCard)
        
        // Обновляем информацию в зависимости от режима
        val goal = userPreferences.getSelectedGoal()
        when (goal) {
            UserGoal.PREGNANCY -> {
                // Показываем виджет размера малыша
                babySizeCard?.visibility = android.view.View.VISIBLE
                cycleDayTextView.visibility = android.view.View.GONE
                cycleDayLabelTextView.visibility = android.view.View.GONE
                pregnancyProbabilityTextView.visibility = android.view.View.GONE
                
                val pregnancyStartDate = userPreferences.getPregnancyStartDate()
                if (pregnancyStartDate.isNotEmpty()) {
                    try {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val startCalendar = Calendar.getInstance()
                        startCalendar.time = dateFormat.parse(pregnancyStartDate) ?: return
                        val currentCalendar = Calendar.getInstance()
                        val daysDiff = ((currentCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                        val week = if (daysDiff >= 0) (daysDiff / 7) + 1 else 1
                        
                        // Обновляем виджет размера малыша
                        updateBabySizeWidget(week)
                    } catch (e: Exception) {
                        babySizeCard?.visibility = android.view.View.GONE
                    }
                } else {
                    babySizeCard?.visibility = android.view.View.GONE
                }
            }
            UserGoal.MENOPAUSE -> {
                babySizeCard?.visibility = android.view.View.GONE
                cycleDayTextView.visibility = android.view.View.VISIBLE
                cycleDayLabelTextView.visibility = android.view.View.VISIBLE
                pregnancyProbabilityTextView.visibility = android.view.View.VISIBLE
                pregnancyProbabilityTextView.text = "Менопауза"
            }
            else -> {
                // Базовый режим отслеживания цикла
                babySizeCard?.visibility = android.view.View.GONE
                cycleDayTextView.visibility = android.view.View.VISIBLE
                cycleDayLabelTextView.visibility = android.view.View.VISIBLE
                pregnancyProbabilityTextView.visibility = android.view.View.VISIBLE
                
                // Используем CalendarModeHelper для адаптивной информации
                val modeHelper = CalendarModeHelper(userPreferences)
                val cycleInfoText = modeHelper.getCycleInfoText()
                cycleDayTextView.text = cycleInfoText
                
                val cycleLength = userPreferences.getCycleLength()
                val lastPeriodStart = userPreferences.getLastPeriodStart()
                
                if (lastPeriodStart.isEmpty() || cycleLength == 0) {
                    pregnancyProbabilityTextView.text = ""
                    return
                }
                
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val lastPeriodCalendar = Calendar.getInstance()
                    lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return
                    val daysDiff = ((calendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val cycleDay = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1
                    
                    val (probability, phase) = when {
                        cycleDay in 1..5 -> "низкая вероятность забеременнеть" to "Менструация"
                        cycleDay in 6..14 -> "средняя вероятность забеременнеть" to "Фолликулярная фаза"
                        cycleDay in 15..17 -> "высокая вероятность забеременнеть" to "Овуляция"
                        cycleDay in 18..(cycleLength - 2) -> "низкая вероятность забеременнеть" to "Лютеиновая фаза"
                        else -> "низкая вероятность забеременнеть" to "Предменструальная фаза"
                    }
                    
                    val spannableString = SpannableString(probability)
                    spannableString.setSpan(UnderlineSpan(), 0, probability.length, 0)
                    pregnancyProbabilityTextView.text = spannableString
                } catch (e: Exception) {
                    pregnancyProbabilityTextView.text = ""
                }
            }
        }
    }
    
    /**
     * Обновить виджет размера малыша
     */
    private fun updateBabySizeWidget(week: Int) {
        val babySizeWeekTextView = findViewById<TextView>(R.id.babySizeWeekTextView)
        val babySizeEmojiTextView = findViewById<TextView>(R.id.babySizeEmojiTextView)
        val babySizeFruitTextView = findViewById<TextView>(R.id.babySizeFruitTextView)
        val babySizeCmTextView = findViewById<TextView>(R.id.babySizeCmTextView)
        val babySizeDescriptionTextView = findViewById<TextView>(R.id.babySizeDescriptionTextView)
        val babySizeProgressBar = findViewById<ProgressBar>(R.id.babySizeProgressBar)
        val babySizeNextTextView = findViewById<TextView>(R.id.babySizeNextTextView)
        
        val currentSize = BabySizeHelper.getBabySizeForWeek(week)
        val nextSize = BabySizeHelper.getNextBabySize(week)
        val progress = BabySizeHelper.getProgressToNext(week)
        
        babySizeWeekTextView?.text = "Неделя $week"
        babySizeEmojiTextView?.text = currentSize.emoji
        babySizeEmojiTextView?.visibility = android.view.View.VISIBLE
        babySizeFruitTextView?.text = "Размером с ${currentSize.fruit}"
        babySizeCmTextView?.text = "${currentSize.sizeCm} см"
        babySizeDescriptionTextView?.text = currentSize.description
        
        babySizeProgressBar?.progress = (progress * 100).toInt()
        
        if (nextSize != null) {
            babySizeNextTextView?.text = "Следующий: ${nextSize.fruit} ${nextSize.emoji}"
            babySizeNextTextView?.visibility = android.view.View.VISIBLE
        } else {
            babySizeNextTextView?.visibility = android.view.View.GONE
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
            // Переход на полный календарь при долгом нажатии или двойном нажатии
            // Для простоты сделаем переход на полный календарь
            val intent = Intent(this, FullCalendarActivity::class.java)
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

