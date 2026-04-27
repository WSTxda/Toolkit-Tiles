package com.wstxda.toolkit.tiles.compass

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseForegroundTileService
import com.wstxda.toolkit.manager.compass.CompassManager
import com.wstxda.toolkit.manager.compass.CompassModule
import com.wstxda.toolkit.ui.icon.CompassIconProvider
import com.wstxda.toolkit.ui.label.CompassLabelProvider
import kotlinx.coroutines.flow.Flow

class CompassTileService : BaseForegroundTileService() {

    private val compassManager by lazy { CompassModule.getInstance(applicationContext) }
    private val labelProvider by lazy { CompassLabelProvider(applicationContext) }
    private val iconProvider by lazy { CompassIconProvider(applicationContext) }

    override fun isFeatureSupported(): Boolean = CompassManager.isSupported(this)
    override fun isFeatureEnabled(): Boolean = compassManager.isEnabled.value
    override fun resumeFeature() = compassManager.resume()
    override fun pauseFeature() = compassManager.pause()
    override fun toggleFeature() = compassManager.toggle()

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        compassManager.isEnabled,
        compassManager.currentDegrees,
    )

    override fun updateTile() {
        val isEnabled = compassManager.isEnabled.value
        val degrees = compassManager.currentDegrees.value

        setTileState(
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(isEnabled, degrees),
            subtitle = labelProvider.getSubtitle(isEnabled),
            icon = iconProvider.getIcon(isEnabled, degrees),
        )
    }
}