package com.example.engine.games

import android.graphics.Canvas
import android.graphics.Paint
import com.example.engine.AudioSynthesizer

interface IJ2MEGameEngine {
    val title: String
    val canvasWidth: Int
    val canvasHeight: Int

    fun init(width: Int, height: Int)
    fun update(deltaMs: Long, audio: AudioSynthesizer)
    fun render(canvas: Canvas, paint: Paint)
    fun keyPressed(keyCode: Int, audio: AudioSynthesizer)
    fun keyReleased(keyCode: Int)
    fun pointerPressed(x: Float, y: Float, audio: AudioSynthesizer)
    fun pointerReleased(x: Float, y: Float)
    fun reset()
    fun getScore(): Int
    fun isGameOver(): Boolean
    fun getSaveState(): String
    fun restoreSaveState(stateData: String)
}
