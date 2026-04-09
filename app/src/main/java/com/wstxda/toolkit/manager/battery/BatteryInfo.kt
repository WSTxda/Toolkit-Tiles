package com.wstxda.toolkit.manager.battery

import kotlin.math.abs

data class BatteryInfo(
    val level: Int = 0,
    val voltageMv: Int = 0,
    val currentMa: Int = 0,
    val temperatureTenths: Int = 0,
    val healthCode: Int = android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN,
    val isCharging: Boolean = false,
    val isFull: Boolean = false,
    val isPowerSave: Boolean = false,
    val chargingSource: BatteryChargingSource = BatteryChargingSource.NONE,
) {
    val voltageV: Float get() = voltageMv / 1000f
    val temperatureC: Float get() = temperatureTenths / 10f
    val isLow: Boolean get() = !isCharging && level < 25
    val signedCurrentMa: Int get() = if (isCharging) abs(currentMa) else -abs(currentMa)
    val wattageW: Float get() = voltageV * (abs(currentMa) / 1000f)
}