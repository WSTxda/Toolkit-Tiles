package com.wstxda.toolkit.manager.system

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.wstxda.toolkit.permissions.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ScreenshotManager(context: Context) {

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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

    fun cleanup() {
        appContext.contentResolver.unregisterContentObserver(accessibilityObserver)
        managerScope.cancel()
    }

    private fun checkPermission() {
        _isPermissionGranted.value = permissionManager.isAccessibilityServiceEnabled()
    }
}