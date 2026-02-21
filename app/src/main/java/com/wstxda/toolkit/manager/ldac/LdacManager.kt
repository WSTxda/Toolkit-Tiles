package com.wstxda.toolkit.manager.ldac

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.quicksettings.TileService
import com.wstxda.toolkit.tiles.ldac.LdacTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LdacManager(context: Context) {

    companion object {
        const val SETTING_KEY = "bluetooth_audio_ldac_codec_playback_quality"
        const val DEFAULT_VALUE = 1003 // Best Effort
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _currentState = MutableStateFlow<LdacState>(LdacState.ADAPTIVE)
    val currentState = _currentState.asStateFlow()
    private var isObserverRegistered = false

    private val stateCycle = LdacState.entries

    private val settingsObserver = object : android.database.ContentObserver(null) {
        override fun onChange(selfChange: Boolean) {
            synchronizeState()
        }
    }

    fun synchronizeState() {
        managerScope.launch {
            val systemValue = getSystemValue()
            _currentState.value = stateCycle.find { it.value == systemValue } ?: LdacState.ADAPTIVE
            toggleObserver(true)
        }
    }

    fun isPermissionGranted(): Boolean {
        return appContext.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun cycleState(): Boolean {
        if (!isPermissionGranted()) return false

        val currentIndex = stateCycle.indexOf(_currentState.value)
        val nextIndex = (currentIndex + 1) % stateCycle.size
        val nextState = stateCycle[nextIndex]

        val result = setSystemValue(nextState.value)
        if (result) {
            requestTileUpdate()
        }
        return result
    }

    private fun getSystemValue(): Int {
        return try {
            Settings.Global.getInt(appContext.contentResolver, SETTING_KEY, DEFAULT_VALUE)
        } catch (_: Exception) {
            DEFAULT_VALUE
        }
    }

    private fun setSystemValue(value: Int): Boolean {
        return try {
            Settings.Global.putInt(appContext.contentResolver, SETTING_KEY, value)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun toggleObserver(enable: Boolean) {
        if (enable && !isObserverRegistered) {
            appContext.contentResolver.registerContentObserver(
                Settings.Global.getUriFor(SETTING_KEY),
                false,
                settingsObserver
            )
            isObserverRegistered = true
        } else if (!enable && isObserverRegistered) {
            try {
                appContext.contentResolver.unregisterContentObserver(settingsObserver)
            } catch (_: IllegalArgumentException) {
            }
            isObserverRegistered = false
        }
    }

    private fun requestTileUpdate() {
        TileService.requestListeningState(
            appContext, ComponentName(appContext, LdacTileService::class.java)
        )
    }
}