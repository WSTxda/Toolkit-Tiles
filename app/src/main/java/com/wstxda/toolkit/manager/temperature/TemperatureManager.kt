package com.wstxda.toolkit.manager.temperature

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.edit
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
        private const val PREFS_NAME = "temperature_prefs"
        private const val KEY_UNIT_FAHRENHEIT = "unit_fahrenheit"
        private const val REFRESH_RATE_MS = 1000L
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _temperature = MutableStateFlow(0f)
    val temperature = _temperature.asStateFlow()

    private val _isFahrenheit = MutableStateFlow(prefs.getBoolean(KEY_UNIT_FAHRENHEIT, false))
    val isFahrenheit = _isFahrenheit.asStateFlow()

    private var pollingJob: Job? = null
    private var isPanelOpen = false

    fun setListening(listening: Boolean) {
        if (isPanelOpen == listening) return
        isPanelOpen = listening
        if (listening) {
            updateData()
            startPolling()
        } else {
            stopPolling()
        }
    }

    fun toggleUnit() {
        val nextValue = !_isFahrenheit.value
        _isFahrenheit.value = nextValue
        prefs.edit { putBoolean(KEY_UNIT_FAHRENHEIT, nextValue) }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = managerScope.launch {
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
        val intent = appContext.registerReceiver(
            null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val tempInt = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        _temperature.value = tempInt / 10f
    }
}