package com.example.engine.games

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.engine.AudioSynthesizer
import com.example.engine.J2MEKeyCodes

class BounceGame : IJ2MEGameEngine {
    override val title: String = "Bounce Ball Classic"
    override var canvasWidth: Int = 240
    override var canvasHeight: Int = 320

    // Ball physics
    private var ballX = 40f
    private var ballY = 200f
    private var ballVx = 0f
    private var ballVy = 0f
    private val ballRadius = 12f
    private val gravity = 550f
    private val bounceStrength = -260f
    private val superBounce = -420f
    private var isOnGround = false

    private var lives = 3
    private var score = 0
    private var gameOver = false
    private var levelWon = false
    private var currentLevel = 1

    // Controls
    private var moveLeft = false
    private var moveRight = false

    // World Elements
    private data class Ring(val x: Float, val y: Float, var collected: Boolean = false)
    private data class Spike(val x: Float, val y: Float)
    private data class Platform(val x: Float, val y: Float, val w: Float, val h: Float, val isSpring: Boolean = false)

    private val rings = mutableListOf<Ring>()
    private val spikes = mutableListOf<Spike>()
    private val platforms = mutableListOf<Platform>()
    private var exitDoorX = 200f
    private var exitDoorY = 80f

    private val ballPaint = Paint().apply { color = Color.rgb(255, 40, 40) }
    private val ringPaint = Paint().apply {
        color = Color.rgb(255, 215, 0)
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
    }
    private val platformPaint = Paint().apply { color = Color.rgb(46, 125, 50) }
    private val spikePaint = Paint().apply { color = Color.rgb(60, 60, 60) }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 14f
        isFakeBoldText = true
    }

    override fun init(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        setupLevel(1)
    }

    private fun setupLevel(lvl: Int) {
        currentLevel = lvl
        ballX = 40f
        ballY = 240f
        ballVx = 0f
        ballVy = 0f
        gameOver = false
        levelWon = false

        rings.clear()
        spikes.clear()
        platforms.clear()

        // Base floor
        platforms.add(Platform(0f, canvasHeight - 20f, canvasWidth.toFloat(), 20f))

        if (lvl == 1) {
            // Level 1 layout
            platforms.add(Platform(20f, 220f, 80f, 12f))
            platforms.add(Platform(120f, 180f, 90f, 12f))
            platforms.add(Platform(40f, 130f, 80f, 12f))
            platforms.add(Platform(150f, 100f, 80f, 12f, isSpring = true)) // Trampoline spring!

            rings.add(Ring(60f, 195f))
            rings.add(Ring(160f, 155f))
            rings.add(Ring(70f, 105f))
            rings.add(Ring(180f, 60f))

            spikes.add(Spike(110f, canvasHeight - 20f))
            spikes.add(Spike(130f, canvasHeight - 20f))

            exitDoorX = 200f
            exitDoorY = 70f
        } else {
            // Level 2 layout
            platforms.add(Platform(40f, 230f, 60f, 10f))
            platforms.add(Platform(130f, 210f, 70f, 10f))
            platforms.add(Platform(30f, 150f, 70f, 10f, isSpring = true))
            platforms.add(Platform(120f, 110f, 100f, 10f))

            rings.add(Ring(70f, 205f))
            rings.add(Ring(160f, 185f))
            rings.add(Ring(60f, 90f))
            rings.add(Ring(170f, 85f))

            spikes.add(Spike(80f, canvasHeight - 20f))
            spikes.add(Spike(100f, canvasHeight - 20f))
            spikes.add(Spike(150f, 110f))

            exitDoorX = 190f
            exitDoorY = 80f
        }
    }

    override fun reset() {
        lives = 3
        score = 0
        setupLevel(1)
    }

    override fun update(deltaMs: Long, audio: AudioSynthesizer) {
        if (gameOver || levelWon) return
        val dt = deltaMs / 1000f

        // Horizontal movement
        if (moveLeft) ballVx = -120f
        else if (moveRight) ballVx = 120f
        else ballVx *= 0.85f

        ballVy += gravity * dt
        ballX += ballVx * dt
        ballY += ballVy * dt

        // Boundaries
        if (ballX < ballRadius) {
            ballX = ballRadius
            ballVx = -ballVx * 0.5f
        } else if (ballX > canvasWidth - ballRadius) {
            ballX = canvasWidth - ballRadius
            ballVx = -ballVx * 0.5f
        }

        isOnGround = false

        // Check platforms collision
        for (p in platforms) {
            if (ballX + ballRadius > p.x && ballX - ballRadius < p.x + p.w) {
                // Landing on platform from above
                if (ballY + ballRadius >= p.y && ballY - ballRadius < p.y + p.h && ballVy > 0) {
                    ballY = p.y - ballRadius
                    if (p.isSpring) {
                        ballVy = superBounce
                        audio.playJumpSound()
                    } else {
                        ballVy = bounceStrength
                        audio.playJumpSound()
                    }
                    isOnGround = true
                }
            }
        }

        // Check ring collection
        for (r in rings) {
            if (!r.collected && Math.hypot((ballX - r.x).toDouble(), (ballY - r.y).toDouble()) < ballRadius + 10) {
                r.collected = true
                score += 100
                audio.playCoinChime()
            }
        }

        // Check spikes
        for (s in spikes) {
            if (Math.hypot((ballX - s.x).toDouble(), (ballY - s.y).toDouble()) < ballRadius + 6) {
                audio.playExplosionSound()
                lives--
                if (lives <= 0) {
                    gameOver = true
                } else {
                    ballX = 40f
                    ballY = 220f
                    ballVy = 0f
                }
                break
            }
        }

        // Check Exit Door
        val allRingsCollected = rings.all { it.collected }
        if (allRingsCollected && Math.hypot((ballX - exitDoorX).toDouble(), (ballY - exitDoorY).toDouble()) < 22) {
            audio.playCoinChime()
            score += 500
            if (currentLevel < 2) {
                setupLevel(2)
            } else {
                levelWon = true
            }
        }
    }

    override fun render(canvas: Canvas, paint: Paint) {
        // Sky gradient background
        canvas.drawColor(Color.rgb(135, 206, 235))

        // Draw sun
        paint.color = Color.rgb(255, 220, 60)
        canvas.drawCircle(canvasWidth - 30f, 35f, 18f, paint)

        // Draw platforms
        for (p in platforms) {
            if (p.isSpring) {
                paint.color = Color.rgb(255, 105, 180) // Pink trampoline
                canvas.drawRoundRect(RectF(p.x, p.y, p.x + p.w, p.y + p.h), 4f, 4f, paint)
                paint.color = Color.WHITE
                canvas.drawText("▲▲", p.x + p.w / 2f - 12f, p.y + p.h - 2f, paint)
            } else {
                platformPaint.color = Color.rgb(76, 175, 80)
                canvas.drawRect(p.x, p.y, p.x + p.w, p.y + p.h, platformPaint)
                paint.color = Color.rgb(56, 142, 60)
                canvas.drawRect(p.x, p.y + p.h - 3f, p.x + p.w, p.y + p.h, paint)
            }
        }

        // Draw spikes
        spikePaint.color = Color.rgb(50, 50, 50)
        for (s in spikes) {
            val path = android.graphics.Path().apply {
                moveTo(s.x - 8f, s.y)
                lineTo(s.x, s.y - 14f)
                lineTo(s.x + 8f, s.y)
                close()
            }
            canvas.drawPath(path, spikePaint)
        }

        // Draw golden rings
        for (r in rings) {
            if (!r.collected) {
                ringPaint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(r.x, r.y, 8f, ringPaint)
            }
        }

        // Draw exit door / hoop
        val allRingsCollected = rings.all { it.collected }
        paint.color = if (allRingsCollected) Color.rgb(0, 230, 118) else Color.GRAY
        canvas.drawRoundRect(RectF(exitDoorX - 12f, exitDoorY - 20f, exitDoorX + 12f, exitDoorY), 4f, 4f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(exitDoorX, exitDoorY - 10f, 6f, paint)

        // Draw Red Bounce Ball with 3D shine
        ballPaint.color = Color.rgb(230, 30, 30)
        canvas.drawCircle(ballX, ballY, ballRadius, ballPaint)
        paint.color = Color.argb(180, 255, 255, 255)
        canvas.drawCircle(ballX - 3.5f, ballY - 3.5f, 3.5f, paint)

        // Top HUD
        textPaint.color = Color.rgb(20, 20, 20)
        canvas.drawText("LVL: $currentLevel", 10f, 18f, textPaint)
        canvas.drawText("SCORE: $score", 70f, 18f, textPaint)
        textPaint.color = Color.rgb(200, 30, 30)
        val hearts = "♥ ".repeat(lives.coerceAtLeast(0))
        canvas.drawText(hearts, canvasWidth - 55f, 18f, textPaint)

        val ringsLeft = rings.count { !it.collected }
        textPaint.color = Color.rgb(180, 120, 0)
        canvas.drawText("RINGS: $ringsLeft", 10f, 34f, textPaint)

        if (gameOver) {
            paint.color = Color.argb(190, 0, 0, 0)
            canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)
            textPaint.color = Color.RED
            textPaint.textSize = 20f
            canvas.drawText("GAME OVER", canvasWidth / 2f - 55f, canvasHeight / 2f - 10f, textPaint)
            textPaint.color = Color.WHITE
            textPaint.textSize = 12f
            canvas.drawText("กด 5 เพื่อเล่นใหม่", canvasWidth / 2f - 40f, canvasHeight / 2f + 20f, textPaint)
            textPaint.textSize = 14f
        } else if (levelWon) {
            paint.color = Color.argb(190, 0, 0, 0)
            canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)
            textPaint.color = Color.rgb(57, 255, 20)
            textPaint.textSize = 20f
            canvas.drawText("YOU WIN!", canvasWidth / 2f - 45f, canvasHeight / 2f - 10f, textPaint)
            textPaint.color = Color.WHITE
            textPaint.textSize = 12f
            canvas.drawText("กด 5 เพื่อเล่นรอบใหม่", canvasWidth / 2f - 55f, canvasHeight / 2f + 20f, textPaint)
            textPaint.textSize = 14f
        }
    }

    override fun keyPressed(keyCode: Int, audio: AudioSynthesizer) {
        if (gameOver || levelWon) {
            if (keyCode == J2MEKeyCodes.KEY_FIRE || keyCode == J2MEKeyCodes.KEY_NUM5) {
                reset()
            }
            return
        }
        when (keyCode) {
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = true
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = true
            J2MEKeyCodes.KEY_UP, J2MEKeyCodes.KEY_FIRE, J2MEKeyCodes.KEY_NUM2, J2MEKeyCodes.KEY_NUM5 -> {
                if (isOnGround) {
                    ballVy = bounceStrength
                    audio.playJumpSound()
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
        if (gameOver || levelWon) {
            reset()
            return
        }
        if (x < ballX) moveLeft = true
        else moveRight = true
        if (isOnGround) {
            ballVy = bounceStrength
            audio.playJumpSound()
        }
    }

    override fun pointerReleased(x: Float, y: Float) {
        moveLeft = false
        moveRight = false
    }

    override fun getScore(): Int = score
    override fun isGameOver(): Boolean = gameOver

    override fun getSaveState(): String = "$score,$lives,$currentLevel,$ballX,$ballY"
    override fun restoreSaveState(stateData: String) {
        try {
            val parts = stateData.split(",")
            if (parts.size >= 5) {
                score = parts[0].toInt()
                lives = parts[1].toInt()
                currentLevel = parts[2].toInt()
                ballX = parts[3].toFloat()
                ballY = parts[4].toFloat()
                gameOver = false
                levelWon = false
            }
        } catch (e: Exception) {
        }
    }
}
