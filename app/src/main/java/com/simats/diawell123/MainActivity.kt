package com.simats.diawell123

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.simats.diawell123.ui.glucose.LogGlucoseScreen
import com.simats.diawell123.ui.glucose.ReadingDetailsScreen
import com.simats.diawell123.ui.home.HomeScreen
import com.simats.diawell123.ui.meds.MedicationReminderScreen
import com.simats.diawell123.ui.meds.MedicationsScreen
import com.simats.diawell123.ui.notifications.NotificationsScreen
import com.simats.diawell123.ui.onboarding.OnboardingContainerScreen
import com.simats.diawell123.ui.profile.EditProfileScreen
import com.simats.diawell123.ui.profile.FoodPreferencesSettingsScreen
import com.simats.diawell123.ui.profile.HealthPreferencesScreen
import com.simats.diawell123.ui.profile.PatientProfileScreen
import com.simats.diawell123.ui.reports.WeeklyReportScreen
import com.simats.diawell123.ui.settings.DeleteAccountScreen
import com.simats.diawell123.ui.settings.ExportRecordsScreen
import com.simats.diawell123.ui.settings.HelpSupportScreen
import com.simats.diawell123.ui.settings.LogoutConfirmationDialog
import com.simats.diawell123.ui.settings.NotificationPreferencesScreen
import com.simats.diawell123.ui.settings.PrivacyPolicyScreen
import com.simats.diawell123.ui.settings.PrivacySecurityScreen
import com.simats.diawell123.ui.settings.SettingsScreen
import com.simats.diawell123.ui.settings.TermsOfUseScreen
import com.simats.diawell123.ui.theme.Diawell123Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Diawell123Theme {
                var currentScreen by remember { mutableStateOf("splash") }

                LaunchedEffect(Unit) {
                    delay(2200)
                    if (currentScreen == "splash") {
                        currentScreen = "onboarding"
                    }
                }

                when (currentScreen) {
                    "splash" -> SplashScreen()

                    "onboarding" -> OnboardingContainerScreen(
                        onFinishOnboarding = { currentScreen = "signup" },
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
                        onLogGlucoseClick = { currentScreen = "log_glucose" },
                        onTrackFoodClick = { currentScreen = "explore_food" },
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
                        onBackClick = { currentScreen = "home" },
                        onSaveReadingClick = { currentScreen = "glucose_history" },
                    )

                    "glucose_history" -> GlucoseHistoryScreen(
                        onBackClick = { currentScreen = "home" },
                        onLogGlucoseClick = { currentScreen = "log_glucose" },
                        onReadingClick = { currentScreen = "reading_details" },
                    )

                    "reading_details" -> ReadingDetailsScreen(
                        onBackClick = { currentScreen = "glucose_history" },
                    )

                    "daily_diet_plan" -> DailyDietPlanScreen(
                        onBackClick = { currentScreen = "home" },
                        onMealDetailsClick = { currentScreen = "food_details" },
                        onHomeTabClick = { currentScreen = "home" },
                        onDietTabClick = { currentScreen = "daily_diet_plan" },
                        onMedsTabClick = { currentScreen = "medications" },
                        onReportsTabClick = { currentScreen = "weekly_report" },
                        onSettingsTabClick = { currentScreen = "settings" },
                    )

                    "explore_food" -> ExploreFoodScreen(
                        onBackClick = { currentScreen = "daily_diet_plan" },
                        onMealDetailsClick = { currentScreen = "food_details" },
                    )

                    "food_details" -> FoodDetailsScreen(
                        onBackClick = { currentScreen = "daily_diet_plan" },
                        onAddToFoodLogClick = { currentScreen = "food_log" },
                    )

                    "food_log" -> FoodLogScreen(
                        onBackClick = { currentScreen = "home" },
                        onAddFoodClick = { currentScreen = "explore_food" },
                        onMealDetailsClick = { currentScreen = "food_details" },
                    )

                    "nutrition_summary" -> NutritionSummaryScreen(
                        onBackClick = { currentScreen = "home" },
                        onReturnToFoodLogClick = { currentScreen = "food_log" },
                    )

                    "medications" -> MedicationsScreen(
                        onBackClick = { currentScreen = "home" },
                        onAddMedicationClick = { currentScreen = "medication_reminder" },
                        onReminderClick = { currentScreen = "medication_reminder" },
                        onHomeTabClick = { currentScreen = "home" },
                        onDietTabClick = { currentScreen = "daily_diet_plan" },
                        onMedsTabClick = { currentScreen = "medications" },
                        onReportsTabClick = { currentScreen = "weekly_report" },
                        onSettingsTabClick = { currentScreen = "settings" },
                    )

                    "medication_reminder" -> MedicationReminderScreen(
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
                        onPrivacyPolicyClick = { currentScreen = "privacy_policy" },
                        onTermsOfUseClick = { currentScreen = "terms_of_use" },
                        onExportRecordsClick = { currentScreen = "export_records" },
                        onDeleteAccountClick = { currentScreen = "delete_account" },
                    )

                    "privacy_policy" -> PrivacyPolicyScreen(
                        onBackClick = { currentScreen = "privacy_security" },
                    )

                    "terms_of_use" -> TermsOfUseScreen(
                        onBackClick = { currentScreen = "privacy_security" },
                    )

                    "export_records" -> ExportRecordsScreen(
                        onBackClick = { currentScreen = "privacy_security" },
                        onExportClick = { currentScreen = "privacy_security" },
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
                        onConfirmLogoutClick = { currentScreen = "login" },
                    )

                    else -> SplashScreen()
                }
            }
        }
    }
}
