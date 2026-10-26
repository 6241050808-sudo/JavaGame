package com.example.data.model

enum class FrameInterpolationMode(val label: String, val description: String) {
    OFF("ปิด (Off)", "ความเร็วเฟรมเรตแท้เดิมแบบเรโทร (Native J2ME FPS)"),
    BLEND_50("ผสมเฟรม (Frame Blending)", "ผสมผสานสองเฟรมต่อเนื่องเพื่อลดภาพกระตุก"),
    MOTION_ADAPTIVE_60("แทรกเฟรม 60 FPS (Motion 60Hz)", "ประมวลผลแทรกเฟรมกลางอัตโนมัติให้ภาพเคลื่อนไหวลื่นไหล"),
    ULTRA_SMOOTH_120("แทรกเฟรม 120 FPS (Smooth 120Hz)", "ความลื่นไหลระดับสูงสุด เหมาะกับหน้าจอ Refresh Rate สูง")
}

enum class ResolutionEnhancer(val label: String, val description: String) {
    NEAREST_NEIGHBOR("พิกเซลแท้ (Pixel Perfect)", "พิกเซลคมชัดดั้งเดิม ไม่เบลอ"),
    SCALE2X_EPX("Scale2x / EPX (ลดรอยหยักพิกเซล)", "อัลกอริทึมเพิ่มความละเอียดภาพพิกเซลให้เนียนคม ไร้รอยหยักบันได"),
    BILINEAR_SMOOTH("Bilinear Smooth (นุ่มนวล)", "เกลี่ยสีนุ่มนวล ลดความแข็งของขอบภาพ"),
    CRT_SCANLINES("จอแก้วเรโทร (CRT Scanlines)", "เส้นสแกนไลน์และเอฟเฟกต์จอแก้วย้อนยุคสุดคลาสสิก"),
    LCD_SUBPIXEL_GRID("ตารางหน้าจอ LCD (Nokia Grid)", "จำลองเมทริกซ์ซับพิกเซลของหน้าจอมือถือยุค 2000s"),
    SHARPEN_HIGHRES("ซูเปอร์ชาร์ป (Adaptive Sharp)", "เพิ่มความคมชัดและเร่งความลึกของรายละเอียดกราฟิก")
}

enum class ScreenAspectRatio(val label: String) {
    FIT_RATIO("คงอัตราส่วนเดิม (Original Ratio)"),
    RATIO_4_3("อัตราส่วน 4:3"),
    RATIO_1_1("สี่เหลี่ยมจัตุรัส 1:1"),
    FULLSCREEN_STRETCH("เต็มจอ (Stretch)")
}

enum class BezelFrameSkin(val label: String) {
    NONE("ไม่มีกรอบ (Modern Frameless)"),
    NOKIA_3310("โนเกียคลาสสิก (Nokia 3310 Retro)"),
    NOKIA_N95("โนเกีย N-Series (Cyber Silver)"),
    SONY_ERICSSON("โซนี่ อีริคสัน (Walkman Black)")
}

data class EmulationConfig(
    val frameInterpolation: FrameInterpolationMode = FrameInterpolationMode.MOTION_ADAPTIVE_60,
    val resolutionEnhancer: ResolutionEnhancer = ResolutionEnhancer.SCALE2X_EPX,
    val aspectRatio: ScreenAspectRatio = ScreenAspectRatio.FIT_RATIO,
    val bezelSkin: BezelFrameSkin = BezelFrameSkin.NONE,
    val targetFps: Int = 60,
    val speedMultiplier: Float = 1.0f,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 0.8f,
    val showFpsCounter: Boolean = true,
    val enableHaptics: Boolean = true,
    val hapticIntensity: Int = 2 // 1: Light, 2: Medium, 3: Strong
)
