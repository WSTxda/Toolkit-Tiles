package com.wstxda.toolkit.tiles.usbdebugging

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSecureSettingsActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.usbdebugging.UsbDebuggingManager
import com.wstxda.toolkit.ui.icon.UsbDebuggingIconProvider
import com.wstxda.toolkit.ui.label.UsbDebuggingLabelProvider
import kotlinx.coroutines.flow.Flow

class UsbDebuggingTileService : BaseTileService() {

    private val usbDebuggingManager by lazy { UsbDebuggingManager(applicationContext) }
    private val labelProvider by lazy { UsbDebuggingLabelProvider(applicationContext) }
    private val iconProvider by lazy { UsbDebuggingIconProvider(applicationContext) }

    override fun onStartListening() {
        usbDebuggingManager.start()
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        usbDebuggingManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        usbDebuggingManager.cleanup()
    }

    override fun onClick() {
        if (!usbDebuggingManager.hasPermission()) {
            startActivityAndCollapse(WriteSecureSettingsActivity::class.java)
            return
        }
        if (!usbDebuggingManager.isDeveloperOptionsEnabled.value) return

        usbDebuggingManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        usbDebuggingManager.isEnabled,
        usbDebuggingManager.isDeveloperOptionsEnabled,
    )

    override fun updateTile() {
        val hasPermission = usbDebuggingManager.hasPermission()
        val isEnabled = usbDebuggingManager.isEnabled.value
        val isDeveloperOptionsEnabled = usbDebuggingManager.isDeveloperOptionsEnabled.value

        setTileState(
            state = when {
                !isDeveloperOptionsEnabled -> Tile.STATE_UNAVAILABLE
                !hasPermission -> Tile.STATE_INACTIVE
                isEnabled -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(
                isEnabled, hasPermission, isDeveloperOptionsEnabled
            ),
            icon = iconProvider.getIcon(isEnabled),
        )
    }
}