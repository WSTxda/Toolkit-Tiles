package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.charge.ChargeState

class ChargeIconProvider(private val context: Context) {

    fun getIcon(state: ChargeState): Icon {
        val resId = when (state) {
            ChargeState.INACTIVE -> R.drawable.ic_charge_protection_off
            ChargeState.STANDBY -> R.drawable.ic_charge_protection_permission
            ChargeState.ARMED -> R.drawable.ic_charge_protection_on
            ChargeState.TRIGGERED -> R.drawable.ic_charge_protection_alert
        }
        return Icon.createWithResource(context, resId)
    }
}