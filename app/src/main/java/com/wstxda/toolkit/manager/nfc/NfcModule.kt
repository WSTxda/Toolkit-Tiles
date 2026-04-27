package com.wstxda.toolkit.manager.nfc

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object NfcModule {

    private val holder = SingletonHolder(::NfcManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}