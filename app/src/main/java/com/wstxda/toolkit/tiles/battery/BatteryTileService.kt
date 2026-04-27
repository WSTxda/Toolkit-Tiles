package com.wstxda.toolkit.tiles.battery

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.battery.BatteryModule
import com.wstxda.toolkit.ui.icon.BatteryIconProvider
import com.wstxda.toolkit.ui.label.BatteryLabelProvider
import kotlinx.coroutines.flow.Flow

class BatteryTileService : BaseTileService() {

    private val batteryManager by lazy { BatteryModule.getInstance(applicationContext) }
    private val labelProvider by lazy { BatteryLabelProvider(applicationContext) }
    private val iconProvider by lazy { BatteryIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        batteryManager.setListening(true)
    }

    override fun onStopListening() {
        super.onStopListening()
        batteryManager.setListening(false)
    }

    override fun onClick() {
        batteryManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        batteryManager.batteryInfo,
        batteryManager.displayState,
    )

    override fun updateTile() {
        val batteryInfo = batteryManager.batteryInfo.value
        val displayState = batteryManager.displayState.value

        setTileState(
            state = if (batteryInfo.isCharging) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(batteryInfo, displayState),
            subtitle = labelProvider.getSubtitle(batteryInfo, displayState),
            icon = iconProvider.getIcon(batteryInfo),
        )
    }
}