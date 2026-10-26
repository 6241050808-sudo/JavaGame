package com.example.data.model

import android.graphics.Bitmap

enum class GameSource {
    BUILT_IN,
    CUSTOM_JAR
}

data class J2MEGame(
    val id: String,
    val title: String,
    val vendor: String = "J2ME Community",
    val version: String = "1.0.0",
    val description: String = "",
    val source: GameSource = GameSource.BUILT_IN,
    val jarUriString: String? = null,
    val iconBitmap: Bitmap? = null,
    val canvasWidth: Int = 240,
    val canvasHeight: Int = 320,
    val targetFps: Int = 30,
    val lastPlayed: Long = System.currentTimeMillis(),
    val playTimeMinutes: Int = 0,
    val highScore: Int = 0
)

data class JarManifestInfo(
    val midletName: String,
    val midletVendor: String,
    val midletVersion: String,
    val midletIconPath: String?,
    val mainClass: String?,
    val configuration: String = "CLDC-1.1",
    val profile: String = "MIDP-2.0",
    val totalClasses: Int = 0,
    val totalFiles: Int = 0,
    val jarSizeBytes: Long = 0L,
    val iconBitmap: Bitmap? = null
)
