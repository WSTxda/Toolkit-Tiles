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
import kotlin.time.Duration.Companion.milliseconds

class TemperatureManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "temperature_prefs"
        private const val KEY_UNIT = "unit"
        private const val REFRESH_RATE_MS = 1000L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _temperature = MutableStateFlow(0f)
    val temperature = _temperature.asStateFlow()

    private val _unit = MutableStateFlow(loadUnit())
    val unit = _unit.asStateFlow()

    private var pollingJob: Job? = null
    private var isPanelOpen = false

    fun toggleUnit() {
        val next = _unit.value.next()
        _unit.value = next
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_UNIT, next.name)
        }
    }

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

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = managerScope.launch {
            while (isActive) {
                delay(REFRESH_RATE_MS.milliseconds)
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

    private fun loadUnit(): TemperatureUnit {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_UNIT, TemperatureUnit.CELSIUS.name)
        return runCatching { TemperatureUnit.valueOf(savedName!!) }.getOrDefault(TemperatureUnit.CELSIUS)
    }
}
