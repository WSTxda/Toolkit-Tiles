package com.wstxda.toolkit.manager.usbdebugging

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsbDebuggingManager(context: Context) {

    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver
    private val permissionManager = PermissionManager(appContext)
    private val _isEnabled = MutableStateFlow(getCurrentState())
    val isEnabled = _isEnabled.asStateFlow()

    private val _isDeveloperOptionsEnabled = MutableStateFlow(isDeveloperOptionsEnabledInternal())
    val isDeveloperOptionsEnabled = _isDeveloperOptionsEnabled.asStateFlow()

    private var isListening = false

    private val adbUri = Settings.Global.getUriFor(Settings.Global.ADB_ENABLED)
    private val devOptionsUri =
        Settings.Global.getUriFor(Settings.Global.DEVELOPMENT_SETTINGS_ENABLED)

    private val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            when (uri) {
                adbUri -> syncStateWithSystem()
                devOptionsUri -> syncDeveloperOptionsState()
            }
        }
    }

    private fun getCurrentState(): Boolean {
        return try {
            Settings.Global.getInt(contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }
    }

    private fun isDeveloperOptionsEnabledInternal(): Boolean {
        return try {
            Settings.Global.getInt(
                contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
            ) == 1
        } catch (_: Exception) {
            false
        }
    }

    private fun syncStateWithSystem() {
        val systemState = getCurrentState()
        if (_isEnabled.value != systemState) {
            _isEnabled.value = systemState
        }
    }

    private fun syncDeveloperOptionsState() {
        val systemState = isDeveloperOptionsEnabledInternal()
        if (_isDeveloperOptionsEnabled.value != systemState) {
            _isDeveloperOptionsEnabled.value = systemState
            if (!systemState && _isEnabled.value) {
                applyState(false)
            }
        }
    }

    fun start() {
        if (isListening) return
        syncStateWithSystem()
        syncDeveloperOptionsState()
        contentResolver.registerContentObserver(adbUri, false, settingsObserver)
        contentResolver.registerContentObserver(devOptionsUri, false, settingsObserver)
        isListening = true
    }

    fun stop() {
        if (!isListening) return
        contentResolver.unregisterContentObserver(settingsObserver)
        isListening = false
    }

    fun cleanup() {
        stop()
    }

    fun hasPermission(): Boolean = permissionManager.hasWriteSecureSettingsPermission()

    fun toggle() {
        if (!hasPermission()) return

        if (!_isDeveloperOptionsEnabled.value) {
            applyState(false)
            return
        }

        val newState = !_isEnabled.value
        applyState(newState)
    }

    private fun applyState(enabled: Boolean): Boolean {
        return try {
            Settings.Global.putInt(
                contentResolver, Settings.Global.ADB_ENABLED, if (enabled) 1 else 0
            )
            _isEnabled.value = enabled
            true
        } catch (_: Exception) {
            syncStateWithSystem()
            false
        }
    }
}