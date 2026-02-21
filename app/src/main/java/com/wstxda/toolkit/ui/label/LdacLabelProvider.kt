package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.ldac.LdacState

class LdacLabelProvider(private val context: Context) {

    fun getLabel(): String {
        return context.getString(R.string.ldac_tile)
    }

    fun getSubtitle(state: LdacState, hasPermission: Boolean, isConnected: Boolean): String {
        if (!hasPermission) {
            return context.getString(R.string.tile_setup)
        }

        if (!isConnected) {
            return context.getString(R.string.ldac_not_connected)
        }

        return when (state) {
            LdacState.ADAPTIVE -> context.getString(R.string.ldac_quality_adaptive)
            LdacState.CONNECTION -> context.getString(R.string.ldac_quality_330)
            LdacState.BALANCED -> context.getString(R.string.ldac_quality_660)
            LdacState.QUALITY -> context.getString(R.string.ldac_quality_990)
        }
    }
}