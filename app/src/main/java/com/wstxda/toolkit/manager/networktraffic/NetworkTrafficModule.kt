package com.wstxda.toolkit.manager.networktraffic

import android.content.Context

object NetworkTrafficModule {
    @Volatile
    private var instance: NetworkTrafficManager? = null

    fun getInstance(context: Context): NetworkTrafficManager {
        return instance ?: synchronized(this) {
            instance ?: NetworkTrafficManager(context.applicationContext).also { instance = it }
        }
    }
}