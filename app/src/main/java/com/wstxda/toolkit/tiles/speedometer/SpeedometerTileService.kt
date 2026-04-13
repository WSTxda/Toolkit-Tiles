package com.wstxda.toolkit.tiles.speedometer

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.LocationPermissionActivity
import com.wstxda.toolkit.base.BaseForegroundSensorTileService
import com.wstxda.toolkit.manager.speedometer.SpeedometerModule
import com.wstxda.toolkit.permissions.PermissionManager
import com.wstxda.toolkit.ui.icon.SpeedometerIconProvider
import com.wstxda.toolkit.ui.label.SpeedometerLabelProvider
import kotlinx.coroutines.flow.Flow

class SpeedometerTileService : BaseForegroundSensorTileService() {

    private val speedometerManager by lazy { SpeedometerModule.getInstance(applicationContext) }
    private val labelProvider by lazy { SpeedometerLabelProvider(applicationContext) }
    private val iconProvider by lazy { SpeedometerIconProvider(applicationContext) }
    private val permissionManager by lazy { PermissionManager(applicationContext) }

    override val sampleIntervalMs: Long = 500L

    override fun isSensorSupported(): Boolean =
        permissionManager.hasLocationPermission() && permissionManager.isLocationEnabled()

    override fun isSensorEnabled(): Boolean = speedometerManager.isEnabled.value
    override fun resumeSensor() = speedometerManager.resume()
    override fun pauseSensor() = speedometerManager.pause()
    override fun toggleSensor() = speedometerManager.toggle()
    override fun onForceStop() = speedometerManager.forceStop()

    override fun onSensorNotSupported() {
        if (!permissionManager.hasLocationPermission()) {
            startActivityAndCollapse(LocationPermissionActivity::class.java)
            return
        }
        super.onSensorNotSupported()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        speedometerManager.isEnabled,
        speedometerManager.speedKmh,
        speedometerManager.unit,
    )

    override fun updateTile() {
        val hasPermission = permissionManager.hasLocationPermission()
        val isEnabled = speedometerManager.isEnabled.value
        val speedKmh = speedometerManager.speedKmh.value
        val unit = speedometerManager.unit.value

        setTileState(
            state = if (isEnabled && hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(unit, speedKmh, hasPermission),
            subtitle = labelProvider.getSubtitle(unit, hasPermission),
            icon = iconProvider.getIcon(),
        )
    }
}