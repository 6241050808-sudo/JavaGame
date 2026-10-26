package com.example.ui.components

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
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.model.ControlLayoutPreset
import com.example.data.model.VirtualControlsConfig
import com.example.data.model.defaultClassicNokiaKeypadButtons
import com.example.data.model.defaultModernGamepadButtons
import com.example.ui.theme.ArcadeOrange
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ControlCustomizerSheet(
    config: VirtualControlsConfig,
    onConfigChange: (VirtualControlsConfig) -> Unit,
    onEnterDragEditMode: () -> Unit,
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
                        Icon(Icons.Default.Gamepad, contentDescription = "ปุ่มควบคุม", tint = ArcadeOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ปรับแต่งปุ่มควบคุม (Virtual Pad)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_control_customizer_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "ปิด", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enter Drag Edit Mode button
                Button(
                    onClick = {
                        onEnterDragEditMode()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enter_drag_edit_mode_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ArcadeOrange, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.OpenWith, contentDescription = "ลากวางปุ่มอิสระ")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("เข้าสู่โหมดลากวางปุ่มอิสระบนจอ", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Presets
                Text(
                    text = "รูปแบบปุ่มสำเร็จรูป (Layout Presets)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArcadeOrange
                )

                ControlLayoutPreset.values().forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newButtons = when (preset) {
                                    ControlLayoutPreset.CLASSIC_NOKIA_KEYPAD -> defaultClassicNokiaKeypadButtons()
                                    ControlLayoutPreset.MODERN_GAMEPAD -> defaultModernGamepadButtons()
                                    ControlLayoutPreset.SPLIT_ARCADE -> defaultModernGamepadButtons()
                                    ControlLayoutPreset.MINIMAL_FLOATING -> defaultModernGamepadButtons().take(7)
                                    ControlLayoutPreset.CUSTOM_USER -> config.customButtons
                                }
                                onConfigChange(
                                    config.copy(
                                        currentPreset = preset,
                                        customButtons = newButtons
                                    )
                                )
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = config.currentPreset == preset,
                            onClick = {
                                val newButtons = when (preset) {
                                    ControlLayoutPreset.CLASSIC_NOKIA_KEYPAD -> defaultClassicNokiaKeypadButtons()
                                    ControlLayoutPreset.MODERN_GAMEPAD -> defaultModernGamepadButtons()
                                    ControlLayoutPreset.SPLIT_ARCADE -> defaultModernGamepadButtons()
                                    ControlLayoutPreset.MINIMAL_FLOATING -> defaultModernGamepadButtons().take(7)
                                    ControlLayoutPreset.CUSTOM_USER -> config.customButtons
                                }
                                onConfigChange(config.copy(currentPreset = preset, customButtons = newButtons))
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = ArcadeOrange)
                        )
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(text = preset.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(text = preset.description, color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Opacity Slider
                Text(
                    text = "ความโปร่งใสปุ่ม (${(config.globalOpacity * 100).toInt()}%)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArcadeOrange
                )
                Slider(
                    value = config.globalOpacity,
                    onValueChange = { onConfigChange(config.copy(globalOpacity = it)) },
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = ArcadeOrange, activeTrackColor = ArcadeOrange)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Button Size Scale
                Text(
                    text = "ขนาดปุ่ม (${(config.buttonScale * 100).toInt()}%)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ArcadeOrange
                )
                Slider(
                    value = config.buttonScale,
                    onValueChange = { onConfigChange(config.copy(buttonScale = it)) },
                    valueRange = 0.75f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = ArcadeOrange, activeTrackColor = ArcadeOrange)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reset to Default button
                OutlinedButton(
                    onClick = {
                        onConfigChange(
                            VirtualControlsConfig(
                                currentPreset = ControlLayoutPreset.MODERN_GAMEPAD,
                                globalOpacity = 0.85f,
                                buttonScale = 1.0f,
                                customButtons = defaultModernGamepadButtons()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "คืนค่าเริ่มต้น")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("รีเซ็ตเป็นค่าเริ่มต้น (Reset)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black)
                ) {
                    Text("บันทึกการตั้งค่า (Done)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
