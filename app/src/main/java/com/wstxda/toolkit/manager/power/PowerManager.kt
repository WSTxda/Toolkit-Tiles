package com.wstxda.toolkit.manager.power

import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.wstxda.toolkit.permissions.PermissionManager
import com.wstxda.toolkit.services.accessibility.TileAccessibilityAction
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PowerManager(context: Context) {

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted = _isPermissionGranted.asStateFlow()

    private val accessibilityObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            checkPermission()
        }
    }

    init {
        appContext.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),
            false,
            accessibilityObserver
        )
        checkPermission()
    }

    fun powerMenu() {
        if (!_isPermissionGranted.value) return

        val intent = Intent(appContext, TileAccessibilityService::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                putExtra(
                    TileAccessibilityService.ACTION_KEY,
                    TileAccessibilityAction.POWER_DIALOG
                )
            }
        }
        appContext.startService(intent)
    }

    fun cleanup() {
        appContext.contentResolver.unregisterContentObserver(accessibilityObserver)
    }

    private fun checkPermission() {
        _isPermissionGranted.value = permissionManager.isAccessibilityServiceEnabled()
    }
}