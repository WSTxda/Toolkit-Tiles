package com.wstxda.toolkit.manager.power

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.core.content.edit
import com.wstxda.toolkit.tiles.power.CaffeineTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CaffeineManager(context: Context) {

    companion object {
        private const val PREF_NAME = "caffeine_prefs"
        private const val PREF_KEY_ORIGINAL = "original_timeout"
        private const val PREF_KEY_EXPECTED = "expected_timeout"
        private const val DEFAULT_TIMEOUT = 60000
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _currentState = MutableStateFlow<CaffeineState>(CaffeineState.Off)
    val currentState = _currentState.asStateFlow()
    private var isReceiverRegistered = false

    private val stateCycle = listOf(
        CaffeineState.Off,
        CaffeineState.FiveMinutes,
        CaffeineState.TenMinutes,
        CaffeineState.ThirtyMinutes,
        CaffeineState.OneHour,
        CaffeineState.Infinite
    )

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                managerScope.launch {
                    restoreOriginalTimeout()
                }
            }
        }
    }

    fun synchronizeState() {
        managerScope.launch {
            val prefs = getPrefs()
            val expectedTimeout = prefs.getInt(PREF_KEY_EXPECTED, -1)

            if (expectedTimeout != -1) {
                val systemTimeout = getSystemTimeout()

                if (systemTimeout != expectedTimeout) {
                    forceReset()
                } else {
                    val restoredState =
                        stateCycle.find { it.timeout == expectedTimeout } ?: CaffeineState.Off
                    _currentState.value = restoredState
                    if (restoredState != CaffeineState.Off) {
                        toggleReceiver(true)
                    }
                }
            } else {
                _currentState.value = CaffeineState.Off
                toggleReceiver(false)
            }
        }
    }

    fun isPermissionGranted(): Boolean {
        return Settings.System.canWrite(appContext)
    }

    fun cycleState() {
        managerScope.launch {
            if (!isPermissionGranted()) return@launch

            val currentIndex = stateCycle.indexOf(_currentState.value)
            val nextIndex = (currentIndex + 1) % stateCycle.size
            val nextState = stateCycle[nextIndex]

            applyState(nextState)
        }
    }

    private fun applyState(newState: CaffeineState) {
        if (newState == CaffeineState.Off) {
            restoreOriginalTimeout()
        } else {
            if (_currentState.value == CaffeineState.Off) {
                saveOriginalTimeout()
            }

            if (setSystemTimeout(newState.timeout)) {
                _currentState.value = newState
                getPrefs().edit { putInt(PREF_KEY_EXPECTED, newState.timeout) }
                toggleReceiver(true)
            } else {
                forceReset()
            }
        }
        requestTileUpdate()
    }

    private fun saveOriginalTimeout() {
        val current = getSystemTimeout()
        if (stateCycle.any { it.timeout == current && it != CaffeineState.Off }) {
            return
        }
        getPrefs().edit { putInt(PREF_KEY_ORIGINAL, current) }
    }

    private fun restoreOriginalTimeout() {
        val original = getPrefs().getInt(PREF_KEY_ORIGINAL, DEFAULT_TIMEOUT)
        setSystemTimeout(original)
        forceReset()
    }

    private fun forceReset() {
        _currentState.value = CaffeineState.Off
        getPrefs().edit {
            remove(PREF_KEY_EXPECTED)
        }
        toggleReceiver(false)
        requestTileUpdate()
    }

    private fun toggleReceiver(enable: Boolean) {
        if (enable && !isReceiverRegistered) {
            appContext.registerReceiver(
                screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF)
            )
            isReceiverRegistered = true
        } else if (!enable && isReceiverRegistered) {
            try {
                appContext.unregisterReceiver(screenOffReceiver)
            } catch (_: IllegalArgumentException) {
            }
            isReceiverRegistered = false
        }
    }

    private fun getSystemTimeout(): Int {
        return try {
            Settings.System.getInt(appContext.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (_: Exception) {
            DEFAULT_TIMEOUT
        }
    }

    private fun setSystemTimeout(timeout: Int): Boolean {
        return try {
            Settings.System.putInt(
                appContext.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun requestTileUpdate() {
        TileService.requestListeningState(
            appContext, ComponentName(appContext, CaffeineTileService::class.java)
        )
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}