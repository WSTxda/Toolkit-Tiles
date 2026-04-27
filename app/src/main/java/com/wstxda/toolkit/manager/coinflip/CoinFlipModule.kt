package com.wstxda.toolkit.manager.coinflip

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object CoinFlipModule {

    private val holder = SingletonHolder(::CoinFlipManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}