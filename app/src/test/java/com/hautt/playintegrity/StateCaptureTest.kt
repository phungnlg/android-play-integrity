package com.hautt.playintegrity

import com.hautt.playintegrity.model.AppStateSnapshot
import com.hautt.playintegrity.service.StateCapture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StateCaptureTest {

    @Test
    fun capture_returnsSnapshotWithCorrectFields() {
        val fields = mapOf("name" to "Alice", "amount" to "100")
        val selections = listOf("PAYMENT")
        val (snapshot, _) = StateCapture.capture("TestScreen", fields, selections)

        assertEquals("TestScreen", snapshot.screenName)
        assertEquals(fields, snapshot.visibleFields)
        assertEquals(selections, snapshot.selections)
        assertTrue(snapshot.capturedAtMillis > 0)
    }

    @Test
    fun toCanonicalJson_sortedKeys() {
        val snapshot = AppStateSnapshot(
            screenName = "Test",
            visibleFields = mapOf("z_field" to "1", "a_field" to "2"),
            selections = listOf("X"),
            capturedAtMillis = 1000
        )

        val json = StateCapture.toCanonicalJson(snapshot)

        // Keys should be alphabetically sorted in output
        val capturedIdx = json.indexOf("capturedAtMillis")
        val screenIdx = json.indexOf("screenName")
        val selectionsIdx = json.indexOf("selections")
        val visibleIdx = json.indexOf("visibleFields")

        assertTrue(capturedIdx < screenIdx)
        assertTrue(screenIdx < selectionsIdx)
        assertTrue(selectionsIdx < visibleIdx)
    }

    @Test
    fun toCanonicalJson_nestedMapSorted() {
        val snapshot = AppStateSnapshot(
            screenName = "Test",
            visibleFields = mapOf("z_key" to "val1", "a_key" to "val2"),
            selections = emptyList(),
            capturedAtMillis = 1000
        )

        val json = StateCapture.toCanonicalJson(snapshot)
        val aKeyIdx = json.indexOf("a_key")
        val zKeyIdx = json.indexOf("z_key")
        assertTrue(aKeyIdx < zKeyIdx)
    }

    @Test
    fun toCanonicalJson_deterministic() {
        val snapshot = AppStateSnapshot(
            screenName = "Home",
            visibleFields = mapOf("b" to "2", "a" to "1"),
            selections = listOf("SEL"),
            capturedAtMillis = 5000
        )

        val json1 = StateCapture.toCanonicalJson(snapshot)
        val json2 = StateCapture.toCanonicalJson(snapshot)
        assertEquals(json1, json2)
    }

    @Test
    fun toCanonicalJson_differentInputsDifferentOutput() {
        val snapshot1 = AppStateSnapshot(
            screenName = "Home",
            visibleFields = mapOf("text" to "hello"),
            selections = listOf("A"),
            capturedAtMillis = 1000
        )
        val snapshot2 = snapshot1.copy(
            visibleFields = mapOf("text" to "world")
        )

        val json1 = StateCapture.toCanonicalJson(snapshot1)
        val json2 = StateCapture.toCanonicalJson(snapshot2)
        assertNotEquals(json1, json2)
    }
}
