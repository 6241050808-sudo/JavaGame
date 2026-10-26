package com.example.engine

import android.graphics.Bitmap
import com.example.data.model.FrameInterpolationMode

class FrameInterpolationEngine {

    private var previousFramePixels: IntArray? = null
    private var currentFramePixels: IntArray? = null
    private var interpolatedFramePixels: IntArray? = null
    private var cachedOutputBitmap: Bitmap? = null

    // Performance & FPS tracking
    private var lastFrameTimeNs: Long = 0L
    private var nativeFrameCount: Int = 0
    private var interpolatedFrameCount: Int = 0
    private var lastFpsUpdateMs: Long = 0L

    var currentNativeFps: Float = 30f
        private set
    var currentDisplayFps: Float = 60f
        private set
    var currentFrameTimeMs: Float = 16.6f
        private set
    var totalInterpolatedFrames: Long = 0L
        private set

    fun registerNativeFrame(newBitmap: Bitmap) {
        val width = newBitmap.width
        val height = newBitmap.height
        val totalPixels = width * height

        if (currentFramePixels == null || currentFramePixels!!.size != totalPixels) {
            currentFramePixels = IntArray(totalPixels)
            previousFramePixels = IntArray(totalPixels)
            interpolatedFramePixels = IntArray(totalPixels)
            cachedOutputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        } else {
            // Shift current into previous
            System.arraycopy(currentFramePixels!!, 0, previousFramePixels!!, 0, totalPixels)
        }

        newBitmap.getPixels(currentFramePixels!!, 0, width, 0, 0, width, height)
        nativeFrameCount++

        val nowMs = System.currentTimeMillis()
        if (nowMs - lastFpsUpdateMs >= 1000) {
            val deltaSec = (nowMs - lastFpsUpdateMs) / 1000f
            currentNativeFps = nativeFrameCount / deltaSec
            currentDisplayFps = (nativeFrameCount + interpolatedFrameCount) / deltaSec
            nativeFrameCount = 0
            interpolatedFrameCount = 0
            lastFpsUpdateMs = nowMs
        }
    }

    /**
     * Generates an interpolated intermediate frame between previous and current frames
     * using the specified interpolation mode and progress ratio (0.0f..1.0f).
     */
    fun getRenderableFrame(
        baseBitmap: Bitmap,
        mode: FrameInterpolationMode,
        subframeAlpha: Float = 0.5f
    ): Bitmap {
        if (mode == FrameInterpolationMode.OFF || previousFramePixels == null || currentFramePixels == null) {
            return baseBitmap
        }

        val totalPixels = currentFramePixels!!.size
        val prev = previousFramePixels!!
        val curr = currentFramePixels!!
        val out = interpolatedFramePixels!!

        when (mode) {
            FrameInterpolationMode.BLEND_50 -> {
                // High performance 50/50 bitwise fast blend
                for (i in 0 until totalPixels) {
                    val c1 = prev[i]
                    val c2 = curr[i]
                    // Fast parallel averaging without channel overflow:
                    // ((c1 and 0xFEFEFEFE) ushr 1) + ((c2 and 0xFEFEFEFE) ushr 1)
                    val r = (((c1 shr 16) and 0xFF) + ((c2 shr 16) and 0xFF)) shr 1
                    val g = (((c1 shr 8) and 0xFF) + ((c2 shr 8) and 0xFF)) shr 1
                    val b = ((c1 and 0xFF) + (c2 and 0xFF)) shr 1
                    val a = (((c1 ushr 24) and 0xFF) + ((c2 ushr 24) and 0xFF)) shr 1
                    out[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
            FrameInterpolationMode.MOTION_ADAPTIVE_60, FrameInterpolationMode.ULTRA_SMOOTH_120 -> {
                // Motion Adaptive Linear Interpolation with Alpha weighting
                val a2 = (subframeAlpha * 256).toInt().coerceIn(0, 256)
                val a1 = 256 - a2

                for (i in 0 until totalPixels) {
                    val c1 = prev[i]
                    val c2 = curr[i]

                    // If pixel hasn't changed (static background), keep it intact
                    if (c1 == c2) {
                        out[i] = c1
                    } else {
                        val r = ((((c1 shr 16) and 0xFF) * a1) + (((c2 shr 16) and 0xFF) * a2)) shr 8
                        val g = ((((c1 shr 8) and 0xFF) * a1) + (((c2 shr 8) and 0xFF) * a2)) shr 8
                        val b = (((c1 and 0xFF) * a1) + ((c2 and 0xFF) * a2)) shr 8
                        out[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                    }
                }
            }
            FrameInterpolationMode.OFF -> return baseBitmap
        }

        interpolatedFrameCount++
        totalInterpolatedFrames++

        val output = cachedOutputBitmap ?: Bitmap.createBitmap(
            baseBitmap.width,
            baseBitmap.height,
            Bitmap.Config.ARGB_8888
        ).also { cachedOutputBitmap = it }

        output.setPixels(out, 0, baseBitmap.width, 0, 0, baseBitmap.width, baseBitmap.height)
        return output
    }
}
