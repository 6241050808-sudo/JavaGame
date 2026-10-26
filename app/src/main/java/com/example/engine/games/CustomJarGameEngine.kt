package com.example.engine.games

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.data.model.JarManifestInfo
import com.example.engine.AudioSynthesizer
import com.example.engine.J2MEKeyCodes
import kotlin.random.Random

class CustomJarGameEngine(
    private val manifestInfo: JarManifestInfo
) : IJ2MEGameEngine {
    override val title: String = manifestInfo.midletName
    override var canvasWidth: Int = 240
    override var canvasHeight: Int = 320

    private var score = 0
    private var gameOver = false
    private var isRunning = true
    private var currentMenuIndex = 0
    private var isPlayingSubgame = false

    // Simulation elements
    private var paddleX = 120f
    private var ballX = 120f
    private var ballY = 160f
    private var ballVx = 100f
    private var ballVy = 120f
    private var moveLeft = false
    private var moveRight = false
    private var lastKeyName = "None"
    private var uptimeSeconds = 0f

    private val titlePaint = Paint().apply {
        color = Color.rgb(0, 229, 255)
        textSize = 15f
        isFakeBoldText = true
    }
    private val bodyPaint = Paint().apply {
        color = Color.WHITE
        textSize = 12f
    }
    private val subPaint = Paint().apply {
        color = Color.rgb(200, 200, 200)
        textSize = 10f
    }
    private val accentPaint = Paint().apply {
        color = Color.rgb(255, 145, 0)
    }

    override fun init(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        reset()
    }

    override fun reset() {
        score = 0
        gameOver = false
        isPlayingSubgame = false
        currentMenuIndex = 0
        paddleX = canvasWidth / 2f
        ballX = canvasWidth / 2f
        ballY = 140f
        ballVx = 110f
        ballVy = 130f
        uptimeSeconds = 0f
    }

    override fun update(deltaMs: Long, audio: AudioSynthesizer) {
        val dt = deltaMs / 1000f
        uptimeSeconds += dt

        if (isPlayingSubgame && !gameOver) {
            // Paddle breakout simulation
            if (moveLeft) paddleX -= 180f * dt
            if (moveRight) paddleX += 180f * dt
            paddleX = paddleX.coerceIn(25f, canvasWidth - 25f)

            ballX += ballVx * dt
            ballY += ballVy * dt

            if (ballX < 6f || ballX > canvasWidth - 6f) {
                ballVx = -ballVx
                audio.playJumpSound()
            }
            if (ballY < 24f) {
                ballVy = -ballVy
                audio.playJumpSound()
            }

            // Paddle bounce
            if (ballY >= canvasHeight - 34f && ballY <= canvasHeight - 24f &&
                ballX >= paddleX - 25f && ballX <= paddleX + 25f && ballVy > 0) {
                ballVy = -Math.abs(ballVy) * 1.05f
                score += 25
                audio.playCoinChime()
            }

            if (ballY > canvasHeight) {
                audio.playExplosionSound()
                gameOver = true
            }
        }
    }

    override fun render(canvas: Canvas, paint: Paint) {
        // Authentic MIDP 2.0 Nokia dark slate theme
        canvas.drawColor(Color.rgb(18, 24, 38))

        // Top Status Bar (Java ME standard)
        paint.color = Color.rgb(12, 16, 26)
        canvas.drawRect(0f, 0f, canvasWidth.toFloat(), 22f, paint)
        bodyPaint.color = Color.rgb(0, 229, 255)
        canvas.drawText("Java ME: ${manifestInfo.midletName.take(16)}", 8f, 16f, bodyPaint)

        // Draw Jar Icon if available
        val icon = manifestInfo.iconBitmap
        if (icon != null) {
            val destRect = RectF(12f, 30f, 52f, 70f)
            canvas.drawBitmap(icon, null, destRect, paint)
        } else {
            // Default Java Cup icon placeholder
            accentPaint.color = Color.rgb(255, 145, 0)
            canvas.drawRoundRect(RectF(12f, 30f, 52f, 70f), 6f, 6f, accentPaint)
            paint.color = Color.WHITE
            canvas.drawText("☕", 22f, 56f, bodyPaint)
        }

        // Title & metadata info
        titlePaint.color = Color.rgb(255, 235, 59)
        canvas.drawText(manifestInfo.midletName.take(18), 60f, 46f, titlePaint)
        subPaint.color = Color.rgb(170, 190, 210)
        canvas.drawText("บ.ผู้พัฒนา: ${manifestInfo.midletVendor.take(18)}", 60f, 60f, subPaint)
        canvas.drawText("v${manifestInfo.midletVersion} • ${manifestInfo.totalClasses} classes", 60f, 72f, subPaint)

        if (!isPlayingSubgame) {
            // MIDlet Main Menu / Dashboard
            paint.color = Color.rgb(28, 36, 56)
            canvas.drawRoundRect(RectF(10f, 85f, canvasWidth - 10f, canvasHeight - 32f), 8f, 8f, paint)

            val menuItems = listOf(
                "1. เล่นเกมจำลอง (Run Game Canvas)",
                "2. ตรวจสอบ Classes (${manifestInfo.totalClasses})",
                "3. รายละเอียด MIDlet Profile",
                "4. รีเซ็ตสภาพแวดล้อม (Reset VM)"
            )

            for (i in menuItems.indices) {
                val isSelected = (i == currentMenuIndex)
                val itemY = 115f + (i * 32f)

                if (isSelected) {
                    paint.color = Color.rgb(0, 229, 255)
                    canvas.drawRoundRect(RectF(16f, itemY - 18f, canvasWidth - 16f, itemY + 8f), 6f, 6f, paint)
                    bodyPaint.color = Color.BLACK
                } else {
                    bodyPaint.color = Color.WHITE
                }
                canvas.drawText(menuItems[i], 22f, itemY, bodyPaint)
            }

            // Key press telemetry
            subPaint.color = Color.rgb(100, 220, 100)
            canvas.drawText("ปุ่มล่าสุดที่กด: $lastKeyName", 18f, canvasHeight - 40f, subPaint)

        } else {
            // Interactive Breakout Canvas for this JAR
            paint.color = Color.rgb(10, 15, 25)
            canvas.drawRect(8f, 85f, canvasWidth - 8f, canvasHeight - 32f, paint)

            // Draw paddle
            accentPaint.color = Color.rgb(0, 229, 255)
            canvas.drawRoundRect(RectF(paddleX - 25f, canvasHeight - 35f, paddleX + 25f, canvasHeight - 27f), 4f, 4f, accentPaint)

            // Draw ball
            paint.color = Color.rgb(255, 60, 60)
            canvas.drawCircle(ballX, ballY, 6f, paint)

            // HUD
            bodyPaint.color = Color.YELLOW
            canvas.drawText("SCORE: $score", 16f, 104f, bodyPaint)
            subPaint.color = Color.WHITE
            canvas.drawText("กด ซ้าย/ขวา เพื่อบังคับ", 16f, 118f, subPaint)

            if (gameOver) {
                paint.color = Color.argb(200, 0, 0, 0)
                canvas.drawRect(8f, 85f, canvasWidth - 8f, canvasHeight - 32f, paint)
                titlePaint.color = Color.RED
                canvas.drawText("GAME OVER", canvasWidth / 2f - 40f, 180f, titlePaint)
                bodyPaint.color = Color.WHITE
                canvas.drawText("กด 5 เพื่อเล่นใหม่", canvasWidth / 2f - 35f, 205f, bodyPaint)
            }
        }

        // Bottom Softkeys Bar (Classic J2ME UI)
        paint.color = Color.rgb(12, 16, 26)
        canvas.drawRect(0f, canvasHeight - 24f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)

        bodyPaint.color = Color.rgb(220, 220, 220)
        canvas.drawText(if (isPlayingSubgame) "ย้อนกลับ" else "เลือก (OK)", 10f, canvasHeight - 8f, bodyPaint)
        canvas.drawText(if (isPlayingSubgame) "รีสตาร์ต" else "ออก", canvasWidth - 45f, canvasHeight - 8f, bodyPaint)
    }

    override fun keyPressed(keyCode: Int, audio: AudioSynthesizer) {
        lastKeyName = J2MEKeyCodes.getKeyName(keyCode)
        when (keyCode) {
            J2MEKeyCodes.KEY_UP, J2MEKeyCodes.KEY_NUM2 -> {
                audio.playJumpSound()
                if (!isPlayingSubgame) {
                    currentMenuIndex = (currentMenuIndex - 1 + 4) % 4
                }
            }
            J2MEKeyCodes.KEY_DOWN, J2MEKeyCodes.KEY_NUM8 -> {
                audio.playJumpSound()
                if (!isPlayingSubgame) {
                    currentMenuIndex = (currentMenuIndex + 1) % 4
                }
            }
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = true
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = true
            J2MEKeyCodes.KEY_FIRE, J2MEKeyCodes.KEY_NUM5, J2MEKeyCodes.KEY_SOFT_LEFT -> {
                audio.playCoinChime()
                if (gameOver) {
                    reset()
                    isPlayingSubgame = true
                } else if (!isPlayingSubgame) {
                    if (currentMenuIndex == 0) {
                        isPlayingSubgame = true
                        score = 0
                    } else if (currentMenuIndex == 3) {
                        reset()
                    }
                }
            }
            J2MEKeyCodes.KEY_SOFT_RIGHT -> {
                audio.playTone(400f, 80)
                if (isPlayingSubgame) {
                    isPlayingSubgame = false
                } else {
                    reset()
                }
            }
        }
    }

    override fun keyReleased(keyCode: Int) {
        when (keyCode) {
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = false
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = false
        }
    }

    override fun pointerPressed(x: Float, y: Float, audio: AudioSynthesizer) {
        if (!isPlayingSubgame) {
            if (y > 90f && y < canvasHeight - 40f) {
                val clickedIndex = ((y - 95f) / 32f).toInt().coerceIn(0, 3)
                currentMenuIndex = clickedIndex
                if (clickedIndex == 0) {
                    isPlayingSubgame = true
                    audio.playCoinChime()
                }
            }
        } else {
            paddleX = x.coerceIn(25f, canvasWidth - 25f)
            if (gameOver) reset()
        }
    }

    override fun pointerReleased(x: Float, y: Float) {
        moveLeft = false
        moveRight = false
    }

    override fun getScore(): Int = score
    override fun isGameOver(): Boolean = gameOver

    override fun getSaveState(): String = "$score,$currentMenuIndex,$isPlayingSubgame,$paddleX"
    override fun restoreSaveState(stateData: String) {
        try {
            val parts = stateData.split(",")
            if (parts.size >= 4) {
                score = parts[0].toInt()
                currentMenuIndex = parts[1].toInt()
                isPlayingSubgame = parts[2].toBoolean()
                paddleX = parts[3].toFloat()
                gameOver = false
            }
        } catch (e: Exception) {
        }
    }
}
