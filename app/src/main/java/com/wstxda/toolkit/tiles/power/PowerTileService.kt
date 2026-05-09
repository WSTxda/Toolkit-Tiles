package com.wstxda.toolkit.tiles.power

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.power.PowerModule
import com.wstxda.toolkit.ui.icon.PowerIconProvider
import com.wstxda.toolkit.ui.label.PowerLabelProvider
import kotlinx.coroutines.flow.Flow

class PowerTileService : BaseTileService() {

    private val powerManager by lazy { PowerModule.getInstance(applicationContext) }
    private val labelProvider by lazy { PowerLabelProvider(applicationContext) }
    private val iconProvider by lazy { PowerIconProvider(applicationContext) }

    override fun onClick() {
        if (!powerManager.isPermissionGranted.value) {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
            return
        }
        powerManager.powerMenu()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        powerManager.isPermissionGranted,
    )

    override fun updateTile() {
        val hasPermission = powerManager.isPermissionGranted.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon(),
        )
    }
}