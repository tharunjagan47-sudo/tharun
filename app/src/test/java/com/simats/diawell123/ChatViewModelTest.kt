package com.simats.diawell123

import com.simats.diawell123.data.api.NetworkConstants
import com.simats.diawell123.data.model.AlertType
import com.simats.diawell123.data.model.BadgeType
import com.simats.diawell123.ui.ai.ChatViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying Dr. Jarvis Chat Clinical Highlighting & Macro Extraction Logic.
 */
class ChatViewModelTest {

    @Test
    fun test_network_constants_defaults() {
        assertEquals("http://10.0.2.2/", NetworkConstants.EMULATOR_BASE_URL)
        assertEquals("http://10.0.2.2/", NetworkConstants.BASE_URL)
        assertEquals("DiaWell_backend/ai/chat.php", NetworkConstants.CHAT_ENDPOINT)
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
        assertTrue("Fibre metric should be found", fibreMetric != null)
        assertTrue("Fibre >= 3.0g must be highlighted", fibreMetric!!.isHighlight)
        assertEquals("🌾 High-Fibre: 5.1g", fibreMetric.label)

        val calMetric = metrics.firstOrNull { it.badgeType == BadgeType.CALORIES }
        assertTrue("Calories metric should be found", calMetric != null)
        assertEquals("🔥 195 kcal", calMetric!!.label)

        val carbMetric = metrics.firstOrNull { it.badgeType == BadgeType.CARBOHYDRATES }
        assertTrue("Carb metric should be found", carbMetric != null)
        assertEquals("🍞 Carbs: 30.9g", carbMetric!!.label)
    }

    @Test
    fun test_extract_low_fibre_metric_no_highlight() {
        val text = "Dal dhokli provides 1.86g fibre and 68.7 kcal per 100g."
        val metrics = ChatViewModel.extractNutritionMetrics(text)

        val fibreMetric = metrics.firstOrNull { it.badgeType == BadgeType.HIGH_FIBRE }
        assertTrue("Fibre metric should be found", fibreMetric != null)
        assertFalse("Fibre < 3.0g must NOT be highlighted as high-fibre", fibreMetric!!.isHighlight)
        assertEquals("🌾 Fibre: 1.86g", fibreMetric.label)
    }

    @Test
    fun test_extract_met_activity_metric() {
        val text = "Brisk walking at 3.5 mph has an estimated intensity of 4.3 METs."
        val metrics = ChatViewModel.extractNutritionMetrics(text)

        val metMetric = metrics.firstOrNull { it.badgeType == BadgeType.MET_ACTIVITY }
        assertTrue("MET metric should be found", metMetric != null)
        assertTrue(metMetric!!.isHighlight)
        assertEquals("⚡ 4.3 METs", metMetric.label)
    }
}

