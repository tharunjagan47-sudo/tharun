package com.simats.diawell123.data.local

import android.content.Context
import android.content.SharedPreferences
import com.simats.diawell123.data.model.FoodPreferencesData
import com.simats.diawell123.data.model.UserData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Session Manager backed by Android SharedPreferences for persistent authentication.
 * Keeps user session active across app restarts, reboots, and backgrounding.
 * Invalidates session only on explicit user logout or server 401 Unauthorized.
 */
class SessionManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "diawell_user_session"

        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_DIABETES_TYPE = "diabetes_type"
        private const val KEY_AGE = "age"
        private const val KEY_HEIGHT_CM = "height_cm"
        private const val KEY_WEIGHT_KG = "weight_kg"
        private const val KEY_CURRENT_GLUCOSE = "current_glucose_mg_dl"
        private const val KEY_TARGET_GLUCOSE = "target_glucose_mg_dl"
        private const val KEY_PROFILE_PHOTO = "profile_photo"
        private const val KEY_PROFILE_PHOTO_URL = "profile_photo_url"
        private const val KEY_IS_PROFILE_COMPLETED = "is_profile_completed"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_SESSION_COOKIE = "session_cookie"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_LAST_ACTIVE_TIMESTAMP = "last_active_timestamp"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
        private const val KEY_EXPIRED_REASON = "session_expired_reason"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_BIOMETRIC_UNLOCK_ENABLED = "biometric_unlock_enabled"
        private const val KEY_DATA_AI_PREFS_ENABLED = "data_ai_prefs_enabled"
        private const val KEY_REMINDER_SOUND = "reminder_sound"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_QUIET_HOURS = "quiet_hours"
        private const val KEY_MED_ALARMS_ENABLED = "pref_med_alarms_enabled"
        private const val KEY_SMART_NUDGES_ENABLED = "pref_smart_nudges_enabled"
        private const val KEY_DR_JARVIS_VOICE_ENABLED = "pref_dr_jarvis_voice_enabled"
        private const val KEY_GLUCOSE_UNIT = "pref_glucose_unit"
        private const val KEY_PHONE = "phone"
        private const val KEY_GENDER = "gender"

        @Volatile
        private var instance: SessionManager? = null

        fun init(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context).also { instance = it }
            }
        }

        fun getInstance(): SessionManager {
            return instance ?: throw IllegalStateException(
                "SessionManager must be initialized before use. Call SessionManager.init(context) in Application class."
            )
        }
    }

    // Shared event flow to broadcast auto-logout across the app (Activity, ViewModels, Compose)
    private val _sessionExpiredEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sessionExpiredEvents: SharedFlow<String> = _sessionExpiredEvents.asSharedFlow()

    /**
     * Saves user details upon successful login or signup and marks session active.
     */
    fun saveUserSession(user: UserData, cookie: String? = null) {
        val now = System.currentTimeMillis()
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
            user.userId?.let { putInt(KEY_USER_ID, it) }
            putString(KEY_FULL_NAME, user.fullName.orEmpty())
            putString(KEY_EMAIL, user.email.orEmpty())
            putString(KEY_DIABETES_TYPE, user.diabetesType.orEmpty())
            putInt(KEY_AGE, user.age ?: -1)
            putFloat(KEY_HEIGHT_CM, user.heightCm ?: -1f)
            putFloat(KEY_WEIGHT_KG, user.weightKg ?: -1f)
            putFloat(KEY_CURRENT_GLUCOSE, user.currentGlucoseMgDl ?: -1f)
            putFloat(KEY_TARGET_GLUCOSE, user.targetGlucoseMgDl ?: -1f)
            putString(KEY_PROFILE_PHOTO, user.profilePhoto.orEmpty())
            putString(KEY_PROFILE_PHOTO_URL, user.profilePhotoUrl.orEmpty())
            putBoolean(KEY_IS_PROFILE_COMPLETED, user.isProfileCompleted ?: false)
            putString(KEY_AUTH_TOKEN, user.token.orEmpty())
            if (!cookie.isNullOrBlank()) {
                putString(KEY_SESSION_COOKIE, cookie)
            }
            putLong(KEY_LOGIN_TIMESTAMP, now)
            putLong(KEY_LAST_ACTIVE_TIMESTAMP, now)
            remove(KEY_EXPIRED_REASON)
            apply()
        }
    }

    /**
     * Checks if a valid persistent session exists for the user.
     * Returns true if user is logged in with a valid positive user ID.
     */
    fun hasActiveSession(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val userId = prefs.getInt(KEY_USER_ID, -1)
        if (loggedIn && userId > 0) {
            return true
        }
        // If data is corrupted or loggedIn is false, ensure session is clean
        if (loggedIn && userId <= 0) {
            clearSession(reason = "Invalid session data. Please log in again.")
            return false
        }
        return false
    }

    /**
     * Convenience getter for login state.
     */
    fun isLoggedIn(): Boolean = hasActiveSession()

    /**
     * Retrieves stored user session data.
     */
    fun getUserSession(): UserData? {
        if (!hasActiveSession()) return null

        val userId = prefs.getInt(KEY_USER_ID, -1).takeIf { it > 0 }
        val age = prefs.getInt(KEY_AGE, -1).takeIf { it != -1 }
        val height = prefs.getFloat(KEY_HEIGHT_CM, -1f).takeIf { it != -1f }
        val weight = prefs.getFloat(KEY_WEIGHT_KG, -1f).takeIf { it != -1f }
        val currentGlucose = prefs.getFloat(KEY_CURRENT_GLUCOSE, -1f).takeIf { it != -1f }
        val targetGlucose = prefs.getFloat(KEY_TARGET_GLUCOSE, -1f).takeIf { it != -1f }

        return UserData(
            userId = userId,
            fullName = prefs.getString(KEY_FULL_NAME, null),
            email = prefs.getString(KEY_EMAIL, null),
            diabetesType = prefs.getString(KEY_DIABETES_TYPE, null),
            age = age,
            heightCm = height,
            weightKg = weight,
            currentGlucoseMgDl = currentGlucose,
            targetGlucoseMgDl = targetGlucose,
            profilePhoto = prefs.getString(KEY_PROFILE_PHOTO, null),
            profilePhotoUrl = prefs.getString(KEY_PROFILE_PHOTO_URL, null),
            isProfileCompleted = prefs.getBoolean(KEY_IS_PROFILE_COMPLETED, false),
            token = prefs.getString(KEY_AUTH_TOKEN, null)
        )
    }

    fun getUserId(): Int? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        return if (id > 0) id else null
    }

    fun getSessionCookie(): String? {
        return prefs.getString(KEY_SESSION_COOKIE, null)
    }

    fun saveSessionCookie(cookie: String) {
        prefs.edit().putString(KEY_SESSION_COOKIE, cookie).apply()
    }

    fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true).apply()
    }

    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }

    fun isOnboardingCompleted(): Boolean = hasCompletedOnboarding()

    fun saveUserName(name: String) {
        prefs.edit().putString(KEY_FULL_NAME, name).apply()
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    fun saveProfilePhotoUrl(url: String) {
        prefs.edit().putString(KEY_PROFILE_PHOTO_URL, url).apply()
    }

    fun saveProfilePhoto(path: String) {
        prefs.edit().putString(KEY_PROFILE_PHOTO, path).apply()
    }

    /**
     * Updates activity timestamp.
     */
    fun updateLastActiveTime() {
        if (hasActiveSession()) {
            prefs.edit().putLong(KEY_LAST_ACTIVE_TIMESTAMP, System.currentTimeMillis()).apply()
        }
    }

    /**
     * Clears user session, resets credentials in SharedPreferences,
     * and broadcasts session termination.
     * Retains onboarding completion so user lands on Login screen instead of onboarding.
     */
    fun clearSession(reason: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            remove(KEY_USER_ID)
            remove(KEY_FULL_NAME)
            remove(KEY_EMAIL)
            remove(KEY_DIABETES_TYPE)
            remove(KEY_AGE)
            remove(KEY_HEIGHT_CM)
            remove(KEY_WEIGHT_KG)
            remove(KEY_CURRENT_GLUCOSE)
            remove(KEY_TARGET_GLUCOSE)
            remove(KEY_PROFILE_PHOTO)
            remove(KEY_PROFILE_PHOTO_URL)
            remove(KEY_IS_PROFILE_COMPLETED)
            remove(KEY_AUTH_TOKEN)
            remove(KEY_SESSION_COOKIE)
            remove(KEY_LOGIN_TIMESTAMP)
            remove(KEY_LAST_ACTIVE_TIMESTAMP)
            if (!reason.isNullOrBlank()) {
                putString(KEY_EXPIRED_REASON, reason)
            } else {
                remove(KEY_EXPIRED_REASON)
            }
            apply()
        }

        // Broadcast event so any active screens transition to Login
        _sessionExpiredEvents.tryEmit(reason ?: "Session ended.")
    }

    /**
     * Retrieves and clears any expired reason message (consumed once by LoginScreen).
     */
    fun consumeSessionExpiredReason(): String? {
        val reason = prefs.getString(KEY_EXPIRED_REASON, null)
        if (reason != null) {
            prefs.edit().remove(KEY_EXPIRED_REASON).apply()
        }
        return reason
    }

    fun isAppLockEnabled(): Boolean = prefs.getBoolean(KEY_APP_LOCK_ENABLED, false)
    fun setAppLockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, enabled).apply()

    fun isBiometricUnlockEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_UNLOCK_ENABLED, false)
    fun setBiometricUnlockEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_BIOMETRIC_UNLOCK_ENABLED, enabled).apply()

    fun isDataAiPrefsEnabled(): Boolean = prefs.getBoolean(KEY_DATA_AI_PREFS_ENABLED, true)
    fun setDataAiPrefsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_DATA_AI_PREFS_ENABLED, enabled).apply()

    fun getReminderSound(): String = prefs.getString(KEY_REMINDER_SOUND, "Gentle Chime") ?: "Gentle Chime"
    fun setReminderSound(sound: String) = prefs.edit().putString(KEY_REMINDER_SOUND, sound).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()

    fun getQuietHours(): String = prefs.getString(KEY_QUIET_HOURS, "10:00 PM - 7:00 AM") ?: "10:00 PM - 7:00 AM"
    fun setQuietHours(hours: String) = prefs.edit().putString(KEY_QUIET_HOURS, hours).apply()

    fun isMedAlarmsEnabled(): Boolean = prefs.getBoolean(KEY_MED_ALARMS_ENABLED, true)
    fun setMedAlarmsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_MED_ALARMS_ENABLED, enabled).apply()

    fun isSmartNudgesEnabled(): Boolean = prefs.getBoolean(KEY_SMART_NUDGES_ENABLED, true)
    fun setSmartNudgesEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SMART_NUDGES_ENABLED, enabled).apply()

    fun isDrJarvisVoiceEnabled(): Boolean = prefs.getBoolean(KEY_DR_JARVIS_VOICE_ENABLED, true)
    fun setDrJarvisVoiceEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_DR_JARVIS_VOICE_ENABLED, enabled).apply()

    fun getGlucoseUnit(): String = prefs.getString(KEY_GLUCOSE_UNIT, "mg/dL") ?: "mg/dL"
    fun setGlucoseUnit(unit: String) = prefs.edit().putString(KEY_GLUCOSE_UNIT, unit).apply()

    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""
    fun setPhone(phone: String) = prefs.edit().putString(KEY_PHONE, phone).apply()

    fun getGender(): String = prefs.getString(KEY_GENDER, "Male") ?: "Male"
    fun setGender(gender: String) = prefs.edit().putString(KEY_GENDER, gender).apply()

    fun saveFoodPreferences(data: FoodPreferencesData) {
        prefs.edit().apply {
            putString("pref_food_preferred_diet", data.preferredDiet)
            putString("pref_food_allergies", data.allergies.joinToString(","))
            putString("pref_food_preferred_styles", data.preferredStyles.joinToString(","))
            putString("pref_food_foods_to_avoid", data.foodsToAvoid.joinToString(","))
            apply()
        }
    }

    fun getCachedFoodPreferences(): FoodPreferencesData? {
        val diet = prefs.getString("pref_food_preferred_diet", null) ?: return null
        val allergies = prefs.getString("pref_food_allergies", "")
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val styles = prefs.getString("pref_food_preferred_styles", "")
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        val avoid = prefs.getString("pref_food_foods_to_avoid", "")
            ?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
        return FoodPreferencesData(
            userId = getUserId() ?: 0,
            preferredDiet = diet,
            allergies = allergies,
            preferredStyles = styles,
            foodsToAvoid = avoid
        )
    }
}
