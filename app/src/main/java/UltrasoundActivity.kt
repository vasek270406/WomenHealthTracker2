package com.example.womenhealthtracker

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class UltrasoundActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestoreHelper: FirestoreHelper
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private var ultrasoundDate: Calendar = Calendar.getInstance()
    private var pregnancyWeek: Int = 1
    private var selectedImagePath: String? = null
    
    private lateinit var imagePreview: ImageView
    private lateinit var dateButton: Button
    private lateinit var weekEditText: EditText
    private lateinit var notesEditText: EditText
    private lateinit var pickFromGalleryButton: Button
    private lateinit var saveButton: Button
    
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImagePath = it.toString()
            // Загружаем изображение в ImageView
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imagePreview.setImageBitmap(bitmap)
                inputStream?.close()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ultrasound)
        
        // Включаем кнопку назад в action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        userPreferences = UserPreferences(this)
        firestoreHelper = FirestoreHelper(this)
        
        // Получаем текущую неделю беременности
        val pregnancyStartDate = userPreferences.getPregnancyStartDate()
        if (pregnancyStartDate.isNotEmpty()) {
            try {
                val startCalendar = Calendar.getInstance()
                startCalendar.time = dateFormat.parse(pregnancyStartDate) ?: Date()
                val currentCalendar = Calendar.getInstance()
                val daysDiff = ((currentCalendar.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                pregnancyWeek = if (daysDiff >= 0) (daysDiff / 7) + 1 else 1
            } catch (e: Exception) {
                pregnancyWeek = 1
            }
        }
        
        setupViews()
        setupDatePicker()
        setupImageButtons()
        setupSaveButton()
    }
    
    private fun setupViews() {
        imagePreview = findViewById(R.id.imagePreview)
        dateButton = findViewById(R.id.dateButton)
        weekEditText = findViewById(R.id.weekEditText)
        notesEditText = findViewById(R.id.notesEditText)
        pickFromGalleryButton = findViewById(R.id.pickFromGalleryButton)
        saveButton = findViewById(R.id.saveButton)
        
        weekEditText.setText(pregnancyWeek.toString())
        updateDateButton()
    }
    
    private fun setupDatePicker() {
        dateButton.setOnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    ultrasoundDate.set(year, month, dayOfMonth)
                    updateDateButton()
                },
                ultrasoundDate.get(Calendar.YEAR),
                ultrasoundDate.get(Calendar.MONTH),
                ultrasoundDate.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
    
    private fun updateDateButton() {
        val displayFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
        dateButton.text = displayFormat.format(ultrasoundDate.time)
    }
    
    private fun setupImageButtons() {
        pickFromGalleryButton.setOnClickListener {
            // Открываем галерею для выбора изображения
            pickImageLauncher.launch("image/*")
        }
    }
    
    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            saveUltrasound()
        }
    }
    
    private fun saveUltrasound() {
        val week = weekEditText.text.toString().toIntOrNull() ?: pregnancyWeek
        val notes = notesEditText.text.toString().trim()
        
        // Создаем визит к врачу типа "УЗИ"
        val visit = DoctorVisit(
            date = dateFormat.format(ultrasoundDate.time),
            type = "УЗИ",
            doctorName = "",
            notes = notes,
            photos = if (selectedImagePath != null) listOf(selectedImagePath!!) else emptyList()
        )
        
        // Сохранение в PregnancyData
        val pregnancyData = userPreferences.getPregnancyData()
        val updatedVisits = pregnancyData.visits + visit
        
        val updatedPregnancyData = pregnancyData.copy(visits = updatedVisits)
        userPreferences.savePregnancyData(updatedPregnancyData)
        
        // Сохранение в Firestore
        val userId = userPreferences.getUserId()
        if (userId.isNotEmpty()) {
            firestoreHelper.savePregnancyData(
                userId,
                updatedPregnancyData,
                onSuccess = {
                    Toast.makeText(this, "Фото УЗИ сохранено", Toast.LENGTH_SHORT).show()
                    finish()
                },
                onError = { error ->
                    Toast.makeText(this, "Фото УЗИ сохранено локально", Toast.LENGTH_SHORT).show()
                    finish()
                }
            )
        } else {
            Toast.makeText(this, "Фото УЗИ сохранено", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

