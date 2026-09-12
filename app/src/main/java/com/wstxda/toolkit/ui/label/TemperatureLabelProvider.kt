package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.temperature.TemperatureUnit
import java.util.Locale

class TemperatureLabelProvider(private val context: Context) {

    fun getLabel(tempC: Float, unit: TemperatureUnit): CharSequence {
        return when (unit) {
            TemperatureUnit.CELSIUS -> String.format(
                Locale.US,
                context.getString(R.string.temperature_tile_celsius),
                tempC
            )

            TemperatureUnit.FAHRENHEIT -> String.format(
                Locale.US,
                context.getString(R.string.temperature_tile_fahrenheit),
                celsiusToFahrenheit(tempC)
            )
        }
    }

    fun getSubtitle(): CharSequence {
        return context.getString(R.string.temperature_tile)
    }

    private fun celsiusToFahrenheit(celsius: Float): Float = (celsius * 1.8f) + 32f
}
