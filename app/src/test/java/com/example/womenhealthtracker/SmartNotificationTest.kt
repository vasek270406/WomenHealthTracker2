package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для SmartNotification
 */
class SmartNotificationTest {

    @Test
    fun `test SmartNotification creation with all fields`() {
        val notification = SmartNotification(
            id = "test_id_1",
            type = NotificationType.DAILY_REMINDER,
            title = "Test Title",
            body = "Test Body",
            scheduledHour = 20,
            scheduledMinute = 30,
            targetMode = UserGoal.CYCLE_TRACKING,
            isEnabled = true,
            repeatDaily = true,
            repeatWeekly = false,
            repeatMonthly = false
        )

        assertEquals("test_id_1", notification.id)
        assertEquals(NotificationType.DAILY_REMINDER, notification.type)
        assertEquals("Test Title", notification.title)
        assertEquals("Test Body", notification.body)
        assertEquals(20, notification.scheduledHour)
        assertEquals(30, notification.scheduledMinute)
        assertEquals(UserGoal.CYCLE_TRACKING, notification.targetMode)
        assertTrue(notification.isEnabled)
        assertTrue(notification.repeatDaily)
        assertFalse(notification.repeatWeekly)
        assertFalse(notification.repeatMonthly)
    }

    @Test
    fun `test SmartNotification creation with default values`() {
        val notification = SmartNotification(
            id = "test_id_2",
            type = NotificationType.FERTILITY_WINDOW,
            title = "Title",
            body = "Body",
            scheduledHour = 9,
            scheduledMinute = 0,
            targetMode = UserGoal.PREGNANCY
        )

        assertTrue(notification.isEnabled)
        assertTrue(notification.repeatDaily)
        assertFalse(notification.repeatWeekly)
        assertFalse(notification.repeatMonthly)
    }

    @Test
    fun `test SmartNotification toMap`() {
        val notification = SmartNotification(
            id = "test_id_3",
            type = NotificationType.PREGNANCY_CHECKUP,
            title = "Checkup Reminder",
            body = "Don't forget your appointment",
            scheduledHour = 10,
            scheduledMinute = 15,
            targetMode = UserGoal.PREGNANCY,
            isEnabled = true,
            repeatDaily = false,
            repeatWeekly = true,
            repeatMonthly = false
        )

        val map = notification.toMap()

        assertEquals("test_id_3", map["id"])
        assertEquals("PREGNANCY_CHECKUP", map["type"])
        assertEquals("Checkup Reminder", map["title"])
        assertEquals("Don't forget your appointment", map["body"])
        assertEquals(10, map["scheduledHour"])
        assertEquals(15, map["scheduledMinute"])
        assertEquals("PREGNANCY", map["targetMode"])
        assertEquals(true, map["isEnabled"])
        assertEquals(false, map["repeatDaily"])
        assertEquals(true, map["repeatWeekly"])
        assertEquals(false, map["repeatMonthly"])
    }

    @Test
    fun `test SmartNotification fromMap with all fields`() {
        val map = mapOf(
            "id" to "test_id_4",
            "type" to "SYMPTOM_TRACKING",
            "title" to "Track Symptoms",
            "body" to "Time to log your symptoms",
            "scheduledHour" to 18L,
            "scheduledMinute" to 45L,
            "targetMode" to "MENOPAUSE",
            "isEnabled" to true,
            "repeatDaily" to true,
            "repeatWeekly" to false,
            "repeatMonthly" to true
        )

        val notification = SmartNotification.fromMap(map)

        assertEquals("test_id_4", notification.id)
        assertEquals(NotificationType.SYMPTOM_TRACKING, notification.type)
        assertEquals("Track Symptoms", notification.title)
        assertEquals("Time to log your symptoms", notification.body)
        assertEquals(18, notification.scheduledHour)
        assertEquals(45, notification.scheduledMinute)
        assertEquals(UserGoal.MENOPAUSE, notification.targetMode)
        assertTrue(notification.isEnabled)
        assertTrue(notification.repeatDaily)
        assertFalse(notification.repeatWeekly)
        assertTrue(notification.repeatMonthly)
    }

    @Test
    fun `test SmartNotification fromMap with missing fields`() {
        val map = mapOf(
            "id" to "test_id_5",
            "title" to "Title",
            "body" to "Body"
        )

        val notification = SmartNotification.fromMap(map)

        assertEquals("test_id_5", notification.id)
        assertEquals(NotificationType.DAILY_REMINDER, notification.type) // default
        assertEquals("Title", notification.title)
        assertEquals("Body", notification.body)
        assertEquals(20, notification.scheduledHour) // default
        assertEquals(0, notification.scheduledMinute) // default
        assertEquals(UserGoal.CYCLE_TRACKING, notification.targetMode) // default
        assertTrue(notification.isEnabled) // default
        assertTrue(notification.repeatDaily) // default
        assertFalse(notification.repeatWeekly) // default
        assertFalse(notification.repeatMonthly) // default
    }

    @Test
    fun `test SmartNotification fromMap with invalid enum values`() {
        val map = mapOf(
            "id" to "test_id_6",
            "type" to "INVALID_TYPE",
            "targetMode" to "INVALID_MODE",
            "title" to "Title",
            "body" to "Body"
        )

        val notification = SmartNotification.fromMap(map)

        assertEquals("test_id_6", notification.id)
        assertEquals(NotificationType.DAILY_REMINDER, notification.type) // fallback to default
        assertEquals(UserGoal.CYCLE_TRACKING, notification.targetMode) // fallback to default
    }

    @Test
    fun `test SmartNotification fromMap with null values`() {
        val map = mapOf<String, Any>(
            "id" to null as Any? ?: "",
            "type" to null as Any? ?: "DAILY_REMINDER",
            "title" to null as Any? ?: "",
            "body" to null as Any? ?: "",
            "scheduledHour" to null as Any? ?: 20L,
            "scheduledMinute" to null as Any? ?: 0L,
            "targetMode" to null as Any? ?: "CYCLE_TRACKING"
        )

        val notification = SmartNotification.fromMap(map)

        assertEquals("", notification.id)
        assertEquals(NotificationType.DAILY_REMINDER, notification.type)
        assertEquals("", notification.title)
        assertEquals("", notification.body)
        assertEquals(20, notification.scheduledHour)
        assertEquals(0, notification.scheduledMinute)
        assertEquals(UserGoal.CYCLE_TRACKING, notification.targetMode)
    }

    @Test
    fun `test SmartNotification roundtrip conversion`() {
        val original = SmartNotification(
            id = "test_id_7",
            type = NotificationType.CYCLE_PREDICTION,
            title = "Cycle Prediction",
            body = "Your period is coming",
            scheduledHour = 8,
            scheduledMinute = 0,
            targetMode = UserGoal.CYCLE_TRACKING,
            isEnabled = false,
            repeatDaily = false,
            repeatWeekly = true,
            repeatMonthly = false
        )

        val map = original.toMap()
        val restored = SmartNotification.fromMap(map)

        assertEquals(original.id, restored.id)
        assertEquals(original.type, restored.type)
        assertEquals(original.title, restored.title)
        assertEquals(original.body, restored.body)
        assertEquals(original.scheduledHour, restored.scheduledHour)
        assertEquals(original.scheduledMinute, restored.scheduledMinute)
        assertEquals(original.targetMode, restored.targetMode)
        assertEquals(original.isEnabled, restored.isEnabled)
        assertEquals(original.repeatDaily, restored.repeatDaily)
        assertEquals(original.repeatWeekly, restored.repeatWeekly)
        assertEquals(original.repeatMonthly, restored.repeatMonthly)
    }

    @Test
    fun `test SmartNotification scheduled time bounds`() {
        val notification1 = SmartNotification(
            id = "test",
            type = NotificationType.DAILY_REMINDER,
            title = "Title",
            body = "Body",
            scheduledHour = 0,
            scheduledMinute = 0,
            targetMode = UserGoal.CYCLE_TRACKING
        )

        val notification2 = SmartNotification(
            id = "test",
            type = NotificationType.DAILY_REMINDER,
            title = "Title",
            body = "Body",
            scheduledHour = 23,
            scheduledMinute = 59,
            targetMode = UserGoal.CYCLE_TRACKING
        )

        assertEquals(0, notification1.scheduledHour)
        assertEquals(0, notification1.scheduledMinute)
        assertEquals(23, notification2.scheduledHour)
        assertEquals(59, notification2.scheduledMinute)
    }

    @Test
    fun `test all NotificationType enum values`() {
        assertEquals(NotificationType.DAILY_REMINDER, NotificationType.valueOf("DAILY_REMINDER"))
        assertEquals(NotificationType.CYCLE_PREDICTION, NotificationType.valueOf("CYCLE_PREDICTION"))
        assertEquals(NotificationType.FERTILITY_WINDOW, NotificationType.valueOf("FERTILITY_WINDOW"))
        assertEquals(NotificationType.PREGNANCY_CHECKUP, NotificationType.valueOf("PREGNANCY_CHECKUP"))
        assertEquals(NotificationType.SYMPTOM_TRACKING, NotificationType.valueOf("SYMPTOM_TRACKING"))
        assertEquals(NotificationType.MEDICAL_REMINDER, NotificationType.valueOf("MEDICAL_REMINDER"))
        assertEquals(NotificationType.CUSTOM, NotificationType.valueOf("CUSTOM"))
    }

    @Test
    fun `test all UserGoal enum values`() {
        assertEquals(UserGoal.CYCLE_TRACKING, UserGoal.valueOf("CYCLE_TRACKING"))
        assertEquals(UserGoal.PREGNANCY, UserGoal.valueOf("PREGNANCY"))
        assertEquals(UserGoal.MENOPAUSE, UserGoal.valueOf("MENOPAUSE"))
    }
}

