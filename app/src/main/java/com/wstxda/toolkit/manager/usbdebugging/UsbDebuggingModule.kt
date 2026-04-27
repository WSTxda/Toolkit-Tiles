package com.wstxda.toolkit.manager.usbdebugging

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object UsbDebuggingModule {

    private val holder = SingletonHolder(::UsbDebuggingManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}