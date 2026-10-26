package com.example.engine.games

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.engine.AudioSynthesizer
import com.example.engine.J2MEKeyCodes
import kotlin.random.Random

class SpaceImpactGame : IJ2MEGameEngine {
    override val title: String = "Space Impact Revival"
    override var canvasWidth: Int = 240
    override var canvasHeight: Int = 320

    private var playerX = 30f
    private var playerY = 160f
    private val playerSpeed = 160f
    private var lives = 3
    private var score = 0
    private var gameOver = false
    private var isBossBattle = false
    private var bossHp = 20

    // Controls
    private var moveUp = false
    private var moveDown = false
    private var moveLeft = false
    private var moveRight = false
    private var isFiring = false
    private var lastShotTime = 0L

    // Entities
    private data class Laser(var x: Float, var y: Float, val isEnemy: Boolean = false)
    private data class Enemy(var x: Float, var y: Float, val type: Int, var hp: Int, var vy: Float = 0f)
    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Int)
    private data class Star(var x: Float, var y: Float, val speed: Float, val size: Float)

    private val lasers = mutableListOf<Laser>()
    private val enemies = mutableListOf<Enemy>()
    private val particles = mutableListOf<Particle>()
    private val stars = mutableListOf<Star>()

    private var spawnTimer = 0f
    private val starPaint = Paint().apply { color = Color.WHITE }
    private val playerPaint = Paint().apply { color = Color.rgb(0, 229, 255) } // Cyan
    private val enemyPaint = Paint().apply { color = Color.rgb(255, 60, 60) }
    private val textPaint = Paint().apply {
        color = Color.rgb(57, 255, 20)
        textSize = 14f
        isFakeBoldText = true
    }

    override fun init(width: Int, height: Int) {
        canvasWidth = width
        canvasHeight = height
        playerX = 25f
        playerY = height / 2f
        stars.clear()
        for (i in 0 until 40) {
            stars.add(Star(Random.nextFloat() * width, Random.nextFloat() * height, Random.nextFloat() * 80f + 20f, Random.nextFloat() * 2f + 1f))
        }
        reset()
    }

    override fun reset() {
        lives = 3
        score = 0
        gameOver = false
        isBossBattle = false
        bossHp = 25
        lasers.clear()
        enemies.clear()
        particles.clear()
        playerX = 30f
        playerY = canvasHeight / 2f
    }

    override fun update(deltaMs: Long, audio: AudioSynthesizer) {
        if (gameOver) return
        val dt = deltaMs / 1000f

        // Move stars
        for (star in stars) {
            star.x -= star.speed * dt
            if (star.x < 0) {
                star.x = canvasWidth.toFloat()
                star.y = Random.nextFloat() * canvasHeight
            }
        }

        // Move player
        if (moveUp) playerY -= playerSpeed * dt
        if (moveDown) playerY += playerSpeed * dt
        if (moveLeft) playerX -= playerSpeed * dt
        if (moveRight) playerX += playerSpeed * dt

        playerX = playerX.coerceIn(15f, canvasWidth - 30f)
        playerY = playerY.coerceIn(25f, canvasHeight - 25f)

        // Player shooting
        val now = System.currentTimeMillis()
        if (isFiring && now - lastShotTime > 180) {
            lastShotTime = now
            lasers.add(Laser(playerX + 16f, playerY))
            audio.playLaserSound()
        }

        // Update lasers
        val laserIter = lasers.iterator()
        while (laserIter.hasNext()) {
            val l = laserIter.next()
            if (l.isEnemy) {
                l.x -= 220f * dt
                // Check player collision
                if (Math.abs(l.x - playerX) < 14f && Math.abs(l.y - playerY) < 12f) {
                    laserIter.remove()
                    playerHit(audio)
                    continue
                }
            } else {
                l.x += 350f * dt
                // Check enemy collision
                var hit = false
                for (e in enemies) {
                    if (Math.abs(l.x - e.x) < 16f && Math.abs(l.y - e.y) < 14f) {
                        e.hp--
                        hit = true
                        createExplosion(e.x, e.y, Color.YELLOW, 6)
                        if (e.hp <= 0) {
                            score += if (e.type == 99) 500 else 50
                            audio.playExplosionSound()
                            createExplosion(e.x, e.y, Color.rgb(255, 100, 0), 16)
                        }
                        break
                    }
                }
                if (hit) {
                    laserIter.remove()
                    continue
                }
            }
            if (l.x > canvasWidth + 20 || l.x < -20) {
                laserIter.remove()
            }
        }

        // Spawn enemies
        spawnTimer += dt
        if (spawnTimer > 1.2f && !isBossBattle) {
            spawnTimer = 0f
            val type = if (score > 300 && Random.nextFloat() < 0.35f) 2 else 1
            enemies.add(Enemy(canvasWidth + 20f, Random.nextFloat() * (canvasHeight - 60f) + 30f, type, hp = type, vy = if (type == 2) 50f else 0f))
        }

        // Boss trigger
        if (score >= 600 && !isBossBattle && enemies.isEmpty()) {
            isBossBattle = true
            enemies.add(Enemy(canvasWidth - 40f, canvasHeight / 2f, 99, hp = bossHp, vy = 80f))
        }

        // Update enemies
        val enemyIter = enemies.iterator()
        while (enemyIter.hasNext()) {
            val e = enemyIter.next()
            if (e.type == 99) {
                // Boss movement
                e.y += e.vy * dt
                if (e.y < 50 || e.y > canvasHeight - 50) e.vy = -e.vy
                if (Random.nextFloat() < 0.04f) {
                    lasers.add(Laser(e.x - 20f, e.y, isEnemy = true))
                }
            } else {
                e.x -= 100f * dt
                e.y += e.vy * dt
                if (e.y < 30 || e.y > canvasHeight - 30) e.vy = -e.vy
                if (e.type == 2 && Random.nextFloat() < 0.015f) {
                    lasers.add(Laser(e.x - 12f, e.y, isEnemy = true))
                }
            }

            // Check collision with player
            if (Math.abs(e.x - playerX) < 18f && Math.abs(e.y - playerY) < 16f) {
                e.hp = 0
                playerHit(audio)
            }

            if (e.hp <= 0) {
                enemyIter.remove()
                if (e.type == 99) {
                    isBossBattle = false
                    score += 1000
                }
            } else if (e.x < -30) {
                enemyIter.remove()
            }
        }

        // Update particles
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt * 2f
            if (p.life <= 0) pIter.remove()
        }
    }

    private fun playerHit(audio: AudioSynthesizer) {
        lives--
        audio.playExplosionSound()
        createExplosion(playerX, playerY, Color.RED, 20)
        if (lives <= 0) {
            gameOver = true
        }
    }

    private fun createExplosion(x: Float, y: Float, color: Int, count: Int) {
        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = Random.nextFloat() * 120f + 30f
            particles.add(Particle(x, y, (Math.cos(angle) * speed).toFloat(), (Math.sin(angle) * speed).toFloat(), 1f, color))
        }
    }

    override fun render(canvas: Canvas, paint: Paint) {
        // Space background
        canvas.drawColor(Color.rgb(10, 14, 26))

        // Draw stars
        starPaint.color = Color.rgb(180, 200, 255)
        for (s in stars) {
            canvas.drawCircle(s.x, s.y, s.size, starPaint)
        }

        // Draw player ship
        if (!gameOver) {
            playerPaint.color = Color.rgb(0, 229, 255)
            // Futuristic fighter craft
            canvas.drawRect(playerX - 10f, playerY - 4f, playerX + 12f, playerY + 4f, playerPaint)
            canvas.drawRect(playerX - 6f, playerY - 12f, playerX + 2f, playerY - 4f, playerPaint)
            canvas.drawRect(playerX - 6f, playerY + 4f, playerX + 2f, playerY + 12f, playerPaint)
            // Cockpit
            paint.color = Color.YELLOW
            canvas.drawRect(playerX + 2f, playerY - 2f, playerX + 8f, playerY + 2f, paint)
            // Thruster flame
            paint.color = Color.rgb(255, 140, 0)
            val flicker = Random.nextFloat() * 6f
            canvas.drawRect(playerX - 16f - flicker, playerY - 2f, playerX - 10f, playerY + 2f, paint)
        }

        // Draw lasers
        paint.color = Color.rgb(57, 255, 20)
        for (l in lasers) {
            if (l.isEnemy) {
                paint.color = Color.rgb(255, 70, 70)
                canvas.drawCircle(l.x, l.y, 4f, paint)
            } else {
                paint.color = Color.rgb(0, 255, 230)
                canvas.drawRect(l.x - 6f, l.y - 2f, l.x + 6f, l.y + 2f, paint)
            }
        }

        // Draw enemies
        for (e in enemies) {
            if (e.type == 99) {
                // Boss Mothership
                enemyPaint.color = Color.rgb(220, 30, 80)
                canvas.drawRoundRect(RectF(e.x - 22f, e.y - 26f, e.x + 22f, e.y + 26f), 8f, 8f, enemyPaint)
                paint.color = Color.YELLOW
                canvas.drawCircle(e.x - 8f, e.y, 7f, paint)
            } else if (e.type == 2) {
                enemyPaint.color = Color.rgb(255, 150, 0)
                canvas.drawRect(e.x - 8f, e.y - 8f, e.x + 8f, e.y + 8f, enemyPaint)
            } else {
                enemyPaint.color = Color.rgb(240, 50, 50)
                canvas.drawCircle(e.x, e.y, 7f, enemyPaint)
            }
        }

        // Draw explosion particles
        for (p in particles) {
            paint.color = p.color
            paint.alpha = (p.life * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, 2.5f, paint)
        }
        paint.alpha = 255

        // HUD: Score, Lives
        textPaint.color = Color.rgb(57, 255, 20)
        canvas.drawText("SCORE: $score", 10f, 18f, textPaint)
        textPaint.color = Color.rgb(255, 70, 70)
        val hearts = "♥ ".repeat(lives.coerceAtLeast(0))
        canvas.drawText(hearts, canvasWidth - 65f, 18f, textPaint)

        if (isBossBattle) {
            textPaint.color = Color.YELLOW
            canvas.drawText("WARNING: BOSS", canvasWidth / 2f - 45f, 34f, textPaint)
        }

        if (gameOver) {
            paint.color = Color.argb(190, 0, 0, 0)
            canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), paint)
            textPaint.color = Color.RED
            textPaint.textSize = 20f
            canvas.drawText("GAME OVER", canvasWidth / 2f - 55f, canvasHeight / 2f - 10f, textPaint)
            textPaint.color = Color.WHITE
            textPaint.textSize = 12f
            canvas.drawText("กด 5 หรือ ไฟร์ เพื่อเริ่มใหม่", canvasWidth / 2f - 65f, canvasHeight / 2f + 20f, textPaint)
            textPaint.textSize = 14f
        }
    }

    override fun keyPressed(keyCode: Int, audio: AudioSynthesizer) {
        if (gameOver) {
            if (keyCode == J2MEKeyCodes.KEY_FIRE || keyCode == J2MEKeyCodes.KEY_NUM5) {
                reset()
            }
            return
        }
        when (keyCode) {
            J2MEKeyCodes.KEY_UP, J2MEKeyCodes.KEY_NUM2 -> moveUp = true
            J2MEKeyCodes.KEY_DOWN, J2MEKeyCodes.KEY_NUM8 -> moveDown = true
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = true
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = true
            J2MEKeyCodes.KEY_FIRE, J2MEKeyCodes.KEY_NUM5 -> isFiring = true
            J2MEKeyCodes.KEY_NUM0 -> {
                // Smart bomb
                audio.playExplosionSound()
                for (e in enemies) {
                    e.hp -= 3
                    createExplosion(e.x, e.y, Color.CYAN, 8)
                }
            }
        }
    }

    override fun keyReleased(keyCode: Int) {
        when (keyCode) {
            J2MEKeyCodes.KEY_UP, J2MEKeyCodes.KEY_NUM2 -> moveUp = false
            J2MEKeyCodes.KEY_DOWN, J2MEKeyCodes.KEY_NUM8 -> moveDown = false
            J2MEKeyCodes.KEY_LEFT, J2MEKeyCodes.KEY_NUM4 -> moveLeft = false
            J2MEKeyCodes.KEY_RIGHT, J2MEKeyCodes.KEY_NUM6 -> moveRight = false
            J2MEKeyCodes.KEY_FIRE, J2MEKeyCodes.KEY_NUM5 -> isFiring = false
        }
    }

    override fun pointerPressed(x: Float, y: Float, audio: AudioSynthesizer) {
        if (gameOver) {
            reset()
            return
        }
        // Direct touch control
        playerX = x
        playerY = y
        isFiring = true
    }

    override fun pointerReleased(x: Float, y: Float) {
        isFiring = false
    }

    override fun getScore(): Int = score
    override fun isGameOver(): Boolean = gameOver

    override fun getSaveState(): String = "$score,$lives,$playerX,$playerY,$bossHp"
    override fun restoreSaveState(stateData: String) {
        try {
            val parts = stateData.split(",")
            if (parts.size >= 5) {
                score = parts[0].toInt()
                lives = parts[1].toInt()
                playerX = parts[2].toFloat()
                playerY = parts[3].toFloat()
                bossHp = parts[4].toInt()
                gameOver = false
            }
        } catch (e: Exception) {
        }
    }
}
