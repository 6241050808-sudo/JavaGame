package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.data.model.GameSource
import com.example.data.model.J2MEGame
import com.example.data.model.JarManifestInfo
import com.example.data.model.SaveStateSlot
import com.example.engine.JarParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameRepository(private val context: Context) {

    private val _games = MutableStateFlow<List<J2MEGame>>(getDefaultGames())
    val games: StateFlow<List<J2MEGame>> = _games.asStateFlow()

    private val saveSlots = mutableMapOf<String, MutableMap<Int, SaveStateSlot>>()

    private fun getDefaultGames(): List<J2MEGame> {
        return listOf(
            J2MEGame(
                id = "space_impact",
                title = "Space Impact Revival",
                vendor = "Nokia Original (J2ME)",
                version = "1.2.0",
                description = "เกมยานยิงอวกาศในตำนานบนมือถือโนเกีย ยิงเลเซอร์ หลบอุกกาบาต และปราบบอสมารดา",
                source = GameSource.BUILT_IN,
                canvasWidth = 240,
                canvasHeight = 320,
                highScore = 1450
            ),
            J2MEGame(
                id = "bounce",
                title = "Bounce Ball Classic",
                vendor = "Nokia Mobile",
                version = "2.0.1",
                description = "การผจญภัยของลูกบอลสีแดง กระโดดเก็บห่วงทอง เด้งแทรมโพลีน และหลบหนามแหลมคม",
                source = GameSource.BUILT_IN,
                canvasWidth = 240,
                canvasHeight = 320,
                highScore = 900
            ),
            J2MEGame(
                id = "retro_racer",
                title = "Retro Racer 2D",
                vendor = "Gameloft Tribute",
                version = "1.0.5",
                description = "เกมแข่งรถซิ่งทางหลวงสุดมันส์ แซงการจราจร เก็บไนโตรเทอร์โบเพื่อเร่งความเร็วสูงสุด",
                source = GameSource.BUILT_IN,
                canvasWidth = 240,
                canvasHeight = 320,
                highScore = 2200
            )
        )
    }

    fun addJarGame(uri: Uri, manifest: JarManifestInfo): J2MEGame {
        val newGame = J2MEGame(
            id = "jar_" + System.currentTimeMillis(),
            title = manifest.midletName,
            vendor = manifest.midletVendor,
            version = manifest.midletVersion,
            description = "ไฟล์ JAR: ${manifest.totalClasses} classes, ${manifest.configuration}/${manifest.profile}",
            source = GameSource.CUSTOM_JAR,
            jarUriString = uri.toString(),
            iconBitmap = manifest.iconBitmap,
            canvasWidth = 240,
            canvasHeight = 320
        )
        val currentList = _games.value.toMutableList()
        currentList.add(0, newGame)
        _games.value = currentList
        return newGame
    }

    fun updateScore(gameId: String, newScore: Int) {
        val currentList = _games.value.map { g ->
            if (g.id == gameId && newScore > g.highScore) {
                g.copy(highScore = newScore, lastPlayed = System.currentTimeMillis())
            } else if (g.id == gameId) {
                g.copy(lastPlayed = System.currentTimeMillis())
            } else g
        }
        _games.value = currentList
    }

    fun saveGameSlot(gameId: String, slotIndex: Int, score: Int, stateData: String, thumbnail: Bitmap?): SaveStateSlot {
        val gameSlots = saveSlots.getOrPut(gameId) { mutableMapOf() }
        val slot = SaveStateSlot(
            slotIndex = slotIndex,
            gameId = gameId,
            timestamp = System.currentTimeMillis(),
            score = score,
            stateData = stateData,
            thumbnail = thumbnail
        )
        gameSlots[slotIndex] = slot
        return slot
    }

    fun getGameSlots(gameId: String): Map<Int, SaveStateSlot> {
        return saveSlots[gameId] ?: emptyMap()
    }
}
