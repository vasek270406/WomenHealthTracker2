package com.example.womenhealthtracker

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.gridlayout.widget.GridLayout
import androidx.gridlayout.widget.GridLayout.LayoutParams
import java.text.SimpleDateFormat
import java.util.*

class FullCalendarActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var displayedMonth = Calendar.getInstance().get(Calendar.MONTH)
    private var displayedYear = Calendar.getInstance().get(Calendar.YEAR)
    private var isMonthView = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_calendar)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        setupNavigation()
        setupViewToggle()
        setupCalendar()
        setupToolbar()
        setupBottomNavigation()
    }
    
    private fun setupToolbar() {
        val addEventButton = findViewById<Button>(R.id.addEventButton)
        val forecastButton = findViewById<Button>(R.id.forecastButton)
        
        addEventButton.setOnClickListener {
            showAddEventDialog()
        }
        
        forecastButton.setOnClickListener {
            showForecastDialog()
        }
    }
    
    private fun showAddEventDialog() {
        val options = arrayOf(
            "Отметить начало цикла",
            "Добавить запись о самочувствии",
            "Быстрое добавление",
            "Редактировать даты месячных"
        )
        
        AlertDialog.Builder(this)
            .setTitle("Добавить событие")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val today = Calendar.getInstance()
                        val todayString = dateFormat.format(today.time)
                        showPeriodDialog(todayString)
                    }
                    1 -> {
                        val intent = Intent(this, SymptomTrackerActivity::class.java)
                        startActivity(intent)
                    }
                    2 -> {
                        val intent = Intent(this, QuickAddActivity::class.java)
                        startActivity(intent)
                    }
                    3 -> {
                        showEditPeriodDatesDialog()
                    }
                }
            }
            .show()
    }
    
    private fun showEditPeriodDatesDialog() {
        val periodDates = userPreferences.getPeriodDates().toList().sorted()
        
        if (periodDates.isEmpty()) {
            Toast.makeText(this, "Нет сохраненных дат месячных", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Группируем даты по месяцам для удобства
        val dateFormatDisplay = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val dateFormatMonth = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        
        val groupedDates = mutableMapOf<String, MutableList<Pair<String, String>>>()
        
        periodDates.forEach { dateString ->
            try {
                val calendar = Calendar.getInstance()
                calendar.time = dateFormat.parse(dateString) ?: return@forEach
                val monthKey = dateFormatMonth.format(calendar.time)
                val dateDisplay = dateFormatDisplay.format(calendar.time)
                
                if (!groupedDates.containsKey(monthKey)) {
                    groupedDates[monthKey] = mutableListOf()
                }
                groupedDates[monthKey]?.add(Pair(dateString, dateDisplay))
            } catch (e: Exception) {
                // Игнорируем ошибки парсинга
            }
        }
        
        // Создаем упрощенный список с группировкой, сортируем по датам
        val items = mutableListOf<String>()
        val dateMap = mutableMapOf<Int, String>()
        var index = 0
        
        // Сортируем месяцы по самой ранней дате в каждом месяце
        val sortedMonths = groupedDates.toList().sortedByDescending { (_, dates) ->
            dates.minOfOrNull { it.first } ?: ""
        }
        
        sortedMonths.forEach { (month, dates) ->
            items.add("📅 $month (${dates.size} ${getDayWord(dates.size)})")
            dateMap[index] = month
            index++
        }
        
        if (items.isEmpty()) {
            Toast.makeText(this, "Нет сохраненных дат месячных", Toast.LENGTH_SHORT).show()
            return
        }
        
        AlertDialog.Builder(this)
            .setTitle("Редактировать даты месячных")
            .setItems(items.toTypedArray()) { _, which ->
                val selectedMonth = dateMap[which]
                if (selectedMonth != null) {
                    val monthDates = groupedDates[selectedMonth] ?: return@setItems
                    showMonthDatesDialog(selectedMonth, monthDates)
                }
            }
            .setNegativeButton("Отмена", null)
            .setNeutralButton("Удалить все") { _, _ ->
                showDeleteAllConfirmation()
            }
            .show()
    }
    
    private fun showMonthDatesDialog(month: String, dates: List<Pair<String, String>>) {
        // Сортируем даты по возрастанию
        val sortedDates = dates.sortedBy { it.first }
        val dateStrings = sortedDates.map { it.second }.toTypedArray()
        
        AlertDialog.Builder(this)
            .setTitle(month)
            .setItems(dateStrings) { _, which ->
                val selectedDate = sortedDates[which].first
                showPeriodDateOptionsDialog(selectedDate)
            }
            .setNegativeButton("Назад") { dialog, _ ->
                dialog.dismiss()
                showEditPeriodDatesDialog()
            }
            .show()
    }
    
    private fun showDeleteAllConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Удалить все даты?")
            .setMessage("Вы уверены, что хотите удалить все сохраненные даты месячных?")
            .setPositiveButton("Удалить") { _, _ ->
                userPreferences.savePeriodDates(emptySet())
                userPreferences.saveLastPeriodStart("")
                Toast.makeText(this, "Все даты удалены", Toast.LENGTH_SHORT).show()
                refreshCalendarView()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun getDayWord(count: Int): String {
        return when {
            count % 10 == 1 && count % 100 != 11 -> "день"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "дня"
            else -> "дней"
        }
    }
    
    private fun togglePeriodDate(dateString: String, isCurrentlyPeriod: Boolean) {
        val periodDates = userPreferences.getPeriodDates().toMutableSet()
        
        if (isCurrentlyPeriod) {
            // Удаляем дату
            if (periodDates.remove(dateString)) {
                userPreferences.savePeriodDates(periodDates)
                
                // Если это была дата начала цикла, обновляем lastPeriodStart
                val lastPeriodStart = userPreferences.getLastPeriodStart()
                if (lastPeriodStart == dateString) {
                    // Находим новую самую раннюю дату или очищаем
                    val sortedDates = periodDates.sorted()
                    if (sortedDates.isNotEmpty()) {
                        userPreferences.saveLastPeriodStart(sortedDates.first())
                    } else {
                        userPreferences.saveLastPeriodStart("")
                    }
                }
                
                Toast.makeText(this, "Дата удалена", Toast.LENGTH_SHORT).show()
                refreshCalendarView()
            }
        } else {
            // Добавляем дату
            periodDates.add(dateString)
            userPreferences.savePeriodDates(periodDates)
            
            // Если это первая дата или самая ранняя, обновляем lastPeriodStart
            val lastPeriodStart = userPreferences.getLastPeriodStart()
            if (lastPeriodStart.isEmpty() || dateString < lastPeriodStart) {
                userPreferences.saveLastPeriodStart(dateString)
            }
            
            Toast.makeText(this, "Дата отмечена", Toast.LENGTH_SHORT).show()
            refreshCalendarView()
        }
    }
    
    private fun refreshCalendarView() {
        // Обновляем календарь в зависимости от текущего вида
        if (isMonthView) {
            setupCalendar()
        } else {
            setupWeekView()
        }
    }
    
    /**
     * Проверяет, находится ли дата в текущем периоде (сегодня и близкие даты)
     * Разрешаем отмечать даты в диапазоне ±30 дней от сегодня
     */
    private fun isDateInCurrentPeriod(dateString: String): Boolean {
        try {
            val today = Calendar.getInstance()
            val todayString = dateFormat.format(today.time)
            
            // Сегодня всегда можно отмечать
            if (dateString == todayString) {
                return true
            }
            
            // Парсим проверяемую дату
            val dateCalendar = Calendar.getInstance()
            dateCalendar.time = dateFormat.parse(dateString) ?: return false
            
            // Вычисляем разницу в днях
            val daysDiff = ((dateCalendar.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            
            // Разрешаем отмечать даты в диапазоне от -30 до +7 дней (прошлое и ближайшее будущее)
            // Это позволяет отмечать прошедшие дни и ближайшие дни вперед
            return daysDiff >= -30 && daysDiff <= 7
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun showPeriodDateOptionsDialog(dateString: String) {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = dateFormat.parse(dateString) ?: return
        } catch (e: Exception) {
            return
        }
        
        val dayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val dateDisplay = dayFormat.format(calendar.time)
        
        AlertDialog.Builder(this)
            .setTitle("Дата: $dateDisplay")
            .setItems(arrayOf("Удалить", "Изменить дату")) { _, which ->
                when (which) {
                    0 -> {
                        removePeriodDate(dateString)
                    }
                    1 -> {
                        showChangePeriodDateDialog(dateString)
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun showChangePeriodDateDialog(oldDateString: String) {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = dateFormat.parse(oldDateString) ?: return
        } catch (e: Exception) {
            return
        }
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newCalendar = Calendar.getInstance()
                newCalendar.set(selectedYear, selectedMonth, selectedDay)
                val newDateString = dateFormat.format(newCalendar.time)
                
                // Удаляем старую дату и добавляем новую
                val periodDates = userPreferences.getPeriodDates().toMutableSet()
                periodDates.remove(oldDateString)
                periodDates.add(newDateString)
                userPreferences.savePeriodDates(periodDates)
                
                Toast.makeText(this, "Дата изменена", Toast.LENGTH_SHORT).show()
                refreshCalendarView()
            },
            year,
            month,
            day
        ).show()
    }
    
    private fun showForecastDialog() {
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            Toast.makeText(this, "Настройте цикл для получения прогноза", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val lastPeriodCalendar = Calendar.getInstance()
            lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return
            
            val nextPeriodCalendar = Calendar.getInstance()
            nextPeriodCalendar.time = lastPeriodCalendar.time
            nextPeriodCalendar.add(Calendar.DAY_OF_MONTH, cycleLength)
            
            val nextOvulationCalendar = Calendar.getInstance()
            nextOvulationCalendar.time = lastPeriodCalendar.time
            nextOvulationCalendar.add(Calendar.DAY_OF_MONTH, cycleLength + 14)
            
            val dateFormatDisplay = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            val nextPeriod = dateFormatDisplay.format(nextPeriodCalendar.time)
            val nextOvulation = dateFormatDisplay.format(nextOvulationCalendar.time)
            
            val message = "Прогноз на следующий цикл:\n\n" +
                    "Следующая менструация: $nextPeriod\n" +
                    "Овуляция: $nextOvulation"
            
            AlertDialog.Builder(this)
                .setTitle("Прогноз цикла")
                .setMessage(message)
                .setPositiveButton("ОК", null)
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка расчета прогноза", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupNavigation() {
        val prevMonthButton = findViewById<ImageButton>(R.id.prevMonthButton)
        val nextMonthButton = findViewById<ImageButton>(R.id.nextMonthButton)
        val todayButton = findViewById<Button>(R.id.todayButton)
        
        prevMonthButton.setOnClickListener {
            if (isMonthView) {
                displayedMonth--
                if (displayedMonth < 0) {
                    displayedMonth = 11
                    displayedYear--
                }
                setupCalendar()
            } else {
                // Навигация по неделям
                if (currentWeekStart != null) {
                    currentWeekStart!!.add(Calendar.WEEK_OF_YEAR, -1)
                    displayedMonth = currentWeekStart!!.get(Calendar.MONTH)
                    displayedYear = currentWeekStart!!.get(Calendar.YEAR)
                }
                setupWeekView()
            }
        }
        
        nextMonthButton.setOnClickListener {
            if (isMonthView) {
                displayedMonth++
                if (displayedMonth > 11) {
                    displayedMonth = 0
                    displayedYear++
                }
                setupCalendar()
            } else {
                // Навигация по неделям
                if (currentWeekStart != null) {
                    currentWeekStart!!.add(Calendar.WEEK_OF_YEAR, 1)
                    displayedMonth = currentWeekStart!!.get(Calendar.MONTH)
                    displayedYear = currentWeekStart!!.get(Calendar.YEAR)
                }
                setupWeekView()
            }
        }
        
        todayButton.setOnClickListener {
            val today = Calendar.getInstance()
            displayedMonth = today.get(Calendar.MONTH)
            displayedYear = today.get(Calendar.YEAR)
            currentWeekStart = null // Сброс для использования текущей недели
            if (isMonthView) {
                setupCalendar()
            } else {
                setupWeekView()
            }
        }
    }
    
    private fun setupViewToggle() {
        val monthViewButton = findViewById<TextView>(R.id.monthViewButton)
        val weekViewButton = findViewById<TextView>(R.id.weekViewButton)
        
        monthViewButton.setOnClickListener {
            if (!isMonthView) {
                isMonthView = true
                monthViewButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                monthViewButton.setTypeface(null, android.graphics.Typeface.BOLD)
                weekViewButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                weekViewButton.setTypeface(null, android.graphics.Typeface.NORMAL)
                setupCalendar()
            }
        }
        
        weekViewButton.setOnClickListener {
            if (isMonthView) {
                isMonthView = false
                weekViewButton.setTextColor(ContextCompat.getColor(this, android.R.color.black))
                weekViewButton.setTypeface(null, android.graphics.Typeface.BOLD)
                monthViewButton.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                monthViewButton.setTypeface(null, android.graphics.Typeface.NORMAL)
                setupWeekView()
            }
        }
    }
    
    private var currentWeekStart: Calendar? = null
    
    private fun setupWeekView() {
        val calendarGrid = findViewById<GridLayout>(R.id.calendarGrid)
        val monthYearTextView = findViewById<TextView>(R.id.monthYearTextView)
        val nextMonthDaysContainer = findViewById<LinearLayout>(R.id.nextMonthDaysContainer)
        val nextMonthTextView = findViewById<TextView>(R.id.nextMonthTextView)
        
        // Скрываем предпросмотр следующего месяца
        nextMonthTextView.visibility = android.view.View.GONE
        nextMonthDaysContainer.visibility = android.view.View.GONE
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, displayedMonth)
        calendar.set(Calendar.YEAR, displayedYear)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        // Если это первый запуск вида недели, используем текущую дату
        if (currentWeekStart == null) {
            currentWeekStart = Calendar.getInstance()
        } else {
            // Используем сохраненную неделю
            calendar.time = currentWeekStart!!.time
        }
        
        // Находим начало недели (понедельник)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        calendar.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        currentWeekStart = calendar.clone() as Calendar
        
        // Установка заголовка с неделей
        val weekStart = calendar.clone() as Calendar
        val weekEnd = calendar.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_MONTH, 6)
        
        val startDay = weekStart.get(Calendar.DAY_OF_MONTH)
        val endDay = weekEnd.get(Calendar.DAY_OF_MONTH)
        val startMonth = weekStart.get(Calendar.MONTH)
        val endMonth = weekEnd.get(Calendar.MONTH)
        val year = weekStart.get(Calendar.YEAR)
        
        val monthNames = arrayOf("января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря")
        
        val weekText = if (startMonth == endMonth) {
            "$startDay - $endDay ${monthNames[startMonth]} $year"
        } else {
            "$startDay ${monthNames[startMonth]} - $endDay ${monthNames[endMonth]} $year"
        }
        monthYearTextView.text = weekText
        
        // Очистка сетки
        val childCount = calendarGrid.childCount
        if (childCount > 7) {
            calendarGrid.removeViews(7, childCount - 7)
        }
        
        // Добавляем дни недели
        for (i in 0..6) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.add(Calendar.DAY_OF_MONTH, i)
            val day = dayCalendar.get(Calendar.DAY_OF_MONTH)
            val month = dayCalendar.get(Calendar.MONTH)
            val yearDay = dayCalendar.get(Calendar.YEAR)
            
            val dayView = createDayCell(day, month, yearDay)
            val params = LayoutParams().apply {
                width = 0
                height = LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(i, 1f)
                rowSpec = GridLayout.spec(1)
                setMargins(4, 4, 4, 4)
            }
            dayView.layoutParams = params
            calendarGrid.addView(dayView)
        }
    }
    
    private fun setupCalendar() {
        val calendarGrid = findViewById<GridLayout>(R.id.calendarGrid)
        val monthYearTextView = findViewById<TextView>(R.id.monthYearTextView)
        val nextMonthDaysContainer = findViewById<LinearLayout>(R.id.nextMonthDaysContainer)
        val nextMonthTextView = findViewById<TextView>(R.id.nextMonthTextView)
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, displayedMonth)
        calendar.set(Calendar.YEAR, displayedYear)
        
        // Установка месяца и года
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        monthYearTextView.text = monthFormat.format(calendar.time)
        
        // Получение первого дня месяца и количества дней
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        // Преобразование дня недели (воскресенье = 1, понедельник = 2, и т.д.)
        val startDay = if (firstDayOfWeek == Calendar.SUNDAY) 7 else firstDayOfWeek - 1
        
        // Вычисляем максимальное количество необходимых строк
        // Пустые ячейки: (startDay - 1), плюс дни месяца, все делим на 7
        val totalCells = (startDay - 1) + daysInMonth
        val maxRow = (totalCells + 6) / 7  // Округляем вверх
        
        // Устанавливаем динамическое количество строк (минимум 7 для заголовка + 6 недель)
        calendarGrid.rowCount = maxOf(7, maxRow + 1)  // +1 для строки заголовка
        
        // Очистка сетки (кроме заголовков дней недели)
        val childCount = calendarGrid.childCount
        if (childCount > 7) {
            calendarGrid.removeViews(7, childCount - 7)
        }
        
        // Добавление пустых ячеек до первого дня месяца (начинаем с row 1, после дней недели)
        for (i in 1 until startDay) {
            val emptyView = TextView(this)
            val params = LayoutParams().apply {
                width = 0
                height = LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(i - 1, 1f)
                rowSpec = GridLayout.spec(1) // Всегда строка 1, после дней недели
                setMargins(4, 4, 4, 4)
            }
            emptyView.layoutParams = params
            calendarGrid.addView(emptyView)
        }
        
        // Добавление дней месяца (начинаем с row 1, после дней недели)
        for (day in 1..daysInMonth) {
            val dayView = createDayCell(day, displayedMonth, displayedYear)
            // Исправляем расчет строки: дни недели в row 0, дни месяца начинаются с row 1
            val row = 1 + (startDay + day - 2) / 7
            val col = (startDay + day - 2) % 7
            
            // Проверяем, что строка не превышает установленное количество строк
            if (row < calendarGrid.rowCount) {
                val params = LayoutParams().apply {
                    width = 0
                    height = LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(col, 1f)
                    rowSpec = GridLayout.spec(row)
                    setMargins(4, 4, 4, 4)
                }
                dayView.layoutParams = params
                calendarGrid.addView(dayView)
            }
        }
        
        // Предпросмотр следующего месяца (только для вида месяца)
        val nextCalendar = Calendar.getInstance()
        nextCalendar.set(Calendar.MONTH, displayedMonth)
        nextCalendar.set(Calendar.YEAR, displayedYear)
        nextCalendar.add(Calendar.MONTH, 1)
        
        val nextMonthFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        nextMonthTextView.text = nextMonthFormat.format(nextCalendar.time)
        nextMonthTextView.visibility = android.view.View.VISIBLE
        nextMonthDaysContainer.visibility = android.view.View.VISIBLE
        
        // Добавление первых дней следующего месяца
        nextMonthDaysContainer.removeAllViews()
        for (day in 1..8) {
            val dayView = TextView(this)
            dayView.text = day.toString()
            dayView.textSize = 16f
            dayView.gravity = Gravity.CENTER
            dayView.setPadding(12, 12, 12, 12)
            dayView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 0, 8, 0)
            dayView.layoutParams = layoutParams
            
            nextMonthDaysContainer.addView(dayView)
        }
    }
    
    private fun createDayCell(day: Int, month: Int, year: Int): LinearLayout {
        val dayContainer = LinearLayout(this)
        dayContainer.orientation = LinearLayout.VERTICAL
        dayContainer.gravity = Gravity.CENTER
        dayContainer.setPadding(8, 8, 8, 8)
        dayContainer.setMinimumWidth(48)
        dayContainer.setMinimumHeight(48)
        
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateString = dateFormat.format(calendar.time)
        
        val currentCalendar = Calendar.getInstance()
        val isToday = (day == currentCalendar.get(Calendar.DAY_OF_MONTH) && 
                      month == currentCalendar.get(Calendar.MONTH) && 
                      year == currentCalendar.get(Calendar.YEAR))
        
        // Получаем сохраненные даты месячных
        val periodDates = userPreferences.getPeriodDates()
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        
        // Определяем тип дня
        val dayType = getDayType(dateString, periodDates, lastPeriodStart, cycleLength)
        
        // Устанавливаем цвет фона в зависимости от типа дня
        when (dayType) {
            DayType.CURRENT_PERIOD -> {
                dayContainer.background = ContextCompat.getDrawable(this, R.drawable.day_circle_period_current)
            }
            DayType.PREVIOUS_PERIOD -> {
                dayContainer.background = ContextCompat.getDrawable(this, R.drawable.day_circle_period_previous)
            }
            DayType.OVULATION -> {
                dayContainer.background = ContextCompat.getDrawable(this, R.drawable.day_circle_ovulation)
            }
            DayType.LUTEAL -> {
                dayContainer.background = ContextCompat.getDrawable(this, R.drawable.day_circle_luteal)
            }
            DayType.TODAY -> {
                dayContainer.background = ContextCompat.getDrawable(this, R.drawable.day_circle_selected)
            }
            else -> {
                // Обычный день - белый/серый фон
            }
        }
        
        // Номер дня
        val dayTextView = TextView(this)
        dayTextView.text = day.toString()
        dayTextView.textSize = 14f
        dayTextView.gravity = Gravity.CENTER
        dayTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        dayContainer.addView(dayTextView)
        
        // Индикаторы убраны (без эмодзи)
        
        // Проверяем режим отслеживания цикла
        val goal = userPreferences.getSelectedGoal()
        val isCycleTrackingMode = (goal == UserGoal.CYCLE_TRACKING)
        val isPeriodDay = periodDates.contains(dateString)
        
        // Проверяем, находится ли дата в текущем периоде (сегодня и близкие даты)
        val isInCurrentPeriod = isDateInCurrentPeriod(dateString)
        
        // В режиме отслеживания цикла: клик переключает дату месячных только для текущего периода
        if (isCycleTrackingMode && isInCurrentPeriod) {
            dayContainer.setOnClickListener {
                togglePeriodDate(dateString, isPeriodDay)
            }
        } else {
            // В других режимах или для далеких дат: показываем карточку дня
            dayContainer.setOnClickListener {
                showDayCard(dateString, day, month, year, dayType)
            }
        }
        
        // Долгое нажатие для отметки начала месячных (если не в режиме отслеживания цикла)
        if (!isCycleTrackingMode) {
            dayContainer.setOnLongClickListener {
                showPeriodDialog(dateString)
                true
            }
        } else {
            // В режиме отслеживания цикла долгое нажатие показывает карточку дня
            dayContainer.setOnLongClickListener {
                showDayCard(dateString, day, month, year, dayType)
                true
            }
        }
        
        return dayContainer
    }
    
    private fun showDayCard(dateString: String, day: Int, month: Int, year: Int, dayType: DayType) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dayFormat = SimpleDateFormat("d MMMM yyyy, EEEE", Locale("ru"))
        val dateDisplay = dayFormat.format(calendar.time)
        
        val periodDates = userPreferences.getPeriodDates()
        val cycleLength = userPreferences.getCycleLength()
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val dayData = userPreferences.getDayData(dateString)
        val isPeriodDay = periodDates.contains(dateString)
        
        // Расчет дня цикла
        var cycleDayText = ""
        var phaseText = ""
        if (lastPeriodStart.isNotEmpty() && cycleLength > 0) {
            try {
                val lastPeriodCalendar = Calendar.getInstance()
                lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return
                val daysDiff = ((calendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                val cycleDay = ((daysDiff % cycleLength) + cycleLength) % cycleLength + 1
                cycleDayText = "$cycleDay-й день цикла"
                
                phaseText = when {
                    cycleDay in 1..5 -> " | Менструация"
                    cycleDay in 6..14 -> " | Фолликулярная фаза"
                    cycleDay in 15..17 -> " | Овуляция"
                    cycleDay in 18..(cycleLength - 2) -> " | Лютеиновая фаза"
                    else -> " | Предменструальная фаза"
                }
            } catch (e: Exception) {
                // Игнорируем ошибку
            }
        }
        
        // Формируем информацию о сохраненных данных
        val moodLabels = listOf("Плохое", "Нейтральное", "Хорошее", "Отличное", "Превосходное")
        val moodText = if (dayData != null && dayData.mood >= 0 && dayData.mood <= 4) {
            moodLabels[dayData.mood]
        } else {
            "-"
        }
        
        val symptomsText = if (dayData != null && dayData.symptoms.isNotEmpty()) {
            dayData.symptoms.take(3).joinToString(", ") { it.name }
        } else {
            "-"
        }
        
        val habitsText = buildString {
            if (dayData != null) {
                if (dayData.waterIntake != null && dayData.waterIntake > 0) {
                    append("Вода: ${String.format("%.1f", dayData.waterIntake)} л")
                }
                if (dayData.vitamins) {
                    if (isNotEmpty()) append(", ")
                    append("Витамины")
                }
                if (dayData.sleepHours != null) {
                    if (isNotEmpty()) append(", ")
                    append("Сон: ${dayData.sleepHours.toInt()}ч")
                }
            }
            if (isEmpty()) {
                append("-")
            }
        }
        
        val message = buildString {
            append("$dateDisplay\n\n")
            if (cycleDayText.isNotEmpty()) {
                append("$cycleDayText$phaseText\n\n")
            }
            if (isPeriodDay) {
                append("День месячных\n\n")
            }
            append("Настроение: $moodText\n")
            append("Симптомы: $symptomsText\n")
            append("Привычки: $habitsText\n")
            if (dayData?.notes?.isNotEmpty() == true) {
                append("\nЗаметки: ${dayData.notes}")
            }
        }
        
        val builder = AlertDialog.Builder(this)
            .setTitle("Информация о дне")
            .setMessage(message)
            .setPositiveButton("Редактировать") { _, _ ->
                // Переход на быстрое добавление
                val intent = Intent(this, QuickAddActivity::class.java)
                intent.putExtra("date", dateString)
                startActivity(intent)
            }
        
        // Добавляем кнопки для управления датами месячных
        if (isPeriodDay) {
            builder.setNeutralButton("Удалить дату месячных") { _, _ ->
                removePeriodDate(dateString)
            }
        } else {
            builder.setNeutralButton("Отметить начало цикла") { _, _ ->
                showPeriodDialog(dateString)
            }
        }
        
        builder.setNegativeButton("Закрыть", null)
            .show()
    }
    
    private fun removePeriodDate(dateString: String) {
        val periodDates = userPreferences.getPeriodDates().toMutableSet()
        if (periodDates.remove(dateString)) {
            userPreferences.savePeriodDates(periodDates)
            
            // Если это была дата начала цикла, обновляем lastPeriodStart
            val lastPeriodStart = userPreferences.getLastPeriodStart()
            if (lastPeriodStart == dateString) {
                val sortedDates = periodDates.sorted()
                if (sortedDates.isNotEmpty()) {
                    userPreferences.saveLastPeriodStart(sortedDates.first())
                } else {
                    userPreferences.saveLastPeriodStart("")
                }
            }
            
            Toast.makeText(this, "Дата месячных удалена", Toast.LENGTH_SHORT).show()
            refreshCalendarView()
        }
    }
    
    private enum class DayType {
        CURRENT_PERIOD,
        PREVIOUS_PERIOD,
        OVULATION,
        LUTEAL,
        TODAY,
        NORMAL
    }
    
    private fun getDayType(dateString: String, periodDates: Set<String>, lastPeriodStart: String, cycleLength: Int): DayType {
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)
        
        // Проверяем, является ли это сегодняшним днем
        if (dateString == today) {
            return DayType.TODAY
        }
        
        // Парсим текущую дату
        val currentDateCalendar = Calendar.getInstance()
        try {
            currentDateCalendar.time = dateFormat.parse(dateString) ?: return DayType.NORMAL
        } catch (e: Exception) {
            return DayType.NORMAL
        }
        
        // Если нет сохраненных данных о циклах
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            // Проверяем, есть ли эта дата в сохраненных датах
            if (periodDates.contains(dateString)) {
                return DayType.CURRENT_PERIOD
            }
            return DayType.NORMAL
        }
        
        // Парсим дату последнего начала месячных
        val lastPeriodCalendar = Calendar.getInstance()
        try {
            lastPeriodCalendar.time = dateFormat.parse(lastPeriodStart) ?: return DayType.NORMAL
        } catch (e: Exception) {
            return DayType.NORMAL
        }
        
        // Вычисляем разницу в днях от последнего начала месячных
        val daysDiff = ((currentDateCalendar.timeInMillis - lastPeriodCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        
        // Вычисляем день цикла (0 - первый день цикла)
        val dayOfCycle = ((daysDiff % cycleLength) + cycleLength) % cycleLength
        
        // Месячные обычно длятся 3-7 дней (используем 5 дней)
        val periodDuration = 5
        
        // Овуляция обычно происходит на 14 день цикла (может быть 12-16)
        val ovulationDay = 14
        
        // Лютеиновая фаза начинается после овуляции и длится до начала следующего цикла
        val lutealStartDay = ovulationDay + 1
        val lutealEndDay = cycleLength - 1
        
        // Проверяем, является ли это днем месячных
        // Сначала проверяем сохраненные даты (для точности)
        val isInSavedPeriod = periodDates.contains(dateString)
        
        // Также проверяем расчетные дни месячных (первые дни цикла)
        val isCalculatedPeriod = dayOfCycle < periodDuration
        
        if (isInSavedPeriod || isCalculatedPeriod) {
            // Определяем, текущий это цикл или предыдущий
            // Текущий цикл: дни от 0 до cycleLength
            // Предыдущие циклы: дни меньше 0 или больше cycleLength
            if (daysDiff >= 0 && daysDiff < cycleLength) {
                return DayType.CURRENT_PERIOD
            } else {
                // Для предыдущих циклов проверяем, есть ли эта дата в сохраненных
                if (isInSavedPeriod) {
                    return DayType.PREVIOUS_PERIOD
                }
                // Если это расчетный день предыдущего цикла, также отмечаем
                if (isCalculatedPeriod && daysDiff < 0) {
                    return DayType.PREVIOUS_PERIOD
                }
            }
        }
        
        // Проверяем фазы цикла для всех циклов
        when {
            // Овуляция (примерно на 14 день, ±2 дня)
            dayOfCycle in (ovulationDay - 2)..(ovulationDay + 2) -> {
                return DayType.OVULATION
            }
            // Лютеиновая фаза (после овуляции до начала следующего цикла)
            dayOfCycle in lutealStartDay..lutealEndDay -> {
                return DayType.LUTEAL
            }
        }
        
        return DayType.NORMAL
    }
    
    private fun showPeriodDialog(dateString: String) {
        val calendar = Calendar.getInstance()
        try {
            calendar.time = dateFormat.parse(dateString) ?: return
        } catch (e: Exception) {
            return
        }
        
        val dayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val dateDisplay = dayFormat.format(calendar.time)
        
        AlertDialog.Builder(this)
            .setTitle("Отметить начало месячных")
            .setMessage("Отметить $dateDisplay как начало месячных?")
            .setPositiveButton("Да") { _, _ ->
                userPreferences.savePeriodStartDate(dateString)
                Toast.makeText(this, "Дата сохранена", Toast.LENGTH_SHORT).show()
                // Обновляем календарь
                refreshCalendarView()
            }
            .setNegativeButton("Отмена", null)
            .show()
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
            // Переход на главный экран календаря
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

