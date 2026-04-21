package com.wstxda.toolkit.tiles.luxmeter

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseForegroundSensorTileService
import com.wstxda.toolkit.manager.luxmeter.LuxMeterManager
import com.wstxda.toolkit.manager.luxmeter.LuxMeterModule
import com.wstxda.toolkit.ui.icon.LuxMeterIconProvider
import com.wstxda.toolkit.ui.label.LuxMeterLabelProvider
import kotlinx.coroutines.flow.Flow

class LuxMeterTileService : BaseForegroundSensorTileService() {

    private val luxMeterManager by lazy { LuxMeterModule.getInstance(applicationContext) }
    private val labelProvider by lazy { LuxMeterLabelProvider(applicationContext) }
    private val iconProvider by lazy { LuxMeterIconProvider(applicationContext) }

    override val sampleIntervalMs: Long = 100L

    override fun isSensorSupported(): Boolean = LuxMeterManager.isSupported(this)
    override fun isSensorEnabled(): Boolean = luxMeterManager.isEnabled.value
    override fun resumeSensor() = luxMeterManager.resume()
    override fun pauseSensor() = luxMeterManager.pause()
    override fun toggleSensor() = luxMeterManager.toggle()
    override fun onForceStop() = luxMeterManager.forceStop()

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        luxMeterManager.isEnabled,
        luxMeterManager.lux,
    )

    override fun updateTile() {
        val isEnabled = luxMeterManager.isEnabled.value
        val lux = luxMeterManager.lux.value

        setTileState(
            state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(isEnabled, lux),
            subtitle = labelProvider.getSubtitle(isEnabled),
            icon = iconProvider.getIcon(),
        )
    }
}