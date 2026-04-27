package com.wstxda.toolkit.manager.screenshot

import android.content.Context
import com.wstxda.toolkit.base.SingletonHolder

object ScreenshotModule {

    private val holder = SingletonHolder(::ScreenshotManager)

    fun getInstance(context: Context) = holder.getInstance(context)
}