package com.wstxda.toolkit.tiles.compass

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.compass.CompassManager
import com.wstxda.toolkit.manager.compass.CompassModule
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.CompassIconProvider
import com.wstxda.toolkit.ui.label.CompassLabelProvider
import kotlinx.coroutines.flow.Flow

private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class CompassTileService : BaseTileService() {

    private val compassModule by lazy { CompassModule.getInstance(applicationContext) }
    private val compassLabelProvider by lazy { CompassLabelProvider(applicationContext) }
    private val compassIconProvider by lazy { CompassIconProvider(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(
            channel()
        )
    }

    override fun onStartListening() {
        super.onStartListening()
        compassModule.resume()

        if (compassModule.isEnabled.value) {
            startCompassService()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        compassModule.pause()
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onClick() {
        if (!CompassManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        compassModule.toggle()
        if (compassModule.isEnabled.value) {
            startCompassService()
        } else {
            stopCompassService()
        }
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(compassModule.isEnabled, compassModule.currentDegrees)
    }

    private fun startCompassService() {
        try {
            startForegroundCompat(NOTIFICATION_ID, notification())
        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                compassModule.forceStop()
            } else {
                throw e
            }
        }
    }

    private fun stopCompassService() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun updateTile() {
        val isEnabled = compassModule.isEnabled.value
        val degrees = compassModule.currentDegrees.value

        setTileState(
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = compassLabelProvider.getLabel(isEnabled, degrees),
            subtitle = compassLabelProvider.getSubtitle(isEnabled),
            icon = compassIconProvider.getIcon(isEnabled, degrees)
        )
    }
}