package com.wstxda.toolkit.manager.charge

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.wstxda.toolkit.manager.lock.LockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChargeProtectionManager(context: Context) {

    private val appContext = context.applicationContext
    private val lockManager = LockManager(appContext)

    private val _currentState = MutableStateFlow(ChargeState.INACTIVE)
    val currentState = _currentState.asStateFlow()

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> handlePowerConnected()
                Intent.ACTION_POWER_DISCONNECTED -> handlePowerDisconnected()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        appContext.registerReceiver(powerReceiver, filter)
    }

    fun toggle() {
        if (!lockManager.isPermissionGranted.value || !hasNotificationPermission()) {
            return
        }

        val isCharging = isDeviceCharging()

        when (_currentState.value) {
            ChargeState.INACTIVE -> {
                if (isCharging) {
                    _currentState.value = ChargeState.ARMED
                } else {
                    _currentState.value = ChargeState.STANDBY
                }
            }
            ChargeState.STANDBY, ChargeState.ARMED -> {
                _currentState.value = ChargeState.INACTIVE
            }
            ChargeState.TRIGGERED -> {
            }
        }
    }

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true

        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun resetState() {
        _currentState.value = ChargeState.INACTIVE
    }

    fun hasPermission(): Boolean = lockManager.isPermissionGranted.value

    private fun handlePowerConnected() {
        if (_currentState.value == ChargeState.STANDBY) {
            _currentState.value = ChargeState.ARMED
        }
    }

    private fun handlePowerDisconnected() {
        if (_currentState.value == ChargeState.ARMED) {
            triggerAlarm()
        }
    }

    private fun triggerAlarm() {
        _currentState.value = ChargeState.TRIGGERED

        lockManager.lockScreen()

        val intent = Intent(appContext, ChargeAlarmService::class.java)
        appContext.startForegroundService(intent)
    }

    private fun isDeviceCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            appContext.registerReceiver(null, filter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }
}