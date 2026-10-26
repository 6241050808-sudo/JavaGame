package com.example.data.model

import android.graphics.Bitmap

data class SaveStateSlot(
    val slotIndex: Int, // 1..5
    val gameId: String,
    val timestamp: Long,
    val score: Int,
    val stateData: String,
    val thumbnail: Bitmap? = null
)
