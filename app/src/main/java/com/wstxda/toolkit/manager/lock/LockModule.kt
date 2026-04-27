package com.wstxda.toolkit.manager.lock

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object LockModule {

    private val holder = SingletonHolder(::LockManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}