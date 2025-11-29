package com.example.womenhealthtracker

import java.io.Serializable

// Причина задержки
enum class DelayReason {
    PREGNANCY,              // Беременность
    STRESS,                 // Стресс
    LIFESTYLE_CHANGE,       // Изменение образа жизни
    HORMONAL_FLUCTUATION,   // Гормональные колебания
    ILLNESS,                // Заболевание
    MEDICATION,             // Лекарства
    TRAVEL,                 // Путешествие
    DIET_CHANGE,            // Изменение диеты
    EXERCISE_CHANGE,        // Изменение тренировок
    UNKNOWN                 // Неизвестно
}

// Контекст задержки
data class DelayContext(
    val hadSexualActivity: Boolean? = null,  // Был ли ПА в фертильное окно
    val stress: Boolean = false,              // Стресс
    val travel: Boolean = false,              // Путешествие
    val dietChange: Boolean = false,          // Изменение диеты
    val exerciseChange: Boolean = false,      // Изменение тренировок
    val illness: Boolean = false,             // Заболевание
    val medication: Boolean = false,          // Лекарства
    val unusualSymptoms: List<String> = emptyList()  // Необычные симптомы
) : Serializable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "hadSexualActivity" to (hadSexualActivity ?: false),
            "stress" to stress,
            "travel" to travel,
            "dietChange" to dietChange,
            "exerciseChange" to exerciseChange,
            "illness" to illness,
            "medication" to medication,
            "unusualSymptoms" to unusualSymptoms
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): DelayContext {
            return DelayContext(
                hadSexualActivity = map["hadSexualActivity"] as? Boolean,
                stress = map["stress"] as? Boolean ?: false,
                travel = map["travel"] as? Boolean ?: false,
                dietChange = map["dietChange"] as? Boolean ?: false,
                exerciseChange = map["exerciseChange"] as? Boolean ?: false,
                illness = map["illness"] as? Boolean ?: false,
                medication = map["medication"] as? Boolean ?: false,
                unusualSymptoms = (map["unusualSymptoms"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
}

// Запись о задержке
data class DelayRecord(
    val id: String,                             // Уникальный ID
    val expectedPeriodDate: String,              // Ожидаемая дата начала (yyyy-MM-dd)
    val delayStartDate: String,                  // Дата начала задержки (yyyy-MM-dd)
    val delayDays: Int,                          // Количество дней задержки
    val context: DelayContext,                   // Контекст задержки
    val analyzedReasons: List<Pair<DelayReason, Int>>,  // Причины с вероятностью (0-100)
    val recommendations: List<String>,          // Рекомендации
    val resolved: Boolean = false,               // Решена ли задержка
    val resolvedDate: String? = null,           // Дата решения (если решена)
    val notes: String = ""                       // Заметки пользователя
) : Serializable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "expectedPeriodDate" to expectedPeriodDate,
            "delayStartDate" to delayStartDate,
            "delayDays" to delayDays,
            "context" to context.toMap(),
            "analyzedReasons" to analyzedReasons.map { mapOf("reason" to it.first.name, "probability" to it.second) },
            "recommendations" to recommendations,
            "resolved" to resolved,
            "resolvedDate" to (resolvedDate ?: ""),
            "notes" to notes
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): DelayRecord {
            val reasonsList = (map["analyzedReasons"] as? List<*>)?.mapNotNull { reasonMap ->
                val rm = reasonMap as? Map<*, *>
                if (rm != null) {
                    val reason = try {
                        DelayReason.valueOf(rm["reason"] as? String ?: "")
                    } catch (e: Exception) {
                        DelayReason.UNKNOWN
                    }
                    val probability = (rm["probability"] as? Long)?.toInt() ?: 0
                    Pair(reason, probability)
                } else null
            } ?: emptyList()
            
            return DelayRecord(
                id = map["id"] as? String ?: "",
                expectedPeriodDate = map["expectedPeriodDate"] as? String ?: "",
                delayStartDate = map["delayStartDate"] as? String ?: "",
                delayDays = (map["delayDays"] as? Long)?.toInt() ?: 0,
                context = DelayContext.fromMap((map["context"] as? Map<*, *>)?.mapKeys { it.key.toString() }?.mapValues { it.value as Any } ?: emptyMap()),
                analyzedReasons = reasonsList,
                recommendations = (map["recommendations"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                resolved = map["resolved"] as? Boolean ?: false,
                resolvedDate = map["resolvedDate"] as? String,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}

// Рекомендация на основе анализа
data class DelayRecommendation(
    val title: String,
    val description: String,
    val actionType: RecommendationAction,
    val actionData: String? = null  // Дополнительные данные для действия
)

enum class RecommendationAction {
    PREGNANCY_TEST,           // Сделать тест на беременность
    ENTER_TEST_RESULT,        // Ввести результат теста
    OPEN_MEDITATION,          // Открыть медитацию
    TRACK_MOOD,              // Отслеживать настроение
    CONSULT_DOCTOR,          // Консультация врача
    GENERATE_REPORT,         // Сформировать отчет
    FIND_DOCTOR              // Найти врача
}


