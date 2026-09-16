package com.wstxda.toolkit.manager.ldac

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object LdacModule {
    private val holder = SingletonHolder(::LdacManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}
