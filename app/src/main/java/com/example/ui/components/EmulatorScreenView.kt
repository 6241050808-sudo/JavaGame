package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BezelFrameSkin
import com.example.data.model.EmulationConfig
import com.example.data.model.ScreenAspectRatio
import com.example.engine.ResolutionEnhancer

@Composable
fun EmulatorScreenView(
    bitmap: Bitmap,
    config: EmulationConfig,
    nativeFps: Float,
    displayFps: Float,
    onPointerDown: (Float, Float) -> Unit,
    onPointerUp: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Process image through resolution enhancer
    val enhancedBitmap = remember(bitmap, config.resolutionEnhancer) {
        ResolutionEnhancer.enhanceResolution(bitmap, config.resolutionEnhancer)
    }

    BoxWithConstraints(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(enhancedBitmap) {
                detectTapGestures(
                    onPress = { offset ->
                        // Convert screen touch coordinates back to game coordinates (240x320)
                        val gameX = (offset.x / size.width) * 240f
                        val gameY = (offset.y / size.height) * 320f
                        onPointerDown(gameX, gameY)
                        tryAwaitRelease()
                        onPointerUp(gameX, gameY)
                    }
                )
            }
            .pointerInput(enhancedBitmap) {
                detectDragGestures { change, _ ->
                    val gameX = (change.position.x / size.width) * 240f
                    val gameY = (change.position.y / size.height) * 320f
                    onPointerDown(gameX, gameY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val srcW = enhancedBitmap.width.toFloat()
            val srcH = enhancedBitmap.height.toFloat()

            // Calculate destination rect based on selected Aspect Ratio
            val (dstW, dstH) = when (config.aspectRatio) {
                ScreenAspectRatio.FULLSCREEN_STRETCH -> Pair(canvasW, canvasH)
                ScreenAspectRatio.RATIO_1_1 -> {
                    val s = minOf(canvasW, canvasH)
                    Pair(s, s)
                }
                ScreenAspectRatio.RATIO_4_3 -> {
                    val targetRatio = 4f / 3f
                    if (canvasW / canvasH > targetRatio) {
                        Pair(canvasH * targetRatio, canvasH)
                    } else {
                        Pair(canvasW, canvasW / targetRatio)
                    }
                }
                ScreenAspectRatio.FIT_RATIO -> {
                    val targetRatio = srcW / srcH
                    if (canvasW / canvasH > targetRatio) {
                        Pair(canvasH * targetRatio, canvasH)
                    } else {
                        Pair(canvasW, canvasW / targetRatio)
                    }
                }
            }

            val left = (canvasW - dstW) / 2f
            val top = (canvasH - dstH) / 2f

            drawIntoCanvas { nativeCanvas ->
                val srcRect = Rect(0, 0, enhancedBitmap.width, enhancedBitmap.height)
                val dstRect = Rect(left.toInt(), top.toInt(), (left + dstW).toInt(), (top + dstH).toInt())
                val paint = android.graphics.Paint().apply {
                    isFilterBitmap = (config.resolutionEnhancer == com.example.data.model.ResolutionEnhancer.BILINEAR_SMOOTH)
                }
                nativeCanvas.nativeCanvas.drawBitmap(enhancedBitmap, srcRect, dstRect, paint)
            }

            // Draw Bezel Frame overlay if active
            when (config.bezelSkin) {
                BezelFrameSkin.NOKIA_3310 -> {
                    drawRect(
                        color = Color(0xFF263238),
                        topLeft = Offset(0f, 0f),
                        size = Size(canvasW, top)
                    )
                    drawRect(
                        color = Color(0xFF263238),
                        topLeft = Offset(0f, top + dstH),
                        size = Size(canvasW, canvasH - (top + dstH))
                    )
                }
                BezelFrameSkin.NOKIA_N95 -> {
                    drawRect(
                        color = Color(0xFF1E2430),
                        topLeft = Offset(0f, 0f),
                        size = Size(left, canvasH)
                    )
                    drawRect(
                        color = Color(0xFF1E2430),
                        topLeft = Offset(left + dstW, 0f),
                        size = Size(canvasW - (left + dstW), canvasH)
                    )
                }
                BezelFrameSkin.SONY_ERICSSON -> {
                    drawRect(
                        color = Color(0xFF0F1115),
                        topLeft = Offset(0f, 0f),
                        size = Size(canvasW, top)
                    )
                }
                BezelFrameSkin.NONE -> {}
            }
        }

        // FPS and Enhancer Overlay Badge
        if (config.showFpsCounter) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xCC0A0D14))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "DISP: ${displayFps.toInt()} FPS | GAME: ${nativeFps.toInt()} FPS | ${config.resolutionEnhancer.label.take(15)}",
                    color = Color(0xFF00E5FF),
                    fontSize = 11.sp
                )
            }
        }
    }
}
