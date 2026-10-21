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
import com.simats.diawell123.ui.profile.HealthPreferencesScreen
import com.simats.diawell123.ui.profile.PatientProfileScreen
import com.simats.diawell123.ui.reports.WeeklyReportScreen
import com.simats.diawell123.ui.settings.LogoutConfirmationDialog
import com.simats.diawell123.ui.settings.SettingsScreen
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
                        onMealsTabClick = { currentScreen = "food_log" },
                        onGlucoseTabClick = { currentScreen = "glucose_history" },
                        onReportsTabClick = { currentScreen = "weekly_report" },
                        onSettingsTabClick = { currentScreen = "settings" },
                        onProfileClick = { currentScreen = "patient_profile" },
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
                    )

                    "explore_food" -> ExploreFoodScreen(
                        onBackClick = { currentScreen = "home" },
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
                        onMealsTabClick = { currentScreen = "food_log" },
                        onGlucoseTabClick = { currentScreen = "glucose_history" },
                        onSettingsTabClick = { currentScreen = "settings" },
                    )

                    "notifications" -> NotificationsScreen(
                        onBackClick = { currentScreen = "home" },
                        onHomeTabClick = { currentScreen = "home" },
                        onMealsTabClick = { currentScreen = "food_log" },
                        onGlucoseTabClick = { currentScreen = "glucose_history" },
                        onReportsTabClick = { currentScreen = "weekly_report" },
                        onSettingsTabClick = { currentScreen = "settings" },
                        onNotificationClick = { currentScreen = "medication_reminder" },
                    )

                    "settings" -> SettingsScreen(
                        onBackClick = { currentScreen = "home" },
                        onPersonalProfileClick = { currentScreen = "patient_profile" },
                        onNotificationPrefsClick = { currentScreen = "notifications" },
                        onFoodPrefsClick = { currentScreen = "health_preferences" },
                        onAiChatbotClick = { currentScreen = "ai_chatbot" },
                        onLogoutClick = { currentScreen = "logout_dialog" },
                        onHomeTabClick = { currentScreen = "home" },
                        onMealsTabClick = { currentScreen = "food_log" },
                        onGlucoseTabClick = { currentScreen = "glucose_history" },
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
