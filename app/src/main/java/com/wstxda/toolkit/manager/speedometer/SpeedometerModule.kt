package com.wstxda.toolkit.manager.speedometer

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object SpeedometerModule {

    private val holder = SingletonHolder(::SpeedometerManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}
