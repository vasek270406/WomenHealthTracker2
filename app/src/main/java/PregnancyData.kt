package com.example.womenhealthtracker

import java.io.Serializable

// Симптом беременности
data class PregnancySymptom(
    val type: String, // тип симптома
    val intensity: Int = 1, // интенсивность 1-5
    val date: String // yyyy-MM-dd
) : Serializable

// Визит к врачу
data class DoctorVisit(
    val date: String, // yyyy-MM-dd
    val time: String = "", // HH:mm
    val type: String = "", // УЗИ, анализы, консультация
    val doctorName: String = "",
    val notes: String = "",
    val photos: List<String> = emptyList() // пути к фото УЗИ
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): DoctorVisit {
            return DoctorVisit(
                date = map["date"] as? String ?: "",
                time = map["time"] as? String ?: "",
                type = map["type"] as? String ?: "",
                doctorName = map["doctorName"] as? String ?: "",
                notes = map["notes"] as? String ?: "",
                photos = (map["photos"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "date" to date,
            "time" to time,
            "type" to type,
            "doctorName" to doctorName,
            "notes" to notes,
            "photos" to photos
        )
    }
}

// Шевеление ребенка
data class BabyKick(
    val date: String, // yyyy-MM-dd
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val kickCount: Int = 0
) : Serializable

// Пункт чек-листа беременности
data class PregnancyChecklistItem(
    val id: String,
    val title: String,
    val trimester: Int, // 1, 2, или 3
    val completed: Boolean = false,
    val completedDate: String = "" // yyyy-MM-dd
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): PregnancyChecklistItem {
            return PregnancyChecklistItem(
                id = map["id"] as? String ?: "",
                title = map["title"] as? String ?: "",
                trimester = (map["trimester"] as? Long)?.toInt() ?: 1,
                completed = map["completed"] as? Boolean ?: false,
                completedDate = map["completedDate"] as? String ?: ""
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "trimester" to trimester,
            "completed" to completed,
            "completedDate" to completedDate
        )
    }
}

// Данные о беременности
data class PregnancyData(
    val pregnancyStartDate: String, // yyyy-MM-dd - дата начала беременности
    val estimatedDueDate: String = "", // yyyy-MM-dd - предполагаемая дата родов
    val symptoms: List<PregnancySymptom> = emptyList(),
    val visits: List<DoctorVisit> = emptyList(),
    val kicks: List<BabyKick> = emptyList(),
    val checklist: List<PregnancyChecklistItem> = emptyList(),
    val notes: String = ""
) : Serializable {
    // Расчет недели беременности
    fun getCurrentWeek(): Int {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return try {
            val startDate = dateFormat.parse(pregnancyStartDate) ?: return 0
            val currentDate = java.util.Calendar.getInstance()
            val startCalendar = java.util.Calendar.getInstance()
            startCalendar.time = startDate
            
            val daysDiff = ((currentDate.timeInMillis - startCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff < 0) return 0
            (daysDiff / 7) + 1
        } catch (e: Exception) {
            0
        }
    }
    
    // Определение триместра
    fun getTrimester(): Int {
        val week = getCurrentWeek()
        return when {
            week <= 12 -> 1
            week <= 27 -> 2
            else -> 3
        }
    }
    
    // Расчет предполагаемой даты родов (280 дней от начала беременности)
    fun calculateDueDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return try {
            val startDate = dateFormat.parse(pregnancyStartDate) ?: return ""
            val dueCalendar = java.util.Calendar.getInstance()
            dueCalendar.time = startDate
            dueCalendar.add(java.util.Calendar.DAY_OF_YEAR, 280)
            dateFormat.format(dueCalendar.time)
        } catch (e: Exception) {
            ""
        }
    }
    
    companion object {
        fun fromMap(map: Map<String, Any>): PregnancyData {
            val symptomsList = (map["symptoms"] as? List<*>)?.mapNotNull { symptomMap ->
                val sm = symptomMap as? Map<*, *>
                if (sm != null) {
                    PregnancySymptom(
                        type = sm["type"] as? String ?: "",
                        intensity = (sm["intensity"] as? Long)?.toInt() ?: 1,
                        date = sm["date"] as? String ?: ""
                    )
                } else null
            } ?: emptyList()
            
            val visitsList = (map["visits"] as? List<*>)?.mapNotNull { visitMap ->
                val vm = visitMap as? Map<*, *>
                if (vm != null) {
                    val visitData = vm.mapKeys { it.key.toString() }.mapValues { it.value as Any }
                    DoctorVisit.fromMap(visitData)
                } else null
            } ?: emptyList()
            
            val checklistList = (map["checklist"] as? List<*>)?.mapNotNull { itemMap ->
                val im = itemMap as? Map<*, *>
                if (im != null) {
                    val checklistData = im.mapKeys { it.key.toString() }.mapValues { it.value as Any }
                    PregnancyChecklistItem.fromMap(checklistData)
                } else null
            } ?: emptyList()
            
            val kicksList = (map["kicks"] as? List<*>)?.mapNotNull { kickMap ->
                val km = kickMap as? Map<*, *>
                if (km != null) {
                    BabyKick(
                        date = km["date"] as? String ?: "",
                        startTime = km["startTime"] as? String ?: "",
                        endTime = km["endTime"] as? String ?: "",
                        kickCount = (km["kickCount"] as? Long)?.toInt() ?: 0
                    )
                } else null
            } ?: emptyList()
            
            return PregnancyData(
                pregnancyStartDate = map["pregnancyStartDate"] as? String ?: "",
                estimatedDueDate = map["estimatedDueDate"] as? String ?: "",
                symptoms = symptomsList,
                visits = visitsList,
                kicks = kicksList,
                checklist = checklistList,
                notes = map["notes"] as? String ?: ""
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "pregnancyStartDate" to pregnancyStartDate,
            "estimatedDueDate" to estimatedDueDate,
            "symptoms" to symptoms.map { 
                mapOf(
                    "type" to it.type,
                    "intensity" to it.intensity,
                    "date" to it.date
                )
            },
            "visits" to visits.map { it.toMap() },
            "kicks" to kicks.map {
                mapOf(
                    "date" to it.date,
                    "startTime" to it.startTime,
                    "endTime" to it.endTime,
                    "kickCount" to it.kickCount
                )
            },
            "checklist" to checklist.map { it.toMap() },
            "notes" to notes
        )
    }
}

