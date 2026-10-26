package com.example

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.J2MEGame
import com.example.data.repository.GameRepository
import com.example.engine.JarParser
import com.example.ui.screens.EmulatorScreen
import com.example.ui.screens.GameLibraryScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = GameRepository(applicationContext)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    RetroJarApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun RetroJarApp(repository: GameRepository) {
    val games by repository.games.collectAsState()
    var activeGame by remember { mutableStateOf<J2MEGame?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (activeGame != null) {
        EmulatorScreen(
            game = activeGame!!,
            repository = repository,
            onBackToLibrary = { activeGame = null }
        )
    } else {
        GameLibraryScreen(
            games = games,
            onSelectGame = { game ->
                activeGame = game
            },
            onImportJar = { uri: Uri ->
                try {
                    val manifest = JarParser.parseJarFile(context, uri)
                    val newGame = repository.addJarGame(uri, manifest)
                    activeGame = newGame
                } catch (e: Exception) {
                    Toast.makeText(context, "ข้อผิดพลาดในการโหลดไฟล์ JAR: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}
