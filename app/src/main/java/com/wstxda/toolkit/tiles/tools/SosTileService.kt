package com.wstxda.toolkit.tiles.tools

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.tools.SosManager
import com.wstxda.toolkit.ui.icon.SosIconProvider
import com.wstxda.toolkit.ui.label.SosLabelProvider
import kotlinx.coroutines.flow.Flow

class SosTileService : BaseTileService() {

    private val sosManager by lazy { SosManager(applicationContext) }
    private val sosLabelProvider by lazy { SosLabelProvider(applicationContext) }
    private val sosIconProvider by lazy { SosIconProvider(applicationContext) }

    override fun onDestroy() {
        super.onDestroy()
        sosManager.cleanup()
    }

    override fun onClick() {
        if (!sosManager.hasFlashHardware()) {
            Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show()
            return
        }
        sosManager.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(sosManager.isActive, sosManager.isFlashAvailable)
    }

    override fun updateTile() {
        val active = sosManager.isActive.value
        val isHardwareAvailable = sosManager.hasFlashHardware()
        val isSystemAvailable = sosManager.isFlashAvailable.value
        val isFullyAvailable = isHardwareAvailable && isSystemAvailable

        setTileState(
            state = when {
                !isFullyAvailable -> Tile.STATE_UNAVAILABLE
                active -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = sosLabelProvider.getLabel(),
            subtitle = sosLabelProvider.getSubtitle(active, isFullyAvailable),
            icon = sosIconProvider.getIcon()
        )
    }
}