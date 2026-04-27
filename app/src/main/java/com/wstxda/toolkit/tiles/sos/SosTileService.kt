package com.wstxda.toolkit.tiles.sos

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sos.SosModule
import com.wstxda.toolkit.ui.icon.SosIconProvider
import com.wstxda.toolkit.ui.label.SosLabelProvider
import kotlinx.coroutines.flow.Flow

class SosTileService : BaseTileService() {

    private val sosManager by lazy { SosModule.getInstance(applicationContext) }
    private val labelProvider by lazy { SosLabelProvider(applicationContext) }
    private val iconProvider by lazy { SosIconProvider(applicationContext) }

    override fun onClick() {
        if (!sosManager.hasFlashHardware()) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_SHORT).show()
            return
        }
        if (qsTile?.state == Tile.STATE_UNAVAILABLE) return

        sosManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        sosManager.isActive,
        sosManager.isFlashAvailable,
    )

    override fun updateTile() {
        val isActive = sosManager.isActive.value
        val isHardwareAvailable = sosManager.hasFlashHardware()
        val isSystemAvailable = sosManager.isFlashAvailable.value
        val isFullyAvailable = isHardwareAvailable && isSystemAvailable

        setTileState(
            state = when {
                !isFullyAvailable -> Tile.STATE_UNAVAILABLE
                isActive -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(isActive, isFullyAvailable),
            icon = iconProvider.getIcon(),
        )
    }
}