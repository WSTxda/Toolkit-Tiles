package com.wstxda.toolkit.tiles.temperature

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.temperature.TemperatureModule
import com.wstxda.toolkit.ui.icon.TemperatureIconProvider
import com.wstxda.toolkit.ui.label.TemperatureLabelProvider
import kotlinx.coroutines.flow.Flow

class TemperatureTileService : BaseTileService() {

    private val temperatureManager by lazy { TemperatureModule.getInstance(applicationContext) }
    private val temperatureLabelProvider by lazy { TemperatureLabelProvider(applicationContext) }
    private val temperatureIconProvider by lazy { TemperatureIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        temperatureManager.setListening(true)
    }

    override fun onStopListening() {
        super.onStopListening()
        temperatureManager.setListening(false)
    }

    override fun onClick() {
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(temperatureManager.temperature)
    }

    override fun updateTile() {
        val temp = temperatureManager.temperature.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = temperatureLabelProvider.getLabel(temp),
            subtitle = temperatureLabelProvider.getSubtitle(),
            icon = temperatureIconProvider.getIcon()
        )
    }
}