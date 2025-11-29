package com.example.womenhealthtracker

import java.io.Serializable

// Симптом менопаузы
data class MenopauseSymptom(
    val type: String, // приливы, потливость, перепады настроения, etc
    val intensity: Int = 1, // 1-5
    val triggers: List<String> = emptyList(), // кофеин, стресс, алкоголь, etc
    val date: String // yyyy-MM-dd
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): MenopauseSymptom {
            return MenopauseSymptom(
                type = map["type"] as? String ?: "",
                intensity = (map["intensity"] as? Long)?.toInt() ?: 1,
                triggers = (map["triggers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                date = map["date"] as? String ?: ""
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "type" to type,
            "intensity" to intensity,
            "triggers" to triggers,
            "date" to date
        )
    }
}

// Результат опросника шкалы менопаузы (MRS - Menopause Rating Scale)
data class MRSResult(
    val totalScore: Int,
    val categoryScores: Map<String, Int>, // категории: соматические, психологические, урогенитальные
    val testDate: String // yyyy-MM-dd
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): MRSResult {
            val categoryScoresMap = (map["categoryScores"] as? Map<*, *>)?.mapNotNull { entry ->
                val key = entry.key as? String
                val value = (entry.value as? Long)?.toInt()
                if (key != null && value != null) {
                    key to value
                } else null
            }?.toMap() ?: emptyMap()
            
            return MRSResult(
                totalScore = (map["totalScore"] as? Long)?.toInt() ?: 0,
                categoryScores = categoryScoresMap,
                testDate = map["testDate"] as? String ?: ""
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "totalScore" to totalScore,
            "categoryScores" to categoryScores,
            "testDate" to testDate
        )
    }
}







