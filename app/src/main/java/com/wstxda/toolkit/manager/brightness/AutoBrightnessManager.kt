package com.wstxda.toolkit.manager.brightness

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoBrightnessManager(context: Context) {

    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver
    private val permissionManager = PermissionManager(appContext)
    private val _isEnabled = MutableStateFlow(getCurrentSystemMode())
    val isEnabled = _isEnabled.asStateFlow()

    private var isListening = false
    private val brightnessUri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE)

    private val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri == null || uri == brightnessUri) {
                syncStateWithSystem()
            }
        }
    }

    private fun getCurrentSystemMode(): Boolean {
        return try {
            val mode = Settings.System.getInt(
                contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE
            )
            mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
        } catch (_: Exception) {
            false
        }
    }

    private fun syncStateWithSystem() {
        val systemState = getCurrentSystemMode()
        if (_isEnabled.value != systemState) {
            _isEnabled.value = systemState
        }
    }

    fun start() {
        if (isListening) return
        syncStateWithSystem()
        contentResolver.registerContentObserver(brightnessUri, false, settingsObserver)
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

    fun isPermissionGranted(): Boolean = permissionManager.hasWriteSettingsPermission()

    fun toggle() {
        if (!isPermissionGranted()) return
        val newState = !_isEnabled.value
        if (setSystemMode(newState)) {
            _isEnabled.value = newState
        }
    }

    private fun setSystemMode(enable: Boolean): Boolean {
        return try {
            val mode = if (enable) {
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            } else {
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            }
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, mode)
            true
        } catch (_: Exception) {
            false
        }
    }
}