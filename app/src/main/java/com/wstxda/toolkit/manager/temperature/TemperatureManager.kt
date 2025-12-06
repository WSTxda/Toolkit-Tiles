package com.wstxda.toolkit.manager.temperature

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TemperatureManager(context: Context) {

    companion object {
        private const val REFRESH_RATE_MS = 1000L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _temperature = MutableStateFlow(0f)
    val temperature = _temperature.asStateFlow()
    private var pollingJob: Job? = null

    fun setListening(listening: Boolean) {
        if (listening) {
            startPolling()
        } else {
            stopPolling()
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = managerScope.launch {
            updateData()
            while (isActive) {
                delay(REFRESH_RATE_MS)
                updateData()
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun updateData() {
        val intent = appContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tempInt = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        _temperature.value = tempInt / 10f
    }
}