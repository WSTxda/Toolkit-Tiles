package com.wstxda.toolkit.manager.power

import android.content.Context

object CaffeineModule {
    @Volatile
    private var instance: CaffeineManager? = null

    fun getInstance(context: Context): CaffeineManager {
        return instance ?: synchronized(this) {
            instance ?: CaffeineManager(context.applicationContext).also { instance = it }
        }
    }
}