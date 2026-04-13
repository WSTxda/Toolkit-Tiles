package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.speedometer.SpeedometerUnit
import kotlin.math.roundToInt

class SpeedometerLabelProvider(private val context: Context) {

    fun getLabel(unit: SpeedometerUnit, speedKmh: Float, hasPermission: Boolean): CharSequence {
        if (!hasPermission) return context.getString(R.string.speedometer_tile)
        return when (unit) {
            SpeedometerUnit.KMH -> context.getString(R.string.speedometer_tile_speed_kmh, speedKmh.roundToInt())
            SpeedometerUnit.MPH -> context.getString(R.string.speedometer_tile_speed_mph, (speedKmh * 0.621371f).roundToInt())
            SpeedometerUnit.DISABLED -> context.getString(R.string.speedometer_tile)
        }
    }

    fun getSubtitle(unit: SpeedometerUnit, hasPermission: Boolean): CharSequence {
        if (!hasPermission) return context.getString(R.string.tile_setup)
        return when (unit) {
            SpeedometerUnit.KMH -> context.getString(R.string.speedometer_unit_kmh)
            SpeedometerUnit.MPH -> context.getString(R.string.speedometer_unit_mph)
            SpeedometerUnit.DISABLED -> context.getString(R.string.tile_off)
        }
    }
}