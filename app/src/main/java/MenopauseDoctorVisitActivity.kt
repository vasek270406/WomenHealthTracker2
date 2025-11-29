package com.example.womenhealthtracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

data class Specialist(
    val type: String,
    val description: String,
    val frequency: String,
    val iconRes: Int
)

class MenopauseDoctorVisitActivity : AppCompatActivity() {
    
    private val specialists = listOf(
        Specialist("Гинеколог", "Ежегодный осмотр, гормональная терапия", "Ежегодно", android.R.drawable.ic_menu_agenda),
        Specialist("Маммолог", "Маммография, УЗИ молочных желез", "Раз в 2 года", android.R.drawable.ic_menu_agenda),
        Specialist("Эндокринолог", "Гормональный фон, щитовидная железа", "По необходимости", android.R.drawable.ic_menu_agenda),
        Specialist("Кардиолог", "Контроль давления, здоровья сердца", "Ежегодно", android.R.drawable.ic_menu_agenda),
        Specialist("Остеопат", "Плотность костей, остеопороз", "Раз в 2-3 года", android.R.drawable.ic_menu_agenda),
        Specialist("Дерматолог", "Изменения кожи, уход", "По необходимости", android.R.drawable.ic_menu_agenda)
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menopause_doctor_visit)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        setupSpecialists()
        setupBottomNavigation()
    }
    
    private fun setupSpecialists() {
        val container = findViewById<LinearLayout>(R.id.specialistsContainer)
        
        // Информационная карточка
        val infoCard = createInfoCard()
        container.addView(infoCard)
        
        // Карточки специалистов
        specialists.forEach { specialist ->
            val card = createSpecialistCard(specialist)
            container.addView(card)
        }
    }
    
    private fun createInfoCard(): CardView {
        val card = CardView(this)
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 16, 16, 16)
        }
        card.radius = 12f
        card.setCardElevation(2f)
        
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(16, 16, 16, 16)
        layout.setBackgroundColor(getColor(android.R.color.holo_blue_light))
        
        val icon = TextView(this)
        icon.text = ""
        icon.visibility = android.view.View.GONE
        icon.textSize = 48f
        icon.gravity = android.view.Gravity.CENTER
        layout.addView(icon)
        
        val title = TextView(this)
        title.text = "Регулярные осмотры - забота о себе!"
        title.textSize = 16f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        title.gravity = android.view.Gravity.CENTER
        title.setPadding(0, 8, 0, 4)
        layout.addView(title)
        
        val description = TextView(this)
        description.text = "В этот период особенно важно следить за здоровьем"
        description.textSize = 14f
        description.gravity = android.view.Gravity.CENTER
        layout.addView(description)
        
        card.addView(layout)
        return card
    }
    
    private fun createSpecialistCard(specialist: Specialist): CardView {
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
        layout.orientation = LinearLayout.HORIZONTAL
        layout.setPadding(16, 16, 16, 16)
        
        val icon = TextView(this)
        icon.text = ""
        icon.visibility = android.view.View.GONE
        icon.textSize = 32f
        icon.setPadding(0, 0, 16, 0)
        layout.addView(icon)
        
        val textLayout = LinearLayout(this)
        textLayout.orientation = LinearLayout.VERTICAL
        textLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        
        val title = TextView(this)
        title.text = specialist.type
        title.textSize = 18f
        title.setTypeface(null, android.graphics.Typeface.BOLD)
        textLayout.addView(title)
        
        val desc = TextView(this)
        desc.text = specialist.description
        desc.textSize = 14f
        desc.setTextColor(getColor(android.R.color.darker_gray))
        desc.setPadding(0, 4, 0, 4)
        textLayout.addView(desc)
        
        val frequency = TextView(this)
        frequency.text = "Рекомендуется: ${specialist.frequency}"
        frequency.textSize = 12f
        frequency.setTextColor(getColor(android.R.color.holo_green_dark))
        textLayout.addView(frequency)
        
        layout.addView(textLayout)
        
        val arrow = TextView(this)
        arrow.text = "→"
        arrow.visibility = android.view.View.GONE
        arrow.textSize = 24f
        layout.addView(arrow)
        
        card.setOnClickListener {
            val intent = Intent(this, MenopauseDoctorVisitBookingActivity::class.java)
            intent.putExtra("selectedSpecialist", specialist.type)
            startActivity(intent)
        }
        
        card.addView(layout)
        return card
    }
    
    private fun setupBottomNavigation() {
        val settingsButtonView = findViewById<View>(R.id.settingsButton)
        val calendarButtonView = findViewById<View>(R.id.calendarButton)
        val notificationsButtonView = findViewById<View>(R.id.notificationsButton)
        val profileButtonView = findViewById<View>(R.id.profileButton)
        
        (settingsButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        
        (calendarButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, MenopauseHomeActivity::class.java)
            startActivity(intent)
        }
        
        (notificationsButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        }
        
        (profileButtonView as? ImageButton)?.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

