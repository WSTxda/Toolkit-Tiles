package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R

class UsbDebuggingLabelProvider(private val context: Context) {

    fun getLabel(): CharSequence {
        return context.getString(R.string.usb_debugging_tile)
    }

    fun getSubtitle(
        isActive: Boolean, hasPermission: Boolean, isDevOptionsEnabled: Boolean
    ): CharSequence {
        if (!isDevOptionsEnabled) {
            return context.getString(R.string.tile_unavailable)
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