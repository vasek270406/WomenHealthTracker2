package com.example.womenhealthtracker

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class PregnancySetupActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var selectedMethod = 0 // 0 - по месячным, 1 - по зачатию, 2 - по неделе
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pregnancy_setup)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        
        val methodGroup = findViewById<RadioGroup>(R.id.methodGroup)
        val lastPeriodButton = findViewById<Button>(R.id.lastPeriodButton)
        val conceptionButton = findViewById<Button>(R.id.conceptionButton)
        val weekEditText = findViewById<EditText>(R.id.weekEditText)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val dueDateTextView = findViewById<TextView>(R.id.dueDateTextView)
        
        // Выбор метода расчета
        methodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.methodLastPeriod -> {
                    selectedMethod = 0
                    lastPeriodButton.isEnabled = true
                    conceptionButton.isEnabled = false
                    weekEditText.isEnabled = false
                }
                R.id.methodConception -> {
                    selectedMethod = 1
                    lastPeriodButton.isEnabled = false
                    conceptionButton.isEnabled = true
                    weekEditText.isEnabled = false
                }
                R.id.methodWeek -> {
                    selectedMethod = 2
                    lastPeriodButton.isEnabled = false
                    conceptionButton.isEnabled = false
                    weekEditText.isEnabled = true
                }
            }
        }
        
        // Выбор даты последних месячных
        lastPeriodButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    val dateString = dateFormat.format(selectedCalendar.time)
                    
                    // Сохраняем дату
                    userPreferences.savePregnancyStartDate(dateString)
                    
                    // Рассчитываем предполагаемую дату родов (+280 дней)
                    val dueCalendar = Calendar.getInstance()
                    dueCalendar.time = selectedCalendar.time
                    dueCalendar.add(Calendar.DAY_OF_YEAR, 280)
                    val dueDateString = dateFormat.format(dueCalendar.time)
                    
                    // Отображаем дату
                    val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                    lastPeriodButton.text = displayFormat.format(selectedCalendar.time)
                    dueDateTextView.text = "Предполагаемая дата родов: ${displayFormat.format(dueCalendar.time)}"
                    dueDateTextView.visibility = TextView.VISIBLE
                },
                year,
                month,
                day
            ).show()
        }
        
        // Выбор даты зачатия
        conceptionButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    
                    // Для расчета по зачатию добавляем 14 дней назад (примерная дата последних месячных)
                    selectedCalendar.add(Calendar.DAY_OF_YEAR, -14)
                    val dateString = dateFormat.format(selectedCalendar.time)
                    
                    userPreferences.savePregnancyStartDate(dateString)
                    
                    // Рассчитываем предполагаемую дату родов
                    val dueCalendar = Calendar.getInstance()
                    dueCalendar.time = selectedCalendar.time
                    dueCalendar.add(Calendar.DAY_OF_YEAR, 280)
                    val dueDateString = dateFormat.format(dueCalendar.time)
                    
                    val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
                    conceptionButton.text = displayFormat.format(selectedCalendar.time)
                    dueDateTextView.text = "Предполагаемая дата родов: ${displayFormat.format(dueCalendar.time)}"
                    dueDateTextView.visibility = TextView.VISIBLE
                },
                year,
                month,
                day
            ).show()
        }
        
        // Продолжить
        continueButton.setOnClickListener {
            when (selectedMethod) {
                0, 1 -> {
                    // По дате месячных или зачатия
                    val startDate = userPreferences.getPregnancyStartDate()
                    if (startDate.isEmpty()) {
                        Toast.makeText(this, "Пожалуйста, выберите дату", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    
                    // Сохраняем режим беременности
                    userPreferences.saveSelectedGoal(UserGoal.PREGNANCY)
                    
                    // Создаем данные беременности
                    val pregnancyData = PregnancyData(
                        pregnancyStartDate = startDate,
                        estimatedDueDate = calculateDueDate(startDate)
                    )
                    
                    // Сохраняем в Firestore
                    savePregnancyData(pregnancyData)
                    
                    Toast.makeText(this, "Режим беременности активирован!", Toast.LENGTH_SHORT).show()
                    
                    // Возвращаем результат успешной настройки
                    setResult(RESULT_OK)
                    
                    // Переход на главный экран
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                2 -> {
                    // По неделе беременности
                    val weekText = weekEditText.text.toString().trim()
                    val week = weekText.toIntOrNull()
                    
                    if (week == null || week < 1 || week > 42) {
                        Toast.makeText(this, "Введите корректную неделю (1-42)", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    
                    // Рассчитываем дату начала беременности (назад на недели)
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.WEEK_OF_YEAR, -week)
                    val startDate = dateFormat.format(calendar.time)
                    
                    userPreferences.savePregnancyStartDate(startDate)
                    userPreferences.saveSelectedGoal(UserGoal.PREGNANCY)
                    
                    val pregnancyData = PregnancyData(
                        pregnancyStartDate = startDate,
                        estimatedDueDate = calculateDueDate(startDate)
                    )
                    
                    savePregnancyData(pregnancyData)
                    
                    Toast.makeText(this, "Режим беременности активирован!", Toast.LENGTH_SHORT).show()
                    
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    
    private fun calculateDueDate(startDate: String): String {
        return try {
            val start = dateFormat.parse(startDate) ?: return ""
            val calendar = Calendar.getInstance()
            calendar.time = start
            calendar.add(Calendar.DAY_OF_YEAR, 280)
            dateFormat.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun savePregnancyData(pregnancyData: PregnancyData) {
        try {
            val firebaseAuth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val firestoreHelper = FirestoreHelper(this)
                firestoreHelper.savePregnancyData(
                    userId = currentUser.uid,
                    pregnancyData = pregnancyData,
                    onSuccess = {},
                    onError = {}
                )
            }
        } catch (e: Exception) {
            // Игнорируем ошибку синхронизации
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

