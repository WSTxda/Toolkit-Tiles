package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.battery.BatteryChargingSource
import com.wstxda.toolkit.manager.battery.BatteryInfo

class BatteryIconProvider(private val context: Context) {

    fun getIcon(info: BatteryInfo): Icon {
        val resId = when {
            info.isPowerSave -> R.drawable.ic_battery_saver

            info.isCharging -> when (info.chargingSource) {
                BatteryChargingSource.AC -> R.drawable.ic_battery_charging_ac
                BatteryChargingSource.USB -> R.drawable.ic_battery_charging_usb
                BatteryChargingSource.WIRELESS -> R.drawable.ic_battery_charging_wireless
                BatteryChargingSource.DOCK -> R.drawable.ic_battery_charging_dock
                BatteryChargingSource.NONE -> R.drawable.ic_battery_charging_none
            }

            info.isFull || info.level >= 100 -> R.drawable.ic_battery_full
            info.level >= 75 -> R.drawable.ic_battery_75
            info.level >= 50 -> R.drawable.ic_battery_50
            info.level >= 25 -> R.drawable.ic_battery_25
            info.level >= 10 -> R.drawable.ic_battery_10
            else -> R.drawable.ic_battery_low
        }
        return Icon.createWithResource(context, resId)
    }
}