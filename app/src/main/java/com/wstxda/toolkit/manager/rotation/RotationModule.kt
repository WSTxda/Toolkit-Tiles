package com.wstxda.toolkit.manager.rotation

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object RotationModule {

    private val holder = SingletonHolder(::RotationManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}