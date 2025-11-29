package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для MenopauseData (MenopauseSymptom и MRSResult)
 */
class MenopauseDataTest {

    // ========== Тесты для MenopauseSymptom ==========

    @Test
    fun `test MenopauseSymptom creation with all fields`() {
        val symptom = MenopauseSymptom(
            type = "приливы",
            intensity = 4,
            triggers = listOf("кофеин", "стресс"),
            date = "2024-01-15"
        )

        assertEquals("приливы", symptom.type)
        assertEquals(4, symptom.intensity)
        assertEquals(listOf("кофеин", "стресс"), symptom.triggers)
        assertEquals("2024-01-15", symptom.date)
    }

    @Test
    fun `test MenopauseSymptom creation with default values`() {
        val symptom = MenopauseSymptom(
            type = "потливость",
            date = "2024-01-15"
        )

        assertEquals("потливость", symptom.type)
        assertEquals(1, symptom.intensity)
        assertEquals(emptyList<String>(), symptom.triggers)
        assertEquals("2024-01-15", symptom.date)
    }

    @Test
    fun `test MenopauseSymptom toMap`() {
        val symptom = MenopauseSymptom(
            type = "приливы",
            intensity = 5,
            triggers = listOf("кофеин", "алкоголь"),
            date = "2024-01-15"
        )

        val map = symptom.toMap()

        assertEquals("приливы", map["type"])
        assertEquals(5, map["intensity"])
        assertEquals(listOf("кофеин", "алкоголь"), map["triggers"])
        assertEquals("2024-01-15", map["date"])
    }

    @Test
    fun `test MenopauseSymptom fromMap with all fields`() {
        val map = mapOf(
            "type" to "приливы",
            "intensity" to 4L,
            "triggers" to listOf("кофеин", "стресс"),
            "date" to "2024-01-15"
        )

        val symptom = MenopauseSymptom.fromMap(map)

        assertEquals("приливы", symptom.type)
        assertEquals(4, symptom.intensity)
        assertEquals(listOf("кофеин", "стресс"), symptom.triggers)
        assertEquals("2024-01-15", symptom.date)
    }

    @Test
    fun `test MenopauseSymptom fromMap with missing fields`() {
        val map = mapOf(
            "type" to "потливость",
            "date" to "2024-01-15"
        )

        val symptom = MenopauseSymptom.fromMap(map)

        assertEquals("потливость", symptom.type)
        assertEquals(1, symptom.intensity) // default value
        assertEquals(emptyList<String>(), symptom.triggers)
        assertEquals("2024-01-15", symptom.date)
    }

    @Test
    fun `test MenopauseSymptom fromMap with null values`() {
        val map = mapOf<String, Any>(
            "type" to null as Any? ?: "",
            "intensity" to null as Any? ?: 1L,
            "triggers" to null as Any? ?: emptyList<String>(),
            "date" to null as Any? ?: ""
        )

        val symptom = MenopauseSymptom.fromMap(map)

        assertEquals("", symptom.type)
        assertEquals(1, symptom.intensity)
        assertEquals(emptyList<String>(), symptom.triggers)
        assertEquals("", symptom.date)
    }

    @Test
    fun `test MenopauseSymptom roundtrip conversion`() {
        val original = MenopauseSymptom(
            type = "перепады настроения",
            intensity = 3,
            triggers = listOf("стресс", "недосып"),
            date = "2024-01-15"
        )

        val map = original.toMap()
        val restored = MenopauseSymptom.fromMap(map)

        assertEquals(original.type, restored.type)
        assertEquals(original.intensity, restored.intensity)
        assertEquals(original.triggers, restored.triggers)
        assertEquals(original.date, restored.date)
    }

    @Test
    fun `test MenopauseSymptom intensity bounds`() {
        val symptom1 = MenopauseSymptom("тип", 1, emptyList(), "2024-01-15")
        val symptom5 = MenopauseSymptom("тип", 5, emptyList(), "2024-01-15")

        assertEquals(1, symptom1.intensity)
        assertEquals(5, symptom5.intensity)
    }

    // ========== Тесты для MRSResult ==========

    @Test
    fun `test MRSResult creation with all fields`() {
        val categoryScores = mapOf(
            "соматические" to 8,
            "психологические" to 6,
            "урогенитальные" to 4
        )

        val result = MRSResult(
            totalScore = 18,
            categoryScores = categoryScores,
            testDate = "2024-01-15"
        )

        assertEquals(18, result.totalScore)
        assertEquals(8, result.categoryScores["соматические"])
        assertEquals(6, result.categoryScores["психологические"])
        assertEquals(4, result.categoryScores["урогенитальные"])
        assertEquals("2024-01-15", result.testDate)
    }

    @Test
    fun `test MRSResult toMap`() {
        val categoryScores = mapOf(
            "соматические" to 8,
            "психологические" to 6
        )

        val result = MRSResult(
            totalScore = 14,
            categoryScores = categoryScores,
            testDate = "2024-01-15"
        )

        val map = result.toMap()

        assertEquals(14, map["totalScore"])
        assertEquals(categoryScores, map["categoryScores"])
        assertEquals("2024-01-15", map["testDate"])
    }

    @Test
    fun `test MRSResult fromMap with all fields`() {
        val categoryScoresMap = mapOf(
            "соматические" to 8L,
            "психологические" to 6L,
            "урогенитальные" to 4L
        )

        val map = mapOf(
            "totalScore" to 18L,
            "categoryScores" to categoryScoresMap,
            "testDate" to "2024-01-15"
        )

        val result = MRSResult.fromMap(map)

        assertEquals(18, result.totalScore)
        assertEquals(8, result.categoryScores["соматические"])
        assertEquals(6, result.categoryScores["психологические"])
        assertEquals(4, result.categoryScores["урогенитальные"])
        assertEquals("2024-01-15", result.testDate)
    }

    @Test
    fun `test MRSResult fromMap with missing fields`() {
        val map = mapOf<String, Any>(
            "testDate" to "2024-01-15"
        )

        val result = MRSResult.fromMap(map)

        assertEquals(0, result.totalScore)
        assertEquals(emptyMap<String, Int>(), result.categoryScores)
        assertEquals("2024-01-15", result.testDate)
    }

    @Test
    fun `test MRSResult fromMap with empty categoryScores`() {
        val map = mapOf(
            "totalScore" to 10L,
            "categoryScores" to emptyMap<String, Long>(),
            "testDate" to "2024-01-15"
        )

        val result = MRSResult.fromMap(map)

        assertEquals(10, result.totalScore)
        assertEquals(emptyMap<String, Int>(), result.categoryScores)
        assertEquals("2024-01-15", result.testDate)
    }

    @Test
    fun `test MRSResult fromMap with invalid categoryScores entries`() {
        val invalidMap = mapOf(
            "validKey" to 5L,
            "invalidValue" to "not a number",
            "nullKey" to null
        )

        val map = mapOf(
            "totalScore" to 5L,
            "categoryScores" to invalidMap,
            "testDate" to "2024-01-15"
        )

        val result = MRSResult.fromMap(map)

        assertEquals(5, result.totalScore)
        assertEquals(1, result.categoryScores.size)
        assertEquals(5, result.categoryScores["validKey"])
        assertFalse(result.categoryScores.containsKey("invalidValue"))
        assertFalse(result.categoryScores.containsKey("nullKey"))
    }

    @Test
    fun `test MRSResult roundtrip conversion`() {
        val categoryScores = mapOf(
            "соматические" to 8,
            "психологические" to 6,
            "урогенитальные" to 4
        )

        val original = MRSResult(
            totalScore = 18,
            categoryScores = categoryScores,
            testDate = "2024-01-15"
        )

        val map = original.toMap()
        val restored = MRSResult.fromMap(map)

        assertEquals(original.totalScore, restored.totalScore)
        assertEquals(original.categoryScores, restored.categoryScores)
        assertEquals(original.testDate, restored.testDate)
    }
}

