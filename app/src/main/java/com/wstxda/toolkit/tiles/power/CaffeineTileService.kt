package com.wstxda.toolkit.tiles.power

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.power.CaffeineModule
import com.wstxda.toolkit.manager.power.CaffeineState
import com.wstxda.toolkit.ui.icon.CaffeineIconProvider
import com.wstxda.toolkit.ui.label.CaffeineLabelProvider
import kotlinx.coroutines.flow.Flow

class CaffeineTileService : BaseTileService() {

    private val caffeineModule by lazy { CaffeineModule.getInstance(applicationContext) }
    private val caffeineLabelProvider by lazy { CaffeineLabelProvider(applicationContext) }
    private val caffeineIconProvider by lazy { CaffeineIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        caffeineModule.synchronizeState()
    }

    override fun onClick() {
        if (caffeineModule.isPermissionGranted()) {
            caffeineModule.cycleState()
        } else {
            startActivityAndCollapse(WriteSettingsPermissionActivity::class.java)
        }
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(caffeineModule.currentState)
    }

    override fun updateTile() {
        val state = caffeineModule.currentState.value
        val hasPermission = caffeineModule.isPermissionGranted()

        setTileState(
            state = if (state != CaffeineState.Off && hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = caffeineLabelProvider.getLabel(state, hasPermission),
            subtitle = caffeineLabelProvider.getSubtitle(state, hasPermission),
            icon = caffeineIconProvider.getIcon(state)
        )
    }
}