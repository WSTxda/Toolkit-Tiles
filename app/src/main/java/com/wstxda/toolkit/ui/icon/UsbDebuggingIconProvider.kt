package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R

class UsbDebuggingIconProvider(private val context: Context) {

    fun getIcon(isActive: Boolean): Icon {
        val iconRes = if (isActive) {
            R.drawable.ic_usb_on
        } else {
            R.drawable.ic_usb_off
        }
        return Icon.createWithResource(context, iconRes)
    }
}