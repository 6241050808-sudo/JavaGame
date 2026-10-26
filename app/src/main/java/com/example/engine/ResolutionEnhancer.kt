package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.example.data.model.ResolutionEnhancer

object ResolutionEnhancer {

    private var scale2xCachedBitmap: Bitmap? = null
    private var filterCachedBitmap: Bitmap? = null

    /**
     * Applies the selected resolution enhancer algorithm to the input J2ME frame.
     */
    fun enhanceResolution(
        sourceBitmap: Bitmap,
        enhancer: ResolutionEnhancer
    ): Bitmap {
        return when (enhancer) {
            ResolutionEnhancer.NEAREST_NEIGHBOR -> sourceBitmap
            ResolutionEnhancer.SCALE2X_EPX -> applyScale2x(sourceBitmap)
            ResolutionEnhancer.CRT_SCANLINES -> applyCrtScanlines(sourceBitmap)
            ResolutionEnhancer.LCD_SUBPIXEL_GRID -> applyLcdGrid(sourceBitmap)
            ResolutionEnhancer.SHARPEN_HIGHRES -> applySharpenFilter(sourceBitmap)
            ResolutionEnhancer.BILINEAR_SMOOTH -> applyBilinearUpscale(sourceBitmap)
        }
    }

    /**
     * Scale2x / EPX (Eric's Pixel Expansion) algorithm.
     * Doubles resolution (e.g. 240x320 -> 480x640) while smoothing pixel art contours.
     */
    private fun applyScale2x(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val outW = w * 2
        val outH = h * 2

        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val outPixels = IntArray(outW * outH)

        for (y in 0 until h) {
            val yPrev = if (y > 0) y - 1 else 0
            val yNext = if (y < h - 1) y + 1 else h - 1

            for (x in 0 until w) {
                val xPrev = if (x > 0) x - 1 else 0
                val xNext = if (x < w - 1) x + 1 else w - 1

                val p = srcPixels[y * w + x]
                val a = srcPixels[yPrev * w + x] // top
                val b = srcPixels[y * w + xNext] // right
                val c = srcPixels[y * w + xPrev] // left
                val d = srcPixels[yNext * w + x] // bottom

                // Scale2x pixel rules
                var p1 = p
                var p2 = p
                var p3 = p
                var p4 = p

                if (c == a && c != d && a != b) p1 = a
                if (a == b && a != c && b != d) p2 = b
                if (d == c && d != b && c != a) p3 = c
                if (b == d && b != a && d != c) p4 = d

                val outRow1 = (y * 2) * outW + (x * 2)
                val outRow2 = (y * 2 + 1) * outW + (x * 2)

                outPixels[outRow1] = p1
                outPixels[outRow1 + 1] = p2
                outPixels[outRow2] = p3
                outPixels[outRow2 + 1] = p4
            }
        }

        val output = if (scale2xCachedBitmap?.width == outW && scale2xCachedBitmap?.height == outH) {
            scale2xCachedBitmap!!
        } else {
            Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888).also { scale2xCachedBitmap = it }
        }
        output.setPixels(outPixels, 0, outW, 0, 0, outW, outH)
        return output
    }

    /**
     * Authentic CRT Scanlines simulation with subtle phosphor lines.
     */
    private fun applyCrtScanlines(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        for (y in 0 until h) {
            if (y % 2 == 1) {
                val rowOffset = y * w
                for (x in 0 until w) {
                    val color = pixels[rowOffset + x]
                    val a = (color ushr 24) and 0xFF
                    val r = (((color shr 16) and 0xFF) * 75) / 100 // 25% scanline darkening
                    val g = (((color shr 8) and 0xFF) * 75) / 100
                    val b = ((color and 0xFF) * 85) / 100 // slight cool phosphor tint
                    pixels[rowOffset + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }

        val output = getFilterBitmap(w, h)
        output.setPixels(pixels, 0, w, 0, 0, w, h)
        return output
    }

    /**
     * Authentic Nokia / Sony Ericsson LCD subpixel grid pattern.
     */
    private fun applyLcdGrid(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val pixels = IntArray(w * h)
        src.getPixels(pixels, 0, w, 0, 0, w, h)

        for (y in 0 until h) {
            val isGridRow = (y % 3 == 0)
            val rowOffset = y * w
            for (x in 0 until w) {
                val isGridCol = (x % 3 == 0)
                if (isGridRow || isGridCol) {
                    val color = pixels[rowOffset + x]
                    val a = (color ushr 24) and 0xFF
                    val r = (((color shr 16) and 0xFF) * 82) / 100
                    val g = (((color shr 8) and 0xFF) * 88) / 100 // slight green LCD tint
                    val b = (((color and 0xFF) * 80) / 100)
                    pixels[rowOffset + x] = (a shl 24) or (r shl 16) or (g shl 8) or b
                }
            }
        }

        val output = getFilterBitmap(w, h)
        output.setPixels(pixels, 0, w, 0, 0, w, h)
        return output
    }

    /**
     * 3x3 Convolution Sharpening Filter.
     */
    private fun applySharpenFilter(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val srcPixels = IntArray(w * h)
        src.getPixels(srcPixels, 0, w, 0, 0, w, h)
        val outPixels = IntArray(w * h)

        for (y in 1 until h - 1) {
            val yTop = (y - 1) * w
            val yMid = y * w
            val yBot = (y + 1) * w

            for (x in 1 until w - 1) {
                val cMid = srcPixels[yMid + x]
                val cTop = srcPixels[yTop + x]
                val cBot = srcPixels[yBot + x]
                val cLeft = srcPixels[yMid + x - 1]
                val cRight = srcPixels[yMid + x + 1]

                val r = (5 * ((cMid shr 16) and 0xFF) -
                        ((cTop shr 16) and 0xFF) -
                        ((cBot shr 16) and 0xFF) -
                        ((cLeft shr 16) and 0xFF) -
                        ((cRight shr 16) and 0xFF)).coerceIn(0, 255)

                val g = (5 * ((cMid shr 8) and 0xFF) -
                        ((cTop shr 8) and 0xFF) -
                        ((cBot shr 8) and 0xFF) -
                        ((cLeft shr 8) and 0xFF) -
                        ((cRight shr 8) and 0xFF)).coerceIn(0, 255)

                val b = (5 * (cMid and 0xFF) -
                        (cTop and 0xFF) -
                        (cBot and 0xFF) -
                        (cLeft and 0xFF) -
                        (cRight and 0xFF)).coerceIn(0, 255)

                outPixels[yMid + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val output = getFilterBitmap(w, h)
        output.setPixels(outPixels, 0, w, 0, 0, w, h)
        return output
    }

    private fun applyBilinearUpscale(src: Bitmap): Bitmap {
        val outW = src.width * 2
        val outH = src.height * 2
        val output = getFilterBitmap(outW, outH)
        val canvas = Canvas(output)
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        val rectSrc = android.graphics.Rect(0, 0, src.width, src.height)
        val rectDst = android.graphics.Rect(0, 0, outW, outH)
        canvas.drawBitmap(src, rectSrc, rectDst, paint)
        return output
    }

    private fun getFilterBitmap(w: Int, h: Int): Bitmap {
        val cached = filterCachedBitmap
        return if (cached != null && cached.width == w && cached.height == h) {
            cached
        } else {
            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { filterCachedBitmap = it }
        }
    }
}
