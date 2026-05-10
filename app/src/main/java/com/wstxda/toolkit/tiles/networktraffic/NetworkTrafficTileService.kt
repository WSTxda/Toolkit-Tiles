package com.wstxda.toolkit.tiles.networktraffic

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.networktraffic.NetworkTrafficModule
import com.wstxda.toolkit.ui.icon.NetworkTrafficIconProvider
import com.wstxda.toolkit.ui.label.NetworkTrafficLabelProvider
import kotlinx.coroutines.flow.Flow

class NetworkTrafficTileService : BaseTileService() {

    private val networkTrafficManager by lazy { NetworkTrafficModule.getInstance(applicationContext) }
    private val labelProvider by lazy { NetworkTrafficLabelProvider(applicationContext) }
    private val iconProvider by lazy { NetworkTrafficIconProvider(applicationContext) }

    override fun onStartListening() {
        networkTrafficManager.setListening(true)
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        networkTrafficManager.setListening(false)
    }

    override fun onClick() {
        networkTrafficManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        networkTrafficManager.currentState,
        networkTrafficManager.speedValue,
    )

    override fun updateTile() {
        val state = networkTrafficManager.currentState.value
        val speed = networkTrafficManager.speedValue.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(speed),
            subtitle = labelProvider.getSubtitle(state),
            icon = iconProvider.getIcon(state),
        )
    }
}