package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BezelFrameSkin
import com.example.data.model.EmulationConfig
import com.example.data.model.FrameInterpolationMode
import com.example.data.model.ResolutionEnhancer
import com.example.data.model.ScreenAspectRatio
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DisplaySettingsDialog(
    config: EmulationConfig,
    onConfigChange: (EmulationConfig) -> Unit,
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
                        Icon(
                            Icons.Default.DisplaySettings,
                            contentDescription = "การตั้งค่าจอภาพ",
                            tint = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "การแสดงผลและภาพลื่นไหล",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_display_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Frame Interpolation (ฟังชั่นแทรกเฟรม)
                Text(
                    text = "ฟังชั่นแทรกเฟรม (Frame Interpolation)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                Text(
                    text = "สร้างเฟรมภาพกลางระหว่างเฟรมเพื่อยกระดับความลื่นไหลระดับ 60Hz/120Hz",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FrameInterpolationMode.values().forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfigChange(config.copy(frameInterpolation = mode)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.frameInterpolation == mode,
                            onClick = { onConfigChange(config.copy(frameInterpolation = mode)) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(text = mode.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(text = mode.description, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Resolution Enhancer (เพิ่มความละเอียด & ปรับแต่งจอภาพ)
                Text(
                    text = "เพิ่มความละเอียดภาพ (Resolution Enhancer)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                Text(
                    text = "อัลกอริทึมปรับปรุงพิกเซลให้คมชัด เนียนตา หรือเพิ่มเอฟเฟกต์จอแก้ว",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ResolutionEnhancer.values().forEach { enhancer ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfigChange(config.copy(resolutionEnhancer = enhancer)) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.resolutionEnhancer == enhancer,
                            onClick = { onConfigChange(config.copy(resolutionEnhancer = enhancer)) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(text = enhancer.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(text = enhancer.description, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 3: Screen Aspect Ratio
                Text(
                    text = "อัตราส่วนหน้าจอ (Aspect Ratio)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                ScreenAspectRatio.values().forEach { ratio ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfigChange(config.copy(aspectRatio = ratio)) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.aspectRatio == ratio,
                            onClick = { onConfigChange(config.copy(aspectRatio = ratio)) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Text(text = ratio.label, color = TextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 4: Retro Phone Bezels
                Text(
                    text = "กรอบมือถือย้อนยุค (Retro Phone Bezel)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                BezelFrameSkin.values().forEach { skin ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfigChange(config.copy(bezelSkin = skin)) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.bezelSkin == skin,
                            onClick = { onConfigChange(config.copy(bezelSkin = skin)) },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Text(text = skin.label, color = TextPrimary, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FPS Overlay Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "แสดงแถบ FPS & ค่าสถิติ", color = TextPrimary, fontSize = 13.sp)
                    Switch(
                        checked = config.showFpsCounter,
                        onCheckedChange = { onConfigChange(config.copy(showFpsCounter = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_display_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("เสร็จสิ้น (Apply)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
