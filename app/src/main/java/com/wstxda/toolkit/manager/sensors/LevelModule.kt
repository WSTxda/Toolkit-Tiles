package com.wstxda.toolkit.manager.sensors

import android.content.Context

object LevelModule {
    @Volatile
    private var instance: LevelManager? = null

    fun getInstance(context: Context): LevelManager {
        return instance ?: synchronized(this) {
            instance ?: LevelManager(context.applicationContext).also { instance = it }
        }
    }
}