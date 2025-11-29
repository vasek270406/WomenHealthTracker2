package com.example.womenhealthtracker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class DelayAnalysisActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var delayAnalyzer: DelayAnalyzer
    private lateinit var container: LinearLayout
    
    private var delayDays: Int = 0
    private var expectedPeriodDate: String = ""
    private var delayContext = DelayContext()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delay_analysis)
        
        setupToolbar()
        
        userPreferences = UserPreferences(this)
        delayAnalyzer = DelayAnalyzer()
        container = findViewById(R.id.delayContainer)
        
        // Получаем данные о задержке из Intent или вычисляем
        val delayDaysFromIntent = intent.getIntExtra("delayDays", -1)
        if (delayDaysFromIntent > 0) {
            delayDays = delayDaysFromIntent
            expectedPeriodDate = intent.getStringExtra("expectedPeriodDate") ?: ""
        } else {
            calculateDelay()
        }
        
        if (delayDays > 0) {
            showDelayInfo()
            showQuestionnaire()
        } else {
            showNoDelayMessage()
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun calculateDelay() {
        val lastPeriodStart = userPreferences.getLastPeriodStart()
        val cycleLength = userPreferences.getCycleLength()
        
        if (lastPeriodStart.isEmpty() || cycleLength == 0) {
            delayDays = 0
            return
        }
        
        expectedPeriodDate = delayAnalyzer.calculateExpectedPeriodDate(lastPeriodStart, cycleLength) ?: ""
        if (expectedPeriodDate.isNotEmpty()) {
            delayDays = delayAnalyzer.calculateDelayDays(expectedPeriodDate)
        }
    }
    
    private fun showDelayInfo() {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        val title = TextView(this)
        title.text = "Отслеживаем вашу задержку"
        title.textSize = 20f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.setPadding(0, 0, 0, 12)
        layout.addView(title)
        
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        val expectedDateText = try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expectedPeriodDate)
            if (date != null) dateFormat.format(date) else expectedPeriodDate
        } catch (e: Exception) {
            expectedPeriodDate
        }
        
        val infoText = TextView(this)
        infoText.text = "На основе ваших данных, цикл должен был начаться $expectedDateText.\nСегодня ${delayDays}-й день задержки."
        infoText.textSize = 14f
        infoText.setTextColor(Color.parseColor("#666666"))
        infoText.setPadding(0, 0, 0, 16)
        layout.addView(infoText)
        
        val supportText = TextView(this)
        supportText.text = "Не волнуйтесь, задержки время от времени случаются у большинства женщин. Ваш организм реагирует на внутренние и внешние изменения."
        supportText.textSize = 13f
        supportText.setTextColor(Color.parseColor("#888888"))
        layout.addView(supportText)
        
        card.addView(layout)
        container.addView(card)
    }
    
    private fun showQuestionnaire() {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        val title = TextView(this)
        title.text = "Помогите нам понять причину"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.setPadding(0, 0, 0, 16)
        layout.addView(title)
        
        // ПА в фертильное окно
        val sexQuestion = createQuestion("Был ли у вас половой акт в последнем фертильном окне?")
        val sexGroup = RadioGroup(this)
        sexGroup.orientation = RadioGroup.HORIZONTAL
        val sexYes = RadioButton(this).apply { text = "Да"; id = View.generateViewId() }
        val sexNo = RadioButton(this).apply { text = "Нет"; id = View.generateViewId() }
        val sexDontRemember = RadioButton(this).apply { text = "Не помню"; id = View.generateViewId() }
        sexGroup.addView(sexYes)
        sexGroup.addView(sexNo)
        sexGroup.addView(sexDontRemember)
        sexGroup.setOnCheckedChangeListener { _, checkedId ->
            delayContext = delayContext.copy(
                hadSexualActivity = when (checkedId) {
                    sexYes.id -> true
                    sexNo.id -> false
                    else -> null
                }
            )
        }
        layout.addView(sexQuestion)
        layout.addView(sexGroup)
        
        // Чекбоксы для факторов
        val factorsTitle = TextView(this)
        factorsTitle.text = "Испытывали ли вы в этом месяце?"
        factorsTitle.textSize = 14f
        factorsTitle.setTypeface(null, android.graphics.Typeface.BOLD)
        factorsTitle.setPadding(0, 16, 0, 8)
        layout.addView(factorsTitle)
        
        val stressCheck = createCheckbox("Повышенный стресс") { checked ->
            delayContext = delayContext.copy(stress = checked)
        }
        layout.addView(stressCheck)
        
        val travelCheck = createCheckbox("Смену часовых поясов / путешествие") { checked ->
            delayContext = delayContext.copy(travel = checked)
        }
        layout.addView(travelCheck)
        
        val dietCheck = createCheckbox("Изменения в диете") { checked ->
            delayContext = delayContext.copy(dietChange = checked)
        }
        layout.addView(dietCheck)
        
        val exerciseCheck = createCheckbox("Изменения в режиме тренировок") { checked ->
            delayContext = delayContext.copy(exerciseChange = checked)
        }
        layout.addView(exerciseCheck)
        
        val illnessCheck = createCheckbox("Простудное или иное заболевание") { checked ->
            delayContext = delayContext.copy(illness = checked)
        }
        layout.addView(illnessCheck)
        
        val medicationCheck = createCheckbox("Прием новых лекарств") { checked ->
            delayContext = delayContext.copy(medication = checked)
        }
        layout.addView(medicationCheck)
        
        // Кнопка анализа
        val analyzeButton = Button(this)
        analyzeButton.text = "Проанализировать"
        analyzeButton.setBackgroundColor(Color.parseColor("#FFB6C1"))
        analyzeButton.setTextColor(Color.parseColor("#000000"))
        analyzeButton.setPadding(0, 16, 0, 16)
        analyzeButton.setOnClickListener {
            performAnalysis()
        }
        layout.addView(analyzeButton)
        
        card.addView(layout)
        container.addView(card)
    }
    
    private fun createQuestion(text: String): TextView {
        val question = TextView(this)
        question.text = text
        question.textSize = 14f
        question.setTextColor(Color.parseColor("#000000"))
        question.setPadding(0, 0, 0, 8)
        return question
    }
    
    private fun createCheckbox(text: String, onChecked: (Boolean) -> Unit): CheckBox {
        val checkbox = CheckBox(this)
        checkbox.text = text
        checkbox.textSize = 14f
        checkbox.setTextColor(Color.parseColor("#000000"))
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            onChecked(isChecked)
        }
        return checkbox
    }
    
    private fun performAnalysis() {
        // Очищаем контейнер
        container.removeAllViews()
        
        // Показываем информацию о задержке
        showDelayInfo()
        
        // Анализируем причины
        val cycleHistory = userPreferences.getCycleHistory()
        val reasons = delayAnalyzer.analyzeDelay(delayDays, delayContext, cycleHistory)
        
        // Показываем причины
        showReasons(reasons)
        
        // Генерируем рекомендации
        val allRecommendations = delayAnalyzer.generateRecommendations(delayDays, reasons, delayContext)
        
        // Фильтруем рекомендации: убираем "Консультация врача" и "Сформировать отчет" в режиме беременности
        val goal = userPreferences.getSelectedGoal()
        val recommendations = if (goal == UserGoal.PREGNANCY) {
            allRecommendations.filter { 
                it.actionType != RecommendationAction.CONSULT_DOCTOR && 
                it.actionType != RecommendationAction.GENERATE_REPORT 
            }
        } else {
            allRecommendations
        }
        
        showRecommendations(recommendations)
        
        // Сохраняем запись о задержке
        saveDelayRecord(reasons, recommendations)
    }
    
    private fun showReasons(reasons: List<Pair<DelayReason, Int>>) {
        if (reasons.isEmpty()) return
        
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        val title = TextView(this)
        title.text = "Возможные причины"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.setPadding(0, 0, 0, 16)
        layout.addView(title)
        
        reasons.forEach { (reason, probability) ->
            val reasonLayout = LinearLayout(this)
            reasonLayout.orientation = LinearLayout.HORIZONTAL
            reasonLayout.setPadding(0, 8, 0, 8)
            
            val reasonText = TextView(this)
            reasonText.text = getReasonDisplayName(reason)
            reasonText.textSize = 14f
            reasonText.setTextColor(Color.parseColor("#000000"))
            reasonText.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            reasonLayout.addView(reasonText)
            
            val probabilityText = TextView(this)
            val probabilityLabel = if (probability >= 70) "Высокая" else if (probability >= 40) "Средняя" else "Низкая"
            probabilityText.text = "$probabilityLabel ($probability%)"
            probabilityText.textSize = 12f
            probabilityText.setTextColor(
                when {
                    probability >= 70 -> Color.parseColor("#FF6B6B")
                    probability >= 40 -> Color.parseColor("#FFA500")
                    else -> Color.parseColor("#888888")
                }
            )
            reasonLayout.addView(probabilityText)
            
            layout.addView(reasonLayout)
        }
        
        card.addView(layout)
        container.addView(card)
    }
    
    private fun showRecommendations(recommendations: List<DelayRecommendation>) {
        if (recommendations.isEmpty()) return
        
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(20, 20, 20, 20)
        
        val title = TextView(this)
        title.text = "Ваши следующие шаги"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.setPadding(0, 0, 0, 16)
        layout.addView(title)
        
        recommendations.forEach { recommendation ->
            val recCard = CardView(this)
            recCard.radius = 8f
            recCard.setCardElevation(1f)
            recCard.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
            recCard.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
            
            val recLayout = LinearLayout(this)
            recLayout.orientation = LinearLayout.VERTICAL
            recLayout.setPadding(16, 16, 16, 16)
            
            val recTitle = TextView(this)
            recTitle.text = recommendation.title
            recTitle.textSize = 15f
            recTitle.setTypeface(null, android.graphics.Typeface.BOLD)
            recTitle.setTextColor(Color.parseColor("#000000"))
            recTitle.setPadding(0, 0, 0, 8)
            recLayout.addView(recTitle)
            
            val recDesc = TextView(this)
            recDesc.text = recommendation.description
            recDesc.textSize = 13f
            recDesc.setTextColor(Color.parseColor("#666666"))
            recLayout.addView(recDesc)
            
            val actionButton = Button(this)
            actionButton.text = getActionButtonText(recommendation.actionType)
            actionButton.setBackgroundColor(Color.parseColor("#FFB6C1"))
            actionButton.setTextColor(Color.parseColor("#000000"))
            actionButton.setPadding(0, 12, 0, 12)
            actionButton.setOnClickListener {
                handleRecommendationAction(recommendation)
            }
            actionButton.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 12, 0, 0)
            }
            recLayout.addView(actionButton)
            
            recCard.addView(recLayout)
            layout.addView(recCard)
        }
        
        card.addView(layout)
        container.addView(card)
    }
    
    private fun getReasonDisplayName(reason: DelayReason): String {
        return when (reason) {
            DelayReason.PREGNANCY -> "Беременность"
            DelayReason.STRESS -> "Стресс"
            DelayReason.LIFESTYLE_CHANGE -> "Изменение образа жизни"
            DelayReason.HORMONAL_FLUCTUATION -> "Гормональные колебания"
            DelayReason.ILLNESS -> "Заболевание"
            DelayReason.MEDICATION -> "Лекарства"
            DelayReason.TRAVEL -> "Путешествие"
            DelayReason.DIET_CHANGE -> "Изменение диеты"
            DelayReason.EXERCISE_CHANGE -> "Изменение тренировок"
            DelayReason.UNKNOWN -> "Неизвестно"
        }
    }
    
    private fun getActionButtonText(action: RecommendationAction): String {
        return when (action) {
            RecommendationAction.PREGNANCY_TEST -> "Узнать больше о тестах"
            RecommendationAction.ENTER_TEST_RESULT -> "Ввести результат теста"
            RecommendationAction.OPEN_MEDITATION -> "Открыть медитацию"
            RecommendationAction.TRACK_MOOD -> "Отслеживать настроение"
            RecommendationAction.CONSULT_DOCTOR -> "Записаться к врачу"
            RecommendationAction.GENERATE_REPORT -> "Сформировать отчет"
            RecommendationAction.FIND_DOCTOR -> "Найти врача"
        }
    }
    
    private fun handleRecommendationAction(recommendation: DelayRecommendation) {
        when (recommendation.actionType) {
            RecommendationAction.TRACK_MOOD -> {
                val intent = Intent(this, CalendarActivity::class.java)
                startActivity(intent)
            }
            RecommendationAction.CONSULT_DOCTOR -> {
                // Открываем экран записи к врачу
                openDoctorBooking()
            }
            RecommendationAction.FIND_DOCTOR -> {
                Toast.makeText(this, "Функция поиска врача будет доступна в следующей версии", Toast.LENGTH_SHORT).show()
            }
            RecommendationAction.GENERATE_REPORT -> {
                // Генерируем отчет
                generateAndShowReport()
            }
            RecommendationAction.PREGNANCY_TEST -> {
                Toast.makeText(this, "Рекомендуется сделать тест на беременность утром", Toast.LENGTH_LONG).show()
            }
            RecommendationAction.ENTER_TEST_RESULT -> {
                Toast.makeText(this, "Функция ввода результата теста будет доступна в следующей версии", Toast.LENGTH_SHORT).show()
            }
            RecommendationAction.OPEN_MEDITATION -> {
                Toast.makeText(this, "Функция медитации будет доступна в следующей версии", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun saveDelayRecord(reasons: List<Pair<DelayReason, Int>>, recommendations: List<DelayRecommendation>) {
        val record = DelayRecord(
            id = UUID.randomUUID().toString(),
            expectedPeriodDate = expectedPeriodDate,
            delayStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
            delayDays = delayDays,
            context = delayContext,
            analyzedReasons = reasons,
            recommendations = recommendations.map { it.title }
        )
        
        userPreferences.saveDelayRecord(record)
    }
    
    private fun showNoDelayMessage() {
        val card = createCard()
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)
        layout.gravity = android.view.Gravity.CENTER
        
        val icon = TextView(this)
        icon.text = "✓"
        icon.textSize = 64f
        icon.gravity = android.view.Gravity.CENTER
        icon.setPadding(0, 0, 0, 16)
        layout.addView(icon)
        
        val title = TextView(this)
        title.text = "Задержки не обнаружено"
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.setTextColor(Color.parseColor("#000000"))
        title.gravity = android.view.Gravity.CENTER
        layout.addView(title)
        
        card.addView(layout)
        container.addView(card)
    }
    
    private fun createCard(): CardView {
        val card = CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
        }
        card.radius = 12f
        card.setCardElevation(2f)
        card.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
        return card
    }
    
    /**
     * Открывает экран записи к врачу
     */
    private fun openDoctorBooking() {
        val goal = userPreferences.getSelectedGoal()
        val intent = when (goal) {
            UserGoal.PREGNANCY -> {
                Intent(this, DoctorVisitActivity::class.java)
            }
            UserGoal.MENOPAUSE -> {
                Intent(this, MenopauseDoctorVisitBookingActivity::class.java)
            }
            else -> {
                // Для других режимов открываем общий экран визита к врачу
                Intent(this, DoctorVisitActivity::class.java)
            }
        }
        startActivity(intent)
    }
    
    /**
     * Генерирует и показывает отчет
     */
    private fun generateAndShowReport() {
        try {
            val reportGenerator = CycleReportGenerator(this)
            val reportUri = reportGenerator.generateCycleReport()
            
            if (reportUri != null) {
                reportGenerator.showReportDialog(reportUri)
            } else {
                Toast.makeText(this, "Ошибка при создании отчета", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("DelayAnalysisActivity", "Ошибка генерации отчета: ${e.message}")
            Toast.makeText(this, "Ошибка при создании отчета: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

