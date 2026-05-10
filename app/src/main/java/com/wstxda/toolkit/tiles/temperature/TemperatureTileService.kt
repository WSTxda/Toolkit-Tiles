package com.wstxda.toolkit.tiles.temperature

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.temperature.TemperatureModule
import com.wstxda.toolkit.ui.icon.TemperatureIconProvider
import com.wstxda.toolkit.ui.label.TemperatureLabelProvider
import kotlinx.coroutines.flow.Flow

class TemperatureTileService : BaseTileService() {

    private val temperatureManager by lazy { TemperatureModule.getInstance(applicationContext) }
    private val labelProvider by lazy { TemperatureLabelProvider(applicationContext) }
    private val iconProvider by lazy { TemperatureIconProvider(applicationContext) }

    override fun onStartListening() {
        temperatureManager.setListening(true)
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        temperatureManager.setListening(false)
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        temperatureManager.temperature,
    )

    override fun updateTile() {
        val temperature = temperatureManager.temperature.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(temperature),
            subtitle = labelProvider.getSubtitle(),
            icon = iconProvider.getIcon(),
        )
    }
}