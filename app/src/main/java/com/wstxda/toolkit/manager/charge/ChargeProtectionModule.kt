package com.wstxda.toolkit.manager.charge

import android.content.Context

object ChargeProtectionModule {
    @Volatile
    private var instance: ChargeProtectionManager? = null

    fun getInstance(context: Context): ChargeProtectionManager {
        return instance ?: synchronized(this) {
            instance ?: ChargeProtectionManager(context.applicationContext).also { instance = it }
        }
    }
}