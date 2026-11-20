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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIncomingIntentExtras(intent)
        DiaWellNotificationHelper.createNotificationChannels(applicationContext)
        SmartNudgeEngine.scheduleAllDailyNudges(applicationContext)
        enableEdgeToEdge()
        setContent {
            Diawell123Theme {
                var currentScreen by remember { mutableStateOf("splash") }
                var screenBeforeLock by remember { mutableStateOf("home") }
                var selectedMealCategory by remember { mutableStateOf("breakfast") }
                var selectedFoodId by remember { mutableStateOf<Int?>(null) }
                var selectedDishName by remember { mutableStateOf<String?>(null) }

                val dietViewModel: DietViewModel = viewModel()
                val glucoseViewModel: GlucoseViewModel = viewModel()
                val homeViewModel: HomeViewModel = viewModel()
                val medsViewModel: MedsViewModel = viewModel()
                val activityViewModel: ActivityViewModel = viewModel()
                val notificationsViewModel: NotificationsViewModel = viewModel()
                val reportsViewModel: ReportsViewModel = viewModel()
                val settingsViewModel: SettingsViewModel = viewModel()
                val profileViewModel: ProfileViewModel = viewModel()
                val chatViewModel: ChatViewModel = viewModel()

                var selectedGlucoseReadingId by remember { mutableStateOf<Int?>(null) }
                var selectedMedication by remember { mutableStateOf<com.simats.diawell123.data.model.MedicationItem?>(null) }

                var selectedTabletName by remember { mutableStateOf("Metformin") }
                var selectedTabletDose by remember { mutableStateOf("500 mg • 1 Tablet") }
                var exportReturnScreen by remember { mutableStateOf("privacy_security") }

                val sessionManager = remember {
                    try {
                        SessionManager.getInstance()
                    } catch (_: Exception) {
                        SessionManager.init(applicationContext)
                    }
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
                                        screenBeforeLock = currentScreen
                                        currentScreen = "app_lock"
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

                // Auto-Logout Observer: Listens to 401 Unauthorized or manual logout broadcasts
                LaunchedEffect(Unit) {
                    sessionManager.sessionExpiredEvents.collect {
                        if (currentScreen !in listOf("login", "signup", "splash")) {
                            currentScreen = "login"
                        }
                    }
                }

                // Handle deep-link screen navigation from notifications, smart nudges, or alarm screen
                LaunchedEffect(externalNavTarget, currentScreen) {
                    val target = externalNavTarget
                    if (!target.isNullOrBlank() && currentScreen != "splash" && sessionManager.isLoggedIn()) {
                        when (target) {
                            "medications" -> {
                                medsViewModel.loadMedications(isPullToRefresh = true)
                                currentScreen = "medications"
                            }
                            "daily_diet_plan", "diet" -> currentScreen = "daily_diet_plan"
                            "ai_chatbot", "dr_jarvis_chat" -> currentScreen = "ai_chatbot"
                            "activity_tracking", "activity" -> currentScreen = "activity_tracking"
                            "walking" -> currentScreen = "walking"
                            "running" -> currentScreen = "running"
                            "cycling" -> currentScreen = "cycling"
                            "log_glucose", "glucose" -> currentScreen = "log_glucose"
                            "home" -> currentScreen = "home"
                        }
                        externalNavTarget = null
                    }
                }

                val lastTabRefreshMs = remember { mutableMapOf<String, Long>() }
                val mainTabOrder = remember {
                    mapOf(
                        "home" to 0,
                        "daily_diet_plan" to 1,
                        "medications" to 2,
                        "weekly_report" to 3,
                        "settings" to 4,
                    )
                }

                // Update activity timestamp on screen changes; defer non-blocking refresh until after tab slide settles
                LaunchedEffect(currentScreen) {
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
                    delay(2600) // Splash duration matching logo spin, audio clicks, and settle
                    if (sessionManager.isLoggedIn()) {
                        if (sessionManager.isAppLockEnabled() && !sessionManager.isAppUnlocked()) {
                            screenBeforeLock = "home"
                            currentScreen = "app_lock"
                        } else {
                            currentScreen = "home"
                        }
                    } else if (sessionManager.isOnboardingCompleted()) {
                        currentScreen = "login"
                    } else {
                        currentScreen = "onboarding"
                    }
                }

                val activeBottomTab = when (currentScreen) {
                    "home" -> "Home"
                    "daily_diet_plan" -> "Diet"
                    "medications" -> "Meds"
                    "weekly_report" -> "Reports"
                    "settings" -> "Settings"
                    "notifications" -> "Home"
                    else -> null
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = currentScreen,
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
                                (fadeIn(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) +
                                    slideInHorizontally(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) { width ->
                                        dir * (width * 0.04f).toInt()
                                    }) togetherWith
                                    (fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) +
                                        slideOutHorizontally(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) { width ->
                                            -dir * (width * 0.04f).toInt()
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
                                currentScreen = "signup"
                            },
                        )

                        "signup" -> SignUpScreen(
                            onSignUpSuccess = {
                                ProfileState.init(applicationContext, sessionManager.getUserId())
                                profileViewModel.clearError()
                                profileViewModel.loadProfile()
                                sessionManager.setAppUnlocked(true)
                                currentScreen = "patient_profile"
                            },
                        ) { currentScreen = "login" }

                        "login" -> LoginScreen(
                            onLoginSuccess = {
                                ProfileState.init(applicationContext, sessionManager.getUserId())
                                sessionManager.setAppUnlocked(true)
                                currentScreen = "home"
                            },
                            onForgotPasswordClick = { currentScreen = "forgot_password" },
                        ) { currentScreen = "signup" }

                        "forgot_password" -> ForgotPasswordScreen(
                            onBackClick = { currentScreen = "login" },
                            onSendResetLinkClick = { currentScreen = "login" },
                        )

                        "patient_profile" -> PatientProfileScreen(
                            viewModel = profileViewModel,
                            onContinueClick = { currentScreen = "health_preferences" },
                        )

                        "health_preferences" -> HealthPreferencesScreen(
                            viewModel = profileViewModel,
                            onBackClick = { currentScreen = "patient_profile" },
                            onSavePreferencesClick = { currentScreen = "home" },
                        )

                        "home" -> HomeScreen(
                            viewModel = homeViewModel,
                            onLogGlucoseClick = { currentScreen = "log_glucose" },
                            onGlucoseHistoryClick = { currentScreen = "glucose_history" },
                            onTrackFoodClick = { currentScreen = "daily_diet_plan" },
                            onTrackFoodCategoryClick = { category ->
                                selectedMealCategory = category
                                currentScreen = "daily_diet_plan"
                            },
                            onActivityTrackingClick = { currentScreen = "activity_tracking" },
                            onDietChatbotClick = { currentScreen = "ai_chatbot" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                            onProfileClick = { currentScreen = "edit_profile" },
                            onExportReportClick = {
                                exportReturnScreen = "home"
                                currentScreen = "export_records"
                            },
                        )

                        "edit_profile" -> EditProfileScreen(
                            viewModel = profileViewModel,
                            onBackClick = { currentScreen = "settings" },
                            onSaveChangesClick = { currentScreen = "settings" },
                        )

                        "log_glucose" -> LogGlucoseScreen(
                            viewModel = glucoseViewModel,
                            onBackClick = { currentScreen = "home" },
                            onSaveReadingClick = { currentScreen = "glucose_history" },
                        )

                        "glucose_history" -> GlucoseHistoryScreen(
                            viewModel = glucoseViewModel,
                            onBackClick = { currentScreen = "home" },
                            onLogGlucoseClick = { currentScreen = "log_glucose" },
                            onReadingClick = { readingId ->
                                selectedGlucoseReadingId = readingId
                                currentScreen = "reading_details"
                            },
                        )

                        "reading_details" -> ReadingDetailsScreen(
                            readingId = selectedGlucoseReadingId,
                            viewModel = glucoseViewModel,
                            onBackClick = { currentScreen = "glucose_history" },
                            onDeleted = { currentScreen = "glucose_history" },
                        )

                        "daily_diet_plan" -> DailyDietPlanScreen(
                            viewModel = dietViewModel,
                            onBackClick = { currentScreen = "home" },
                            onMealDetailsClick = { category, foodId, dishName ->
                                selectedMealCategory = category
                                selectedFoodId = foodId
                                selectedDishName = dishName
                                currentScreen = "food_details"
                            },
                            onFoodLogClick = { currentScreen = "food_log" },
                            onExploreFoodClick = { currentScreen = "explore_food" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                        )

                        "explore_food" -> ExploreFoodScreen(
                            viewModel = dietViewModel,
                            onBackClick = { currentScreen = "daily_diet_plan" },
                            onMealDetailsClick = { category, foodId, dishName ->
                                selectedMealCategory = category
                                selectedFoodId = foodId
                                selectedDishName = dishName
                                currentScreen = "food_details"
                            },
                        )

                        "food_details" -> FoodDetailsScreen(
                            mealCategory = selectedMealCategory,
                            foodId = selectedFoodId,
                            dishName = selectedDishName,
                            viewModel = dietViewModel,
                            onBackClick = { currentScreen = "daily_diet_plan" },
                            onAddToFoodLogClick = { currentScreen = "food_log" },
                        )

                        "food_log" -> FoodLogScreen(
                            viewModel = dietViewModel,
                            onBackClick = { currentScreen = "home" },
                            onAddFoodClick = { currentScreen = "explore_food" },
                            onNutritionSummaryClick = { currentScreen = "nutrition_summary" },
                        )

                        "nutrition_summary" -> NutritionSummaryScreen(
                            onBackClick = { currentScreen = "daily_diet_plan" },
                            onReturnToFoodLogClick = { currentScreen = "food_log" },
                        )

                        "activity_tracking" -> ActivityTrackingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHistoryClick = { currentScreen = "activity_history" },
                        )

                        "walking" -> WalkingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHistoryClick = { currentScreen = "activity_history" },
                        )

                        "running" -> RunningScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHistoryClick = { currentScreen = "activity_history" },
                        )

                        "cycling" -> CyclingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHistoryClick = { currentScreen = "activity_history" },
                        )

                        "activity_history" -> ActivityHistoryScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "activity_tracking" },
                            onStartNewWorkoutClick = { currentScreen = "activity_tracking" },
                        )

                        "medications" -> MedicationsScreen(
                            viewModel = medsViewModel,
                            onBackClick = { currentScreen = "home" },
                            onAddMedicationClick = { currentScreen = "search_medication" },
                            onReminderClick = { med ->
                                selectedMedication = med
                                currentScreen = "medication_reminder"
                            },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                        )

                        "search_medication" -> SearchMedicationScreen(
                            viewModel = medsViewModel,
                            onBackClick = { currentScreen = "medications" },
                            onSelectMedication = { name, dose ->
                                selectedTabletName = name
                                selectedTabletDose = dose
                                currentScreen = "set_medication_reminder"
                            },
                        )

                        "set_medication_reminder" -> SetMedicationReminderScreen(
                            viewModel = medsViewModel,
                            medicationName = selectedTabletName,
                            medicationDose = selectedTabletDose,
                            onBackClick = { currentScreen = "search_medication" },
                            onSaveReminderClick = { currentScreen = "medications" },
                        )

                        "medication_reminder" -> MedicationReminderScreen(
                            viewModel = medsViewModel,
                            medication = selectedMedication,
                            onBackClick = { currentScreen = "medications" },
                            onTakenClick = { currentScreen = "medications" },
                            onSnoozeClick = { currentScreen = "medications" },
                            onSkipClick = { currentScreen = "medications" },
                        )

                        "ai_chatbot", "dr_jarvis_chat" -> AiChatbotScreen(
                            viewModel = chatViewModel,
                            onBackClick = { currentScreen = "home" },
                        )

                        "weekly_report" -> WeeklyReportScreen(
                            viewModel = reportsViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onSettingsTabClick = { currentScreen = "settings" },
                            onExportReportClick = {
                                exportReturnScreen = "weekly_report"
                                currentScreen = "export_records"
                            },
                        )

                        "notifications" -> NotificationsScreen(
                            viewModel = notificationsViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                            onNotificationClick = { currentScreen = "medication_reminder" },
                        )

                        "notification_prefs" -> NotificationPreferencesScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "settings" },
                            onSavePreferencesClick = { currentScreen = "settings" },
                        )

                        "food_preferences_settings" -> FoodPreferencesSettingsScreen(
                            viewModel = profileViewModel,
                            onBackClick = { currentScreen = "settings" },
                            onSaveFoodPreferencesClick = { currentScreen = "settings" },
                        )

                        "privacy_security" -> PrivacySecurityScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "settings" },
                            onChangePasswordClick = { currentScreen = "change_password" },
                            onPrivacyPolicyClick = { currentScreen = "privacy_policy" },
                            onTermsOfUseClick = { currentScreen = "terms_of_use" },
                            onExportRecordsClick = {
                                exportReturnScreen = "privacy_security"
                                currentScreen = "export_records"
                            },
                            onDeleteAccountClick = { currentScreen = "delete_account" },
                        )

                        "change_password" -> ChangePasswordScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "privacy_security" },
                            onSavePasswordClick = { currentScreen = "privacy_security" },
                        )

                        "privacy_policy" -> PrivacyPolicyScreen(
                            onBackClick = { currentScreen = "privacy_security" },
                        )

                        "terms_of_use" -> TermsOfUseScreen(
                            onBackClick = { currentScreen = "privacy_security" },
                        )

                        "export_records" -> ExportRecordsScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = exportReturnScreen },
                            onExportClick = { currentScreen = "export_report_viewer" },
                        )

                        "export_report_viewer" -> ExportReportViewerScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "export_records" },
                            onDoneClick = { currentScreen = exportReturnScreen },
                        )

                        "delete_account" -> DeleteAccountScreen(
                            viewModel = settingsViewModel,
                            onBackClick = { currentScreen = "privacy_security" },
                            onConfirmDeleteClick = { currentScreen = "login" },
                        )

                        "help_support" -> HelpSupportScreen(
                            onBackClick = { currentScreen = "settings" },
                            onContactSupportClick = { currentScreen = "settings" },
                            onAiChatbotClick = { currentScreen = "ai_chatbot" },
                        )

                        "settings" -> SettingsScreen(
                            profileViewModel = profileViewModel,
                            onBackClick = { currentScreen = "home" },
                            onPersonalProfileClick = { currentScreen = "edit_profile" },
                            onNotificationPrefsClick = { currentScreen = "notification_prefs" },
                            onFoodPrefsClick = { currentScreen = "food_preferences_settings" },
                            onPrivacySecurityClick = { currentScreen = "privacy_security" },
                            onHelpSupportClick = { currentScreen = "help_support" },
                            onExportRecordsClick = {
                                exportReturnScreen = "settings"
                                currentScreen = "export_records"
                            },
                            onAiChatbotClick = { currentScreen = "ai_chatbot" },
                            onLogoutClick = { currentScreen = "logout_dialog" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                        )

                        "logout_dialog" -> LogoutConfirmationDialog(
                            onCancelClick = { currentScreen = "settings" },
                            onConfirmLogoutClick = {
                                sessionManager.setAppUnlocked(false)
                                sessionManager.clearSession()
                                ProfileState.clear()
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        AuthRepositoryImpl().logout()
                                    } catch (_: Exception) {}
                                }
                                currentScreen = "login"
                            },
                        )

                        "app_lock" -> AppLockScreen(
                            onUnlocked = {
                                sessionManager.setAppUnlocked(true)
                                currentScreen = if (screenBeforeLock in listOf("splash", "login", "signup", "app_lock")) "home" else screenBeforeLock
                            },
                            onSignOut = {
                                sessionManager.setAppUnlocked(false)
                                sessionManager.clearSession()
                                ProfileState.clear()
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        AuthRepositoryImpl().logout()
                                    } catch (_: Exception) {}
                                }
                                currentScreen = "login"
                            },
                        )

                        else -> SplashScreen()
                    }
                }

                    // Single persistent GPU-accelerated Liquid Glass Bottom Navigation Dock
                    // Never torn down across main tab switches so the liquid glass pill glides continuously at 60/120 FPS
                    AnimatedVisibility(
                        visible = activeBottomTab != null,
                        enter = fadeIn(tween(180)) + slideInVertically(tween(200, easing = FastOutSlowInEasing)) { it / 2 },
                        exit = fadeOut(tween(140)) + slideOutVertically(tween(160)) { it / 2 },
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        IosLiquidGlassBottomNav(
                            selectedTab = activeBottomTab ?: "Home",
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                        )
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
