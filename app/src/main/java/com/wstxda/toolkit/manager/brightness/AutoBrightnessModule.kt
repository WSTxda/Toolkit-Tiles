package com.wstxda.toolkit.manager.brightness

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object AutoBrightnessModule {

    private val holder = SingletonHolder(::AutoBrightnessManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}