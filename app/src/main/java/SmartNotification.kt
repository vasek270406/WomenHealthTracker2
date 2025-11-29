package com.example.womenhealthtracker

import java.io.Serializable

data class SmartNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val scheduledHour: Int,      // Час (0-23)
    val scheduledMinute: Int,    // Минута (0-59)
    val targetMode: UserGoal,    // Для какого режима
    val isEnabled: Boolean = true,
    val repeatDaily: Boolean = true,  // Повторять ежедневно
    val repeatWeekly: Boolean = false, // Повторять еженедельно
    val repeatMonthly: Boolean = false // Повторять ежемесячно
) : Serializable {
    companion object {
        fun fromMap(map: Map<String, Any>): SmartNotification {
            return SmartNotification(
                id = map["id"] as? String ?: "",
                type = try {
                    NotificationType.valueOf(map["type"] as? String ?: "DAILY_REMINDER")
                } catch (e: Exception) {
                    NotificationType.DAILY_REMINDER
                },
                title = map["title"] as? String ?: "",
                body = map["body"] as? String ?: "",
                scheduledHour = (map["scheduledHour"] as? Long)?.toInt() ?: 20,
                scheduledMinute = (map["scheduledMinute"] as? Long)?.toInt() ?: 0,
                targetMode = try {
                    UserGoal.valueOf(map["targetMode"] as? String ?: "CYCLE_TRACKING")
                } catch (e: Exception) {
                    UserGoal.CYCLE_TRACKING
                },
                isEnabled = map["isEnabled"] as? Boolean ?: true,
                repeatDaily = map["repeatDaily"] as? Boolean ?: true,
                repeatWeekly = map["repeatWeekly"] as? Boolean ?: false,
                repeatMonthly = map["repeatMonthly"] as? Boolean ?: false
            )
        }
    }
    
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "type" to type.name,
            "title" to title,
            "body" to body,
            "scheduledHour" to scheduledHour,
            "scheduledMinute" to scheduledMinute,
            "targetMode" to targetMode.name,
            "isEnabled" to isEnabled,
            "repeatDaily" to repeatDaily,
            "repeatWeekly" to repeatWeekly,
            "repeatMonthly" to repeatMonthly
        )
    }
}







