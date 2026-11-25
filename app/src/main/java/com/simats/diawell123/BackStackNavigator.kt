package com.simats.diawell123

/**
 * Pure state helpers for DiaWell's unified, rotation-safe navigation back stack.
 */
object BackStackNavigator {
    val rootExitScreens: Set<String> = setOf(
        "splash",
        "home",
        "login",
        "onboarding",
        "signup",
        "patient_profile",
        "app_lock",
    )

    fun sanitizeStack(
        routes: List<String>,
        fallback: String = "home",
    ): List<String> {
        val cleaned = routes.filter {
            it.isNotBlank() && it != "splash" && it != "logout_dialog"
        }
        return cleaned.ifEmpty { listOf(fallback) }
    }

    fun pushScreen(stack: MutableList<String>, route: String): Boolean {
        val current = stack.lastOrNull()
        if (current == route) return false
        if (stack.size == 1 && stack[0] == "splash") {
            stack.clear()
        }
        stack.add(route)
        return true
    }

    fun replaceRoot(stack: MutableList<String>, route: String) {
        stack.clear()
        stack.add(route)
    }

    fun replaceTop(stack: MutableList<String>, route: String) {
        if (stack.isNotEmpty()) {
            stack[stack.lastIndex] = route
        } else {
            stack.add(route)
        }
    }

    /**
     * Switches top-level tabs while keeping `"home"` as the dashboard root
     * so pressing system back from any secondary tab returns to Home first.
     */
    fun selectBottomTab(stack: MutableList<String>, tabRoute: String): Boolean {
        val current = stack.lastOrNull()
        if (current == tabRoute) return false
        stack.clear()
        stack.add("home")
        if (tabRoute != "home") {
            stack.add(tabRoute)
        }
        return true
    }

    fun canHandleSystemBack(
        stack: List<String>,
        hasOpenOverlay: Boolean = false,
    ): Boolean {
        if (hasOpenOverlay) return true
        if (stack.size > 1) return true
        val current = stack.lastOrNull() ?: "home"
        return current !in rootExitScreens
    }

    /**
     * Pops the top entry off the stack, or returns to `"home"` if a non-root
     * single screen is active. Returns true if the stack changed as a pop.
     */
    fun popBackStack(stack: MutableList<String>): Boolean {
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            return true
        }
        val current = stack.lastOrNull() ?: "home"
        if (current !in rootExitScreens) {
            stack.clear()
            stack.add("home")
            return true
        }
        return false
    }

    /**
     * Pops back up to the most recent occurrence of [targetRoute].
     * Returns true if entries were popped, false if replaced/pushed.
     */
    fun popUpTo(
        stack: MutableList<String>,
        targetRoute: String,
        replaceTopFallback: Boolean = false,
    ): Boolean {
        val targetIdx = stack.lastIndexOf(targetRoute)
        if (targetIdx >= 0) {
            if (targetIdx < stack.lastIndex) {
                while (stack.lastIndex > targetIdx) {
                    stack.removeAt(stack.lastIndex)
                }
                return true
            }
            return false
        }
        if (replaceTopFallback) {
            replaceTop(stack, targetRoute)
        } else {
            pushScreen(stack, targetRoute)
        }
        return false
    }
}
