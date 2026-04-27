package com.wstxda.toolkit.tiles.level

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseForegroundTileService
import com.wstxda.toolkit.manager.level.LevelManager
import com.wstxda.toolkit.manager.level.LevelModule
import com.wstxda.toolkit.ui.icon.LevelIconProvider
import com.wstxda.toolkit.ui.label.LevelLabelProvider
import kotlinx.coroutines.flow.Flow

class LevelTileService : BaseForegroundTileService() {

    private val levelManager by lazy { LevelModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LevelLabelProvider(applicationContext) }
    private val iconProvider by lazy { LevelIconProvider(applicationContext) }

    override fun isFeatureSupported(): Boolean = LevelManager.isSupported(this)
    override fun isFeatureEnabled(): Boolean = levelManager.isEnabled.value
    override fun resumeFeature() = levelManager.resume()
    override fun pauseFeature() = levelManager.pause()
    override fun toggleFeature() = levelManager.toggle()

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