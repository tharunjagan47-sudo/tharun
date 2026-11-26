package com.simats.diawell123

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import com.simats.diawell123.service.DiaWellFirebaseMessagingService
import com.simats.diawell123.util.DiaWellNotificationHelper
import com.simats.diawell123.util.SmartNudgeEngine
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.repository.AuthRepositoryImpl
import com.simats.diawell123.ui.activity.ActivityHistoryScreen
import com.simats.diawell123.ui.activity.ActivityTrackingScreen
import com.simats.diawell123.ui.activity.ActivityViewModel
import com.simats.diawell123.ui.activity.CyclingScreen
import com.simats.diawell123.ui.activity.RunningScreen
import com.simats.diawell123.ui.activity.WalkingScreen
import com.simats.diawell123.ui.ai.AiChatbotScreen
import com.simats.diawell123.ui.ai.ChatViewModel
import com.simats.diawell123.ui.auth.AppLockScreen
import com.simats.diawell123.ui.auth.ForgotPasswordScreen
import com.simats.diawell123.ui.auth.LoginScreen
import com.simats.diawell123.ui.auth.SignUpScreen
import com.simats.diawell123.ui.components.DiaWellAmbientBackground
import com.simats.diawell123.ui.components.DiaWellAssistiveButton
import com.simats.diawell123.ui.diet.DailyDietPlanScreen
import com.simats.diawell123.ui.diet.DietViewModel
import com.simats.diawell123.ui.diet.ExploreFoodScreen
import com.simats.diawell123.ui.diet.FoodDetailsScreen
import com.simats.diawell123.ui.diet.FoodLogScreen
import com.simats.diawell123.ui.diet.NutritionSummaryScreen
import com.simats.diawell123.ui.glucose.GlucoseHistoryScreen
import com.simats.diawell123.ui.glucose.GlucoseViewModel
import com.simats.diawell123.ui.glucose.LogGlucoseScreen
import com.simats.diawell123.ui.glucose.ReadingDetailsScreen
import com.simats.diawell123.ui.home.HomeScreen
import com.simats.diawell123.ui.home.HomeViewModel
import com.simats.diawell123.ui.home.IosLiquidGlassBottomNav
import com.simats.diawell123.ui.home.DiaWellLiquidGlassNavRail
import com.simats.diawell123.ui.meds.MedicationReminderScreen
import com.simats.diawell123.ui.meds.MedicationsScreen
import com.simats.diawell123.ui.meds.MedsViewModel
import com.simats.diawell123.ui.meds.SearchMedicationScreen
import com.simats.diawell123.ui.meds.SetMedicationReminderScreen
import com.simats.diawell123.ui.notifications.NotificationsScreen
import com.simats.diawell123.ui.notifications.NotificationsViewModel
import com.simats.diawell123.ui.onboarding.OnboardingContainerScreen
import com.simats.diawell123.ui.profile.EditProfileScreen
import com.simats.diawell123.ui.profile.FoodPreferencesSettingsScreen
import com.simats.diawell123.ui.profile.HealthPreferencesScreen
import com.simats.diawell123.ui.profile.PatientProfileScreen
import com.simats.diawell123.ui.profile.ProfileState
import com.simats.diawell123.ui.profile.ProfileViewModel
import com.simats.diawell123.ui.reports.ReportsViewModel
import com.simats.diawell123.ui.reports.WeeklyReportScreen
import com.simats.diawell123.ui.settings.ChangePasswordScreen
import com.simats.diawell123.ui.settings.DeleteAccountScreen
import com.simats.diawell123.ui.settings.ExportRecordsScreen
import com.simats.diawell123.ui.settings.ExportReportViewerScreen
import com.simats.diawell123.ui.settings.HelpSupportScreen
import com.simats.diawell123.ui.settings.LogoutConfirmationDialog
import com.simats.diawell123.ui.settings.NotificationPreferencesScreen
import com.simats.diawell123.ui.settings.PrivacyPolicyScreen
import com.simats.diawell123.ui.settings.PrivacySecurityScreen
import com.simats.diawell123.ui.settings.SettingsScreen
import com.simats.diawell123.ui.settings.SettingsViewModel
import com.simats.diawell123.ui.settings.TermsOfUseScreen
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private var externalNavTarget by mutableStateOf<String?>(null)
    private var lastBackgroundTimestamp: Long = 0L

    @Suppress("DEPRECATION")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        try {
            super.startActivityForResult(intent, requestCode, options)
        } catch (e: IllegalArgumentException) {
            if (e.message?.contains("Can only use lower 16 bits", ignoreCase = true) == true) {
                super.startActivityForResult(intent, requestCode and 0xffff, options)
            } else {
                throw e
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            Log.d("MainActivity", "POST_NOTIFICATIONS granted: $granted")
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            lastBackgroundTimestamp = System.currentTimeMillis()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged: orientation=${newConfig.orientation}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("hasShownSplash", DiawellApplication.hasShownSplash)
        outState.putString("savedCurrentScreen", DiawellApplication.currentScreen)
        val filteredStack = DiawellApplication.backStack
            .filter { it.isNotBlank() && it != "splash" && it != "logout_dialog" }
        if (filteredStack.isNotEmpty()) {
            outState.putStringArrayList("savedBackStack", ArrayList(filteredStack))
        }
    }

    private fun restoreNavigationState(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) return
        if (savedInstanceState.getBoolean("hasShownSplash", false)) {
            DiawellApplication.hasShownSplash = true
        }
        val restoredStack = savedInstanceState.getStringArrayList("savedBackStack")
            ?.filter { it.isNotBlank() && it != "splash" && it != "logout_dialog" }
            ?: emptyList()
        if (restoredStack.isNotEmpty()) {
            DiawellApplication.backStack = restoredStack
            DiawellApplication.currentScreen = restoredStack.last()
        } else {
            savedInstanceState.getString("savedCurrentScreen")?.let {
                if (it.isNotBlank() && it != "splash" && it != "logout_dialog") {
                    DiawellApplication.currentScreen = it
                    DiawellApplication.backStack = listOf(it)
                }
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreNavigationState(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreNavigationState(savedInstanceState)
        handleIncomingIntentExtras(intent)
        DiaWellNotificationHelper.createNotificationChannels(applicationContext)
        SmartNudgeEngine.scheduleAllDailyNudges(applicationContext)
        enableEdgeToEdge()
        setContent {
            Diawell123Theme {
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                val initialHasShownSplash = DiawellApplication.hasShownSplash
                var hasShownSplash by rememberSaveable { mutableStateOf(initialHasShownSplash) }
                val initialBackStack = remember {
                    if (initialHasShownSplash) {
                        val fallback = DiawellApplication.currentScreen
                            .takeIf { it.isNotBlank() && it != "splash" && it != "logout_dialog" }
                            ?: "home"
                        BackStackNavigator.sanitizeStack(DiawellApplication.backStack, fallback)
                    } else {
                        listOf("splash")
                    }
                }
                val backStack = rememberSaveable(
                    saver = listSaver(
                        save = { it.toList() },
                        restore = { restored ->
                            BackStackNavigator.sanitizeStack(restored).toMutableStateList()
                        }
                    )
                ) {
                    initialBackStack.toMutableStateList()
                }
                val currentScreen = backStack.lastOrNull() ?: "home"
                var screenStackBeforeLock by rememberSaveable(
                    stateSaver = listSaver<List<String>, String>(
                        save = { it },
                        restore = { restored -> restored.ifEmpty { listOf("home") } }
                    )
                ) { mutableStateOf(listOf("home")) }
                var lastTransitionWasPop by remember { mutableStateOf(false) }
                var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
                var selectedMealCategory by rememberSaveable { mutableStateOf("breakfast") }
                var selectedFoodId by rememberSaveable { mutableStateOf<Int?>(null) }
                var selectedDishName by rememberSaveable { mutableStateOf<String?>(null) }
                var launchSnapFoodInDiet by rememberSaveable { mutableStateOf(false) }

                val sessionManager = remember {
                    try {
                        SessionManager.getInstance()
                    } catch (_: Exception) {
                        SessionManager.init(applicationContext)
                    }
                }
                var activeSessionEpoch by rememberSaveable { mutableStateOf(0) }
                val activeSessionKey = "${activeSessionEpoch}_${sessionManager.getUserId() ?: 0}"

                val dietViewModel: DietViewModel = viewModel(key = "diet_$activeSessionKey")
                val glucoseViewModel: GlucoseViewModel = viewModel(key = "glucose_$activeSessionKey")
                val homeViewModel: HomeViewModel = viewModel(key = "home_$activeSessionKey")
                val medsViewModel: MedsViewModel = viewModel(key = "meds_$activeSessionKey")
                val activityViewModel: ActivityViewModel = viewModel(key = "activity_$activeSessionKey")
                val notificationsViewModel: NotificationsViewModel = viewModel(key = "notif_$activeSessionKey")
                val reportsViewModel: ReportsViewModel = viewModel(key = "reports_$activeSessionKey")
                val settingsViewModel: SettingsViewModel = viewModel(key = "settings_$activeSessionKey")
                val profileViewModel: ProfileViewModel = viewModel(key = "profile_$activeSessionKey")
                val chatViewModel: ChatViewModel = viewModel(key = "chat_$activeSessionKey")

                var selectedGlucoseReadingId by rememberSaveable { mutableStateOf<Int?>(null) }
                var selectedMedication by remember { mutableStateOf<com.simats.diawell123.data.model.MedicationItem?>(null) }

                var selectedTabletName by rememberSaveable { mutableStateOf("Metformin") }
                var selectedTabletDose by rememberSaveable { mutableStateOf("500 mg • 1 Tablet") }
                var exportReturnScreen by rememberSaveable { mutableStateOf("privacy_security") }

                fun replaceRoot(route: String) {
                    showLogoutDialog = false
                    lastTransitionWasPop = false
                    BackStackNavigator.replaceRoot(backStack, route)
                }

                fun replaceTop(route: String) {
                    showLogoutDialog = false
                    lastTransitionWasPop = false
                    BackStackNavigator.replaceTop(backStack, route)
                }

                fun pushScreen(route: String) {
                    showLogoutDialog = false
                    if (BackStackNavigator.pushScreen(backStack, route)) {
                        lastTransitionWasPop = false
                    }
                }

                fun selectBottomTab(tabRoute: String) {
                    showLogoutDialog = false
                    if (BackStackNavigator.selectBottomTab(backStack, tabRoute)) {
                        lastTransitionWasPop = false
                    }
                }

                fun popBackStack() {
                    if (showLogoutDialog) {
                        showLogoutDialog = false
                        return
                    }
                    if (BackStackNavigator.popBackStack(backStack)) {
                        lastTransitionWasPop = true
                    }
                }

                fun popUpTo(targetRoute: String, replaceTopFallback: Boolean = false) {
                    showLogoutDialog = false
                    lastTransitionWasPop = BackStackNavigator.popUpTo(
                        stack = backStack,
                        targetRoute = targetRoute,
                        replaceTopFallback = replaceTopFallback,
                    )
                }

                val canPopSystemBack = BackStackNavigator.canHandleSystemBack(
                    stack = backStack,
                    hasOpenOverlay = showLogoutDialog,
                )
                BackHandler(enabled = canPopSystemBack) {
                    popBackStack()
                }

                // Initialize ProfileState from disk cache & session so avatar and username persist across launches
                LaunchedEffect(Unit) {
                    ProfileState.init(applicationContext, sessionManager.getUserId())
                }

                // Auto-Lock on Resume: If app was backgrounded for more than 2 seconds and App Lock is enabled
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val now = System.currentTimeMillis()
                            if (lastBackgroundTimestamp > 0L && (now - lastBackgroundTimestamp) > 2000L) {
                                if (sessionManager.isLoggedIn() && sessionManager.isAppLockEnabled()) {
                                    sessionManager.setAppUnlocked(false)
                                    if (currentScreen != "app_lock" && currentScreen != "splash") {
                                        screenStackBeforeLock = backStack
                                            .filter { it.isNotBlank() && it != "app_lock" && it != "splash" && it != "logout_dialog" }
                                            .ifEmpty { listOf("home") }
                                        replaceRoot("app_lock")
                                    }
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                LaunchedEffect(Unit) {
                    DiaWellNotificationHelper.scheduleDailyHealthReminders(applicationContext)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                this@MainActivity,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                101
                            )
                        }
                    }
                }

                // Fetch and register FCM Token on startup
                LaunchedEffect(Unit) {
                    try {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                Log.d("FCM_TOKEN", "Current FCM Device Token: $token")
                                token?.let {
                                    DiaWellFirebaseMessagingService.sendTokenToServer(applicationContext, it)
                                }
                            } else {
                                Log.w("FCM_TOKEN", "Fetching FCM registration token failed: ${task.exception?.message}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FCM_TOKEN", "FirebaseMessaging error: ${e.message}")
                    }
                }

                val lastTabRefreshMs = remember { mutableMapOf<String, Long>() }

                // Auto-Logout Observer: Listens to 401 Unauthorized or manual logout broadcasts
                LaunchedEffect(Unit) {
                    sessionManager.sessionExpiredEvents.collect {
                        lastTabRefreshMs.clear()
                        activeSessionEpoch++
                        if (currentScreen !in listOf("login", "signup", "splash")) {
                            replaceRoot("login")
                        }
                    }
                }

                // Handle deep-link screen navigation from notifications, smart nudges, or alarm screen
                LaunchedEffect(externalNavTarget, currentScreen) {
                    val target = externalNavTarget
                    if (!target.isNullOrBlank() && currentScreen != "splash" && currentScreen != "app_lock" && sessionManager.isLoggedIn()) {
                        when (target) {
                            "medications" -> {
                                medsViewModel.loadMedications(isPullToRefresh = true)
                                selectBottomTab("medications")
                            }
                            "daily_diet_plan", "diet" -> selectBottomTab("daily_diet_plan")
                            "weekly_report" -> selectBottomTab("weekly_report")
                            "settings" -> selectBottomTab("settings")
                            "ai_chatbot", "dr_jarvis_chat" -> {
                                selectBottomTab("home")
                                pushScreen("ai_chatbot")
                            }
                            "activity_tracking", "activity" -> {
                                selectBottomTab("home")
                                pushScreen("activity_tracking")
                            }
                            "walking" -> {
                                selectBottomTab("home")
                                pushScreen("walking")
                            }
                            "running" -> {
                                selectBottomTab("home")
                                pushScreen("running")
                            }
                            "cycling" -> {
                                selectBottomTab("home")
                                pushScreen("cycling")
                            }
                            "log_glucose", "glucose" -> {
                                selectBottomTab("home")
                                pushScreen("log_glucose")
                            }
                            "home" -> replaceRoot("home")
                        }
                        externalNavTarget = null
                    }
                }

                val mainTabOrder = remember {
                    mapOf(
                        "home" to 0,
                        "daily_diet_plan" to 1,
                        "ai_chatbot" to 2,
                        "dr_jarvis_chat" to 2,
                        "medications" to 3,
                        "weekly_report" to 4,
                    )
                }

                // Update activity timestamp on screen changes; defer non-blocking refresh until after tab slide settles
                LaunchedEffect(backStack.size, currentScreen) {
                    if (currentScreen != "splash") {
                        DiawellApplication.currentScreen = currentScreen
                        DiawellApplication.backStack = backStack.filter { it.isNotBlank() && it != "splash" && it != "logout_dialog" }
                    }
                    sessionManager.updateLastActiveTime()
                    if (currentScreen in listOf("medications", "set_medication_reminder", "medication_reminder")) {
                        checkExactAlarmPermission()
                    }
                    val now = System.currentTimeMillis()
                    val lastRefresh = lastTabRefreshMs[currentScreen] ?: 0L
                    if (now - lastRefresh > 30_000L) {
                        // Wait 220ms so the 180ms tab switch + liquid glass slide stays locked at 60/120 FPS
                        delay(220)
                        lastTabRefreshMs[currentScreen] = System.currentTimeMillis()
                        when (currentScreen) {
                            "medications" -> medsViewModel.loadMedications(isPullToRefresh = true)
                            "home" -> homeViewModel.loadDashboard(isPullToRefresh = true)
                            "weekly_report" -> reportsViewModel.loadWeeklyReport(isPullToRefresh = true)
                            "food_log" -> dietViewModel.loadFoodLog()
                        }
                    }
                }

                // Splash screen initial routing based on saved persistent authentication session
                LaunchedEffect(Unit) {
                    if (hasShownSplash || DiawellApplication.hasShownSplash) {
                        hasShownSplash = true
                        DiawellApplication.hasShownSplash = true
                        if (currentScreen == "splash") {
                            val restoredStack = DiawellApplication.backStack
                                .filter { it.isNotBlank() && it != "splash" && it != "logout_dialog" }
                            if (restoredStack.isNotEmpty()) {
                                lastTransitionWasPop = false
                                backStack.clear()
                                backStack.addAll(restoredStack)
                            } else {
                                val nextRoot = DiawellApplication.currentScreen.takeIf { it != "splash" }
                                    ?: if (sessionManager.isLoggedIn()) {
                                        if (sessionManager.isAppLockEnabled() && !sessionManager.isAppUnlocked()) "app_lock" else "home"
                                    } else {
                                        "onboarding"
                                    }
                                replaceRoot(nextRoot)
                            }
                        }
                        return@LaunchedEffect
                    }
                    delay(2600) // Splash duration matching logo spin, audio clicks, and settle
                    hasShownSplash = true
                    DiawellApplication.hasShownSplash = true
                    if (sessionManager.isLoggedIn()) {
                        if (sessionManager.isAppLockEnabled() && !sessionManager.isAppUnlocked()) {
                            screenStackBeforeLock = listOf("home")
                            replaceRoot("app_lock")
                        } else {
                            replaceRoot("home")
                        }
                    } else {
                        // Show the 3 onboarding screens whenever user is not logged in / has not created an account
                        replaceRoot("onboarding")
                    }
                    DiawellApplication.currentScreen = currentScreen
                    DiawellApplication.backStack = backStack.toList()
                }

                var isAiVoiceModeActive by rememberSaveable { mutableStateOf(true) }
                val activeBottomTab = when (currentScreen) {
                    "home" -> "Home"
                    "daily_diet_plan" -> "Diet"
                    "ai_chatbot", "dr_jarvis_chat" -> if (isAiVoiceModeActive) "AI" else null
                    "medications" -> "Meds"
                    "weekly_report" -> "Reports"
                    "notifications" -> "Home"
                    else -> null
                }

                val navBackdropLayer = rememberGraphicsLayer()

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawWithContent {
                                navBackdropLayer.record {
                                    this@drawWithContent.drawContent()
                                }
                                drawLayer(navBackdropLayer)
                            },
                    ) {
                        DiaWellAmbientBackground()

                        AnimatedContent(
                        targetState = currentScreen,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = if (isLandscape && activeBottomTab != null) 76.dp else 0.dp),
                        transitionSpec = {
                            val fromTabIdx = mainTabOrder[initialState]
                            val toTabIdx = mainTabOrder[targetState]
                            if (initialState == "splash" && (targetState == "home" || targetState == "login" || targetState == "onboarding" || targetState == "app_lock")) {
                                (fadeIn(animationSpec = tween(700, easing = FastOutSlowInEasing)) +
                                    scaleIn(initialScale = 0.94f, animationSpec = tween(700))) togetherWith
                                    (fadeOut(animationSpec = tween(500)) +
                                        scaleOut(targetScale = 1.05f, animationSpec = tween(500)))
                            } else if (fromTabIdx != null && toTabIdx != null) {
                                val dir = if (toTabIdx >= fromTabIdx) 1 else -1
                                (fadeIn(animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)) +
                                    slideInHorizontally(animationSpec = tween(durationMillis = 210, easing = FastOutSlowInEasing)) { width ->
                                        dir * (width * 0.07f).toInt()
                                    }) togetherWith
                                    (fadeOut(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) { width ->
                                            -dir * (width * 0.07f).toInt()
                                        })
                            } else if (lastTransitionWasPop) {
                                (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                    slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { width ->
                                        -(width * 0.08f).toInt()
                                    }) togetherWith
                                    (fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width ->
                                            (width * 0.08f).toInt()
                                        })
                            } else {
                                (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                                    slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { width ->
                                        (width * 0.08f).toInt()
                                    }) togetherWith
                                    (fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(animationSpec = tween(180, easing = FastOutSlowInEasing)) { width ->
                                            -(width * 0.08f).toInt()
                                        })
                            }
                        },
                        label = "ScreenTransition",
                    ) { targetScreen ->
                    when (targetScreen) {
                        "splash" -> SplashScreen()

                        "onboarding" -> OnboardingContainerScreen(
                            onFinishOnboarding = {
                                sessionManager.setOnboardingCompleted()
                                replaceRoot("signup")
                            },
                        )

                        "signup" -> SignUpScreen(
                            onSignUpSuccess = {
                                lastTabRefreshMs.clear()
                                activeSessionEpoch++
                                ProfileState.init(applicationContext, sessionManager.getUserId())
                                profileViewModel.clearError()
                                profileViewModel.loadProfile()
                                sessionManager.setAppUnlocked(true)
                                replaceRoot("patient_profile")
                            },
                        ) {
                            if ("login" in backStack) popUpTo("login") else replaceRoot("login")
                        }

                        "login" -> LoginScreen(
                            onLoginSuccess = {
                                lastTabRefreshMs.clear()
                                activeSessionEpoch++
                                ProfileState.init(applicationContext, sessionManager.getUserId())
                                sessionManager.setAppUnlocked(true)
                                replaceRoot("home")
                            },
                            onForgotPasswordClick = { pushScreen("forgot_password") },
                        ) { pushScreen("signup") }

                        "forgot_password" -> ForgotPasswordScreen(
                            onBackClick = { popBackStack() },
                            onSendResetLinkClick = { popBackStack() },
                        )

                        "patient_profile" -> PatientProfileScreen(
                            viewModel = profileViewModel,
                            onContinueClick = { pushScreen("health_preferences") },
                        )

                        "health_preferences" -> HealthPreferencesScreen(
                            viewModel = profileViewModel,
                            onBackClick = { popBackStack() },
                            onSavePreferencesClick = { replaceRoot("home") },
                        )

                        "home" -> HomeScreen(
                            viewModel = homeViewModel,
                            onLogGlucoseClick = { pushScreen("log_glucose") },
                            onGlucoseHistoryClick = { pushScreen("glucose_history") },
                            onTrackFoodClick = { selectBottomTab("daily_diet_plan") },
                            onTrackFoodCategoryClick = { category ->
                                selectedMealCategory = category
                                selectBottomTab("daily_diet_plan")
                            },
                            onActivityTrackingClick = { pushScreen("activity_tracking") },
                            onDietChatbotClick = { pushScreen("ai_chatbot") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                            onSettingsTabClick = { selectBottomTab("settings") },
                            onProfileClick = { pushScreen("edit_profile") },
                            onExportReportClick = {
                                exportReturnScreen = "home"
                                pushScreen("export_records")
                            },
                        )

                        "edit_profile" -> EditProfileScreen(
                            viewModel = profileViewModel,
                            onBackClick = { popBackStack() },
                            onSaveChangesClick = { popBackStack() },
                        )

                        "log_glucose" -> LogGlucoseScreen(
                            viewModel = glucoseViewModel,
                            onBackClick = { popBackStack() },
                            onSaveReadingClick = { popUpTo("glucose_history", replaceTopFallback = true) },
                        )

                        "glucose_history" -> GlucoseHistoryScreen(
                            viewModel = glucoseViewModel,
                            onBackClick = { popBackStack() },
                            onLogGlucoseClick = { pushScreen("log_glucose") },
                            onReadingClick = { readingId ->
                                selectedGlucoseReadingId = readingId
                                pushScreen("reading_details")
                            },
                        )

                        "reading_details" -> ReadingDetailsScreen(
                            readingId = selectedGlucoseReadingId,
                            viewModel = glucoseViewModel,
                            onBackClick = { popBackStack() },
                            onDeleted = { popBackStack() },
                        )

                        "daily_diet_plan" -> DailyDietPlanScreen(
                            viewModel = dietViewModel,
                            initialOpenSnapFood = launchSnapFoodInDiet,
                            onSnapFoodConsumed = { launchSnapFoodInDiet = false },
                            onBackClick = { popBackStack() },
                            onMealDetailsClick = { category, foodId, dishName ->
                                selectedMealCategory = category
                                selectedFoodId = foodId
                                selectedDishName = dishName
                                pushScreen("food_details")
                            },
                            onFoodLogClick = { pushScreen("food_log") },
                            onExploreFoodClick = { pushScreen("explore_food") },
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                            onSettingsTabClick = { selectBottomTab("settings") },
                        )

                        "explore_food" -> ExploreFoodScreen(
                            viewModel = dietViewModel,
                            onBackClick = { popBackStack() },
                            onMealDetailsClick = { category, foodId, dishName ->
                                selectedMealCategory = category
                                selectedFoodId = foodId
                                selectedDishName = dishName
                                pushScreen("food_details")
                            },
                        )

                        "food_details" -> FoodDetailsScreen(
                            mealCategory = selectedMealCategory,
                            foodId = selectedFoodId,
                            dishName = selectedDishName,
                            viewModel = dietViewModel,
                            onBackClick = { popBackStack() },
                            onAddToFoodLogClick = { popUpTo("food_log", replaceTopFallback = true) },
                        )

                        "food_log" -> FoodLogScreen(
                            viewModel = dietViewModel,
                            onBackClick = { popBackStack() },
                            onAddFoodClick = { pushScreen("explore_food") },
                            onNutritionSummaryClick = { pushScreen("nutrition_summary") },
                        )

                        "nutrition_summary" -> NutritionSummaryScreen(
                            onBackClick = { popBackStack() },
                            onReturnToFoodLogClick = { popUpTo("food_log", replaceTopFallback = true) },
                        )

                        "activity_tracking" -> ActivityTrackingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { popBackStack() },
                            onHistoryClick = { pushScreen("activity_history") },
                        )

                        "walking" -> WalkingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { popBackStack() },
                            onHistoryClick = { pushScreen("activity_history") },
                        )

                        "running" -> RunningScreen(
                            viewModel = activityViewModel,
                            onBackClick = { popBackStack() },
                            onHistoryClick = { pushScreen("activity_history") },
                        )

                        "cycling" -> CyclingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { popBackStack() },
                            onHistoryClick = { pushScreen("activity_history") },
                        )

                        "activity_history" -> ActivityHistoryScreen(
                            viewModel = activityViewModel,
                            onBackClick = { popBackStack() },
                            onStartNewWorkoutClick = { popUpTo("activity_tracking", replaceTopFallback = true) },
                        )

                        "medications" -> MedicationsScreen(
                            viewModel = medsViewModel,
                            onBackClick = { popBackStack() },
                            onAddMedicationClick = { pushScreen("search_medication") },
                            onReminderClick = { med ->
                                selectedMedication = med
                                pushScreen("medication_reminder")
                            },
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                            onSettingsTabClick = { selectBottomTab("settings") },
                        )

                        "search_medication" -> SearchMedicationScreen(
                            viewModel = medsViewModel,
                            onBackClick = { popBackStack() },
                            onSelectMedication = { name, dose ->
                                selectedTabletName = name
                                selectedTabletDose = dose
                                pushScreen("set_medication_reminder")
                            },
                        )

                        "set_medication_reminder" -> SetMedicationReminderScreen(
                            viewModel = medsViewModel,
                            medicationName = selectedTabletName,
                            medicationDose = selectedTabletDose,
                            onBackClick = { popBackStack() },
                            onSaveReminderClick = { popUpTo("medications", replaceTopFallback = true) },
                        )

                        "medication_reminder" -> MedicationReminderScreen(
                            viewModel = medsViewModel,
                            medication = selectedMedication,
                            onBackClick = { popBackStack() },
                            onTakenClick = { popBackStack() },
                            onSnoozeClick = { popBackStack() },
                            onSkipClick = { popBackStack() },
                        )

                        "ai_chatbot", "dr_jarvis_chat" -> AiChatbotScreen(
                            viewModel = chatViewModel,
                            onBackClick = { popBackStack() },
                            onVoiceOverlayVisibilityChanged = { isAiVoiceModeActive = it },
                        )

                        "weekly_report" -> WeeklyReportScreen(
                            viewModel = reportsViewModel,
                            onBackClick = { popBackStack() },
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onSettingsTabClick = { selectBottomTab("settings") },
                            onExportReportClick = {
                                exportReturnScreen = "weekly_report"
                                pushScreen("export_records")
                            },
                        )

                        "notifications" -> NotificationsScreen(
                            viewModel = notificationsViewModel,
                            onBackClick = { popBackStack() },
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                            onSettingsTabClick = { selectBottomTab("settings") },
                            onNotificationClick = { pushScreen("medication_reminder") },
                        )

                        "notification_prefs" -> NotificationPreferencesScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onSavePreferencesClick = { popBackStack() },
                        )

                        "food_preferences_settings" -> FoodPreferencesSettingsScreen(
                            viewModel = profileViewModel,
                            onBackClick = { popBackStack() },
                            onSaveFoodPreferencesClick = { popBackStack() },
                        )

                        "privacy_security" -> PrivacySecurityScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onChangePasswordClick = { pushScreen("change_password") },
                            onPrivacyPolicyClick = { pushScreen("privacy_policy") },
                            onTermsOfUseClick = { pushScreen("terms_of_use") },
                            onExportRecordsClick = {
                                exportReturnScreen = "privacy_security"
                                pushScreen("export_records")
                            },
                            onDeleteAccountClick = { pushScreen("delete_account") },
                        )

                        "change_password" -> ChangePasswordScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onSavePasswordClick = { popBackStack() },
                        )

                        "privacy_policy" -> PrivacyPolicyScreen(
                            onBackClick = { popBackStack() },
                        )

                        "terms_of_use" -> TermsOfUseScreen(
                            onBackClick = { popBackStack() },
                        )

                        "export_records" -> ExportRecordsScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onExportClick = { pushScreen("export_report_viewer") },
                        )

                        "export_report_viewer" -> ExportReportViewerScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onDoneClick = { popUpTo(exportReturnScreen) },
                        )

                        "delete_account" -> DeleteAccountScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { popBackStack() },
                            onConfirmDeleteClick = {
                                lastTabRefreshMs.clear()
                                activeSessionEpoch++
                                replaceRoot("login")
                            },
                        )

                        "help_support" -> HelpSupportScreen(
                            onBackClick = { popBackStack() },
                            onContactSupportClick = { popBackStack() },
                            onAiChatbotClick = { pushScreen("ai_chatbot") },
                        )

                        "settings" -> SettingsScreen(
                            profileViewModel = profileViewModel,
                            onBackClick = { popBackStack() },
                            onPersonalProfileClick = { pushScreen("edit_profile") },
                            onNotificationPrefsClick = { pushScreen("notification_prefs") },
                            onFoodPrefsClick = { pushScreen("food_preferences_settings") },
                            onPrivacySecurityClick = { pushScreen("privacy_security") },
                            onHelpSupportClick = { pushScreen("help_support") },
                            onExportRecordsClick = {
                                exportReturnScreen = "settings"
                                pushScreen("export_records")
                            },
                            onAiChatbotClick = { pushScreen("ai_chatbot") },
                            onLogoutClick = { showLogoutDialog = true },
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                        )

                        "app_lock" -> AppLockScreen(
                            onUnlocked = {
                                sessionManager.setAppUnlocked(true)
                                val restoredStack = screenStackBeforeLock.filter {
                                    it.isNotBlank() && it !in listOf("splash", "login", "signup", "app_lock", "logout_dialog")
                                }.ifEmpty { listOf("home") }
                                lastTransitionWasPop = false
                                backStack.clear()
                                backStack.addAll(restoredStack)
                            },
                            onSignOut = {
                                lastTabRefreshMs.clear()
                                sessionManager.setAppUnlocked(false)
                                sessionManager.clearSession()
                                ProfileState.clear(applicationContext)
                                activeSessionEpoch++
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        AuthRepositoryImpl().logout(clearLocalSession = false)
                                    } catch (_: Exception) {}
                                }
                                replaceRoot("login")
                            },
                        )

                        else -> SplashScreen()
                    }
                    }
                }

                    // Landscape / Tablet Navigation: Slim left-side liquid glass NavigationRail
                    AnimatedVisibility(
                        visible = isLandscape && activeBottomTab != null,
                        enter = fadeIn(tween(180)) + slideInHorizontally(tween(200, easing = FastOutSlowInEasing)) { -it },
                        exit = fadeOut(tween(140)) + slideOutHorizontally(tween(160)) { -it },
                        modifier = Modifier.align(Alignment.CenterStart),
                    ) {
                        DiaWellLiquidGlassNavRail(
                            selectedTab = activeBottomTab ?: "Home",
                            backdropLayer = navBackdropLayer,
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onAiTabClick = {
                                if (currentScreen != "ai_chatbot" && currentScreen != "dr_jarvis_chat") {
                                    isAiVoiceModeActive = true
                                    pushScreen("ai_chatbot")
                                }
                            },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                        )
                    }

                    // Portrait Navigation: GPU-accelerated Liquid Glass Bottom Navigation Dock
                    AnimatedVisibility(
                        visible = !isLandscape && activeBottomTab != null,
                        enter = fadeIn(tween(180)) + slideInVertically(tween(200, easing = FastOutSlowInEasing)) { it / 2 },
                        exit = fadeOut(tween(140)) + slideOutVertically(tween(160)) { it / 2 },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        IosLiquidGlassBottomNav(
                            selectedTab = activeBottomTab ?: "Home",
                            backdropLayer = navBackdropLayer,
                            onHomeTabClick = { selectBottomTab("home") },
                            onDietTabClick = { selectBottomTab("daily_diet_plan") },
                            onAiTabClick = {
                                if (currentScreen != "ai_chatbot" && currentScreen != "dr_jarvis_chat") {
                                    isAiVoiceModeActive = true
                                    pushScreen("ai_chatbot")
                                }
                            },
                            onMedsTabClick = { selectBottomTab("medications") },
                            onReportsTabClick = { selectBottomTab("weekly_report") },
                        )
                    }

                    // iOS AssistiveTouch-style Floating Quick-Action Button
                    val isAssistiveButtonVisible = sessionManager.isLoggedIn() && currentScreen in listOf(
                        "home",
                        "daily_diet_plan",
                        "medications",
                        "weekly_report",
                        "settings",
                        "notifications",
                        "glucose_history",
                        "food_log",
                        "activity_history",
                        "activity_tracking"
                    )

                    DiaWellAssistiveButton(
                        visible = isAssistiveButtonVisible && !showLogoutDialog,
                        modifier = Modifier.zIndex(100f),
                        onDivaAiClick = {
                            pushScreen("ai_chatbot")
                        },
                        onSnapFoodClick = {
                            launchSnapFoodInDiet = true
                            selectBottomTab("daily_diet_plan")
                        },
                        onLogGlucoseClick = {
                            pushScreen("log_glucose")
                        },
                        onLogMealClick = {
                            pushScreen("food_log")
                        },
                        onActivityClick = {
                            pushScreen("activity_tracking")
                        }
                    )

                    if (showLogoutDialog) {
                        BackHandler(enabled = true) {
                            showLogoutDialog = false
                        }
                        Box(modifier = Modifier.fillMaxSize().zIndex(200f)) {
                            LogoutConfirmationDialog(
                                onCancelClick = { showLogoutDialog = false },
                                onConfirmLogoutClick = {
                                    lastTabRefreshMs.clear()
                                    sessionManager.setAppUnlocked(false)
                                    sessionManager.clearSession()
                                    ProfileState.clear(applicationContext)
                                    activeSessionEpoch++
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            AuthRepositoryImpl().logout(clearLocalSession = false)
                                        } catch (_: Exception) {}
                                    }
                                    replaceRoot("login")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntentExtras(intent)
        DiaWellNotificationHelper.stopAlarmSound(this)
    }

    private fun handleIncomingIntentExtras(incomingIntent: Intent?) {
        externalNavTarget = incomingIntent?.getStringExtra("target_screen")
        val cancelNotifId = incomingIntent?.getIntExtra("cancel_notif_id", -1) ?: -1
        if (cancelNotifId > 0) {
            try {
                NotificationManagerCompat.from(this).cancel(cancelNotifId)
            } catch (_: Exception) {}
        }
    }

    override fun onResume() {
        super.onResume()
        checkExactAlarmPermission()
        if (intent?.getStringExtra("target_screen") == "medications") {
            DiaWellNotificationHelper.stopAlarmSound(this)
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    "Please allow Alarms & Reminders for DiaWell to ring on time",
                    Toast.LENGTH_LONG
                ).show()
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to launch exact alarm settings: ${e.message}")
                }
            }
        }
    }
}
