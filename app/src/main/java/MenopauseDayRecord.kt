package com.example.womenhealthtracker

import java.io.Serializable

// Расширенная запись дня для менопаузы
data class MenopauseDayRecord(
    val date: String, // yyyy-MM-dd
    val symptoms: Map<String, SymptomDetail> = emptyMap(), // тип симптома -> детали
    val notes: String = "",
    val mood: Int = 3, // 1-5
    val energy: Int = 3, // 1-5
    val triggers: List<String> = emptyList() // общие триггеры дня
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): MenopauseDayRecord {
            val symptomsMap = (map["symptoms"] as? Map<*, *>)?.mapNotNull { entry ->
                val key = entry.key as? String
                val value = entry.value as? Map<*, *>
                if (key != null && value != null) {
                    val valueMap = value.mapKeys { it.key.toString() }.mapValues { it.value as Any }
                    key to SymptomDetail.fromMap(valueMap as Map<String, Any>)
                } else null
            }?.toMap() ?: emptyMap()
            
            return MenopauseDayRecord(
                date = map["date"] as? String ?: "",
                symptoms = symptomsMap,
                notes = map["notes"] as? String ?: "",
                mood = (map["mood"] as? Long)?.toInt() ?: 3,
                energy = (map["energy"] as? Long)?.toInt() ?: 3,
                triggers = (map["triggers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "date" to date,
            "symptoms" to symptoms.mapValues { it.value.toMap() },
            "notes" to notes,
            "mood" to mood,
            "energy" to energy,
            "triggers" to triggers
        )
    }
}

// Детали симптома
data class SymptomDetail(
    val intensity: Int = 1, // 1-5
    val notes: String = "",
    val specificTriggers: List<String> = emptyList(),
    val durationMinutes: Int? = null // продолжительность в минутах
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): SymptomDetail {
            return SymptomDetail(
                intensity = (map["intensity"] as? Long)?.toInt() ?: 1,
                notes = map["notes"] as? String ?: "",
                specificTriggers = (map["specificTriggers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                durationMinutes = (map["durationMinutes"] as? Long)?.toInt()
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "intensity" to intensity,
            "notes" to notes,
            "specificTriggers" to specificTriggers
        )
        durationMinutes?.let { map["durationMinutes"] = it }
        return map
    }
}

// Типы симптомов менопаузы
enum class MenopauseSymptomType(val displayName: String) {
    HOT_FLASHES("Приливы жара"),
    NIGHT_SWEATS("Ночная потливость"),
    MOOD_SWINGS("Перепады настроения"),
    SLEEP_PROBLEMS("Проблемы со сном"),
    FATIGUE("Усталость"),
    VAGINAL_DRYNESS("Сухость влагалища"),
    LIBIDO_CHANGES("Изменение либидо"),
    JOINT_PAIN("Боли в суставах"),
    WEIGHT_GAIN("Увеличение веса"),
    MEMORY_PROBLEMS("Проблемы с памятью"),
    ANXIETY("Тревожность"),
    HEART_PALPITATIONS("Учащенное сердцебиение"),
    HEADACHES("Головные боли"),
    BLADDER_PROBLEMS("Проблемы с мочевым пузырем"),
    SKIN_CHANGES("Изменения кожи")
}

// Триггеры для симптомов
object MenopauseTriggers {
    val symptomTriggers = mapOf(
        MenopauseSymptomType.HOT_FLASHES to listOf(
            "Кофеин", "Острая пища", "Алкоголь", "Стресс", "Жаркое помещение",
            "Тесная одежда", "Курение", "Горячие напитки"
        ),
        MenopauseSymptomType.MOOD_SWINGS to listOf(
            "Стресс на работе", "Семейные проблемы", "Недосып", "Гормональные колебания",
            "Переутомление", "Конфликтные ситуации"
        ),
        MenopauseSymptomType.SLEEP_PROBLEMS to listOf(
            "Приливы ночью", "Тревожность", "Кофеин вечером", "Поздний ужин",
            "Гаджеты перед сном", "Неудобная кровать"
        ),
        MenopauseSymptomType.FATIGUE to listOf(
            "Плохой сон", "Переутомление", "Несбалансированное питание", "Стресс",
            "Недостаток движения", "Обезвоживание"
        ),
        MenopauseSymptomType.ANXIETY to listOf(
            "Стресс", "Гормональные изменения", "Недосып", "Кофеин",
            "Переутомление", "Неопределенность"
        )
    )
    
    val commonTriggers = listOf(
        "Кофеин", "Острая пища", "Алкоголь", "Стресс", "Жаркое помещение",
        "Тесная одежда", "Недосып", "Поздний ужин", "Переутомление"
    )
}

