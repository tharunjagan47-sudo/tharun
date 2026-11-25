package com.simats.diawell123.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.simats.diawell123.data.local.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Global shared state for patient profile details and avatar image.
 * Provides instant local persistence to internal storage so profile photos
 * persist across app closures, backgrounding, and restarts.
 */
object ProfileState {
    private const val TAG = "ProfileState"
    private const val AVATAR_FILE_PREFIX = "diawell_avatar_"

    private var appContext: Context? = null

    @Volatile
    var activeUserId: Int? = null
        private set

    @Volatile
    var isUploadingAvatar: Boolean = false

    @Volatile
    private var loadedPhotoUrlForUser: Pair<Int, String>? = null

    var userName by mutableStateOf(
        try {
            SessionManager.getInstance().getFullName()?.takeIf { it.isNotBlank() } ?: "User"
        } catch (_: Exception) {
            "User"
        }
    )
    var userEmail by mutableStateOf(
        try {
            SessionManager.getInstance().getUserSession()?.email?.takeIf { it.isNotBlank() } ?: ""
        } catch (_: Exception) {
            ""
        }
    )
    var profileImageBitmap by mutableStateOf<ImageBitmap?>(null)

    private fun getLegacySharedAvatarFile(context: Context): File {
        return File(context.applicationContext.filesDir, "${AVATAR_FILE_PREFIX}current.jpg")
    }

    private fun cleanupLegacySharedAvatar(context: Context) {
        try {
            val legacyFile = getLegacySharedAvatarFile(context)
            if (legacyFile.exists()) {
                legacyFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cleanup legacy shared avatar: ${e.message}")
        }
    }

    /**
     * Deletes the per-user cached avatar file and legacy shared fallback file.
     */
    fun deleteUserAvatarFile(userId: Int, context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext ?: return
        cleanupLegacySharedAvatar(ctx)
        if (userId > 0) {
            try {
                val userFile = getAvatarFile(ctx, userId)
                if (userFile != null && userFile.exists()) {
                    userFile.delete()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete user avatar file for user $userId: ${e.message}")
            }
        }
    }

    /**
     * Initializes ProfileState from persistent SessionManager and strict per-user disk cache.
     * Should be called on app launch (e.g. in MainActivity) or when user logs in.
     */
    fun init(context: Context, userId: Int? = null) {
        val appCtx = context.applicationContext
        appContext = appCtx
        cleanupLegacySharedAvatar(appCtx)

        try {
            val sessionManager = try {
                SessionManager.getInstance()
            } catch (_: Exception) {
                SessionManager.init(appCtx)
            }

            val effectiveUserId = userId ?: sessionManager.getUserId()
            if (effectiveUserId == null || effectiveUserId <= 0) {
                clear(appCtx)
                return
            }

            if (activeUserId != effectiveUserId) {
                profileImageBitmap = null
                loadedPhotoUrlForUser = null
                isUploadingAvatar = false
                userName = "User"
                userEmail = ""
                activeUserId = effectiveUserId
            }

            val fullName = sessionManager.getFullName()?.takeIf { it.isNotBlank() }
                ?: sessionManager.getUserSession()?.fullName?.takeIf { it.isNotBlank() }
            userName = fullName ?: "User"

            val session = sessionManager.getUserSession()
            userEmail = session?.email?.takeIf { it.isNotBlank() } ?: ""

            if (sessionManager.isPfpOwnerMismatch(effectiveUserId, session?.email)) {
                sessionManager.clearUserPfp(effectiveUserId)
                deleteUserAvatarFile(effectiveUserId, appCtx)
            }

            // 1. Try to load per-user Base64 cached avatar from SharedPreferences
            var loaded = false
            val cachedPfp = sessionManager.getUserPfp(effectiveUserId, session?.email)
            if (!cachedPfp.isNullOrBlank()) {
                loaded = setProfilePicFromBase64(cachedPfp, effectiveUserId, appCtx)
            }

            // 2. Fallback to strict per-user local disk cache file (never shared fallback)
            if (!loaded) {
                loaded = loadFromDisk(appCtx, effectiveUserId)
            }

            // 3. Fallback to downloading from profilePhotoUrl if available
            if (!loaded) {
                val photoUrl = session?.profilePhotoUrl
                if (!photoUrl.isNullOrBlank()) {
                    loadPhotoFromUrl(photoUrl, effectiveUserId)
                } else if (!isUploadingAvatar) {
                    profileImageBitmap = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ProfileState: ${e.message}", e)
        }
    }

    /**
     * Authoritatively synchronizes ProfileState with backend user profile / dashboard payload.
     */
    fun syncFromServer(
        userId: Int,
        fullName: String? = null,
        email: String? = null,
        profilePicBase64: String? = null,
        profilePhotoUrl: String? = null,
        context: Context? = null
    ) {
        if (userId <= 0) return
        val ctx = context?.applicationContext ?: appContext

        val sessionManager = try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            ctx?.let { SessionManager.init(it) }
        }

        val loggedInUserId = sessionManager?.getUserId()
        if (loggedInUserId != null && loggedInUserId != userId) {
            Log.d(TAG, "Ignoring stale server profile response for user $userId (active session: $loggedInUserId)")
            return
        }

        if (activeUserId != userId) {
            profileImageBitmap = null
            loadedPhotoUrlForUser = null
            activeUserId = userId
        }

        if (!fullName.isNullOrBlank()) {
            userName = fullName
            sessionManager?.saveUserName(fullName)
        }
        if (!email.isNullOrBlank()) {
            userEmail = email
            sessionManager?.saveUserEmail(email)
        }

        val activeEmail = email?.takeIf { it.isNotBlank() }
            ?: userEmail.takeIf { it.isNotBlank() }
            ?: sessionManager?.getUserSession()?.email

        if (!profilePicBase64.isNullOrBlank()) {
            sessionManager?.saveUserPfp(userId, profilePicBase64, activeEmail)
            setProfilePicFromBase64(profilePicBase64, userId, ctx)
        } else if (!profilePhotoUrl.isNullOrBlank()) {
            sessionManager?.saveProfilePhotoUrl(profilePhotoUrl)
            if (profileImageBitmap == null || loadedPhotoUrlForUser != (userId to profilePhotoUrl)) {
                loadPhotoFromUrl(profilePhotoUrl, userId)
            }
        } else if (!isUploadingAvatar) {
            // Backend confirmed this account currently has no profile photo: ensure no other user's photo shows
            sessionManager?.clearUserPfp(userId)
            deleteUserAvatarFile(userId, ctx)
            profileImageBitmap = null
            loadedPhotoUrlForUser = null
        }
    }

    /**
     * Resolves the per-user local avatar File on device internal storage.
     * Never returns a shared cross-account file.
     */
    fun getAvatarFile(context: Context, userId: Int? = null): File? {
        if (userId == null || userId <= 0) return null
        return File(context.applicationContext.filesDir, "${AVATAR_FILE_PREFIX}$userId.jpg")
    }

    /**
     * Saves a chosen Bitmap to private per-user internal storage and updates ProfileState immediately.
     */
    fun saveProfileBitmap(context: Context, bitmap: Bitmap, userId: Int? = null) {
        val appCtx = context.applicationContext
        appContext = appCtx
        cleanupLegacySharedAvatar(appCtx)

        val effectiveUserId = userId?.takeIf { it > 0 } ?: activeUserId?.takeIf { it > 0 }
        if (effectiveUserId == null) return

        if (activeUserId == null || activeUserId == effectiveUserId) {
            activeUserId = effectiveUserId
            profileImageBitmap = bitmap.asImageBitmap()
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val targetFile = getAvatarFile(appCtx, effectiveUserId) ?: return@launch
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    out.flush()
                }
                Log.d(TAG, "Successfully saved profile photo to disk: ${targetFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save profile photo to disk: ${e.message}", e)
            }
        }
    }

    /**
     * Loads the avatar bitmap from strict per-user local disk cache.
     * Returns true if successfully loaded and set.
     */
    fun loadFromDisk(context: Context, userId: Int? = null): Boolean {
        if (userId == null || userId <= 0) return false
        if (activeUserId != null && activeUserId != userId) return false

        try {
            val sessionManager = try {
                SessionManager.getInstance()
            } catch (_: Exception) {
                null
            }
            if (sessionManager != null && sessionManager.isPfpOwnerMismatch(userId)) {
                deleteUserAvatarFile(userId, context)
                return false
            }

            val userFile = getAvatarFile(context, userId)
            if (userFile != null && userFile.exists() && userFile.length() > 0) {
                val bitmap = BitmapFactory.decodeFile(userFile.absolutePath)
                if (bitmap != null && (activeUserId == null || activeUserId == userId)) {
                    activeUserId = userId
                    profileImageBitmap = bitmap.asImageBitmap()
                    Log.d(TAG, "Successfully loaded profile avatar from disk: ${userFile.name}")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading avatar from disk: ${e.message}", e)
        }
        return false
    }

    /**
     * Asynchronously downloads photo from remote URL, saves to strict per-user local disk, and updates ProfileState.
     */
    fun loadPhotoFromUrl(photoUrl: String, userId: Int? = null) {
        if (photoUrl.isBlank() || userId == null || userId <= 0) return
        val ctx = appContext ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(photoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("ngrok-skip-browser-warning", "true")
                connection.setRequestProperty("User-Agent", "DiaWell-Android-Client")
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val input: InputStream = connection.inputStream
                    val bitmap = BitmapFactory.decodeStream(input)
                    input.close()

                    if (bitmap != null) {
                        val targetFile = getAvatarFile(ctx, userId)
                        if (targetFile != null) {
                            FileOutputStream(targetFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                out.flush()
                            }
                        }

                        withContext(Dispatchers.Main) {
                            val currentSessionUid = try { SessionManager.getInstance().getUserId() } catch (_: Exception) { null }
                            if (activeUserId == userId && (currentSessionUid == null || currentSessionUid == userId)) {
                                profileImageBitmap = bitmap.asImageBitmap()
                                loadedPhotoUrlForUser = userId to photoUrl
                                Log.d(TAG, "Successfully fetched and cached profile avatar from URL: $photoUrl")
                            }
                        }
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load profile photo from URL: ${e.message}")
            }
        }
    }

    /**
     * Decodes a Base64 JPEG string into an ImageBitmap, updates memory state, and saves to per-user disk.
     */
    fun setProfilePicFromBase64(base64Str: String?, userId: Int? = null, context: Context? = null): Boolean {
        if (base64Str.isNullOrBlank()) return false
        val effectiveUserId = userId?.takeIf { it > 0 } ?: activeUserId?.takeIf { it > 0 } ?: return false
        val currentSessionUid = try { SessionManager.getInstance().getUserId() } catch (_: Exception) { null }
        if (currentSessionUid != null && currentSessionUid != effectiveUserId) return false
        if (activeUserId != null && activeUserId != effectiveUserId) return false

        return try {
            val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                activeUserId = effectiveUserId
                profileImageBitmap = bitmap.asImageBitmap()
                val ctx = context ?: appContext
                if (ctx != null) {
                    saveProfileBitmap(ctx, bitmap, effectiveUserId)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error decoding base64 avatar: ${e.message}", e)
            false
        }
    }

    /**
     * Clears profile state on user logout.
     */
    fun clear(context: Context? = null) {
        val ctx = context?.applicationContext ?: appContext
        if (ctx != null) {
            cleanupLegacySharedAvatar(ctx)
        }
        activeUserId = null
        loadedPhotoUrlForUser = null
        isUploadingAvatar = false
        userName = "User"
        userEmail = ""
        profileImageBitmap = null
    }
}
