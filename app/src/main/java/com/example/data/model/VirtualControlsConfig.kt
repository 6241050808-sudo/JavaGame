package com.example.data.model

import com.example.engine.J2MEKeyCodes

enum class ControlLayoutPreset(val label: String, val description: String) {
    MODERN_GAMEPAD("เกมแพดสมัยใหม่ (Modern Gamepad)", "D-Pad ทิศทางด้านซ้าย + ปุ่มแอ็กชัน A, B, X, Y ด้านขวา"),
    CLASSIC_NOKIA_KEYPAD("แป้นพิมพ์มือถือ (Nokia Keypad)", "ปุ่มกดตัวเลข 0-9, *, # สไตล์ปุ่มกดมือถือย้อนยุค"),
    SPLIT_ARCADE("อาร์เคดแยกซ้ายขวา (Split Arcade)", "จอยสติ๊กซ้าย + แผงปุ่มทางลัดขวา สะดวกสำหรับหน้าจอกว้าง"),
    MINIMAL_FLOATING("ลอยโปร่งใส (Minimal Floating)", "เฉพาะปุ่มบังคับและยิง โปร่งใสไม่บังสายตา"),
    CUSTOM_USER("ปรับแต่งเอง (Custom Layout)", "ตำแหน่งและขนาดปุ่มที่คุณออกแบบและบันทึกไว้เอง")
}

data class VirtualButtonItem(
    val id: String,
    val label: String,
    val subLabel: String = "",
    val j2meKeyCode: Int,
    val xRatio: Float, // 0f..1f within control area
    val yRatio: Float, // 0f..1f within control area
    val sizeDp: Int = 56,
    val isCircle: Boolean = true,
    val isVisible: Boolean = true,
    val primaryColorHex: Long = 0xFF00E5FF
)

data class VirtualControlsConfig(
    val currentPreset: ControlLayoutPreset = ControlLayoutPreset.MODERN_GAMEPAD,
    val globalOpacity: Float = 0.85f,
    val buttonScale: Float = 1.0f,
    val showKeypadNumbers: Boolean = true,
    val customButtons: List<VirtualButtonItem> = defaultModernGamepadButtons()
)

fun defaultModernGamepadButtons(): List<VirtualButtonItem> {
    return listOf(
        // Softkeys
        VirtualButtonItem("soft_l", "LSK", "เมนูซ้าย", J2MEKeyCodes.KEY_SOFT_LEFT, 0.10f, 0.10f, sizeDp = 50, isCircle = false),
        VirtualButtonItem("soft_r", "RSK", "เมนูขวา", J2MEKeyCodes.KEY_SOFT_RIGHT, 0.90f, 0.10f, sizeDp = 50, isCircle = false),
        
        // D-Pad Directional Cluster (Left side)
        VirtualButtonItem("dpad_up", "▲", "ขึ้น", J2MEKeyCodes.KEY_UP, 0.22f, 0.40f, sizeDp = 56),
        VirtualButtonItem("dpad_left", "◀", "ซ้าย", J2MEKeyCodes.KEY_LEFT, 0.10f, 0.58f, sizeDp = 56),
        VirtualButtonItem("dpad_fire", "●", "ตกลง/ยิง", J2MEKeyCodes.KEY_FIRE, 0.22f, 0.58f, sizeDp = 60, primaryColorHex = 0xFFFF9100),
        VirtualButtonItem("dpad_right", "▶", "ขวา", J2MEKeyCodes.KEY_RIGHT, 0.34f, 0.58f, sizeDp = 56),
        VirtualButtonItem("dpad_down", "▼", "ลง", J2MEKeyCodes.KEY_DOWN, 0.22f, 0.76f, sizeDp = 56),

        // Action Buttons Cluster (Right side)
        VirtualButtonItem("btn_fire", "A", "ยิง 5", J2MEKeyCodes.KEY_NUM5, 0.88f, 0.58f, sizeDp = 62, primaryColorHex = 0xFFFF007F),
        VirtualButtonItem("btn_jump", "B", "กระโดด 2", J2MEKeyCodes.KEY_NUM2, 0.76f, 0.42f, sizeDp = 56, primaryColorHex = 0xFF00E5FF),
        VirtualButtonItem("btn_action", "X", "กระทำ 4", J2MEKeyCodes.KEY_NUM4, 0.64f, 0.58f, sizeDp = 56, primaryColorHex = 0xFF39FF14),
        VirtualButtonItem("btn_turbo", "Y", "พิเศษ 8", J2MEKeyCodes.KEY_NUM8, 0.76f, 0.74f, sizeDp = 56, primaryColorHex = 0xFFFFEA00),

        // Quick Function Buttons
        VirtualButtonItem("btn_star", "*", "ดาว", J2MEKeyCodes.KEY_STAR, 0.44f, 0.90f, sizeDp = 44, isCircle = false),
        VirtualButtonItem("btn_pound", "#", "ชาร์ป", J2MEKeyCodes.KEY_POUND, 0.56f, 0.90f, sizeDp = 44, isCircle = false),
        VirtualButtonItem("btn_0", "0", "ศูนย์", J2MEKeyCodes.KEY_NUM0, 0.50f, 0.76f, sizeDp = 46, isCircle = false)
    )
}

fun defaultClassicNokiaKeypadButtons(): List<VirtualButtonItem> {
    val buttons = mutableListOf<VirtualButtonItem>()
    // Soft keys & D-pad top
    buttons.add(VirtualButtonItem("soft_l", "LSK", "ซอฟต์ซ้าย", J2MEKeyCodes.KEY_SOFT_LEFT, 0.15f, 0.08f, 52, false))
    buttons.add(VirtualButtonItem("soft_r", "RSK", "ซอฟต์ขวา", J2MEKeyCodes.KEY_SOFT_RIGHT, 0.85f, 0.08f, 52, false))
    buttons.add(VirtualButtonItem("nav_up", "▲", "", J2MEKeyCodes.KEY_UP, 0.50f, 0.05f, 46))
    buttons.add(VirtualButtonItem("nav_left", "◀", "", J2MEKeyCodes.KEY_LEFT, 0.38f, 0.16f, 46))
    buttons.add(VirtualButtonItem("nav_ok", "OK", "", J2MEKeyCodes.KEY_FIRE, 0.50f, 0.16f, 50, primaryColorHex = 0xFFFF9100))
    buttons.add(VirtualButtonItem("nav_right", "▶", "", J2MEKeyCodes.KEY_RIGHT, 0.62f, 0.16f, 46))
    buttons.add(VirtualButtonItem("nav_down", "▼", "", J2MEKeyCodes.KEY_DOWN, 0.50f, 0.27f, 46))

    // 3x4 Numeric Pad (1-9, *, 0, #)
    val keypadGrid = listOf(
        Pair("1", J2MEKeyCodes.KEY_NUM1), Pair("2", J2MEKeyCodes.KEY_NUM2), Pair("3", J2MEKeyCodes.KEY_NUM3),
        Pair("4", J2MEKeyCodes.KEY_NUM4), Pair("5", J2MEKeyCodes.KEY_NUM5), Pair("6", J2MEKeyCodes.KEY_NUM6),
        Pair("7", J2MEKeyCodes.KEY_NUM7), Pair("8", J2MEKeyCodes.KEY_NUM8), Pair("9", J2MEKeyCodes.KEY_NUM9),
        Pair("*", J2MEKeyCodes.KEY_STAR), Pair("0", J2MEKeyCodes.KEY_NUM0), Pair("#", J2MEKeyCodes.KEY_POUND)
    )

    keypadGrid.forEachIndexed { index, pair ->
        val label = pair.first
        val code = pair.second
        val col = index % 3
        val row = index / 3
        val x = 0.25f + col * 0.25f
        val y = 0.42f + row * 0.14f
        buttons.add(
            VirtualButtonItem(
                id = "numpad_$label",
                label = label,
                j2meKeyCode = code,
                xRatio = x,
                yRatio = y,
                sizeDp = 52,
                isCircle = false,
                primaryColorHex = if (label == "5") 0xFFFF9100 else 0xFF00E5FF
            )
        )
    }
    return buttons
}
