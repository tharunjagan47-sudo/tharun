package com.simats.diawell123

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.simats.diawell123.ui.activity.ActivityHistoryScreen
import com.simats.diawell123.ui.activity.ActivityTrackingScreen
import com.simats.diawell123.ui.ai.AiChatbotScreen
import com.simats.diawell123.ui.auth.ForgotPasswordScreen
import com.simats.diawell123.ui.auth.LoginScreen
import com.simats.diawell123.ui.auth.SignUpScreen
import com.simats.diawell123.ui.diet.DailyDietPlanScreen
import com.simats.diawell123.ui.diet.ExploreFoodScreen
import com.simats.diawell123.ui.diet.FoodDetailsScreen
import com.simats.diawell123.ui.diet.FoodLogScreen
import com.simats.diawell123.ui.diet.NutritionSummaryScreen
import com.simats.diawell123.ui.glucose.GlucoseHistoryScreen
import com.simats.diawell123.ui.glucose.GlucoseViewModel
import com.simats.diawell123.ui.glucose.LogGlucoseScreen
import com.simats.diawell123.ui.glucose.ReadingDetailsScreen
import com.simats.diawell123.ui.home.HomeScreen
import com.simats.diawell123.ui.meds.MedicationReminderScreen
import com.simats.diawell123.ui.meds.MedicationsScreen
import com.simats.diawell123.ui.meds.SearchMedicationScreen
import com.simats.diawell123.ui.meds.SetMedicationReminderScreen
import com.simats.diawell123.ui.notifications.NotificationsScreen
import com.simats.diawell123.ui.onboarding.OnboardingContainerScreen
import com.simats.diawell123.ui.profile.EditProfileScreen
import com.simats.diawell123.ui.profile.FoodPreferencesSettingsScreen
import com.simats.diawell123.ui.profile.HealthPreferencesScreen
import com.simats.diawell123.ui.profile.PatientProfileScreen
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
import com.simats.diawell123.ui.settings.TermsOfUseScreen
import com.simats.diawell123.data.local.SessionManager
import com.simats.diawell123.data.repository.AuthRepositoryImpl
import com.simats.diawell123.ui.diet.DietViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Diawell123Theme {
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedMealCategory by remember { mutableStateOf("breakfast") }
                var selectedFoodId by remember { mutableStateOf<Int?>(null) }
                var selectedDishName by remember { mutableStateOf<String?>(null) }

                val dietViewModel: DietViewModel = viewModel()
                val glucoseViewModel: GlucoseViewModel = viewModel()
                val homeViewModel: com.simats.diawell123.ui.home.HomeViewModel = viewModel()
                val medsViewModel: com.simats.diawell123.ui.meds.MedsViewModel = viewModel()
                val activityViewModel: com.simats.diawell123.ui.activity.ActivityViewModel = viewModel()
                var selectedGlucoseReadingId by remember { mutableStateOf<Int?>(null) }
                var selectedMedication by remember { mutableStateOf<com.simats.diawell123.data.model.MedicationItem?>(null) }

                var selectedTabletName by remember { mutableStateOf("Metformin") }
                var selectedTabletDose by remember { mutableStateOf("500 mg • 1 Tablet") }

                val sessionManager = remember {
                    try {
                        SessionManager.getInstance()
                    } catch (_: Exception) {
                        SessionManager.init(applicationContext)
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

                // Update activity timestamp on screen changes
                LaunchedEffect(currentScreen) {
                    sessionManager.updateLastActiveTime()
                }

                // Splash screen initial routing based on saved persistent authentication session
                LaunchedEffect(Unit) {
                    delay(2200) // Show Splash Screen first
                    if (currentScreen == "splash") {
                        if (sessionManager.hasActiveSession()) {
                            // Persistent login: Automatically navigate directly to Dashboard without showing Login
                            currentScreen = "home"
                        } else {
                            // Logged out or session invalid: navigate to Login screen (or Onboarding if first launch)
                            currentScreen = if (sessionManager.hasCompletedOnboarding()) "login" else "onboarding"
                        }
                    }
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        if (initialState == "splash" && (targetState == "home" || targetState == "login" || targetState == "onboarding")) {
                            (fadeIn(animationSpec = tween(700, easing = FastOutSlowInEasing)) +
                                scaleIn(initialScale = 0.94f, animationSpec = tween(700))) togetherWith
                                (fadeOut(animationSpec = tween(500)) +
                                    scaleOut(targetScale = 1.05f, animationSpec = tween(500)))
                        } else {
                            (fadeIn(animationSpec = tween(400)) +
                                slideInHorizontally(animationSpec = tween(400)) { width -> width / 4 }) togetherWith
                                (fadeOut(animationSpec = tween(300)) +
                                    slideOutHorizontally(animationSpec = tween(300)) { width -> -width / 4 })
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
                            onSignUpSuccess = { currentScreen = "patient_profile" },
                        ) { currentScreen = "login" }

                        "login" -> LoginScreen(
                            onLoginSuccess = { currentScreen = "home" },
                            onForgotPasswordClick = { currentScreen = "forgot_password" },
                        ) { currentScreen = "signup" }

                        "forgot_password" -> ForgotPasswordScreen(
                            onBackClick = { currentScreen = "login" },
                            onSendResetLinkClick = { currentScreen = "login" },
                        )

                        "patient_profile" -> PatientProfileScreen(
                            onContinueClick = { currentScreen = "health_preferences" },
                        )

                        "health_preferences" -> HealthPreferencesScreen(
                            onBackClick = { currentScreen = "patient_profile" },
                            onSavePreferencesClick = { currentScreen = "home" },
                        )

                        "home" -> HomeScreen(
                            viewModel = homeViewModel,
                            onLogGlucoseClick = { currentScreen = "log_glucose" },
                            onGlucoseHistoryClick = { currentScreen = "glucose_history" },
                            onTrackFoodClick = { currentScreen = "daily_diet_plan" },
                            onActivityTrackingClick = { currentScreen = "activity_tracking" },
                            onDietChatbotClick = { currentScreen = "ai_chatbot" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                            onProfileClick = { currentScreen = "edit_profile" },
                        )

                        "edit_profile" -> EditProfileScreen(
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
                            onAddFoodClick = { currentScreen = "daily_diet_plan" },
                            onMealDetailsClick = { currentScreen = "food_details" },
                        )

                        "nutrition_summary" -> NutritionSummaryScreen(
                            onBackClick = { currentScreen = "home" },
                            onReturnToFoodLogClick = { currentScreen = "food_log" },
                        )

                        "activity_tracking" -> ActivityTrackingScreen(
                            viewModel = activityViewModel,
                            onBackClick = { currentScreen = "home" },
                            onHistoryClick = { currentScreen = "activity_history" },
                            onFinishActivityClick = { currentScreen = "activity_history" },
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
                            onMedsTabClick = { currentScreen = "medications" },
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

                        "ai_chatbot" -> AiChatbotScreen(
                            onBackClick = { currentScreen = "home" },
                        )

                        "weekly_report" -> WeeklyReportScreen(
                            onBackClick = { currentScreen = "home" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onSettingsTabClick = { currentScreen = "settings" },
                        )

                        "notifications" -> NotificationsScreen(
                            onBackClick = { currentScreen = "home" },
                            onHomeTabClick = { currentScreen = "home" },
                            onDietTabClick = { currentScreen = "daily_diet_plan" },
                            onMedsTabClick = { currentScreen = "medications" },
                            onReportsTabClick = { currentScreen = "weekly_report" },
                            onSettingsTabClick = { currentScreen = "settings" },
                            onNotificationClick = { currentScreen = "medication_reminder" },
                        )

                        "notification_prefs" -> NotificationPreferencesScreen(
                            onBackClick = { currentScreen = "settings" },
                            onSavePreferencesClick = { currentScreen = "settings" },
                        )

                        "food_preferences_settings" -> FoodPreferencesSettingsScreen(
                            onBackClick = { currentScreen = "settings" },
                            onSaveFoodPreferencesClick = { currentScreen = "settings" },
                        )

                        "privacy_security" -> PrivacySecurityScreen(
                            onBackClick = { currentScreen = "settings" },
                            onChangePasswordClick = { currentScreen = "change_password" },
                            onPrivacyPolicyClick = { currentScreen = "privacy_policy" },
                            onTermsOfUseClick = { currentScreen = "terms_of_use" },
                            onExportRecordsClick = { currentScreen = "export_records" },
                            onDeleteAccountClick = { currentScreen = "delete_account" },
                        )

                        "change_password" -> ChangePasswordScreen(
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
                            onBackClick = { currentScreen = "privacy_security" },
                            onExportClick = { currentScreen = "export_report_viewer" },
                        )

                        "export_report_viewer" -> ExportReportViewerScreen(
                            onBackClick = { currentScreen = "export_records" },
                            onDoneClick = { currentScreen = "privacy_security" },
                        )

                        "delete_account" -> DeleteAccountScreen(
                            onBackClick = { currentScreen = "privacy_security" },
                            onConfirmDeleteClick = { currentScreen = "login" },
                        )

                        "help_support" -> HelpSupportScreen(
                            onBackClick = { currentScreen = "settings" },
                            onContactSupportClick = { currentScreen = "settings" },
                        )

                        "settings" -> SettingsScreen(
                            onBackClick = { currentScreen = "home" },
                            onPersonalProfileClick = { currentScreen = "edit_profile" },
                            onNotificationPrefsClick = { currentScreen = "notification_prefs" },
                            onFoodPrefsClick = { currentScreen = "food_preferences_settings" },
                            onPrivacySecurityClick = { currentScreen = "privacy_security" },
                            onHelpSupportClick = { currentScreen = "help_support" },
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
                                sessionManager.clearSession()
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
            }
        }
    }
}
