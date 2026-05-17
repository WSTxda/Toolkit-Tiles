package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.rotation.RotationMode

class RotationLabelProvider(private val context: Context) {

    fun getLabel(mode: RotationMode, hasPermission: Boolean): CharSequence {
        if (!hasPermission) return context.getString(R.string.rotation_tile)

        return when (mode) {
            RotationMode.AUTO -> context.getString(R.string.rotation_tile_auto)
            RotationMode.PORTRAIT -> context.getString(R.string.rotation_tile_portrait)
            RotationMode.LANDSCAPE -> context.getString(R.string.rotation_tile_landscape)
            RotationMode.REVERSE_PORTRAIT -> context.getString(R.string.rotation_tile_reverse_portrait)
            RotationMode.REVERSE_LANDSCAPE -> context.getString(R.string.rotation_tile_reverse_landscape)
        }
    }

    fun getSubtitle(hasPermission: Boolean): CharSequence {
        return if (!hasPermission) {
            context.getString(R.string.tile_setup)
        } else {
            context.getString(R.string.tile_switch)
        }
    }
}