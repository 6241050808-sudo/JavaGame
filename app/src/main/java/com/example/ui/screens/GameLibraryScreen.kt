package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameSource
import com.example.data.model.J2MEGame
import com.example.engine.JarParser
import com.example.ui.components.GitHubApkDialog
import com.example.ui.components.JarInspectorDialog
import com.example.ui.theme.ArcadeOrange
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricYellow
import com.example.ui.theme.HotPink
import com.example.ui.theme.LaserGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameLibraryScreen(
    games: List<J2MEGame>,
    onSelectGame: (J2MEGame) -> Unit,
    onImportJar: (Uri) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedInspectGame by remember { mutableStateOf<J2MEGame?>(null) }
    var showGitHubApkDialog by remember { mutableStateOf(false) }

    // File picker launcher for .jar files
    val jarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                onImportJar(uri)
                Toast.makeText(context, "โหลดไฟล์เกม .JAR สำเร็จแล้ว!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "ไม่สามารถอ่านไฟล์ได้: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val filteredGames = remember(games, searchQuery) {
        if (searchQuery.isBlank()) games
        else games.filter { it.title.contains(searchQuery, ignoreCase = true) || it.vendor.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    jarPickerLauncher.launch(arrayOf("*/*"))
                },
                containerColor = NeonCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("import_jar_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "เปิดไฟล์ JAR")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero App Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF131D33), DarkBackground)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NeonCyan.copy(alpha = 0.2f))
                                    .border(1.5.dp, NeonCyan, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("☕", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "RetroJar Player",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "ตัวจำลองเกม J2ME + แทรกเฟรม 120Hz & ชัดขึ้น",
                                    fontSize = 11.sp,
                                    color = NeonCyan
                                )
                            }
                        }

                        // GitHub APK Action Button
                        IconButton(
                            onClick = { showGitHubApkDialog = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, LaserGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .testTag("github_apk_header_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "GitHub APK", tint = LaserGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { jarPickerLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("pick_jar_file_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("เปิดไฟล์ .JAR", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showGitHubApkDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("show_github_apk_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant, contentColor = LaserGreen)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ทำ APK บน GitHub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ค้นหาชื่อเกม หรือ ผู้พัฒนา...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "ค้นหา", tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .testTag("game_search_input")
            )

            // Games List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "คลังเกมของคุณ (${filteredGames.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "MIDP 2.0 / CLDC 1.1",
                            fontSize = 11.sp,
                            color = ArcadeOrange
                        )
                    }
                }

                items(filteredGames, key = { it.id }) { game ->
                    GameCard(
                        game = game,
                        onPlay = { onSelectGame(game) },
                        onInspect = { selectedInspectGame = game }
                    )
                }
            }
        }
    }

    // Dialogs
    selectedInspectGame?.let { g ->
        JarInspectorDialog(game = g, onDismiss = { selectedInspectGame = null })
    }

    if (showGitHubApkDialog) {
        GitHubApkDialog(onDismiss = { showGitHubApkDialog = false })
    }
}

@Composable
fun GameCard(
    game: J2MEGame,
    onPlay: () -> Unit,
    onInspect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("game_card_${game.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkBorder, DarkBorder.copy(alpha = 0.5f))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val bmp = game.iconBitmap
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = game.title,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (game.id) {
                                "space_impact" -> Color(0xFF00384D)
                                "bounce" -> Color(0xFF4D0014)
                                "retro_racer" -> Color(0xFF4D2600)
                                else -> Color(0xFF1A2338)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (game.id) {
                            "space_impact" -> "🚀"
                            "bounce" -> "🔴"
                            "retro_racer" -> "🏎️"
                            else -> "☕"
                        },
                        fontSize = 24.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = game.vendor,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "คะแนน: ${game.highScore}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElectricYellow
                    )
                    Text(
                        text = "•  240x320",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            IconButton(
                onClick = onInspect,
                modifier = Modifier.testTag("inspect_game_${game.id}")
            ) {
                Icon(Icons.Default.Info, contentDescription = "ข้อมูลเกม", tint = TextSecondary)
            }

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                modifier = Modifier
                    .height(38.dp)
                    .testTag("play_button_${game.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "เล่น", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("เล่น", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
