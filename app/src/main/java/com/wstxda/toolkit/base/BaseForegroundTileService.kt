package com.wstxda.toolkit.base

import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.CallSuper
import com.wstxda.toolkit.R
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat


abstract class BaseForegroundTileService : BaseTileService() {

    private val notificationId: Int by lazy { javaClass.name.hashCode() }

    private val startForegroundImmediately =
        Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    private val canOnlyStartForegroundOnClick =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

    private val canStartForegroundFromLifecycle =
        !startForegroundImmediately && !canOnlyStartForegroundOnClick

    protected abstract fun isFeatureSupported(): Boolean
    protected abstract fun isFeatureEnabled(): Boolean
    protected abstract fun resumeFeature()
    protected abstract fun pauseFeature()
    protected abstract fun toggleFeature()

    protected open fun onFeatureNotSupported() {
        Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel())
        if (startForegroundImmediately) {
            startForegroundSafely()
        }
    }

    @CallSuper
    override fun onStartListening() {
        resumeFeature()
        if (isFeatureEnabled() && canStartForegroundFromLifecycle) {
            startForegroundSafely()
        }
        super.onStartListening()
    }

    @CallSuper
    override fun onStopListening() {
        pauseFeature()
        if (!startForegroundImmediately) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        super.onStopListening()
    }

    @CallSuper
    override fun onDestroy() {
        pauseFeature()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    final override fun onClick() {
        if (!isFeatureSupported()) {
            onFeatureNotSupported()
            return
        }
        toggleFeature()
        if (isFeatureEnabled()) {
            startForegroundSafely()
        } else {
            if (!startForegroundImmediately) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }
        updateTile()
    }

    private fun startForegroundSafely() {
        try {
            startForegroundCompat(notificationId, notification())
        } catch (e: Exception) {
            when {
                e is SecurityException -> Unit
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e is IllegalStateException -> Unit
                else -> throw e
            }
        }
    }
}