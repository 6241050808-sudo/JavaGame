package com.example.engine

object J2MEKeyCodes {
    const val KEY_NUM0 = 48
    const val KEY_NUM1 = 49
    const val KEY_NUM2 = 50
    const val KEY_NUM3 = 51
    const val KEY_NUM4 = 52
    const val KEY_NUM5 = 53
    const val KEY_NUM6 = 54
    const val KEY_NUM7 = 55
    const val KEY_NUM8 = 56
    const val KEY_NUM9 = 57

    const val KEY_STAR = 42
    const val KEY_POUND = 35

    const val KEY_UP = -1
    const val KEY_DOWN = -2
    const val KEY_LEFT = -3
    const val KEY_RIGHT = -4
    const val KEY_FIRE = -5
    const val KEY_SOFT_LEFT = -6
    const val KEY_SOFT_RIGHT = -7
    const val KEY_CLEAR = -8
    const val KEY_SEND = -10
    const val KEY_END = -11

    fun getKeyName(code: Int): String = when (code) {
        KEY_NUM0 -> "0"
        KEY_NUM1 -> "1"
        KEY_NUM2 -> "2 (ขึ้น)"
        KEY_NUM3 -> "3"
        KEY_NUM4 -> "4 (ซ้าย)"
        KEY_NUM5 -> "5 (ยิง/OK)"
        KEY_NUM6 -> "6 (ขวา)"
        KEY_NUM7 -> "7"
        KEY_NUM8 -> "8 (ลง)"
        KEY_NUM9 -> "9"
        KEY_STAR -> "*"
        KEY_POUND -> "#"
        KEY_UP -> "ทิศทางขึ้น (Up)"
        KEY_DOWN -> "ทิศทางลง (Down)"
        KEY_LEFT -> "ทิศทางซ้าย (Left)"
        KEY_RIGHT -> "ทิศทางขวา (Right)"
        KEY_FIRE -> "ไฟร์/ตกลง (Fire/Select)"
        KEY_SOFT_LEFT -> "ซอฟต์คีย์ซ้าย (Soft 1)"
        KEY_SOFT_RIGHT -> "ซอฟต์คีย์ขวา (Soft 2)"
        KEY_CLEAR -> "ล้าง (Clear)"
        KEY_SEND -> "โทรออก (Send)"
        KEY_END -> "วางสาย (End)"
        else -> "Key $code"
    }
}
