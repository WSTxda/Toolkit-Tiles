package com.wstxda.toolkit.manager.rotation

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Surface
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RotationManager(context: Context) {

    private val appContext = context.applicationContext
    private val contentResolver = appContext.contentResolver
    private val permissionManager = PermissionManager(appContext)

    private val _currentMode = MutableStateFlow(getCurrentModeInternal())
    val currentMode = _currentMode.asStateFlow()

    private var isListening = false

    private val accelerometerUri = Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION)
    private val userRotationUri = Settings.System.getUriFor(Settings.System.USER_ROTATION)

    private val settingsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (uri == null || uri == accelerometerUri || uri == userRotationUri) {
                syncStateWithSystem()
            }
        }
    }

    private fun getCurrentModeInternal(): RotationMode {
        return try {
            val isAuto = Settings.System.getInt(
                contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1
            ) == 1

            if (isAuto) return RotationMode.AUTO

            when (Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, 0)) {
                Surface.ROTATION_0 -> RotationMode.PORTRAIT
                Surface.ROTATION_90 -> RotationMode.LANDSCAPE
                Surface.ROTATION_180 -> RotationMode.REVERSE_PORTRAIT
                Surface.ROTATION_270 -> RotationMode.REVERSE_LANDSCAPE
                else -> RotationMode.AUTO
            }
        } catch (_: Exception) {
            RotationMode.AUTO
        }
    }

    private fun syncStateWithSystem() {
        val systemMode = getCurrentModeInternal()
        if (_currentMode.value != systemMode) {
            _currentMode.value = systemMode
        }
    }

    fun start() {
        if (isListening) return
        syncStateWithSystem()
        contentResolver.registerContentObserver(accelerometerUri, false, settingsObserver)
        contentResolver.registerContentObserver(userRotationUri, false, settingsObserver)
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

    fun hasPermission(): Boolean = permissionManager.hasWriteSettingsPermission()

    fun cycleMode() {
        if (!hasPermission()) return
        val nextMode = _currentMode.value.next()
        applyMode(nextMode)
    }

    private fun applyMode(mode: RotationMode) {
        try {
            when (mode) {
                RotationMode.AUTO -> {
                    Settings.System.putInt(
                        contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1
                    )
                }

                else -> {
                    Settings.System.putInt(
                        contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0
                    )
                    Settings.System.putInt(
                        contentResolver, Settings.System.USER_ROTATION, mode.rotation
                    )
                }
            }
            _currentMode.value = mode
        } catch (_: Exception) {
            syncStateWithSystem()
        }
    }
}