package com.example.womenhealthtracker

import java.io.Serializable

data class DayData(
    val date: String, // yyyy-MM-dd
    val mood: Int = -1, // -1=не установлено, 0-4 (0=очень плохое, 4=отличное)
    val symptoms: List<SymptomData> = emptyList(),
    val weight: Float? = null,
    val temperature: Float? = null,
    val sleepHours: Float? = null,
    val waterIntake: Float? = null, // в литрах
    val energy: Int? = null, // 0-100
    val libido: Int? = null, // 0-2 (0=низкое, 1=среднее, 2=высокое)
    val sexualActivity: Boolean = false,
    val vitamins: Boolean = false,
    val notes: String = ""
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): DayData {
            val symptomsList = (map["symptoms"] as? List<*>)?.mapNotNull { symptomMap ->
                val sm = symptomMap as? Map<*, *>
                if (sm != null) {
                    SymptomData(
                        name = sm["name"] as? String ?: "",
                        category = try {
                            SymptomCategory.valueOf(sm["category"] as? String ?: "PHYSICAL")
                        } catch (e: Exception) {
                            SymptomCategory.PHYSICAL
                        },
                        intensity = (sm["intensity"] as? Long)?.toInt() ?: 1,
                        notes = sm["notes"] as? String ?: ""
                    )
                } else null
            } ?: emptyList()
            
            return DayData(
                date = map["date"] as? String ?: "",
                mood = (map["mood"] as? Long)?.toInt() ?: (map["mood"] as? Int) ?: -1,
                symptoms = symptomsList,
                weight = when (val w = map["weight"]) {
                    is String -> w.toFloatOrNull()
                    is Double -> w.toFloat()
                    is Float -> w
                    is Number -> w.toFloat()
                    else -> null
                },
                temperature = when (val t = map["temperature"]) {
                    is String -> t.toFloatOrNull()
                    is Double -> t.toFloat()
                    is Float -> t
                    is Number -> t.toFloat()
                    else -> null
                },
                sleepHours = when (val s = map["sleepHours"]) {
                    is String -> s.toFloatOrNull()
                    is Double -> s.toFloat()
                    is Float -> s
                    is Number -> s.toFloat()
                    else -> null
                },
                waterIntake = when (val w = map["waterIntake"]) {
                    is String -> w.toFloatOrNull()
                    is Double -> w.toFloat()
                    is Float -> w
                    is Number -> w.toFloat()
                    else -> null
                },
                energy = (map["energy"] as? Long)?.toInt() ?: (map["energy"] as? Int),
                libido = (map["libido"] as? Long)?.toInt() ?: (map["libido"] as? Int),
                sexualActivity = map["sexualActivity"] as? Boolean ?: false,
                vitamins = map["vitamins"] as? Boolean ?: false,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
}

data class SymptomData(
    val name: String,
    val category: SymptomCategory,
    val intensity: Int = 1, // 1-3 (1=легкая, 2=средняя, 3=сильная)
    val notes: String = "" // Заметки к симптому (опционально)
) : Serializable

enum class SymptomCategory {
    PHYSICAL,
    EMOTIONAL,
    ACTIVITY,
    HABITS
}

