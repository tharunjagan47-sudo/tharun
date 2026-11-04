package com.simats.diawell123

import com.google.gson.Gson
import com.simats.diawell123.data.api.NetworkConstants
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.data.model.ChatRequest
import com.simats.diawell123.data.model.ChatResponse
import com.simats.diawell123.ui.ai.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit & Integration tests verifying:
 * 1. Network configuration for direct FastAPI communication
 * 2. Gson request serialization (`prompt` field) & response deserialization (`response` field)
 * 3. Clinical emergency alert detection & macro highlighting
 */
class ChatViewModelTest {

    private val gson = Gson()

    @Test
    fun test_network_constants_defaults() {
        // PHP Backend for Auth & Profiles
        assertEquals("http://10.0.2.2/", NetworkConstants.EMULATOR_PHP_BASE_URL)
        assertEquals("http://10.0.2.2/", NetworkConstants.BASE_URL)

        // FastAPI Backend for Dr. Jarvis AI Microservice
        assertEquals("http://10.0.2.2:8000/", NetworkConstants.EMULATOR_AI_BASE_URL)
        assertEquals("http://10.0.2.2:8000/", NetworkConstants.AI_BASE_URL)
        assertEquals("http://172.23.176.99:8000/", NetworkConstants.PHYSICAL_DEVICE_AI_LAN_URL)
        assertEquals("api/v1/chat", NetworkConstants.CHAT_ENDPOINT)
    }

    @Test
    fun test_chat_request_serialization() {
        val request = ChatRequest(
            prompt = "What are 2 good breakfast options for Type 2 diabetes?",
            model = "qwen2.5:3b",
            temperature = 0.7,
            userId = 8
        )
        val json = gson.toJson(request)

        assertTrue(json.contains("prompt"))
        assertTrue(json.contains("What are 2 good breakfast options"))
        assertTrue(json.contains("qwen2.5:3b"))
        assertTrue(json.contains("0.7"))
        assertTrue(json.contains("8"))
    }

    @Test
    fun test_chat_response_deserialization_fastapi() {
        val rawJson = "{\"success\":true,\"model\":\"qwen2.5:3b\",\"response\":\"Hello! For a balanced breakfast, I recommend steel-cut oats with almonds.\",\"created_at\":\"2026-11-04T05:00:13Z\",\"duration_ms\":12039.84}"

        val parsed = gson.fromJson(rawJson, ChatResponse::class.java)
        assertTrue(parsed.success)
        assertEquals("qwen2.5:3b", parsed.model)
        assertEquals("Hello! For a balanced breakfast, I recommend steel-cut oats with almonds.", parsed.getCleanResponse())
        assertEquals(12039.84, parsed.durationMs ?: 0.0, 0.01)
    }

    @Test
    fun test_chat_response_deserialization_fallback_message() {
        val rawJson = "{\"success\":true,\"message\":\"AI fallback response message.\"}"

        val parsed = gson.fromJson(rawJson, ChatResponse::class.java)
        assertTrue(parsed.success)
        assertEquals("AI fallback response message.", parsed.getCleanResponse())
    }

    @Test
    fun test_chat_response_clean_response_empty() {
        val emptyResp = ChatResponse(success = false, response = null, message = null)
        assertEquals("", emptyResp.getCleanResponse())
    }

    @Test
    fun test_detect_hypoglycemia_alert() {
        val text1 = "Blood glucose < 70 mg/dL: Exercise is contraindicated. Consume 15g fast-acting carbs."
        assertEquals(AlertType.HYPOGLYCEMIA_WARNING, ChatViewModel.detectAlertType(text1))

        val text2 = "Your symptoms indicate hypoglycemia. Re-check in 15 minutes."
        assertEquals(AlertType.HYPOGLYCEMIA_WARNING, ChatViewModel.detectAlertType(text2))
    }

    @Test
    fun test_detect_hyperglycemia_alert() {
        val text = "Current glucose reading is > 250 mg/dL. Elevated glucose. Only light walking allowed."
        assertEquals(AlertType.HYPERGLYCEMIA_WARNING, ChatViewModel.detectAlertType(text))
    }

    @Test
    fun test_detect_ketone_alert() {
        val text = "Please test for urine ketones before starting exercise if reading is above 250 mg/dL."
        assertEquals(AlertType.KETONE_WARNING, ChatViewModel.detectAlertType(text))
    }

    @Test
    fun test_detect_contraindicated_alert() {
        val text = "Vigorous aerobic workout is contraindicated under current readings. Seek emergency care if symptoms persist."
        assertEquals(AlertType.GENERAL_CLINICAL_ALERT, ChatViewModel.detectAlertType(text))
    }

    @Test
    fun test_extract_high_fibre_metric_highlight() {
        val text = "Semolina dhokla has 5.1g fibre per 100g and 195 kcal with 30.9g carbs."
        val metrics = ChatViewModel.extractNutritionMetrics(text)

        val fibreMetric = metrics.firstOrNull { it.badgeType == BadgeType.HIGH_FIBRE }
        assertNotNull(fibreMetric)
        assertTrue(fibreMetric!!.isHighlight)
        assertEquals("🌾 High-Fibre: 5.1g", fibreMetric.label)

        val calMetric = metrics.firstOrNull { it.badgeType == BadgeType.CALORIES }
        assertNotNull(calMetric)
        assertEquals("🔥 195 kcal", calMetric!!.label)

        val carbMetric = metrics.firstOrNull { it.badgeType == BadgeType.CARBOHYDRATES }
        assertNotNull(carbMetric)
        assertEquals("🍞 Carbs: 30.9g", carbMetric!!.label)
    }

    @Test
    fun test_extract_low_fibre_metric_no_highlight() {
        val text = "Dal dhokli provides 1.86g fibre and 68.7 kcal per 100g."
        val metrics = ChatViewModel.extractNutritionMetrics(text)

        val fibreMetric = metrics.firstOrNull { it.badgeType == BadgeType.HIGH_FIBRE }
        assertNotNull(fibreMetric)
        assertFalse(fibreMetric!!.isHighlight)
        assertEquals("🌾 Fibre: 1.86g", fibreMetric.label)
    }

    @Test
    fun test_extract_met_activity_metric() {
        val text = "Brisk walking at 3.5 mph has an estimated intensity of 4.3 METs."
        val metrics = ChatViewModel.extractNutritionMetrics(text)

        val metMetric = metrics.firstOrNull { it.badgeType == BadgeType.MET_ACTIVITY }
        assertNotNull(metMetric)
        assertTrue(metMetric!!.isHighlight)
        assertEquals("⚡ 4.3 METs", metMetric.label)
    }
}
