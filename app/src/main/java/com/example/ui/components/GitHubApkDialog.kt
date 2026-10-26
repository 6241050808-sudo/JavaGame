package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.LaserGreen
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GitHubApkDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val gitInstructions = """
git add .
git commit -m "Build RetroJar J2ME Emulator APK"
git push origin main
    """.trimIndent()

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Git Commands", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "คัดลอกคำสั่งเรียบร้อยแล้ว!", Toast.LENGTH_SHORT).show()
    }

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
                        Icon(Icons.Default.Download, contentDescription = "GitHub APK", tint = LaserGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ระบบสร้าง APK ผ่าน GitHub",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_github_apk_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "สร้างไฟล์เวิร์กโฟลว์อัตโนมัติสำเร็จแล้วที่:",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = ".github/workflows/build-apk.yml",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ขั้นตอนการดาวน์โหลดไฟล์ APK:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LaserGreen
                )

                val steps = listOf(
                    "1. พุชโค้ดขึ้น GitHub Repository ของคุณ (ใช้คำสั่งด้านล่าง)",
                    "2. เข้าไปที่หน้า Repository บนเว็บ GitHub แล้วคลิกแท็บ Actions",
                    "3. จะเห็นเวิร์กโฟลว์ \"Build Android APK\" รันอัตโนมัติ (หรือกด Run workflow เองได้ทันที)",
                    "4. เมื่อรันเสร็จ (เครื่องหมายถูกสีเขียว) ให้เลื่อนลงมาที่หัวข้อ Artifacts",
                    "5. คลิกดาวน์โหลด \"RetroJar-APK\" จะได้ไฟล์ APK พร้อมติดตั้งบนมือถือได้ทันที!"
                )

                steps.forEach { step ->
                    Text(
                        text = step,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "คำสั่ง Git สำหรับพุชขึ้น GitHub:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = gitInstructions,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFF80D8FF)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { copyToClipboard(gitInstructions) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "คัดลอก")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("คัดลอกคำสั่ง Git Push")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LaserGreen, contentColor = Color.Black)
                ) {
                    Text("เข้าใจแล้ว (OK)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
