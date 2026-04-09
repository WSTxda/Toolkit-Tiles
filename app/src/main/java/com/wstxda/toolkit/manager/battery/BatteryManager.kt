package com.wstxda.toolkit.manager.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager as AndroidBatteryManager
import android.os.PowerManager
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

class BatteryManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "battery_prefs"
        private const val KEY_DISPLAY_STATE = "display_state"
        private const val REFRESH_RATE_CHARGING_MS = 2000L
        private const val REFRESH_RATE_DISCHARGING_MS = 4000L
    }

    private val appContext = context.applicationContext
    private val androidBatteryManager =
        appContext.getSystemService(Context.BATTERY_SERVICE) as AndroidBatteryManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var pollingJob: Job? = null
    private var isPanelOpen = false

    private val _batteryInfo = MutableStateFlow(BatteryInfo())
    val batteryInfo = _batteryInfo.asStateFlow()

    private val _displayState = MutableStateFlow(loadDisplayState())
    val displayState = _displayState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                _batteryInfo.value = readBatteryInfo(intent)
            }
        }
    }

    private fun registerReceiver() {
        runCatching {
            appContext.registerReceiver(
                batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
        }
    }

    private fun unregisterReceiver() {
        runCatching { appContext.unregisterReceiver(batteryReceiver) }
    }

    fun setListening(listening: Boolean) {
        if (isPanelOpen == listening) return
        isPanelOpen = listening

        if (listening) {
            val sticky = appContext.registerReceiver(
                null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )
            if (sticky != null) _batteryInfo.value = readBatteryInfo(sticky)

            registerReceiver()
            startPolling()
        } else {
            stopPolling()
            unregisterReceiver()
        }
    }

    fun toggle() {
        val next = _displayState.value.next()
        _displayState.value = next
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putString(KEY_DISPLAY_STATE, next.name)
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = managerScope.launch {
            while (isActive) {
                val info = _batteryInfo.value
                _batteryInfo.value = info.copy(
                    currentMa = readCurrentMa(), isPowerSave = powerManager.isPowerSaveMode
                )

                val delayTime =
                    if (info.isCharging) REFRESH_RATE_CHARGING_MS else REFRESH_RATE_DISCHARGING_MS
                delay(delayTime)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun readBatteryInfo(intent: Intent): BatteryInfo {
        val level = intent.getIntExtra(AndroidBatteryManager.EXTRA_LEVEL, 0)
        val scale = intent.getIntExtra(AndroidBatteryManager.EXTRA_SCALE, 100)
        val percent = if (scale > 0) (level * 100 / scale) else level

        val status = intent.getIntExtra(AndroidBatteryManager.EXTRA_STATUS, -1)
        val isCharging =
            status == AndroidBatteryManager.BATTERY_STATUS_CHARGING || status == AndroidBatteryManager.BATTERY_STATUS_FULL
        val isFull = status == AndroidBatteryManager.BATTERY_STATUS_FULL || percent >= 100

        val plugged = intent.getIntExtra(AndroidBatteryManager.EXTRA_PLUGGED, 0)
        val source = when (plugged) {
            AndroidBatteryManager.BATTERY_PLUGGED_AC -> BatteryChargingSource.AC
            AndroidBatteryManager.BATTERY_PLUGGED_USB -> BatteryChargingSource.USB
            AndroidBatteryManager.BATTERY_PLUGGED_WIRELESS -> BatteryChargingSource.WIRELESS
            8 /* BATTERY_PLUGGED_DOCK — API 33 */ -> BatteryChargingSource.DOCK
            else -> BatteryChargingSource.NONE
        }

        return BatteryInfo(
            level = percent,
            voltageMv = intent.getIntExtra(AndroidBatteryManager.EXTRA_VOLTAGE, 0),
            currentMa = _batteryInfo.value.currentMa,
            temperatureTenths = intent.getIntExtra(AndroidBatteryManager.EXTRA_TEMPERATURE, 0),
            healthCode = intent.getIntExtra(
                AndroidBatteryManager.EXTRA_HEALTH, AndroidBatteryManager.BATTERY_HEALTH_UNKNOWN
            ),
            isCharging = isCharging,
            isFull = isFull,
            isPowerSave = powerManager.isPowerSaveMode,
            chargingSource = source,
        )
    }

    private fun readCurrentMa(): Int = runCatching {
        val uA = androidBatteryManager.getIntProperty(
            AndroidBatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        )
        uA / 1000
    }.getOrDefault(0)

    private fun loadDisplayState(): BatteryDisplayState {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_DISPLAY_STATE, BatteryDisplayState.PERCENTAGE.name)
        return runCatching { BatteryDisplayState.valueOf(name!!) }.getOrDefault(
            BatteryDisplayState.PERCENTAGE
        )
    }
}