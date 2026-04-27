package com.wstxda.toolkit.manager.soundmode

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object SoundModeModule {

    private val holder = SingletonHolder(::SoundModeManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}