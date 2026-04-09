package com.wstxda.toolkit.ui.label

import android.content.Context
import android.os.BatteryManager as AndroidBatteryManager
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.battery.BatteryChargingSource
import com.wstxda.toolkit.manager.battery.BatteryDisplayState
import com.wstxda.toolkit.manager.battery.BatteryInfo

class BatteryLabelProvider(private val context: Context) {

    fun getLabel(info: BatteryInfo, state: BatteryDisplayState): CharSequence {
        if (info.level == 0 && info.voltageMv == 0) return context.getString(R.string.battery_tile)
        return when (state) {
            BatteryDisplayState.PERCENTAGE -> context.getString(
                R.string.battery_label_percent, info.level
            )

            BatteryDisplayState.CURRENT -> context.getString(
                R.string.battery_label_current, info.signedCurrentMa
            )

            BatteryDisplayState.VOLTAGE -> context.getString(
                R.string.battery_label_voltage, info.voltageV
            )

            BatteryDisplayState.WATTAGE -> context.getString(
                R.string.battery_label_wattage, info.wattageW
            )

            BatteryDisplayState.TEMPERATURE -> context.getString(
                R.string.battery_label_temperature, info.temperatureC
            )
        }
    }

    fun getSubtitle(info: BatteryInfo, state: BatteryDisplayState): CharSequence? {
        if (state == BatteryDisplayState.TEMPERATURE) return when (info.healthCode) {
            AndroidBatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.battery_health_good)
            AndroidBatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.battery_health_overheat)
            AndroidBatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.battery_health_dead)
            AndroidBatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.battery_health_overvoltage)
            AndroidBatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.battery_health_failure)
            AndroidBatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.battery_health_cold)
            else -> context.getString(R.string.battery_health_unknown)
        }
        return when {
            info.isFull -> context.getString(R.string.battery_status_full)
            info.isPowerSave -> context.getString(R.string.battery_status_saver)
            info.isCharging -> when (info.chargingSource) {
                BatteryChargingSource.AC -> context.getString(R.string.battery_charging_ac)
                BatteryChargingSource.USB -> context.getString(R.string.battery_charging_usb)
                BatteryChargingSource.WIRELESS -> context.getString(R.string.battery_charging_wireless)
                BatteryChargingSource.DOCK -> context.getString(R.string.battery_charging_dock)
                BatteryChargingSource.NONE -> context.getString(R.string.battery_charging)
            }

            info.isLow -> context.getString(R.string.battery_status_low)
            else -> context.getString(R.string.battery_status_discharging)
        }
    }
}