package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.J2MEGame
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun JarInspectorDialog(
    game: J2MEGame,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "ข้อมูลเกม", tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ข้อมูลไฟล์ .JAR / MIDlet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_jar_inspector_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Icon & Game Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val bmp = game.iconBitmap
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = game.title,
                            modifier = Modifier
                                .size(56.dp)
                                .background(DarkSurfaceVariant, RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("☕", fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = game.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = game.vendor, fontSize = 12.sp, color = TextSecondary)
                        Text(text = "เวอร์ชัน: ${game.version}", fontSize = 11.sp, color = NeonCyan)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata Details
                val infoList = listOf(
                    "ประเภทเกม" to if (game.source == com.example.data.model.GameSource.BUILT_IN) "เกมติดตั้งในตัว (Built-in)" else "ไฟล์ภายนอก (.JAR File)",
                    "ความละเอียดดั้งเดิม" to "${game.canvasWidth} x ${game.canvasHeight} px (QVGA)",
                    "มาตรฐานสถาปัตยกรรม" to "CLDC 1.1 / MIDP 2.0 (J2ME)",
                    "คะแนนสูงสุด (High Score)" to "${game.highScore} แต้ม",
                    "ระบบเสียง" to "8-Bit Square Wave / ToneControl Synthesizer",
                    "ระบบแทรกเฟรม" to "รองรับการแทรกเฟรมสูงสุด 120 FPS",
                    "คำอธิบาย" to game.description
                )

                infoList.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Text(text = label, fontSize = 11.sp, color = TextSecondary)
                        Text(text = value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("ปิดหน้าต่าง", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
