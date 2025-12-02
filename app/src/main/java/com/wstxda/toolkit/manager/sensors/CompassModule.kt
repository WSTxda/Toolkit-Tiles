package com.wstxda.toolkit.manager.sensors

import android.content.Context

object CompassModule {
    @Volatile
    private var instance: CompassManager? = null

    fun getInstance(context: Context): CompassManager {
        return instance ?: synchronized(this) {
            instance ?: CompassManager(context.applicationContext).also { instance = it }
        }
    }
}