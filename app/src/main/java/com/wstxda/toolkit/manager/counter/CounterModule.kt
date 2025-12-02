package com.wstxda.toolkit.manager.counter

import android.content.Context

object CounterModule {
    @Volatile
    private var instance: CounterManager? = null

    fun getInstance(context: Context): CounterManager {
        return instance ?: synchronized(this) {
            instance ?: CounterManager(context.applicationContext).also { instance = it }
        }
    }
}