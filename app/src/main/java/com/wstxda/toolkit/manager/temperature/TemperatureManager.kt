package com.wstxda.toolkit.manager.temperature

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TemperatureManager(context: Context) {

    private val appContext = context.applicationContext

    private val _temperature = MutableStateFlow(0f)
    val temperature = _temperature.asStateFlow()

    private var isListening = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                _temperature.value = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
            }
        }
    }

    fun setListening(listening: Boolean) {
        if (isListening == listening) return
        isListening = listening

        if (listening) {
            val sticky = appContext.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            val initialTemp = sticky?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            _temperature.value = initialTemp / 10f

            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    appContext.registerReceiver(
                        batteryReceiver,
                        IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                        Context.RECEIVER_NOT_EXPORTED
                    )
                } else {
                    appContext.registerReceiver(
                        batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                    )
                }
            }
        } else {
            runCatching { appContext.unregisterReceiver(batteryReceiver) }
        }
    }
}