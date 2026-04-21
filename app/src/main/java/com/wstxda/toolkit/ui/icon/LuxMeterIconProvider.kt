package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R

class LuxMeterIconProvider(private val context: Context) {

    fun getIcon(): Icon {
        return Icon.createWithResource(context, R.drawable.ic_lux_meter)
    }
}