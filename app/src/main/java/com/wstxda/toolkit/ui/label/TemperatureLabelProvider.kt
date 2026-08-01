package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import java.util.Locale

class TemperatureLabelProvider(private val context: Context) {

    fun getLabel(temp: Float, isFahrenheit: Boolean): CharSequence {
        val format = if (isFahrenheit) {
            R.string.temperature_tile_format_f
        } else {
            R.string.temperature_tile_format_c
        }
        val value = if (isFahrenheit) (temp * 9 / 5) + 32 else temp
        return String.format(Locale.US, context.getString(format), value)
    }

    fun getSubtitle(): CharSequence {
        return context.getString(R.string.temperature_tile)
    }
}