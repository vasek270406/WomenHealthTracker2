package com.example.womenhealthtracker

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import java.net.UnknownHostException

class FirestoreHelper(private val context: Context? = null) {
    
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreHelper"
    
    // Проверка наличия интернет-соединения
    private fun isNetworkAvailable(): Boolean {
        if (context == null) return true // Если контекст не передан, предполагаем что сеть есть
        
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    // Обработка ошибок сети
    private fun handleError(e: Exception, defaultMessage: String): String {
        return when {
            e is UnknownHostException || 
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
            e.message?.contains("No address associated with hostname", ignoreCase = true) == true -> {
                "Нет подключения к интернету. Проверьте соединение и попробуйте снова."
            }
            e is FirebaseFirestoreException -> {
                when (e.code) {
                    FirebaseFirestoreException.Code.UNAVAILABLE -> {
                        "Сервис временно недоступен. Проверьте подключение к интернету."
                    }
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                        "Нет доступа к данным. Проверьте правила безопасности Firestore."
                    }
                    FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                        "Требуется авторизация. Войдите в аккаунт."
                    }
                    else -> e.message ?: defaultMessage
                }
            }
            else -> e.message ?: defaultMessage
        }
    }
    
    // ========== Работа с профилем пользователя ==========
    
    // Сохранение профиля пользователя
    fun saveUserProfile(
        userId: String,
        profile: UserProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .set(profile.toMap(), SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Профиль пользователя сохранен")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения профиля", e)
                onError(handleError(e, "Ошибка сохранения профиля"))
            }
    }
    
    // Получение профиля пользователя
    fun getUserProfile(
        userId: String,
        onSuccess: (UserProfile?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profile = UserProfile.fromMap(document.data ?: emptyMap())
                    onSuccess(profile)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения профиля", e)
                onError(handleError(e, "Ошибка получения профиля"))
            }
    }
    
    // ========== Работа с данными дня ==========
    
    // Сохранение данных дня
    fun saveDayData(
        userId: String,
        dayData: DayData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("days")
            .document(dayData.date)
            .set(dayData.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Данные дня сохранены: ${dayData.date}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения данных дня", e)
                onError(handleError(e, "Ошибка сохранения данных дня"))
            }
    }
    
    // Получение данных дня
    fun getDayData(
        userId: String,
        date: String,
        onSuccess: (DayData?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("days")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val dayData = DayData.fromMap(document.data ?: emptyMap())
                    onSuccess(dayData)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения данных дня", e)
                onError(handleError(e, "Ошибка получения данных дня"))
            }
    }
    
    // Получение всех данных за период
    fun getDayDataForPeriod(
        userId: String,
        startDate: String,
        endDate: String,
        onSuccess: (List<DayData>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("days")
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dayDataList = querySnapshot.documents.mapNotNull { doc ->
                    DayData.fromMap(doc.data ?: emptyMap())
                }
                onSuccess(dayDataList)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения данных за период", e)
                onError(handleError(e, "Ошибка получения данных за период"))
            }
    }
    
    // Получение всех дат с данными
    fun getAllDatesWithData(
        userId: String,
        onSuccess: (List<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("days")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dates = querySnapshot.documents.mapNotNull { doc -> doc.id }.sorted()
                onSuccess(dates)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения списка дат", e)
                onError(handleError(e, "Ошибка получения списка дат"))
            }
    }
    
    // Удаление данных дня
    fun deleteDayData(
        userId: String,
        date: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("days")
            .document(date)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Данные дня удалены: $date")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка удаления данных дня", e)
                onError(handleError(e, "Ошибка удаления данных дня"))
            }
    }
    
    // ========== Работа с данными фертильности ==========
    
    // Сохранение данных фертильности
    fun saveFertilityData(
        userId: String,
        fertilityData: FertilityData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("fertility")
            .document(fertilityData.date)
            .set(fertilityData.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Данные фертильности сохранены: ${fertilityData.date}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения данных фертильности", e)
                onError(handleError(e, "Ошибка сохранения данных фертильности"))
            }
    }
    
    // Получение данных фертильности
    fun getFertilityData(
        userId: String,
        date: String,
        onSuccess: (FertilityData?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("fertility")
            .document(date)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fertilityData = FertilityData.fromMap(document.data ?: emptyMap())
                    onSuccess(fertilityData)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения данных фертильности", e)
                onError(handleError(e, "Ошибка получения данных фертильности"))
            }
    }
    
    // ========== Работа с данными беременности ==========
    
    // Сохранение данных беременности
    fun savePregnancyData(
        userId: String,
        pregnancyData: PregnancyData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("pregnancy")
            .document("current")
            .set(pregnancyData.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Данные беременности сохранены")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения данных беременности", e)
                onError(handleError(e, "Ошибка сохранения данных беременности"))
            }
    }
    
    // Получение данных беременности
    fun getPregnancyData(
        userId: String,
        onSuccess: (PregnancyData?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("pregnancy")
            .document("current")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val pregnancyData = PregnancyData.fromMap(document.data ?: emptyMap())
                    onSuccess(pregnancyData)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения данных беременности", e)
                onError(handleError(e, "Ошибка получения данных беременности"))
            }
    }
    
    // ========== Работа с данными менопаузы ==========
    
    // Сохранение симптома менопаузы
    fun saveMenopauseSymptom(
        userId: String,
        symptom: MenopauseSymptom,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("menopause_symptoms")
            .document("${symptom.date}_${symptom.type}")
            .set(symptom.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Симптом менопаузы сохранен: ${symptom.date}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения симптома менопаузы", e)
                onError(handleError(e, "Ошибка сохранения симптома менопаузы"))
            }
    }
    
    // Сохранение результата MRS
    fun saveMRSResult(
        userId: String,
        mrsResult: MRSResult,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("mrs_results")
            .document(mrsResult.testDate)
            .set(mrsResult.toMap())
            .addOnSuccessListener {
                Log.d(TAG, "Результат MRS сохранен: ${mrsResult.testDate}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения результата MRS", e)
                onError(handleError(e, "Ошибка сохранения результата MRS"))
            }
    }
    
    // Сохранение визитов к врачу при менопаузе
    fun saveMenopauseDoctorVisits(
        userId: String,
        visits: List<MenopauseDoctorVisit>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Данные будут сохранены локально.")
            return
        }
        
        val visitsMap = visits.associateBy { it.id }
        val batch = db.batch()
        
        visitsMap.forEach { (id, visit) ->
            val docRef = db.collection("users")
                .document(userId)
                .collection("menopause_doctor_visits")
                .document(id)
            batch.set(docRef, visit.toMap())
        }
        
        batch.commit()
            .addOnSuccessListener {
                Log.d(TAG, "Визиты к врачу сохранены: ${visits.size}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка сохранения визитов к врачу", e)
                onError(handleError(e, "Ошибка сохранения визитов к врачу"))
            }
    }
    
    // Получение визитов к врачу при менопаузе
    fun getMenopauseDoctorVisits(
        userId: String,
        onSuccess: (List<MenopauseDoctorVisit>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isNetworkAvailable()) {
            onError("Нет подключения к интернету. Проверьте соединение и попробуйте снова.")
            return
        }
        
        db.collection("users")
            .document(userId)
            .collection("menopause_doctor_visits")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val visits = querySnapshot.documents.mapNotNull { doc ->
                    MenopauseDoctorVisit.fromMap(doc.data ?: emptyMap())
                }
                onSuccess(visits)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка получения визитов к врачу", e)
                onError(handleError(e, "Ошибка получения визитов к врачу"))
            }
    }
}

// Класс для профиля пользователя
data class UserProfile(
    val name: String = "",
    val age: Int = 0,
    val cycleLength: Int = 0,
    val menstruationLength: Int = 5,
    val goals: String = "",
    val lastPeriodStart: String = "",
    val periodDates: List<String> = emptyList(),
    val onboardingCompleted: Boolean = false,
    val notificationPeriod: Boolean = true,
    val notificationFertile: Boolean = true,
    val notificationDaily: Boolean = false,
    val notificationWater: Boolean = false,
    // Новые поля для мульти-режимности
    val selectedGoal: UserGoal = UserGoal.CYCLE_TRACKING,
    val pregnancyStartDate: String = "", // yyyy-MM-dd
    val menopauseStartDate: String = "", // yyyy-MM-dd
    val hasIrregularCycles: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "age" to age,
            "cycleLength" to cycleLength,
            "menstruationLength" to menstruationLength,
            "goals" to goals,
            "lastPeriodStart" to lastPeriodStart,
            "periodDates" to periodDates,
            "onboardingCompleted" to onboardingCompleted,
            "notificationPeriod" to notificationPeriod,
            "notificationFertile" to notificationFertile,
            "notificationDaily" to notificationDaily,
            "notificationWater" to notificationWater,
            "selectedGoal" to selectedGoal.name,
            "pregnancyStartDate" to pregnancyStartDate,
            "menopauseStartDate" to menopauseStartDate,
            "hasIrregularCycles" to hasIrregularCycles
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): UserProfile {
            val goalString = map["selectedGoal"] as? String ?: "CYCLE_TRACKING"
            val selectedGoal = try {
                UserGoal.valueOf(goalString)
            } catch (e: Exception) {
                UserGoal.CYCLE_TRACKING
            }
            
            return UserProfile(
                name = map["name"] as? String ?: "",
                age = (map["age"] as? Long)?.toInt() ?: 0,
                cycleLength = (map["cycleLength"] as? Long)?.toInt() ?: 0,
                menstruationLength = (map["menstruationLength"] as? Long)?.toInt() ?: 5,
                goals = map["goals"] as? String ?: "",
                lastPeriodStart = map["lastPeriodStart"] as? String ?: "",
                periodDates = (map["periodDates"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                onboardingCompleted = map["onboardingCompleted"] as? Boolean ?: false,
                notificationPeriod = map["notificationPeriod"] as? Boolean ?: true,
                notificationFertile = map["notificationFertile"] as? Boolean ?: true,
                notificationDaily = map["notificationDaily"] as? Boolean ?: false,
                notificationWater = map["notificationWater"] as? Boolean ?: false,
                selectedGoal = selectedGoal,
                pregnancyStartDate = map["pregnancyStartDate"] as? String ?: "",
                menopauseStartDate = map["menopauseStartDate"] as? String ?: "",
                hasIrregularCycles = map["hasIrregularCycles"] as? Boolean ?: false
            )
        }
    }
}

// Расширения для DayData для работы с Firestore
fun DayData.toMap(): Map<String, Any> {
    val map = mutableMapOf<String, Any>(
        "date" to date,
        "mood" to mood,
        "sexualActivity" to sexualActivity,
        "vitamins" to vitamins,
        "notes" to notes
    )
    
    // Добавляем опциональные поля только если они не null
    weight?.let { map["weight"] = it.toString() }
    temperature?.let { map["temperature"] = it.toString() }
    sleepHours?.let { map["sleepHours"] = it.toString() }
    waterIntake?.let { map["waterIntake"] = it.toString() }
    energy?.let { map["energy"] = it }
    libido?.let { map["libido"] = it }
    
    // Конвертируем список симптомов
    map["symptoms"] = symptoms.map { symptom ->
        mapOf(
            "name" to symptom.name,
            "category" to symptom.category.name,
            "intensity" to symptom.intensity,
            "notes" to symptom.notes
        )
    }
    
    return map
}

// Метод fromMap теперь находится в companion object класса DayData

