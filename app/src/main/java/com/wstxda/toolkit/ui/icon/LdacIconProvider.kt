package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.ldac.LdacState

class LdacIconProvider(private val context: Context) {

    fun getIcon(state: LdacState, isConnected: Boolean): Icon {
        if (!isConnected) {
            return Icon.createWithResource(context, R.drawable.ic_ldac)
        }

        val resId = when (state) {
            LdacState.ADAPTIVE -> R.drawable.ic_ldac_adaptive
            LdacState.CONNECTION -> R.drawable.ic_ldac_330
            LdacState.BALANCED -> R.drawable.ic_ldac_660
            LdacState.QUALITY -> R.drawable.ic_ldac_990
        }
        return Icon.createWithResource(context, resId)
    }
}