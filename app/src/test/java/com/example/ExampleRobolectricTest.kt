package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.GameRepository
import com.example.engine.J2MEKeyCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RetroJar", appName)
    }

    @Test
    fun `verify default games in repository`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        val defaultGames = repo.games.value

        assertTrue(defaultGames.isNotEmpty())
        assertEquals(3, defaultGames.size)
        assertNotNull(defaultGames.find { it.id == "space_impact" })
        assertNotNull(defaultGames.find { it.id == "bounce" })
        assertNotNull(defaultGames.find { it.id == "retro_racer" })
    }

    @Test
    fun `verify j2me key codes mapping`() {
        assertEquals("5 (ยิง/OK)", J2MEKeyCodes.getKeyName(J2MEKeyCodes.KEY_NUM5))
        assertEquals("ทิศทางขึ้น (Up)", J2MEKeyCodes.getKeyName(J2MEKeyCodes.KEY_UP))
    }
}
