package com.wstxda.toolkit.base

import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.CallSuper
import com.wstxda.toolkit.R
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat

abstract class BaseForegroundSensorTileService : BaseTileService() {

    private val requiresClickToStartForeground =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    protected abstract fun isSensorSupported(): Boolean
    protected abstract fun isSensorEnabled(): Boolean
    protected abstract fun resumeSensor()
    protected abstract fun pauseSensor()
    protected abstract fun toggleSensor()
    protected abstract fun onForceStop()

    protected open fun onSensorNotSupported() {
        Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
    }

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel())
    }

    @CallSuper
    override fun onStartListening() {
        resumeSensor()
        if (isSensorEnabled() && !requiresClickToStartForeground) startForeground()
        super.onStartListening()
    }

    @CallSuper
    override fun onStopListening() {
        super.onStopListening()
        pauseSensor()
    }

    @CallSuper
    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    final override fun onClick() {
        if (!isSensorSupported()) {
            onSensorNotSupported()
            return
        }
        toggleSensor()
        if (isSensorEnabled()) startForeground() else stopForeground()
        updateTile()
    }

    private fun startForeground() {
        try {
            startForegroundCompat(NOTIFICATION_ID, notification())
        } catch (e: Exception) {
            if (e is SecurityException) onForceStop() else throw e
        }
    }

    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}