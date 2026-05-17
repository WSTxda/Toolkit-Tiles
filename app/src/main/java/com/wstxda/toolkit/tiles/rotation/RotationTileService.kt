package com.wstxda.toolkit.tiles.rotation

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.rotation.RotationMode
import com.wstxda.toolkit.manager.rotation.RotationModule
import com.wstxda.toolkit.ui.icon.RotationIconProvider
import com.wstxda.toolkit.ui.label.RotationLabelProvider
import kotlinx.coroutines.flow.Flow

class RotationTileService : BaseTileService() {

    private val rotationManager by lazy { RotationModule.getInstance(applicationContext) }
    private val labelProvider by lazy { RotationLabelProvider(applicationContext) }
    private val iconProvider by lazy { RotationIconProvider(applicationContext) }

    override fun onStartListening() {
        rotationManager.start()
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        rotationManager.stop()
    }

    override fun onClick() {
        if (!rotationManager.hasPermission()) {
            startActivityAndCollapse(WriteSettingsPermissionActivity::class.java)
            return
        }
        rotationManager.cycleMode()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        rotationManager.currentMode,
    )

    override fun updateTile() {
        val hasPermission = rotationManager.hasPermission()
        val currentMode = rotationManager.currentMode.value

        setTileState(
            state = if (currentMode != RotationMode.AUTO && hasPermission) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            },
            label = labelProvider.getLabel(currentMode, hasPermission),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon(currentMode),
        )
    }
}