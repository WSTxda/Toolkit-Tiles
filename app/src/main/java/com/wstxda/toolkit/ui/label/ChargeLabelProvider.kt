package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.charge.ChargeState

class ChargeLabelProvider(private val context: Context) {

    fun getLabel(state: ChargeState): CharSequence {
        return when (state) {
            ChargeState.INACTIVE -> context.getString(R.string.charge_protection_tile)
            ChargeState.STANDBY -> context.getString(R.string.charge_protection_tile)
            ChargeState.ARMED -> context.getString(R.string.charge_protection_tile)
            ChargeState.TRIGGERED -> context.getString(R.string.charge_protection_tile)
        }
    }

    fun getSubtitle(state: ChargeState): CharSequence? {
        return when (state) {
            ChargeState.INACTIVE -> context.getString(R.string.tile_off)
            ChargeState.STANDBY -> context.getString(R.string.charge_protection_tile)
            ChargeState.ARMED -> context.getString(R.string.charge_protection_tile)
            ChargeState.TRIGGERED -> null
        }
    }
}