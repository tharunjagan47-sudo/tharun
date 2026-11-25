package com.simats.diawell123

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackStackNavigatorTest {

    @Test
    fun homeRootAllowsSystemExitWithoutOverlays() {
        val stack = mutableListOf("home")
        assertFalse(BackStackNavigator.canHandleSystemBack(stack, hasOpenOverlay = false))
        assertTrue(BackStackNavigator.canHandleSystemBack(stack, hasOpenOverlay = true))
        assertFalse(BackStackNavigator.popBackStack(stack))
        assertEquals(listOf("home"), stack)
    }

    @Test
    fun secondaryTabBackNavigatesToHomeBeforeExiting() {
        val stack = mutableListOf("home")
        assertTrue(BackStackNavigator.selectBottomTab(stack, "medications"))
        assertEquals(listOf("home", "medications"), stack)
        assertTrue(BackStackNavigator.canHandleSystemBack(stack))

        assertTrue(BackStackNavigator.selectBottomTab(stack, "settings"))
        assertEquals(listOf("home", "settings"), stack)

        assertTrue(BackStackNavigator.popBackStack(stack))
        assertEquals(listOf("home"), stack)
        assertFalse(BackStackNavigator.canHandleSystemBack(stack))
    }

    @Test
    fun deepSubScreenBackStackUnwindsInOrder() {
        val stack = mutableListOf("home")
        BackStackNavigator.selectBottomTab(stack, "settings")
        BackStackNavigator.pushScreen(stack, "privacy_security")
        BackStackNavigator.pushScreen(stack, "export_records")
        BackStackNavigator.pushScreen(stack, "export_report_viewer")

        assertEquals(
            listOf("home", "settings", "privacy_security", "export_records", "export_report_viewer"),
            stack
        )

        assertTrue(BackStackNavigator.popBackStack(stack))
        assertEquals("export_records", stack.last())

        assertTrue(BackStackNavigator.popBackStack(stack))
        assertEquals("privacy_security", stack.last())

        assertTrue(BackStackNavigator.popBackStack(stack))
        assertEquals("settings", stack.last())

        assertTrue(BackStackNavigator.popBackStack(stack))
        assertEquals("home", stack.last())

        assertFalse(BackStackNavigator.canHandleSystemBack(stack))
    }

    @Test
    fun popUpToClearsIntermediateScreensOnCompletion() {
        val stack = mutableListOf("home", "medications", "search_medication", "set_medication_reminder")
        val popped = BackStackNavigator.popUpTo(stack, "medications", replaceTopFallback = true)
        assertTrue(popped)
        assertEquals(listOf("home", "medications"), stack)

        val glucoseStack = mutableListOf("home", "log_glucose")
        val poppedGlucose = BackStackNavigator.popUpTo(glucoseStack, "glucose_history", replaceTopFallback = true)
        assertFalse(poppedGlucose)
        assertEquals(listOf("home", "glucose_history"), glucoseStack)
    }

    @Test
    fun sanitizeStackDropsSplashAndLogoutDialogAcrossRotation() {
        val restored = BackStackNavigator.sanitizeStack(
            listOf("splash", "home", "settings", "logout_dialog")
        )
        assertEquals(listOf("home", "settings"), restored)
        assertEquals(listOf("home"), BackStackNavigator.sanitizeStack(emptyList()))
    }
}
