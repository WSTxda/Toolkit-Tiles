package com.wstxda.toolkit.tiles.networktraffic

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.networktraffic.NetworkTrafficManager
import com.wstxda.toolkit.manager.networktraffic.NetworkTrafficModule
import com.wstxda.toolkit.ui.icon.NetworkTrafficIconProvider
import com.wstxda.toolkit.ui.label.NetworkTrafficLabelProvider
import kotlinx.coroutines.flow.Flow

class NetworkTrafficTileService : BaseTileService() {

    private val networkTrafficManager: NetworkTrafficManager by lazy {
        NetworkTrafficModule.getInstance(applicationContext)
    }
    private val networkTrafficLabelProvider by lazy {
        NetworkTrafficLabelProvider(applicationContext)
    }
    private val networkTrafficIconProvider by lazy {
        NetworkTrafficIconProvider(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        networkTrafficManager.setListening(true)
    }

    override fun onStopListening() {
        super.onStopListening()
        networkTrafficManager.setListening(false)
    }

    override fun onClick() {
        networkTrafficManager.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(
            networkTrafficManager.currentState, networkTrafficManager.speedValue
        )
    }

    override fun updateTile() {
        val state = networkTrafficManager.currentState.value
        val speed = networkTrafficManager.speedValue.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = networkTrafficLabelProvider.getLabel(speed),
            subtitle = networkTrafficLabelProvider.getSubtitle(state),
            icon = networkTrafficIconProvider.getIcon(state)
        )
    }
}