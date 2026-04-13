package com.wstxda.toolkit.tiles.brightness

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.brightness.AutoBrightnessManager
import com.wstxda.toolkit.ui.icon.AutoBrightnessIconProvider
import com.wstxda.toolkit.ui.label.AutoBrightnessLabelProvider
import kotlinx.coroutines.flow.Flow

class AutoBrightnessTileService : BaseTileService() {

    private val brightnessManager by lazy { AutoBrightnessManager(applicationContext) }
    private val labelProvider by lazy { AutoBrightnessLabelProvider(applicationContext) }
    private val iconProvider by lazy { AutoBrightnessIconProvider(applicationContext) }

    override fun onStartListening() {
        brightnessManager.start()
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        brightnessManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        brightnessManager.cleanup()
    }

    override fun onClick() {
        if (!brightnessManager.isPermissionGranted()) {
            startActivityAndCollapse(WriteSettingsPermissionActivity::class.java)
            return
        }
        brightnessManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        brightnessManager.isEnabled,
    )

    override fun updateTile() {
        val isEnabled = brightnessManager.isEnabled.value
        val hasPermission = brightnessManager.isPermissionGranted()

        setTileState(
            state = if (isEnabled && hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(isEnabled, hasPermission),
            icon = iconProvider.getIcon(isEnabled),
        )
    }
}