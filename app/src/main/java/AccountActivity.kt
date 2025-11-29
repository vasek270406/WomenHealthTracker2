package com.example.womenhealthtracker

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar

class AccountActivity : AppCompatActivity() {
    
    private lateinit var userPreferences: UserPreferences
    private lateinit var firestoreHelper: FirestoreHelper
    private val authHelper = FirebaseAuthHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        // Создаем и настраиваем Toolbar с кнопкой назад
        setupToolbar()

        userPreferences = UserPreferences(this)
        firestoreHelper = FirestoreHelper(this)
        
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val cycleLengthEditText = findViewById<EditText>(R.id.cycleLengthEditText)
        
        // Загрузка сохраненных данных
        loadSavedData(nameEditText, ageEditText, cycleLengthEditText)
        
        // Сохранение данных при изменении полей
        setupAutoSave(nameEditText, ageEditText, cycleLengthEditText)

        // Открытие диалога с роллером при клике на поле возраста
        ageEditText.setOnClickListener {
            showAgePickerDialog(ageEditText)
        }

        // Кнопка "Продолжить" - переход на экран разрешений (онбординг)
        val continueButton = findViewById<Button>(R.id.continueButton)
        continueButton.setOnClickListener {
            // Проверяем, заполнены ли обязательные поля
            val name = nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(this, "Пожалуйста, введите ваше имя", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, PermissionsActivity::class.java)
            startActivity(intent)
        }

        // Кнопка "Сменить режим"
        val changeModeButton = findViewById<Button>(R.id.changeModeButton)
        changeModeButton?.setOnClickListener {
            showModeSelectionDialog()
        }
        
        // Обновление отображения текущего режима
        updateCurrentModeDisplay()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_PREGNANCY_SETUP && resultCode == RESULT_OK) {
            // Беременность настроена, переключаемся в режим беременности
            switchToMode(UserGoal.PREGNANCY)
            updateCurrentModeDisplay()
            Toast.makeText(this, "Режим беременности активирован!", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем отображение режима при возврате на экран
        updateCurrentModeDisplay()
        
        // Кнопка "Выйти из аккаунта"
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            showLogoutDialog()
        }

        setupBottomNavigation()
    }
    
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Выход из аккаунта")
            .setMessage("Вы уверены, что хотите выйти из аккаунта? Все данные будут сохранены.")
            .setPositiveButton("Выйти") { _, _ ->
                logout()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    private fun logout() {
        // Выход из Firebase Authentication
        val authHelper = FirebaseAuthHelper(this)
        authHelper.signOut()
        
        // Очищаем статус авторизации в SharedPreferences
        userPreferences.setLoggedIn(false)
        
        // Переход на главный экран (приветственный)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showAgePickerDialog(ageEditText: EditText) {
        val agePicker = NumberPicker(this)
        agePicker.minValue = 13
        agePicker.maxValue = 100
        
        // Установка текущего значения из поля возраста или значение по умолчанию
        val currentAge = ageEditText.text.toString().toIntOrNull() ?: 25
        agePicker.value = currentAge
        
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите возраст")
        builder.setView(agePicker)
        
        builder.setPositiveButton("Готово") { _, _ ->
            val age = agePicker.value
            ageEditText.setText(age.toString())
            userPreferences.saveAge(age)
            syncProfileToFirestore() // Синхронизация с Firestore
        }
        
        builder.setNegativeButton("Отмена", null)
        
        val dialog = builder.create()
        dialog.show()
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
            // Уже на экране профиля
        }
    }
    
    private fun loadSavedData(
        nameEditText: EditText,
        ageEditText: EditText,
        cycleLengthEditText: EditText
    ) {
        // Загрузка имени
        val savedName = userPreferences.getName()
        if (savedName.isNotEmpty()) {
            nameEditText.setText(savedName)
        }
        
        // Загрузка возраста
        val savedAge = userPreferences.getAge()
        if (savedAge > 0) {
            ageEditText.setText(savedAge.toString())
        }
        
        // Загрузка длины цикла
        val savedCycleLength = userPreferences.getCycleLength()
        if (savedCycleLength > 0) {
            cycleLengthEditText.setText(savedCycleLength.toString())
        }
    }
    
    private fun setupAutoSave(
        nameEditText: EditText,
        ageEditText: EditText,
        cycleLengthEditText: EditText
    ) {
        // Автосохранение имени
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                if (name.isNotEmpty()) {
                    userPreferences.saveName(name)
                    syncProfileToFirestore() // Синхронизация с Firestore
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Автосохранение длины цикла
        cycleLengthEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val length = s.toString().toIntOrNull() ?: 0
                if (length > 0) {
                    userPreferences.saveCycleLength(length)
                    syncProfileToFirestore() // Синхронизация с Firestore
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    /**
     * Синхронизация профиля из SharedPreferences в Firestore
     */
    private fun syncProfileToFirestore() {
        val userId = authHelper.getCurrentUserId() ?: userPreferences.getUserId()
        if (userId.isEmpty()) {
            return // Пользователь не авторизован
        }
        
        // Создаем профиль из текущих данных SharedPreferences
        val profile = UserProfile(
            name = userPreferences.getName(),
            age = userPreferences.getAge(),
            cycleLength = userPreferences.getCycleLength(),
            menstruationLength = userPreferences.getMenstruationLength(),
            goals = userPreferences.getGoals(),
            lastPeriodStart = userPreferences.getLastPeriodStart(),
            periodDates = userPreferences.getPeriodDates().toList(),
            onboardingCompleted = userPreferences.isOnboardingCompleted(),
            notificationPeriod = userPreferences.isNotificationPeriodEnabled(),
            notificationFertile = userPreferences.isNotificationFertileEnabled(),
            notificationDaily = userPreferences.isNotificationDailyEnabled(),
            notificationWater = userPreferences.isNotificationWaterEnabled(),
            selectedGoal = userPreferences.getSelectedGoal(),
            pregnancyStartDate = userPreferences.getPregnancyStartDate(),
            menopauseStartDate = userPreferences.getMenopauseStartDate(),
            hasIrregularCycles = userPreferences.hasIrregularCycles()
        )
        
        // Сохраняем в Firestore
        firestoreHelper.saveUserProfile(
            userId = userId,
            profile = profile,
            onSuccess = {
                android.util.Log.d("AccountActivity", "Профиль синхронизирован с Firestore")
            },
            onError = { error ->
                android.util.Log.e("AccountActivity", "Ошибка синхронизации профиля: $error")
            }
        )
    }
    
    /**
     * Показать диалог выбора режима
     */
    /**
     * Показать диалог выбора режима
     */
    private fun showModeSelectionDialog() {
        val currentGoal = userPreferences.getSelectedGoal()
        val options = UserGoal.values().map { GoalHelper.getGoalTitle(it) }.toTypedArray()
        val selectedIndex = UserGoal.values().indexOf(currentGoal)
        
        AlertDialog.Builder(this)
            .setTitle("Сменить режим использования")
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val selectedGoal = UserGoal.values()[which]
                
                // Если выбран тот же режим, просто закрываем диалог
                if (selectedGoal == currentGoal) {
                    dialog.dismiss()
                    return@setSingleChoiceItems
                }
                
                // Если переключаемся на беременность, показываем подтверждение и настройку
                if (selectedGoal == UserGoal.PREGNANCY) {
                    dialog.dismiss()
                    showPregnancyModeSwitchConfirmation()
                } else {
                    // Для других режимов показываем подтверждение
                    dialog.dismiss()
                    showModeSwitchConfirmation(currentGoal, selectedGoal)
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    /**
     * Подтверждение смены режима (кроме беременности)
     */
    private fun showModeSwitchConfirmation(oldGoal: UserGoal, newGoal: UserGoal) {
        AlertDialog.Builder(this)
            .setTitle("Сменить режим?")
            .setMessage("Вы уверены, что хотите переключиться с \"${GoalHelper.getGoalTitle(oldGoal)}\" на \"${GoalHelper.getGoalTitle(newGoal)}\"?")
            .setPositiveButton("Сменить") { _, _ ->
                switchToMode(newGoal)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    /**
     * Подтверждение переключения в режим беременности
     */
    private fun showPregnancyModeSwitchConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Переключиться в режим беременности?")
            .setMessage("Для переключения в режим беременности необходимо указать дату начала беременности. Продолжить?")
            .setPositiveButton("Да, продолжить") { _, _ ->
                // Переход на экран настройки беременности
                val intent = Intent(this, PregnancySetupActivity::class.java)
                @Suppress("DEPRECATION")
                startActivityForResult(intent, REQUEST_CODE_PREGNANCY_SETUP)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    
    /**
     * Переключение на указанный режим
     */
    private fun switchToMode(newGoal: UserGoal) {
        val oldGoal = userPreferences.getSelectedGoal()
        
        // Если выходим из режима беременности, очищаем данные беременности
        if (oldGoal == UserGoal.PREGNANCY && newGoal != UserGoal.PREGNANCY) {
            userPreferences.savePregnancyStartDate("")
        }
        
        // Сохраняем новый режим
        userPreferences.saveSelectedGoal(newGoal)
        
        // Активируем уведомления для нового режима (без показа диалогов)
        val notificationHelper = NotificationHelper(this)
        notificationHelper.activateModeNotifications(newGoal)
        
        // Обновляем отображение
        updateCurrentModeDisplay()
        
        Toast.makeText(this, "Режим изменен: ${GoalHelper.getGoalTitle(newGoal)}", Toast.LENGTH_SHORT).show()
        
        // Если переключились на режим беременности, обновляем главный экран
        if (newGoal == UserGoal.PREGNANCY) {
            // Данные беременности уже сохранены через PregnancySetupActivity
        }
        
        // Не показываем диалоги уведомлений при переключении режима
    }
    
    /**
     * Обновить отображение текущего режима
     */
    private fun updateCurrentModeDisplay() {
        val currentModeTextView = findViewById<TextView>(R.id.currentModeTextView)
        val currentModeDescriptionTextView = findViewById<TextView>(R.id.currentModeDescriptionTextView)
        
        val currentGoal = userPreferences.getSelectedGoal()
        currentModeTextView?.text = GoalHelper.getGoalTitle(currentGoal)
        currentModeDescriptionTextView?.text = GoalHelper.getGoalDescription(currentGoal)
        
        // Если режим беременности, показываем дополнительную информацию
        if (currentGoal == UserGoal.PREGNANCY) {
            val pregnancyInfo = GoalHelper.getPregnancyInfo(userPreferences)
            if (pregnancyInfo != null) {
                currentModeDescriptionTextView?.text = pregnancyInfo
            }
        }
    }
    
    companion object {
        private const val REQUEST_CODE_PREGNANCY_SETUP = 1001
    }
    
    // Метод удален - больше не показываем диалог уведомлений для менопаузы
    
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

