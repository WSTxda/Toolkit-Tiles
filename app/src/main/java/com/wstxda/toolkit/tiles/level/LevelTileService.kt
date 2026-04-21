package com.wstxda.toolkit.tiles.level

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseForegroundSensorTileService
import com.wstxda.toolkit.manager.level.LevelManager
import com.wstxda.toolkit.manager.level.LevelModule
import com.wstxda.toolkit.ui.icon.LevelIconProvider
import com.wstxda.toolkit.ui.label.LevelLabelProvider
import kotlinx.coroutines.flow.Flow

class LevelTileService : BaseForegroundSensorTileService() {

    private val levelManager by lazy { LevelModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LevelLabelProvider(applicationContext) }
    private val iconProvider by lazy { LevelIconProvider(applicationContext) }

    override fun isSensorSupported(): Boolean = LevelManager.isSupported(this)
    override fun isSensorEnabled(): Boolean = levelManager.isEnabled.value
    override fun resumeSensor() = levelManager.resume()
    override fun pauseSensor() = levelManager.pause()
    override fun toggleSensor() = levelManager.toggle()
    override fun onForceStop() = levelManager.forceStop()

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        levelManager.isEnabled,
        levelManager.degrees,
        levelManager.orientation,
    )

    override fun updateTile() {
        val isEnabled = levelManager.isEnabled.value
        val degrees = levelManager.degrees.value
        val orientation = levelManager.orientation.value

        setTileState(
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(isEnabled, degrees),
            subtitle = labelProvider.getSubtitle(isEnabled),
            icon = iconProvider.getIcon(isEnabled, degrees, orientation),
        )
    }
}