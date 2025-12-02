package com.wstxda.toolkit.manager.sensors

import android.content.Context

object LuxMeterModule {
    @Volatile
    private var instance: LuxMeterManager? = null

    fun getInstance(context: Context): LuxMeterManager {
        return instance ?: synchronized(this) {
            instance ?: LuxMeterManager(context.applicationContext).also { instance = it }
        }
    }
}