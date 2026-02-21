package com.wstxda.toolkit.manager.ldac

import android.content.Context

object LdacModule {
    @Volatile
    private var instance: LdacManager? = null

    fun getInstance(context: Context): LdacManager {
        return instance ?: synchronized(this) {
            instance ?: LdacManager(context.applicationContext).also { instance = it }
        }
    }
}