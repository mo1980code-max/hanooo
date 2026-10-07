package com.example.clockstudio.util

data class BatteryInfo(
    val percent: Int,
    val isCharging: Boolean,
)

fun batteryInfoFrom(level: Int, scale: Int, status: Int): BatteryInfo? {
    if (level < 0 || scale <= 0) return null
    val percent = ((level * 100f) / scale).toInt().coerceIn(0, 100)
    return BatteryInfo(
        percent = percent,
        isCharging = status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
            status == android.os.BatteryManager.BATTERY_STATUS_FULL,
    )
}
