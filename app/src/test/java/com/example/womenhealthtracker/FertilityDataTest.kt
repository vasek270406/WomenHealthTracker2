package com.example.womenhealthtracker

import org.junit.Test
import org.junit.Assert.*

/**
 * Тесты для FertilityData
 */
class FertilityDataTest {

    @Test
    fun `test FertilityData creation with all fields`() {
        val data = FertilityData(
            date = "2024-01-15",
            bbt = 36.5f,
            testResult = OvulationTestResult.POSITIVE,
            fluidType = CervicalFluidType.EGG_WHITE,
            notes = "Test notes"
        )

        assertEquals("2024-01-15", data.date)
        assertEquals(36.5f, data.bbt)
        assertEquals(OvulationTestResult.POSITIVE, data.testResult)
        assertEquals(CervicalFluidType.EGG_WHITE, data.fluidType)
        assertEquals("Test notes", data.notes)
    }

    @Test
    fun `test FertilityData creation with minimal fields`() {
        val data = FertilityData(
            date = "2024-01-15"
        )

        assertEquals("2024-01-15", data.date)
        assertNull(data.bbt)
        assertNull(data.testResult)
        assertNull(data.fluidType)
        assertEquals("", data.notes)
    }

    @Test
    fun `test FertilityData toMap with all fields`() {
        val data = FertilityData(
            date = "2024-01-15",
            bbt = 36.5f,
            testResult = OvulationTestResult.POSITIVE,
            fluidType = CervicalFluidType.EGG_WHITE,
            notes = "Test notes"
        )

        val map = data.toMap()

        assertEquals("2024-01-15", map["date"])
        assertEquals(36.5f, map["bbt"])
        assertEquals("POSITIVE", map["testResult"])
        assertEquals("EGG_WHITE", map["fluidType"])
        assertEquals("Test notes", map["notes"])
    }

    @Test
    fun `test FertilityData toMap with null optional fields`() {
        val data = FertilityData(
            date = "2024-01-15",
            notes = "Test"
        )

        val map = data.toMap()

        assertEquals("2024-01-15", map["date"])
        assertEquals("Test", map["notes"])
        assertFalse(map.containsKey("bbt"))
        assertFalse(map.containsKey("testResult"))
        assertFalse(map.containsKey("fluidType"))
    }

    @Test
    fun `test FertilityData fromMap with all fields`() {
        val map = mapOf(
            "date" to "2024-01-15",
            "bbt" to 36.5,
            "testResult" to "POSITIVE",
            "fluidType" to "EGG_WHITE",
            "notes" to "Test notes"
        )

        val data = FertilityData.fromMap(map)

        assertEquals("2024-01-15", data.date)
        assertEquals(36.5f, data.bbt)
        assertEquals(OvulationTestResult.POSITIVE, data.testResult)
        assertEquals(CervicalFluidType.EGG_WHITE, data.fluidType)
        assertEquals("Test notes", data.notes)
    }

    @Test
    fun `test FertilityData fromMap with minimal fields`() {
        val map = mapOf(
            "date" to "2024-01-15"
        )

        val data = FertilityData.fromMap(map)

        assertEquals("2024-01-15", data.date)
        assertNull(data.bbt)
        assertNull(data.testResult)
        assertNull(data.fluidType)
        assertEquals("", data.notes)
    }

    @Test
    fun `test FertilityData fromMap with invalid enum values`() {
        val map = mapOf(
            "date" to "2024-01-15",
            "testResult" to "INVALID_VALUE",
            "fluidType" to "INVALID_TYPE"
        )

        val data = FertilityData.fromMap(map)

        assertEquals("2024-01-15", data.date)
        assertNull(data.testResult)
        assertNull(data.fluidType)
    }

    @Test
    fun `test FertilityData fromMap with null values`() {
        val map = mapOf<String, Any>(
            "date" to "2024-01-15",
            "bbt" to null as Any? ?: "",
            "testResult" to null as Any? ?: "",
            "fluidType" to null as Any? ?: ""
        )

        val data = FertilityData.fromMap(map)

        assertEquals("2024-01-15", data.date)
        assertNull(data.bbt)
        assertNull(data.testResult)
        assertNull(data.fluidType)
    }

    @Test
    fun `test FertilityData roundtrip conversion`() {
        val original = FertilityData(
            date = "2024-01-15",
            bbt = 36.5f,
            testResult = OvulationTestResult.WEAK,
            fluidType = CervicalFluidType.CREAMY,
            notes = "Roundtrip test"
        )

        val map = original.toMap()
        val restored = FertilityData.fromMap(map)

        assertEquals(original.date, restored.date)
        assertEquals(original.bbt, restored.bbt)
        assertEquals(original.testResult, restored.testResult)
        assertEquals(original.fluidType, restored.fluidType)
        assertEquals(original.notes, restored.notes)
    }

    @Test
    fun `test all OvulationTestResult enum values`() {
        assertEquals(OvulationTestResult.NEGATIVE, OvulationTestResult.valueOf("NEGATIVE"))
        assertEquals(OvulationTestResult.WEAK, OvulationTestResult.valueOf("WEAK"))
        assertEquals(OvulationTestResult.POSITIVE, OvulationTestResult.valueOf("POSITIVE"))
    }

    @Test
    fun `test all CervicalFluidType enum values`() {
        assertEquals(CervicalFluidType.DRY, CervicalFluidType.valueOf("DRY"))
        assertEquals(CervicalFluidType.STICKY, CervicalFluidType.valueOf("STICKY"))
        assertEquals(CervicalFluidType.CREAMY, CervicalFluidType.valueOf("CREAMY"))
        assertEquals(CervicalFluidType.EGG_WHITE, CervicalFluidType.valueOf("EGG_WHITE"))
    }
}

