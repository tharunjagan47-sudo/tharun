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

    var userName by mutableStateOf("Mary Jane")
    var userEmail by mutableStateOf("mary.jane@example.com")
    var profileImageBitmap by mutableStateOf<ImageBitmap?>(null)

    /**
     * Initializes ProfileState from persistent SessionManager and disk cache.
     * Should be called on app launch (e.g. in MainActivity) or when user logs in.
     */
    fun init(context: Context, userId: Int? = null) {
        val appCtx = context.applicationContext
        appContext = appCtx

        try {
            val sessionManager = try {
                SessionManager.getInstance()
            } catch (_: Exception) {
                SessionManager.init(appCtx)
            }

            val session = sessionManager.getUserSession()
            if (session != null) {
                session.fullName?.takeIf { it.isNotBlank() }?.let { userName = it }
                session.email?.takeIf { it.isNotBlank() }?.let { userEmail = it }
            }

            val effectiveUserId = userId ?: sessionManager.getUserId()

            // 1. Try to load per-user Base64 cached avatar from SharedPreferences
            var loaded = false
            if (effectiveUserId != null) {
                val cachedPfp = sessionManager.getUserPfp(effectiveUserId)
                if (!cachedPfp.isNullOrBlank()) {
                    loaded = setProfilePicFromBase64(cachedPfp, effectiveUserId, appCtx)
                }
            }

            // 2. Fallback to local disk cache file
            if (!loaded) {
                loaded = loadFromDisk(appCtx, effectiveUserId)
            }

            // 3. Fallback to downloading from profilePhotoUrl if available
            if (!loaded) {
                val photoUrl = session?.profilePhotoUrl
                if (!photoUrl.isNullOrBlank()) {
                    loadPhotoFromUrl(photoUrl, effectiveUserId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ProfileState: ${e.message}", e)
        }
    }

    /**
     * Resolves the local avatar File on device internal storage.
     */
    fun getAvatarFile(context: Context, userId: Int? = null): File {
        val fileName = if (userId != null && userId > 0) {
            "${AVATAR_FILE_PREFIX}$userId.jpg"
        } else {
            "${AVATAR_FILE_PREFIX}current.jpg"
        }
        return File(context.applicationContext.filesDir, fileName)
    }

    /**
     * Saves a chosen Bitmap to private internal storage and updates ProfileState immediately.
     */
    fun saveProfileBitmap(context: Context, bitmap: Bitmap, userId: Int? = null) {
        appContext = context.applicationContext
        profileImageBitmap = bitmap.asImageBitmap()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val targetFile = getAvatarFile(context, userId)
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    out.flush()
                }
                // Also write to current fallback file
                val fallbackFile = getAvatarFile(context, null)
                if (fallbackFile.absolutePath != targetFile.absolutePath) {
                    FileOutputStream(fallbackFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        out.flush()
                    }
                }
                Log.d(TAG, "Successfully saved profile photo to disk: ${targetFile.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save profile photo to disk: ${e.message}", e)
            }
        }
    }

    /**
     * Loads the avatar bitmap from local disk cache.
     * Returns true if successfully loaded and set.
     */
    fun loadFromDisk(context: Context, userId: Int? = null): Boolean {
        try {
            val userFile = getAvatarFile(context, userId)
            val fallbackFile = getAvatarFile(context, null)

            val fileToLoad = when {
                userFile.exists() && userFile.length() > 0 -> userFile
                fallbackFile.exists() && fallbackFile.length() > 0 -> fallbackFile
                else -> null
            }

            if (fileToLoad != null) {
                val bitmap = BitmapFactory.decodeFile(fileToLoad.absolutePath)
                if (bitmap != null) {
                    profileImageBitmap = bitmap.asImageBitmap()
                    Log.d(TAG, "Successfully loaded profile avatar from disk: ${fileToLoad.name}")
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading avatar from disk: ${e.message}", e)
        }
        return false
    }

    /**
     * Asynchronously downloads photo from remote URL, saves to local disk, and updates ProfileState.
     */
    fun loadPhotoFromUrl(photoUrl: String, userId: Int? = null) {
        if (photoUrl.isBlank()) return
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
                        FileOutputStream(targetFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                            out.flush()
                        }
                        val fallbackFile = getAvatarFile(ctx, null)
                        if (fallbackFile.absolutePath != targetFile.absolutePath) {
                            FileOutputStream(fallbackFile).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                out.flush()
                            }
                        }

                        withContext(Dispatchers.Main) {
                            profileImageBitmap = bitmap.asImageBitmap()
                            Log.d(TAG, "Successfully fetched and cached profile avatar from URL: $photoUrl")
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
     * Decodes a Base64 JPEG string into an ImageBitmap, updates memory state, and optionally saves to disk.
     */
    fun setProfilePicFromBase64(base64Str: String?, userId: Int? = null, context: Context? = null): Boolean {
        if (base64Str.isNullOrBlank()) return false
        return try {
            val cleanBase64 = if (base64Str.contains(",")) base64Str.substringAfter(",") else base64Str
            val bytes = android.util.Base64.decode(cleanBase64, android.util.Base64.NO_WRAP)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                profileImageBitmap = bitmap.asImageBitmap()
                val ctx = context ?: appContext
                if (userId != null && ctx != null) {
                    saveProfileBitmap(ctx, bitmap, userId)
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
    fun clear() {
        userName = "User"
        userEmail = ""
        profileImageBitmap = null
    }
}
