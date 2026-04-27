package com.wstxda.toolkit.manager.dns

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object DnsModule {

    private val holder = SingletonHolder(::DnsManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}