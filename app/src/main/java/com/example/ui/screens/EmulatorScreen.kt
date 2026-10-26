package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmulationConfig
import com.example.data.model.FrameInterpolationMode
import com.example.data.model.J2MEGame
import com.example.data.model.VirtualControlsConfig
import com.example.data.repository.GameRepository
import com.example.engine.AudioSynthesizer
import com.example.engine.FrameInterpolationEngine
import com.example.engine.games.BounceGame
import com.example.engine.games.CustomJarGameEngine
import com.example.engine.games.IJ2MEGameEngine
import com.example.engine.games.RetroRacerGame
import com.example.engine.games.SpaceImpactGame
import com.example.ui.components.ControlCustomizerSheet
import com.example.ui.components.DisplaySettingsDialog
import com.example.ui.components.EmulatorScreenView
import com.example.ui.components.VirtualGamepad
import com.example.ui.theme.ArcadeOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LaserGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun EmulatorScreen(
    game: J2MEGame,
    repository: GameRepository,
    onBackToLibrary: () -> Unit
) {
    val context = LocalContext.current

    // Back button handling
    BackHandler {
        onBackToLibrary()
    }

    // Audio synthesizer
    val audioSynth = remember { AudioSynthesizer() }
    var soundMuted by remember { mutableStateOf(!audioSynth.let { true }) }

    // Emulation & Controls configs
    var emulationConfig by remember { mutableStateOf(EmulationConfig()) }
    var controlsConfig by remember { mutableStateOf(VirtualControlsConfig()) }

    // Dialog & Edit Mode states
    var showDisplaySettings by remember { mutableStateOf(false) }
    var showControlCustomizer by remember { mutableStateOf(false) }
    var isDragEditMode by remember { mutableStateOf(false) }
    var selectedEditButtonId by remember { mutableStateOf<String?>(null) }

    // Emulation Engine setup
    val gameEngine = remember(game.id) {
        val engine: IJ2MEGameEngine = when (game.id) {
            "space_impact" -> SpaceImpactGame()
            "bounce" -> BounceGame()
            "retro_racer" -> RetroRacerGame()
            else -> {
                // Custom JAR engine
                val manifestInfo = com.example.data.model.JarManifestInfo(
                    midletName = game.title,
                    midletVendor = game.vendor,
                    midletVersion = game.version,
                    midletIconPath = null,
                    mainClass = null,
                    totalClasses = 14,
                    iconBitmap = game.iconBitmap
                )
                CustomJarGameEngine(manifestInfo)
            }
        }
        engine.init(game.canvasWidth, game.canvasHeight)
        engine
    }

    // Double buffer bitmaps
    val baseBitmap = remember {
        Bitmap.createBitmap(game.canvasWidth, game.canvasHeight, Bitmap.Config.ARGB_8888)
    }
    val gameCanvas = remember { Canvas(baseBitmap) }
    val gamePaint = remember { Paint() }

    // Frame interpolation engine
    val frameInterpolator = remember { FrameInterpolationEngine() }
    var renderBitmap by remember { mutableStateOf(baseBitmap) }

    // Telemetry
    var displayFps by remember { mutableFloatStateOf(60f) }
    var nativeFps by remember { mutableFloatStateOf(30f) }

    // Turbo speed multiplier cycle: 1x -> 1.5x -> 2x -> 3x -> 1x
    var turboSpeed by remember { mutableFloatStateOf(1.0f) }

    // Main Game Emulation Loop
    LaunchedEffect(game.id, turboSpeed, emulationConfig.frameInterpolation) {
        var lastTickNs = System.nanoTime()
        val targetFrameMs = (1000L / emulationConfig.targetFps).coerceAtLeast(8L)

        while (isActive) {
            val nowNs = System.nanoTime()
            val elapsedMs = ((nowNs - lastTickNs) / 1_000_000L).coerceAtLeast(1L)
            lastTickNs = nowNs

            // Update game physics by elapsedMs * turboSpeed
            val deltaToSimulate = (elapsedMs * turboSpeed).toLong().coerceIn(1L, 100L)
            gameEngine.update(deltaToSimulate, audioSynth)

            // Render game canvas
            gameEngine.render(gameCanvas, gamePaint)
            frameInterpolator.registerNativeFrame(baseBitmap)

            // Get frame with interpolation
            val outputFrame = frameInterpolator.getRenderableFrame(
                baseBitmap = baseBitmap,
                mode = emulationConfig.frameInterpolation,
                subframeAlpha = 0.5f
            )
            renderBitmap = outputFrame

            displayFps = frameInterpolator.currentDisplayFps
            nativeFps = frameInterpolator.currentNativeFps

            val frameTimeSpentMs = ((System.nanoTime() - nowNs) / 1_000_000L)
            val sleepTimeMs = (targetFrameMs - frameTimeSpentMs).coerceAtLeast(2L)
            delay(sleepTimeMs)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioSynth.setMuted(true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Game Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackToLibrary,
                    modifier = Modifier.testTag("emulator_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "กลับ", tint = TextPrimary)
                }
                Column(modifier = Modifier.widthIn(max = 140.dp)) {
                    Text(
                        text = game.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "คะแนน: ${gameEngine.getScore()}",
                        color = LaserGreen,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Turbo Speed Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (turboSpeed > 1f) ArcadeOrange else DarkSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("turbo_speed_pill")
                        .clickable {
                            turboSpeed = when (turboSpeed) {
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                2.0f -> 3.0f
                                else -> 1.0f
                            }
                            Toast.makeText(context, "ความเร็วเกม: ${turboSpeed}x", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Text(
                        text = "${turboSpeed}x",
                        color = if (turboSpeed > 1f) Color.Black else NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Save State Button
                IconButton(
                    onClick = {
                        val stateStr = gameEngine.getSaveState()
                        repository.saveGameSlot(game.id, 1, gameEngine.getScore(), stateStr, null)
                        Toast.makeText(context, "บันทึกสถานะเกม (Save State Slot 1) สำเร็จ!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("quick_save_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = "เซฟเกม", tint = NeonCyan)
                }

                // Sound Toggle
                IconButton(
                    onClick = {
                        soundMuted = !soundMuted
                        audioSynth.setMuted(soundMuted)
                    },
                    modifier = Modifier.testTag("sound_toggle_button")
                ) {
                    Icon(
                        if (soundMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "เสียง",
                        tint = if (soundMuted) TextSecondary else NeonCyan
                    )
                }

                // Display Settings (Interpolation & Enhancer)
                IconButton(
                    onClick = { showDisplaySettings = true },
                    modifier = Modifier.testTag("display_settings_icon_button")
                ) {
                    Icon(Icons.Default.DisplaySettings, contentDescription = "การแสดงผล", tint = NeonCyan)
                }

                // Custom Controller Settings
                IconButton(
                    onClick = { showControlCustomizer = true },
                    modifier = Modifier.testTag("customize_controls_icon_button")
                ) {
                    Icon(Icons.Default.Gamepad, contentDescription = "ปรับแต่งปุ่ม", tint = ArcadeOrange)
                }
            }
        }

        // Drag Edit Mode Info Banner
        if (isDragEditMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ArcadeOrange)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ โหมดปรับแต่ง: แตะแล้วลากปุ่มไปยังจุดที่ต้องการ",
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { isDragEditMode = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "เสร็จ", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เสร็จสิ้น", fontSize = 11.sp)
                }
            }
        }

        // Emulator Screen (J2ME Canvas View)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.05f)
        ) {
            EmulatorScreenView(
                bitmap = renderBitmap,
                config = emulationConfig,
                nativeFps = nativeFps,
                displayFps = displayFps,
                onPointerDown = { x, y -> gameEngine.pointerPressed(x, y, audioSynth) },
                onPointerUp = { x, y -> gameEngine.pointerReleased(x, y) },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Virtual Controls Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.95f)
                .background(Color(0xFF070A10))
        ) {
            VirtualGamepad(
                config = controlsConfig,
                isEditMode = isDragEditMode,
                selectedButtonId = selectedEditButtonId,
                onButtonSelect = { selectedEditButtonId = it },
                onButtonMoved = { btnId, newX, newY ->
                    val updatedList = controlsConfig.customButtons.map { btn ->
                        if (btn.id == btnId) btn.copy(xRatio = newX, yRatio = newY) else btn
                    }
                    controlsConfig = controlsConfig.copy(customButtons = updatedList)
                },
                onKeyDown = { code -> gameEngine.keyPressed(code, audioSynth) },
                onKeyUp = { code -> gameEngine.keyReleased(code) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Settings Dialogs
    if (showDisplaySettings) {
        DisplaySettingsDialog(
            config = emulationConfig,
            onConfigChange = { emulationConfig = it },
            onDismiss = { showDisplaySettings = false }
        )
    }

    if (showControlCustomizer) {
        ControlCustomizerSheet(
            config = controlsConfig,
            onConfigChange = { controlsConfig = it },
            onEnterDragEditMode = { isDragEditMode = true },
            onDismiss = { showControlCustomizer = false }
        )
    }
}
