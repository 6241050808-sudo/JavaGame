package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VirtualButtonItem
import com.example.data.model.VirtualControlsConfig
import com.example.ui.theme.NeonCyan

@Composable
fun VirtualGamepad(
    config: VirtualControlsConfig,
    isEditMode: Boolean = false,
    selectedButtonId: String? = null,
    onButtonSelect: (String) -> Unit = {},
    onButtonMoved: (String, Float, Float) -> Unit = { _, _, _ -> },
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService(Vibrator::class.java)
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (e: Exception) {
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        val padWidth = maxWidth
        val padHeight = maxHeight

        config.customButtons.filter { it.isVisible }.forEach { button ->
            val isSelected = isEditMode && (button.id == selectedButtonId)

            val xOffset = padWidth * button.xRatio - (button.sizeDp.dp * config.buttonScale / 2f)
            val yOffset = padHeight * button.yRatio - (button.sizeDp.dp * config.buttonScale / 2f)

            var isPressed by remember { mutableStateOf(false) }

            val shape = if (button.isCircle) CircleShape else RoundedCornerShape(12.dp)
            val btnColor = Color(button.primaryColorHex)

            Box(
                modifier = Modifier
                    .offset { IntOffset(xOffset.roundToPx(), yOffset.roundToPx()) }
                    .size((button.sizeDp.dp * config.buttonScale).coerceAtLeast(48.dp))
                    .alpha(if (isEditMode) 1.0f else config.globalOpacity)
                    .clip(shape)
                    .background(
                        when {
                            isPressed -> btnColor.copy(alpha = 0.8f)
                            isSelected -> Color(0xFFFF9100).copy(alpha = 0.5f)
                            else -> Color(0x33000000)
                        }
                    )
                    .border(
                        width = if (isSelected) 3.dp else 1.5.dp,
                        color = if (isSelected) Color(0xFFFF9100) else if (isPressed) Color.White else btnColor.copy(alpha = 0.7f),
                        shape = shape
                    )
                    .testTag("virtual_btn_${button.id}")
                    .pointerInput(button.id, isEditMode) {
                        if (isEditMode) {
                            detectDragGestures(
                                onDragStart = { onButtonSelect(button.id) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val newXRatio = ((xOffset.toPx() + dragAmount.x + (button.sizeDp.dp.toPx() / 2f)) / padWidth.toPx()).coerceIn(0.05f, 0.95f)
                                    val newYRatio = ((yOffset.toPx() + dragAmount.y + (button.sizeDp.dp.toPx() / 2f)) / padHeight.toPx()).coerceIn(0.05f, 0.95f)
                                    onButtonMoved(button.id, newXRatio, newYRatio)
                                }
                            )
                        } else {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    triggerHaptic()
                                    onKeyDown(button.j2meKeyCode)
                                    tryAwaitRelease()
                                    isPressed = false
                                    onKeyUp(button.j2meKeyCode)
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = button.label,
                    color = if (isPressed) Color.Black else Color.White,
                    fontSize = if (button.label.length > 2) 13.sp else 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
