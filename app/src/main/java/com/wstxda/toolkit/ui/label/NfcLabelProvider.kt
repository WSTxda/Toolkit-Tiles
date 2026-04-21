package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R

class NfcLabelProvider(private val context: Context) {

    fun getLabel(): CharSequence {
        return context.getString(R.string.nfc_tile)
    }

    fun getSubtitle(isActive: Boolean, hasPermission: Boolean, hasHardware: Boolean): CharSequence {
        if (!hasHardware) {
            return context.getString(R.string.tile_not_supported)
        }

        if (!hasPermission) {
            return context.getString(R.string.tile_setup)
        }

        if (isActive) {
            return context.getString(R.string.tile_on)
        }

        return context.getString(R.string.tile_off)
    }
}