package com.simats.diawell123.ui.ai

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.Matrix as GlMatrix
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.annotation.RawRes
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Hardware-accelerated TextureView that renders a looping [MediaPlayer] video through an
 * OpenGL ES 2.0 fragment shader to key out the dark studio backdrop into true transparency,
 * keeping DIVA's face visor opaque and cropping out bottom subtitles/watermarks.
 */
internal class DivaTransparentVideoTextureView(
    context: Context,
    @RawRes private val videoResId: Int,
    private val onVideoReady: () -> Unit,
) : TextureView(context) {

    private var rendererThread: DivaGlVideoThread? = null

    @Volatile
    private var desiredPlaying: Boolean = false

    init {
        isOpaque = false
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                startRenderer(surface, width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int,
            ) {
                rendererThread?.updateViewSize(width, height)
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                stopRenderer()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    }

    fun setPlaying(playing: Boolean) {
        desiredPlaying = playing
        rendererThread?.setPlaying(playing)
    }

    fun releasePlayer() {
        stopRenderer()
    }

    private fun startRenderer(surface: SurfaceTexture, width: Int, height: Int) {
        stopRenderer()
        val thread = DivaGlVideoThread(
            context = context.applicationContext,
            targetSurface = surface,
            videoResId = videoResId,
            initialWidth = width,
            initialHeight = height,
            initialPlaying = desiredPlaying,
            onVideoReady = { post { onVideoReady() } },
        )
        rendererThread = thread
        thread.start()
    }

    private fun stopRenderer() {
        rendererThread?.requestStop()
        rendererThread = null
    }
}

private class DivaGlVideoThread(
    private val context: Context,
    private val targetSurface: SurfaceTexture,
    @RawRes private val videoResId: Int,
    initialWidth: Int,
    initialHeight: Int,
    initialPlaying: Boolean,
    private val onVideoReady: () -> Unit,
) : Thread("DivaGlVideoThread") {

    private val lock = Object()
    private val running = AtomicBoolean(true)
    private var frameAvailable = false

    @Volatile
    private var viewWidth = initialWidth

    @Volatile
    private var viewHeight = initialHeight

    @Volatile
    private var videoWidth = 1280

    @Volatile
    private var videoHeight = 720

    @Volatile
    private var desiredPlaying = initialPlaying

    @Volatile
    private var isPrepared = false

    private var mediaPlayer: MediaPlayer? = null
    private var videoSurfaceTexture: SurfaceTexture? = null
    private var videoSurface: Surface? = null

    private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var oesTextureId = 0
    private var programId = 0

    private val texMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    fun updateViewSize(width: Int, height: Int) {
        viewWidth = width
        viewHeight = height
        synchronized(lock) {
            frameAvailable = true
            lock.notifyAll()
        }
    }

    fun setPlaying(playing: Boolean) {
        desiredPlaying = playing
        val mp = mediaPlayer ?: return
        if (!isPrepared) return
        try {
            if (playing) {
                if (!mp.isPlaying) {
                    mp.start()
                }
            } else {
                if (mp.isPlaying) {
                    mp.pause()
                }
                mp.seekTo(0)
                synchronized(lock) {
                    frameAvailable = true
                    lock.notifyAll()
                }
            }
        } catch (_: IllegalStateException) {
        }
    }

    fun requestStop() {
        running.set(false)
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    override fun run() {
        try {
            initEgl()
            initGlProgram()
            initMediaPlayer()

            val vertexBuffer = createFloatBuffer(QUAD_VERTICES)
            val texBuffer = createFloatBuffer(QUAD_TEX_COORDS)

            val posHandle = GLES20.glGetAttribLocation(programId, "aPosition")
            val texHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
            val mvpHandle = GLES20.glGetUniformLocation(programId, "uMvpMatrix")
            val texMatHandle = GLES20.glGetUniformLocation(programId, "uTexMatrix")
            val samplerHandle = GLES20.glGetUniformLocation(programId, "uTexture")

            while (running.get()) {
                synchronized(lock) {
                    while (running.get() && !frameAvailable) {
                        lock.wait(50L)
                    }
                    frameAvailable = false
                }
                if (!running.get()) break

                val vst = videoSurfaceTexture ?: continue
                try {
                    vst.updateTexImage()
                    vst.getTransformMatrix(texMatrix)
                } catch (_: RuntimeException) {
                    continue
                }

                val vw = viewWidth.coerceAtLeast(1)
                val vh = viewHeight.coerceAtLeast(1)
                GLES20.glViewport(0, 0, vw, vh)
                GLES20.glClearColor(0f, 0f, 0f, 0f)
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                GLES20.glDisable(GLES20.GL_BLEND)

                computeFitMvpMatrix(vw, vh, videoWidth, videoHeight, mvpMatrix)

                GLES20.glUseProgram(programId)
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
                GLES20.glUniform1i(samplerHandle, 0)
                GLES20.glUniformMatrix4fv(mvpHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(texMatHandle, 1, false, texMatrix, 0)

                GLES20.glEnableVertexAttribArray(posHandle)
                GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

                GLES20.glEnableVertexAttribArray(texHandle)
                GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

                GLES20.glDisableVertexAttribArray(posHandle)
                GLES20.glDisableVertexAttribArray(texHandle)

                EGL14.eglSwapBuffers(eglDisplay, eglSurface)
            }
        } catch (t: Throwable) {
            Log.e("DivaGlVideoThread", "GL video rendering error", t)
        } finally {
            cleanup()
        }
    }

    private fun initEgl() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val version = IntArray(2)
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            error("eglInitialize failed")
        }

        val configAttribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(eglDisplay, configAttribs, 0, configs, 0, 1, numConfigs, 0) || numConfigs[0] == 0) {
            error("eglChooseConfig failed")
        }

        val ctxAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
        eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttribs, 0)
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], targetSurface, intArrayOf(EGL14.EGL_NONE), 0)
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            error("eglMakeCurrent failed")
        }
    }

    private fun initGlProgram() {
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        oesTextureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        programId = GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vs)
            GLES20.glAttachShader(prog, fs)
            GLES20.glLinkProgram(prog)
        }
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
    }

    private fun initMediaPlayer() {
        val vst = SurfaceTexture(oesTextureId).apply {
            setOnFrameAvailableListener {
                synchronized(lock) {
                    frameAvailable = true
                    lock.notifyAll()
                }
            }
        }
        videoSurfaceTexture = vst
        val surface = Surface(vst)
        videoSurface = surface

        val mp = MediaPlayer().apply {
            val uri = Uri.parse("android.resource://${context.packageName}/$videoResId")
            setDataSource(context, uri)
            setSurface(surface)
            isLooping = true
            setVolume(0f, 0f)
            setOnVideoSizeChangedListener { _, vw, vh ->
                if (vw > 0 && vh > 0) {
                    this@DivaGlVideoThread.videoWidth = vw
                    this@DivaGlVideoThread.videoHeight = vh
                }
            }
            setOnPreparedListener { player ->
                isPrepared = true
                onVideoReady()
                if (desiredPlaying) {
                    player.start()
                } else {
                    player.seekTo(0)
                }
                synchronized(lock) {
                    frameAvailable = true
                    lock.notifyAll()
                }
            }
            prepareAsync()
        }
        mediaPlayer = mp
    }

    private fun computeFitMvpMatrix(
        vw: Int,
        vh: Int,
        vidW: Int,
        vidH: Int,
        out: FloatArray,
    ) {
        GlMatrix.setIdentityM(out, 0)
        val safeVidW = vidW.coerceAtLeast(1).toFloat()
        val safeVidH = vidH.coerceAtLeast(1).toFloat()
        // Fit ~84% width and ~98% height inside the full-width stage so outstretched arms/hands
        // and lower body never collide with any quad edge while keeping exact idle proportions.
        val scale = minOf(
            vw.toFloat() / (safeVidW * 0.84f),
            vh.toFloat() / (safeVidH * 0.98f),
        )
        val sx = (safeVidW * scale) / vw.toFloat()
        val sy = (safeVidH * scale) / vh.toFloat()
        GlMatrix.translateM(out, 0, 0f, 0.02f, 0f)
        GlMatrix.scaleM(out, 0, sx, sy, 1f)
    }

    private fun compileShader(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun createFloatBuffer(data: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(data.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(data)
                position(0)
            }

    private fun cleanup() {
        try {
            mediaPlayer?.stop()
        } catch (_: IllegalStateException) {
        }
        mediaPlayer?.release()
        mediaPlayer = null
        videoSurface?.release()
        videoSurface = null
        videoSurfaceTexture?.release()
        videoSurfaceTexture = null

        if (programId != 0) {
            GLES20.glDeleteProgram(programId)
            programId = 0
        }
        if (oesTextureId != 0) {
            GLES20.glDeleteTextures(1, intArrayOf(oesTextureId), 0)
            oesTextureId = 0
        }
        if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(eglDisplay, eglSurface)
            if (eglContext != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglTerminate(eglDisplay)
        }
        eglSurface = EGL14.EGL_NO_SURFACE
        eglContext = EGL14.EGL_NO_CONTEXT
        eglDisplay = EGL14.EGL_NO_DISPLAY
    }

    companion object {
        private val QUAD_VERTICES = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )
        private val QUAD_TEX_COORDS = floatArrayOf(
            0f, 0f,
            1f, 0f,
            0f, 1f,
            1f, 1f,
        )

        private const val VERTEX_SHADER = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMvpMatrix;
            uniform mat4 uTexMatrix;
            varying vec2 vSampleUv;
            varying vec2 vVideoUv;
            void main() {
                gl_Position = uMvpMatrix * aPosition;
                vVideoUv = aTexCoord;
                vSampleUv = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            uniform samplerExternalOES uTexture;
            varying vec2 vSampleUv;
            varying vec2 vVideoUv;
            void main() {
                vec4 col = texture2D(uTexture, vSampleUv);
                float luma = dot(col.rgb, vec3(0.299, 0.587, 0.114));

                // Dark navy studio backdrop (#0B1022) has very low red & green (r < 0.09, luma < 0.10),
                // whereas purple/violet robot armor & cyan glow carry strong red/green energy.
                float bodyEnergy = max(col.r * 1.45, max(col.g * 1.15, luma * 1.30));

                // Preserve the dark face visor core inside DIVA's head
                vec2 visorDiff = vec2((vVideoUv.x - 0.50) / 0.125, (vVideoUv.y - 0.70) / 0.12);
                float inVisor = 1.0 - smoothstep(0.65, 1.0, length(visorDiff));

                // Key studio dark-navy backdrop completely to 0.0 alpha so zero rectangular box traces show
                float key = smoothstep(0.14, 0.25, bodyEnergy);
                float alpha = max(key, inVisor);

                // Smooth oval & soft bottom feather so no rectangular edge or hard bottom cut ever appears
                vec2 ovalDiff = vec2((vVideoUv.x - 0.50) / 0.49, (vVideoUv.y - 0.50) / 0.49);
                float ovalMask = 1.0 - smoothstep(0.84, 1.0, length(ovalDiff));
                float edgeBottom = smoothstep(0.0, 0.045, vVideoUv.y);
                float edgeTop = smoothstep(0.0, 0.05, 1.0 - vVideoUv.y);
                float watermarkMask = 1.0 - (smoothstep(0.89, 0.94, vVideoUv.x) * (1.0 - smoothstep(0.12, 0.19, vVideoUv.y)));
                alpha *= ovalMask * edgeBottom * edgeTop * watermarkMask;

                gl_FragColor = vec4(col.rgb * alpha, alpha);
            }
        """
    }
}
