package com.wstxda.toolkit.tiles.sensors

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sensors.LevelManager
import com.wstxda.toolkit.manager.sensors.LevelModule
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.LevelIconProvider
import com.wstxda.toolkit.ui.label.LevelLabelProvider
import kotlinx.coroutines.flow.Flow

private val START_FOREGROUND_IMMEDIATELY =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class LevelTileService : BaseTileService() {

    private val levelModule by lazy { LevelModule.getInstance(applicationContext) }
    private val levelLabelProvider by lazy { LevelLabelProvider(applicationContext) }
    private val levelIconProvider by lazy { LevelIconProvider(applicationContext) }

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
        levelModule.resume()

        if (levelModule.isEnabled.value) {
            startLevelService()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        levelModule.pause()

        if (levelModule.isEnabled.value) {
            stopLevelService(fullyRemove = false)
        }
    }

    override fun onClick() {
        if (!LevelManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        levelModule.toggle()

        if (levelModule.isEnabled.value) {
            startLevelService()
        } else {
            stopLevelService(fullyRemove = true)
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(levelModule.isEnabled, levelModule.degrees, levelModule.orientation)
    }

    private fun startLevelService() {
        try {
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }
        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                levelModule.forceStop()
            } else {
                throw e
            }
        }
    }

    private fun stopLevelService(fullyRemove: Boolean) {
        if (!START_FOREGROUND_IMMEDIATELY) {
            val flags = if (fullyRemove) STOP_FOREGROUND_REMOVE else STOP_FOREGROUND_DETACH
            stopForeground(flags)
        }
    }

    override fun updateTile() {
        val isActive = levelModule.isEnabled.value
        val degrees = levelModule.degrees.value
        val orient = levelModule.orientation.value

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = levelLabelProvider.getLabel(isActive, degrees),
            subtitle = levelLabelProvider.getSubtitle(isActive),
            icon = levelIconProvider.getIcon(isActive, degrees, orient)
        )
    }
}