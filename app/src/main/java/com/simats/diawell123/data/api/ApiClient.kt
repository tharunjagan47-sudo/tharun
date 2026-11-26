package com.simats.diawell123.data.api

import com.simats.diawell123.data.local.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
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
 * X-User-Id injection, multi-host/multi-port retries (prioritizing port 8080), and HTTP 401 auto-logout enforcement.
 */
object ApiClient {

    private val DEFAULT_BASE_URL = NetworkConstants.BASE_URL

    var baseUrl: String = DEFAULT_BASE_URL
        set(value) {
            field = if (value.trim().endsWith("/")) value.trim() else "${value.trim()}/"
            synchronized(this) {
                stickyWorkingEndpoint = null
                retrofitInstance = null
            }
        }

    fun setCustomBaseUrl(url: String) {
        baseUrl = url
    }

    private val DEFAULT_AI_BASE_URL = NetworkConstants.AI_BASE_URL

    var aiBaseUrl: String = DEFAULT_AI_BASE_URL
        set(value) {
            field = if (value.trim().endsWith("/")) value.trim() else "${value.trim()}/"
            synchronized(this) {
                aiRetrofitInstance = null
            }
        }

    fun setCustomAiBaseUrl(url: String) {
        aiBaseUrl = url
    }

    private fun getSessionManager(): SessionManager? {
        return try {
            SessionManager.getInstance()
        } catch (_: Exception) {
            null
        }
    }

    @Volatile
    private var stickyWorkingEndpoint: Triple<String, String, Int>? = null

    val localBackendHostPorts: List<Pair<String, Int>>
        get() = if (NetworkConstants.isEmulator) {
            listOf(
                "10.0.2.2" to 80,
                "127.0.0.1" to 8080,
                "127.0.0.1" to 80,
                "127.0.0.1" to 8000,
                "10.0.2.2" to 8000
            )
        } else {
            listOf(
                "127.0.0.1" to 8080,
                "127.0.0.1" to 80,
                "127.0.0.1" to 8000,
                "10.0.2.2" to 80,
                "10.0.2.2" to 8000
            )
        }

    private val backendEndpoints: List<Triple<String, String, Int>>
        get() = buildList {
            val cloudUrl = NetworkConstants.LIVE_CLOUD_HOSTED_URL.trim()
            if (cloudUrl.isNotEmpty()) {
                val parsed = cloudUrl.toHttpUrlOrNull()
                if (parsed != null) {
                    add(Triple(parsed.scheme, parsed.host, parsed.port))
                }
            }
            for ((host, port) in localBackendHostPorts) {
                add(Triple("http", host, port))
            }
            val ngrokHost = NetworkConstants.NGROK_HOST.trim()
            if (ngrokHost.isNotEmpty()) {
                add(Triple("https", ngrokHost, 443))
            }
        }

    private fun isLocalHost(host: String): Boolean =
        host in setOf("127.0.0.1", "localhost", "10.0.2.2")

    private fun buildEndpointRequest(
        request: okhttp3.Request,
        scheme: String,
        host: String,
        port: Int,
        pathPrefix: String? = null
    ): okhttp3.Request {
        val urlBuilder = request.url.newBuilder()
            .scheme(scheme)
            .host(host)
            .port(port)

        if (pathPrefix != null) {
            val rawPath = request.url.encodedPath
            val cleanPath = when {
                rawPath.startsWith("/diawell_backend/diawell_backend", ignoreCase = true) ->
                    rawPath.substring("/diawell_backend/diawell_backend".length)
                rawPath.startsWith("/diawell_backend", ignoreCase = true) ->
                    rawPath.substring("/diawell_backend".length)
                rawPath.startsWith("/DiaWell_backend", ignoreCase = true) ->
                    rawPath.substring("/DiaWell_backend".length)
                else -> rawPath
            }
            val newPath = pathPrefix.trimEnd('/') + '/' + cleanPath.trimStart('/')
            urlBuilder.encodedPath(newPath)
        }

        return request.newBuilder().url(urlBuilder.build()).build()
    }

    /**
     * Injects required Ngrok browser warning bypass and client User-Agent headers on all requests.
     */
    private val ngrokHeaderInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("ngrok-skip-browser-warning", "true")
            .header("User-Agent", "DiaWell-Android-Client")
            .build()
        chain.proceed(request)
    }

    /**
     * Interceptor to inject session cookies and X-User-Id into requests,
     * extract Set-Cookie (PHPSESSID), seamlessly fail over between Localhost and Ngrok,
     * and automatically log out on HTTP 401.
     */
    private val sessionInterceptor = Interceptor { chain ->
        val sessionManager = getSessionManager()
        val originalRequest = chain.request()
        val rawPath = originalRequest.url.encodedPath

        // Seamlessly route root requests to /diawell_backend/ if not already prefixed
        val effectiveRequest = if (!rawPath.startsWith("/diawell_backend", ignoreCase = true) &&
            !rawPath.startsWith("/DiaWell_backend", ignoreCase = true)
        ) {
            val prefixedPath = "/diawell_backend" + (if (rawPath.startsWith("/")) rawPath else "/$rawPath")
            val newUrl = originalRequest.url.newBuilder().encodedPath(prefixedPath).build()
            originalRequest.newBuilder().url(newUrl).build()
        } else {
            originalRequest
        }

        val requestBuilder = effectiveRequest.newBuilder()

        sessionManager?.let { sm ->
            sm.getUserId()?.let { userId ->
                requestBuilder.header("X-User-Id", userId.toString())
            }

            sm.getSessionCookie()?.let { cookie ->
                requestBuilder.header("Cookie", cookie)
            }

            sm.updateLastActiveTime()
        }

        val builtRequest = requestBuilder.build()
        val cachedEndpoint = stickyWorkingEndpoint
        val finalRequest = if (cachedEndpoint != null) {
            buildEndpointRequest(builtRequest, cachedEndpoint.first, cachedEndpoint.second, cachedEndpoint.third)
        } else {
            builtRequest
        }

        val primaryTimeoutSec = if (isLocalHost(finalRequest.url.host)) 2 else 10
        val response: Response = try {
            val primaryAttempt = chain.withConnectTimeout(primaryTimeoutSec, TimeUnit.SECONDS).proceed(finalRequest)
            if (primaryAttempt.isSuccessful || primaryAttempt.code != 404) {
                stickyWorkingEndpoint = Triple(finalRequest.url.scheme, finalRequest.url.host, finalRequest.url.port)
            }
            primaryAttempt
        } catch (primaryError: java.io.IOException) {
            stickyWorkingEndpoint = null
            val currentEndpoint = Triple(finalRequest.url.scheme, finalRequest.url.host, finalRequest.url.port)
            val candidates = backendEndpoints.filter { it != currentEndpoint }

            var resolvedResponse: Response? = null
            var lastError: java.io.IOException = primaryError

            // Seamless Localhost <-> Ngrok failover across all configured endpoints
            for ((altScheme, altHost, altPort) in candidates) {
                try {
                    val altRequest = buildEndpointRequest(builtRequest, altScheme, altHost, altPort)
                    val timeoutSec = if (isLocalHost(altHost)) 2 else 10
                    val attempt = chain.withConnectTimeout(timeoutSec, TimeUnit.SECONDS).proceed(altRequest)
                    if (attempt.isSuccessful || attempt.code != 404) {
                        stickyWorkingEndpoint = Triple(altScheme, altHost, altPort)
                        resolvedResponse = attempt
                        break
                    } else {
                        attempt.close()
                    }
                } catch (attemptError: java.io.IOException) {
                    lastError = attemptError
                }
            }

            // If primary hosts failed or returned 404, try nested directory /diawell_backend/diawell_backend/
            if (resolvedResponse == null || resolvedResponse.code == 404) {
                for ((altScheme, altHost, altPort) in backendEndpoints) {
                    try {
                        val nestedRequest = buildEndpointRequest(
                            builtRequest,
                            altScheme,
                            altHost,
                            altPort,
                            "/diawell_backend/diawell_backend"
                        )
                        val timeoutSec = if (isLocalHost(altHost)) 2 else 10
                        val attempt = chain.withConnectTimeout(timeoutSec, TimeUnit.SECONDS).proceed(nestedRequest)
                        if (attempt.isSuccessful) {
                            resolvedResponse = attempt
                            break
                        } else {
                            attempt.close()
                        }
                    } catch (_: Exception) {}
                }
            }

            resolvedResponse ?: throw lastError
        }

        sessionManager?.let { sm ->
            val setCookieHeaders = response.headers("Set-Cookie")
            for (header in setCookieHeaders) {
                if (header.contains("PHPSESSID")) {
                    val cookieValue = header.substringBefore(";").trim()
                    sm.saveSessionCookie(cookieValue)
                }
            }

            val requestPath = effectiveRequest.url.encodedPath
            val isAuthEndpoint = requestPath.contains("login.php") ||
                requestPath.contains("signup.php") ||
                requestPath.contains("register.php") ||
                requestPath.contains("logout.php")
            if (response.code == 401 && !isAuthEndpoint && sm.isLoggedIn()) {
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
            .addInterceptor(ngrokHeaderInterceptor)
            .addInterceptor(sessionInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createRetrofit(): Retrofit {
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

    private val aiOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ngrokHeaderInterceptor)
            .addInterceptor(sessionInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createAiRetrofit(): Retrofit {
        val sanitizedUrl = if (aiBaseUrl.trim().endsWith("/")) aiBaseUrl.trim() else "${aiBaseUrl.trim()}/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(aiOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Volatile
    private var aiRetrofitInstance: Retrofit? = null

    private fun getAiRetrofit(): Retrofit {
        return aiRetrofitInstance ?: synchronized(this) {
            aiRetrofitInstance ?: createAiRetrofit().also { aiRetrofitInstance = it }
        }
    }

    val diaWellApiService: DiaWellApiService
        get() = getRetrofit().create(DiaWellApiService::class.java)

    val diaWellBridgeApiService: DiaWellApiService
        get() = getRetrofit().create(DiaWellApiService::class.java)

    val profileApiService: ProfileApiService
        get() = getRetrofit().create(ProfileApiService::class.java)
}
