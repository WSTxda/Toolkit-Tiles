package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import java.util.Locale

class TemperatureLabelProvider(private val context: Context) {

    fun getLabel(temp: Float): CharSequence {
        return String.format(Locale.US, context.getString(R.string.temperature_tile_format), temp)
    }

    fun getSubtitle(): CharSequence? {
        return context.getString(R.string.temperature_tile)
    }
}