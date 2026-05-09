package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R

class PowerLabelProvider(private val context: Context) {

    fun getLabel(): CharSequence {
        return context.getString(R.string.power_tile)
    }

    fun getSubtitle(isPermissionGranted: Boolean): CharSequence {
        return if (isPermissionGranted) {
            context.getString(R.string.tile_open)
        } else {
            context.getString(R.string.tile_setup)
        }
    }
}