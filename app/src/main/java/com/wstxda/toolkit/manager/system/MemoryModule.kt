package com.wstxda.toolkit.manager.system

import android.content.Context

object MemoryModule {
    @Volatile
    private var instance: MemoryManager? = null

    fun getInstance(context: Context): MemoryManager {
        return instance ?: synchronized(this) {
            instance ?: MemoryManager(context.applicationContext).also { instance = it }
        }
    }
}