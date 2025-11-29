package com.example.womenhealthtracker

import java.io.Serializable

// Результат теста на овуляцию
enum class OvulationTestResult {
    NEGATIVE,      // Отрицательный
    WEAK,          // Слабый
    POSITIVE       // Положительный
}

// Тип цервикальной жидкости
enum class CervicalFluidType {
    DRY,           // Сухой
    STICKY,        // Клейкий
    CREAMY,        // Кремовый
    EGG_WHITE      // Яичный белок
}

// Данные о фертильности
data class FertilityData(
    val date: String, // yyyy-MM-dd
    val bbt: Float? = null, // Базальная температура тела
    val testResult: OvulationTestResult? = null, // Результат теста на овуляцию
    val fluidType: CervicalFluidType? = null, // Тип цервикальной жидкости
    val notes: String = ""
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): FertilityData {
            val testResultString = map["testResult"] as? String
            val testResult = testResultString?.let {
                try {
                    OvulationTestResult.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            val fluidTypeString = map["fluidType"] as? String
            val fluidType = fluidTypeString?.let {
                try {
                    CervicalFluidType.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            return FertilityData(
                date = map["date"] as? String ?: "",
                bbt = (map["bbt"] as? Double)?.toFloat(),
                testResult = testResult,
                fluidType = fluidType,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "date" to date,
            "notes" to notes
        )
        
        bbt?.let { map["bbt"] = it }
        testResult?.let { map["testResult"] = it.name }
        fluidType?.let { map["fluidType"] = it.name }
        
        return map
    }
}







