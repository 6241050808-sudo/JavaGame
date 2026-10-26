package com.example.engine

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioSynthesizer {

    private val sampleRate = 22050
    private var isMuted = false
    private var volume = 0.8f
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun playTone(freqHz: Float, durationMs: Int) {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                if (numSamples <= 0) return@launch
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val time = i.toDouble() / sampleRate
                    // 8-bit square wave with subtle harmonics
                    val rawSin = sin(2.0 * PI * freqHz * time)
                    val squareVal = if (rawSin >= 0) 0.65 else -0.65
                    // Envelope attack/decay to eliminate audio clicks
                    val envelope = when {
                        i < 100 -> i / 100.0
                        i > numSamples - 150 -> (numSamples - i) / 150.0
                        else -> 1.0
                    }
                    buffer[i] = (squareVal * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (e: Exception) {
                // Ignore audio play errors
            }
        }
    }

    fun playLaserSound() {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                val durationMs = 120
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 900.0 - (progress * 650.0) // Frequency sweep down
                    val time = i.toDouble() / sampleRate
                    val wave = if (sin(2.0 * PI * freq * time) >= 0) 0.5 else -0.5
                    val envelope = 1.0 - progress
                    buffer[i] = (wave * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (e: Exception) {
            }
        }
    }

    fun playJumpSound() {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                val durationMs = 100
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 300.0 + (progress * 400.0) // Frequency sweep up
                    val time = i.toDouble() / sampleRate
                    val wave = sin(2.0 * PI * freq * time)
                    val envelope = 1.0 - (progress * 0.5)
                    buffer[i] = (wave * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (e: Exception) {
            }
        }
    }

    fun playExplosionSound() {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                val durationMs = 250
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                var lastNoise = 0.0
                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val white = (Math.random() * 2.0 - 1.0)
                    // Low pass filtered noise
                    lastNoise = lastNoise * 0.7 + white * 0.3
                    val envelope = (1.0 - progress) * (1.0 - progress)
                    buffer[i] = (lastNoise * envelope * volume * Short.MAX_VALUE).toInt().toShort()
                }
                playBuffer(buffer)
            } catch (e: Exception) {
            }
        }
    }

    fun playCoinChime() {
        if (isMuted || volume <= 0.01f) return
        scope.launch {
            try {
                playTone(987.77f, 60) // B5
                kotlinx.coroutines.delay(50)
                playTone(1318.51f, 100) // E6
            } catch (e: Exception) {
            }
        }
    }

    private fun playBuffer(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        scope.launch {
            val sleepMs = ((buffer.size.toDouble() / sampleRate) * 1000).toLong() + 50
            kotlinx.coroutines.delay(sleepMs)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
            }
        }
    }
}
