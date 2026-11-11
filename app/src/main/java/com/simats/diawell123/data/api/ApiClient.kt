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



    private val DEFAULT_BASE_URL = NetworkConstants.BASE_URL

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
    private var stickyWorkingHostPort: Pair<String, Int>? = null

    private val localBackendHostPorts: List<Pair<String, Int>>
        get() = if (NetworkConstants.isEmulator) {
            listOf(
                "10.0.2.2" to 80,
                "127.0.0.1" to 80,
                "127.0.0.1" to 8000,
                "192.168.1.7" to 80
            )
        } else {
            listOf(
                "127.0.0.1" to 80,
                "127.0.0.1" to 8000,
                "192.168.1.7" to 80,
                "10.0.2.2" to 80
            )
        }

    private fun buildEndpointRequest(
        request: okhttp3.Request,
        host: String,
        port: Int
    ): okhttp3.Request {
        val updatedUrl = request.url.newBuilder()
            .scheme("http")
            .host(host)
            .port(port)
            .build()
        return request.newBuilder().url(updatedUrl).build()
    }

    /**

     * Interceptor to inject session cookies and X-User-Id into requests,

     * extract Set-Cookie (PHPSESSID), and automatically log out on HTTP 401.

     */

    private val sessionInterceptor = Interceptor { chain ->

        val sessionManager = getSessionManager()

        val originalRequest = chain.request()

        val rawPath = originalRequest.url.encodedPath



        // Seamlessly route local root requests to /DiaWell_backend/ if not already prefixed

        val effectiveRequest = if (!rawPath.startsWith("/DiaWell_backend", ignoreCase = true) &&

            !rawPath.startsWith("/diawell_backend", ignoreCase = true)

        ) {

            val prefixedPath = "/DiaWell_backend" + (if (rawPath.startsWith("/")) rawPath else "/$rawPath")

            val newUrl = originalRequest.url.newBuilder().encodedPath(prefixedPath).build()

            originalRequest.newBuilder().url(newUrl).build()

        } else {

            originalRequest

        }



        val requestBuilder = effectiveRequest.newBuilder()



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



        val builtRequest = requestBuilder.build()
        val cachedHostPort = stickyWorkingHostPort
        val initialIsLocalHost = builtRequest.url.host in setOf("127.0.0.1", "localhost", "10.0.2.2", "192.168.1.7", "192.168.1.9")
        val finalRequest = if (initialIsLocalHost && cachedHostPort != null) {
            buildEndpointRequest(builtRequest, cachedHostPort.first, cachedHostPort.second)
        } else {
            builtRequest
        }

        val response: Response = try {
            chain.withConnectTimeout(4, TimeUnit.SECONDS).proceed(finalRequest)
        } catch (primaryError: java.io.IOException) {
            stickyWorkingHostPort = null
            val currentPair = finalRequest.url.host to finalRequest.url.port
            val candidates = localBackendHostPorts.filter { it != currentPair }

            var resolvedResponse: Response? = null
            var lastError: java.io.IOException = primaryError
            for ((altHost, altPort) in candidates) {
                try {
                    val altRequest = buildEndpointRequest(builtRequest, altHost, altPort)
                    val attempt = chain.withConnectTimeout(3, TimeUnit.SECONDS).proceed(altRequest)
                    stickyWorkingHostPort = altHost to altPort
                    resolvedResponse = attempt
                    break
                } catch (attemptError: java.io.IOException) {
                    lastError = attemptError
                }
            }
            resolvedResponse ?: throw lastError
        }



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

            val requestPath = effectiveRequest.url.encodedPath

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

            .connectTimeout(4, TimeUnit.SECONDS)

            .readTimeout(60, TimeUnit.SECONDS)

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



    private val aiOkHttpClient: OkHttpClient by lazy {

        OkHttpClient.Builder()

            .addInterceptor(loggingInterceptor)

            .connectTimeout(5, TimeUnit.SECONDS)

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

