package com.wstxda.toolkit.manager.caffeine

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.core.content.edit
import com.wstxda.toolkit.permissions.PermissionManager
import com.wstxda.toolkit.tiles.caffeine.CaffeineTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CaffeineManager(context: Context) {

    companion object {
        private const val PREF_NAME = "caffeine_prefs"
        private const val PREF_KEY_ORIGINAL_SCREEN = "original_timeout_screen"
        private const val PREF_KEY_ORIGINAL_SLEEP = "original_timeout_sleep"
        private const val PREF_KEY_EXPECTED_CLAMP = "expected_clamp_timeout"
        private const val PREF_KEY_EXPECTED_STATE_IDX = "expected_state_index"
        private const val DEFAULT_TIMEOUT = 60_000
        private const val SLEEP_TIMEOUT_KEY = "sleep_timeout"
        private const val ASYNC_OEM_CLAMP_CHECK_MS = 1_500L
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val stateMutex = Mutex()
    private val permissionManager = PermissionManager(appContext)
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
                    stateMutex.withLock { restoreOriginalTimeouts() }
                }
            }
        }
    }

    fun synchronizeState() = managerScope.launch {
        stateMutex.withLock {
            val prefs = getPrefs()
            val expectedClampedTimeout = prefs.getInt(PREF_KEY_EXPECTED_CLAMP, -1)

            if (expectedClampedTimeout != -1) {
                val systemTimeout = getSystemTimeout()

                if (systemTimeout != expectedClampedTimeout) {
                    forceReset()
                } else {
                    val stateIndex = prefs.getInt(PREF_KEY_EXPECTED_STATE_IDX, 0)
                    val restoredState = stateCycle.getOrNull(stateIndex) ?: CaffeineState.Off

                    _currentState.value = restoredState
                    if (restoredState != CaffeineState.Off) toggleReceiver(true)
                }
            } else {
                _currentState.value = CaffeineState.Off
                toggleReceiver(false)
            }
        }
    }

    fun isPermissionGranted(): Boolean = permissionManager.hasWriteSettingsPermission()

    fun cycleState() = managerScope.launch {
        if (!isPermissionGranted()) return@launch

        stateMutex.withLock {
            val currentIndex = stateCycle.indexOf(_currentState.value)
            val nextState = stateCycle[(currentIndex + 1) % stateCycle.size]
            applyState(nextState)
        }
    }

    private fun applyState(newState: CaffeineState) {
        if (newState == CaffeineState.Off) {
            restoreOriginalTimeouts()
            return
        }

        if (!getPrefs().contains(PREF_KEY_ORIGINAL_SCREEN)) {
            saveOriginalTimeouts()
        }

        val systemSuccess = setSystemTimeout(newState.timeout)

        if (systemSuccess && newState == CaffeineState.Infinite) {
            setSleepTimeout(-1)
        }

        if (systemSuccess) {
            val realObtainedTimeout = getSystemTimeout()
            val visualIndexState = stateCycle.indexOf(newState)

            getPrefs().edit(commit = true) {
                putInt(PREF_KEY_EXPECTED_CLAMP, realObtainedTimeout)
                putInt(PREF_KEY_EXPECTED_STATE_IDX, visualIndexState)
            }

            _currentState.value = newState
            toggleReceiver(true)
            scheduleOemWatcherCheck(newState)
        } else {
            forceReset()
        }

        requestTileUpdate()
    }

    private fun scheduleOemWatcherCheck(expectedState: CaffeineState) = managerScope.launch {
        delay(ASYNC_OEM_CLAMP_CHECK_MS)
        stateMutex.withLock {
            if (_currentState.value != expectedState) return@withLock

            val cachedClamped = getPrefs().getInt(PREF_KEY_EXPECTED_CLAMP, -1)
            val actualLateTimeout = getSystemTimeout()

            if (cachedClamped != -1 && cachedClamped != actualLateTimeout) {
                forceReset()
            }
        }
    }

    private fun saveOriginalTimeouts() {
        getPrefs().edit(commit = true) {
            putInt(PREF_KEY_ORIGINAL_SCREEN, getSystemTimeout())
            putInt(PREF_KEY_ORIGINAL_SLEEP, getSleepTimeout())
        }
    }

    private fun restoreOriginalTimeouts() {
        val prefs = getPrefs()
        val originalScreen = prefs.getInt(PREF_KEY_ORIGINAL_SCREEN, DEFAULT_TIMEOUT)
        setSystemTimeout(originalScreen)

        if (prefs.contains(PREF_KEY_ORIGINAL_SLEEP)) {
            val originalSleep = prefs.getInt(PREF_KEY_ORIGINAL_SLEEP, -1)
            setSleepTimeout(originalSleep)
        }

        forceReset()
    }

    private fun forceReset() {
        _currentState.value = CaffeineState.Off
        getPrefs().edit { clear() }
        toggleReceiver(false)
        requestTileUpdate()
    }

    private fun toggleReceiver(enable: Boolean) {
        if (enable && !isReceiverRegistered) {
            appContext.registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))
            isReceiverRegistered = true
        } else if (!enable && isReceiverRegistered) {
            runCatching { appContext.unregisterReceiver(screenOffReceiver) }
            isReceiverRegistered = false
        }
    }

    private fun getSystemTimeout(): Int = try {
        Settings.System.getInt(appContext.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
    } catch (_: Exception) {
        DEFAULT_TIMEOUT
    }

    private fun setSystemTimeout(timeout: Int): Boolean = try {
        Settings.System.putInt(
            appContext.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout
        )
        true
    } catch (_: Exception) {
        false
    }

    private fun getSleepTimeout(): Int = try {
        Settings.Secure.getInt(appContext.contentResolver, SLEEP_TIMEOUT_KEY)
    } catch (_: Exception) {
        -1
    }

    private fun setSleepTimeout(timeout: Int): Boolean = try {
        Settings.Secure.putInt(appContext.contentResolver, SLEEP_TIMEOUT_KEY, timeout)
        true
    } catch (_: Exception) {
        false
    }

    private fun requestTileUpdate() {
        TileService.requestListeningState(
            appContext, ComponentName(appContext, CaffeineTileService::class.java)
        )
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}