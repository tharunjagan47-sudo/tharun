package com.simats.diawell123.data.api

import com.simats.diawell123.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit Client configured for backend communication.
 * Automatically handles trailing slash formatting, session cookie persistence,
 * X-User-Id injection, and HTTP 401 auto-logout enforcement.
 */
object ApiClient {

    private const val DEFAULT_BASE_URL = "https://bhft6cwn-80.inc1.devtunnels.ms/diawell_backend"

    var baseUrl: String = DEFAULT_BASE_URL
        set(value) {
            // Retrofit requires base URLs to end with a trailing slash
            field = if (value.trim().endsWith("/")) value.trim() else "${value.trim()}/"
            synchronized(this) {
                retrofitInstance = null
            }
        }

    fun setCustomBaseUrl(url: String) {
        baseUrl = url
    }

    private fun getSessionManager(): SessionManager? {
        return try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Interceptor to inject session cookies and X-User-Id into requests,
     * extract Set-Cookie (PHPSESSID), and automatically log out on HTTP 401.
     */
    private val sessionInterceptor = Interceptor { chain ->
        val sessionManager = getSessionManager()
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        sessionManager?.let { sm ->
            // Inject cached X-User-Id header for PHP backend authentication
            sm.getUserId()?.let { userId ->
                requestBuilder.header("X-User-Id", userId.toString())
            }

            // Inject PHP session cookie (PHPSESSID) if present
            sm.getSessionCookie()?.let { cookie ->
                requestBuilder.header("Cookie", cookie)
            }

            // Track activity timestamp on outgoing request
            sm.updateLastActiveTime()
        }

        val response: Response = chain.proceed(requestBuilder.build())

        sessionManager?.let { sm ->
            // Intercept Set-Cookie from server to persist PHPSESSID
            val setCookieHeaders = response.headers("Set-Cookie")
            for (header in setCookieHeaders) {
                if (header.contains("PHPSESSID")) {
                    val cookieValue = header.substringBefore(";").trim()
                    sm.saveSessionCookie(cookieValue)
                }
            }

            // Automatic logout if backend returns 401 Unauthorized
            val requestPath = originalRequest.url.encodedPath
            val isAuthEndpoint = requestPath.contains("login.php") || requestPath.contains("signup.php") || requestPath.contains("register.php")
            if (response.code == 401 && !isAuthEndpoint) {
                sm.clearSession(reason = "Your session expired on the server. Please log in again.")
            }
        }

        response
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(sessionInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createRetrofit(): Retrofit {
        // Guarantee trailing slash for Retrofit validation
        val sanitizedUrl = if (baseUrl.trim().endsWith("/")) baseUrl.trim() else "${baseUrl.trim()}/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Volatile
    private var retrofitInstance: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            retrofitInstance ?: createRetrofit().also { retrofitInstance = it }
        }
    }

    val authApiService: AuthApiService
        get() = getRetrofit().create(AuthApiService::class.java)

    val dietApiService: DietApiService
        get() = getRetrofit().create(DietApiService::class.java)

    val glucoseApiService: GlucoseApiService
        get() = getRetrofit().create(GlucoseApiService::class.java)

    val homeApiService: HomeApiService
        get() = getRetrofit().create(HomeApiService::class.java)

    val medsApiService: MedsApiService
        get() = getRetrofit().create(MedsApiService::class.java)

    val activityApiService: ActivityApiService
        get() = getRetrofit().create(ActivityApiService::class.java)

    val notificationsApiService: NotificationsApiService
        get() = getRetrofit().create(NotificationsApiService::class.java)

    val reportsApiService: ReportsApiService
        get() = getRetrofit().create(ReportsApiService::class.java)

    val settingsApiService: SettingsApiService
        get() = getRetrofit().create(SettingsApiService::class.java)

    val profileApiService: ProfileApiService
        get() = getRetrofit().create(ProfileApiService::class.java)
}
