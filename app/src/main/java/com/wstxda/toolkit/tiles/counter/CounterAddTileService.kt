package com.wstxda.toolkit.tiles.counter

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.counter.CounterAction
import com.wstxda.toolkit.manager.counter.CounterModule
import com.wstxda.toolkit.ui.icon.CounterIconProvider
import com.wstxda.toolkit.ui.label.CounterLabelProvider
import kotlinx.coroutines.flow.Flow

class CounterAddTileService : BaseTileService() {

    private val counterModule by lazy { CounterModule.getInstance(applicationContext) }
    private val counterLabelProvider by lazy { CounterLabelProvider(applicationContext) }
    private val counterIconProvider by lazy { CounterIconProvider(applicationContext) }

    override fun onClick() {
        counterModule.increment()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(counterModule.count, counterModule.lastAction)
    }

    override fun updateTile() {
        val count = counterModule.count.value
        val action = counterModule.lastAction.value
        val isActive = action == CounterAction.ADD

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = counterLabelProvider.getAddLabel(isActive, count),
            subtitle = counterLabelProvider.getAddSubtitle(isActive),
            icon = counterIconProvider.getAddIcon()
        )
    }
}