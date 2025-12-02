package com.wstxda.toolkit.tiles.counter

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.counter.CounterModule
import com.wstxda.toolkit.ui.icon.CounterIconProvider
import com.wstxda.toolkit.ui.label.CounterLabelProvider
import kotlinx.coroutines.flow.Flow

class CounterResetTileService : BaseTileService() {

    private val counterModule by lazy { CounterModule.getInstance(applicationContext) }
    private val counterLabelProvider by lazy { CounterLabelProvider(applicationContext) }
    private val counterIconProvider by lazy { CounterIconProvider(applicationContext) }

    override fun onClick() {
        counterModule.reset()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(counterModule.count)
    }

    override fun updateTile() {

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = counterLabelProvider.getResetLabel(),
            subtitle = counterLabelProvider.getResetSubtitle(),
            icon = counterIconProvider.getResetIcon()
        )
    }
}