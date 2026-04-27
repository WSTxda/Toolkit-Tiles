package com.wstxda.toolkit.manager.diceroll

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object DiceRollModule {

    private val holder = SingletonHolder(::DiceRollManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}