package com.wstxda.toolkit.manager.power

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object PowerModule {

    private val holder = SingletonHolder(::PowerManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}