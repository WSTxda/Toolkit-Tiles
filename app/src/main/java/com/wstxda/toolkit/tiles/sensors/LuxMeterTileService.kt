package com.wstxda.toolkit.tiles.sensors

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sensors.LuxMeterManager
import com.wstxda.toolkit.manager.sensors.LuxMeterModule
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.LuxMeterIconProvider
import com.wstxda.toolkit.ui.label.LuxMeterLabelProvider
import kotlinx.coroutines.flow.Flow

private val START_FOREGROUND_IMMEDIATELY =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class LuxMeterTileService : BaseTileService() {

    private val luxMeterManager by lazy { LuxMeterModule.getInstance(applicationContext) }
    private val luxMeterLabelProvider by lazy { LuxMeterLabelProvider(applicationContext) }
    private val luxMeterIconProvider by lazy { LuxMeterIconProvider(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        getSystemService(android.app.NotificationManager::class.java)?.createNotificationChannel(
            channel()
        )

        if (START_FOREGROUND_IMMEDIATELY) {
            startForegroundCompat(NOTIFICATION_ID, notification())
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        luxMeterManager.resume()

        if (luxMeterManager.isEnabled.value) {
            startLuxService()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        luxMeterManager.pause()

        if (luxMeterManager.isEnabled.value) {
            stopLuxService(fullyRemove = false)
        }
    }

    override fun onClick() {
        if (!LuxMeterManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        luxMeterManager.toggle()

        if (luxMeterManager.isEnabled.value) {
            startLuxService()
        } else {
            stopLuxService(fullyRemove = true)
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(luxMeterManager.isEnabled, luxMeterManager.lux)
    }

    private fun startLuxService() {
        try {
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }
        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                luxMeterManager.forceStop()
            } else {
                throw e
            }
        }
    }

    private fun stopLuxService(fullyRemove: Boolean) {
        if (!START_FOREGROUND_IMMEDIATELY) {
            val flags = if (fullyRemove) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH
            stopForeground(flags)
        }
    }

    override fun updateTile() {
        val isActive = luxMeterManager.isEnabled.value
        val lux = luxMeterManager.lux.value

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = luxMeterLabelProvider.getLabel(isActive, lux),
            subtitle = luxMeterLabelProvider.getSubtitle(isActive),
            icon = luxMeterIconProvider.getIcon(isActive)
        )
    }
}