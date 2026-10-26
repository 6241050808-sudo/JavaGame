package com.example.engine.games

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.engine.AudioSynthesizer
import com.example.engine.J2MEKeyCodes
import kotlin.random.Random

class RetroRacerGame : IJ2MEGameEngine {
    override val title: String = "Retro Racer 2D"
    override var canvasWidth: Int = 240
    override var canvasHeight: Int = 320

    private var carX = 120f
    private val carY = 250f
    private val carWidth = 22f
    private val carHeight = 36f

    private var speed = 180f
    private var isTurbo = false
    private var score = 0
    private var distanceKm = 0f
    private var gameOver = false

    private var moveLeft = false
    private var moveRight = false

    private data class TrafficCar(var x: Float, var y: Float, val color: Int, val speedRatio: Float)
    private data class RoadItem(var x: Float, var y: Float, val isNitro: Boolean)

    private val traffic = mutableListOf<TrafficCar>()
    private val items = mutableListOf<RoadItem>()
    private var roadOffset = 0f
    private var spawnTimer = 0f

    private val carPaint = Paint().apply { color = Color.rgb(255, 60, 0) }
    private val roadPaint = Paint().apply { color = Color.rgb(40, 44, 52) }
    private val grassPaint = Paint().apply { color = Color.rgb(34, 139, 34) }
    private val stripePaint = Paint().apply { color = Color.WHITE }
    private val textPaint = Paint().apply {
        color = Color.rgb(255, 235, 59)
        textSize = 14f
        isFakeBoldText = true
    }

    override fun init(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        reset()
    }

    override fun reset() {
        carX = canvasWidth / 2f
        speed = 180f
        isTurbo = false
        score = 0
        distanceKm = 0f
        gameOver = false
        traffic.clear()
        items.clear()
    }

    override fun update(deltaMs: Long, audio: AudioSynthesizer) {
        if (gameOver) return
        val dt = deltaMs / 1000f

        val currentSpeed = if (isTurbo) speed * 1.6f else speed
        distanceKm += (currentSpeed * dt) / 500f
        score = (distanceKm * 100).toInt()

        // Steer player car
        if (moveLeft) carX -= 160f * dt
        if (moveRight) carX += 160f * dt

        // Road boundaries (road width from 40f to canvasWidth - 40f)
        carX = carX.coerceIn(52f, canvasWidth - 52f)

        // Road scrolling
        roadOffset = (roadOffset + currentSpeed * dt) % 40f

        // Spawn traffic
        spawnTimer += dt
        if (spawnTimer > (if (isTurbo) 0.6f else 1.0f)) {
            spawnTimer = 0f
            val lanes = listOf(65f, 120f, 175f)
            val laneX = lanes[Random.nextInt(lanes.size)]
            val color = when (Random.nextInt(3)) {
                0 -> Color.rgb(33, 150, 243) // Blue
                1 -> Color.rgb(255, 193, 7)  // Yellow cab
                else -> Color.rgb(76, 175, 80) // Green
            }
            traffic.add(TrafficCar(laneX, -40f, color, Random.nextFloat() * 0.4f + 0.5f))

            if (Random.nextFloat() < 0.4f) {
                val itemLane = lanes[Random.nextInt(lanes.size)]
                items.add(RoadItem(itemLane, -60f, isNitro = Random.nextBoolean()))
            }
        }

        // Update traffic
        val trafIter = traffic.iterator()
        while (trafIter.hasNext()) {
            val t = trafIter.next()
            val relativeSpeed = currentSpeed - (speed * t.speedRatio)
            t.y += relativeSpeed * dt

            // Check collision with player
            if (Math.abs(t.x - carX) < (carWidth - 4f) && Math.abs(t.y - carY) < (carHeight - 4f)) {
                audio.playExplosionSound()
                gameOver = true
                break
            }

            if (t.y > canvasHeight + 50 || t.y < -100) {
                trafIter.remove()
            }
        }

        // Update items
        val itemIter = items.iterator()
        while (itemIter.hasNext()) {
            val item = itemIter.next()
            item.y += currentSpeed * dt

            if (Math.abs(item.x - carX) < 22f && Math.abs(item.y - carY) < 22f) {
                audio.playCoinChime()
                if (item.isNitro) {
                    isTurbo = true
                    score += 200
                } else {
                    score += 150
                }
                itemIter.remove()
                continue
            }

            if (item.y > canvasHeight + 40) itemIter.remove()
        }
    }

    override fun render(canvas: Canvas, paint: Paint) {
        // Grass borders
        canvas.drawColor(Color.rgb(34, 139, 34))

        // Asphalt highway
        val roadLeft = 40f
        val roadRight = canvasWidth - 40f
        canvas.drawRect(roadLeft, 0f, roadRight, canvasHeight.toFloat(), roadPaint)

        // Road lane dashed dividers
        stripePaint.color = Color.WHITE
        stripePaint.strokeWidth = 3f
        val lane1X = roadLeft + (roadRight - roadLeft) / 3f
        val lane2X = roadLeft + ((roadRight - roadLeft) / 3f) * 2f

        var y = -40f + roadOffset
        while (y < canvasHeight + 40f) {
            canvas.drawLine(lane1X, y, lane1X, y + 20f, stripePaint)
            canvas.drawLine(lane2X, y, lane2X, y + 20f, stripePaint)
            y += 40f
        }

        // Draw items (Coins & Nitro canisters)
        for (item in items) {
            if (item.isNitro) {
                paint.color = Color.rgb(0, 229, 255)
                canvas.drawRoundRect(RectF(item.x - 7f, item.y - 10f, item.x + 7f, item.y + 10f), 3f, 3f, paint)
                paint.color = Color.WHITE
                canvas.drawText("N", item.x - 4f, item.y + 4f, paint)
            } else {
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(item.x, item.y, 7f, paint)
            }
        }

        // Draw traffic cars
        for (t in traffic) {
            paint.color = t.color
            canvas.drawRoundRect(RectF(t.x - carWidth / 2f, t.y - carHeight / 2f, t.x + carWidth / 2f, t.y + carHeight / 2f), 4f, 4f, paint)
            // Windshield
            paint.color = Color.rgb(30, 30, 40)
            canvas.drawRect(t.x - 7f, t.y - 8f, t.x + 7f, t.y + 2f, paint)
        }

        // Draw player sports car
        carPaint.color = Color.rgb(244, 67, 54)
        canvas.drawRoundRect(RectF(carX - carWidth / 2f, carY - carHeight / 2f, carX + carWidth / 2f, carY + carHeight / 2f), 5f, 5f, carPaint)
        // Racing stripes
        paint.color = Color.WHITE
        canvas.drawRect(carX - 2f, carY - carHeight / 2f, carX + 2f, carY + carHeight / 2f, paint)
        // Windshield
        paint.color = Color.rgb(20, 30, 45)
        canvas.drawRect(carX - 8f, carY - 6f, carX + 8f, carY + 4f, paint)
        // Headlights
        paint.color = Color.rgb(255, 255, 100)
        canvas.drawCircle(carX - 7f, carY - carHeight / 2f + 2f, 2.5f, paint)
        canvas.drawCircle(carX + 7f, carY - carHeight / 2f + 2f, 2.5f, paint)

        // Nitro exhaust flames if turbo
        if (isTurbo) {
            paint.color = Color.rgb(0, 229, 255)
            val flicker = Random.nextFloat() * 10f
            canvas.drawRect(carX - 6f, carY + carHeight / 2f, carX - 2f, carY + carHeight / 2f + 8f + flicker, paint)
            canvas.drawRect(carX + 2f, carY + carHeight / 2f, carX + 6f, carY + carHeight / 2f + 8f + flicker, paint)
        }

        // HUD Overlay
        textPaint.color = Color.rgb(255, 235, 59)
        canvas.drawText("SCORE: $score", 10f, 18f, textPaint)
        val kmStr = String.format("%.1f KM", distanceKm)
        canvas.drawText(kmStr, canvasWidth - 65f, 18f, textPaint)

        if (isTurbo) {
            textPaint.color = Color.rgb(0, 229, 255)
            canvas.drawText("TURBO BOOST!", canvasWidth / 2f - 40f, 34f, textPaint)
        }

        if (gameOver) {
            paint.color = Color.argb(190, 0, 0, 0)
            canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)
            textPaint.color = Color.RED
            textPaint.textSize = 20f
            canvas.drawText("CRASHED!", canvasWidth / 2f - 45f, canvasHeight / 2f - 10f, textPaint)
            textPaint.color = Color.WHITE
            textPaint.textSize = 12f
            canvas.drawText("กด 5 หรือ รีเซ็ต เพื่อเริ่มใหม่", canvasWidth / 2f - 65f, canvasHeight / 2f + 20f, textPaint)
            textPaint.textSize = 14f
        }
    }

    override fun keyPressed(keyCode: Int, audio: AudioSynthesizer) {
        if (gameOver) {
            if (keyCode == J2MEKeyCodes.KEY_FIRE || keyCode == J2MEKeyCodes.KEY_NUM5) reset()
            return
        }
        when (keyCode) {
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = true
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = true
            J2MEKeyCodes.KEY_UP, J2MEKeyCodes.KEY_FIRE, J2MEKeyCodes.KEY_NUM2, J2MEKeyCodes.KEY_NUM5 -> {
                isTurbo = !isTurbo
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
        if (gameOver) {
            reset()
            return
        }
        if (x < carX) moveLeft = true
        else moveRight = true
    }

    override fun pointerReleased(x: Float, y: Float) {
        moveLeft = false
        moveRight = false
    }

    override fun getScore(): Int = score
    override fun isGameOver(): Boolean = gameOver

    override fun getSaveState(): String = "$score,$distanceKm,$carX"
    override fun restoreSaveState(stateData: String) {
        try {
            val parts = stateData.split(",")
            if (parts.size >= 3) {
                score = parts[0].toInt()
                distanceKm = parts[1].toFloat()
                carX = parts[2].toFloat()
                gameOver = false
            }
        } catch (e: Exception) {
        }
    }
}
