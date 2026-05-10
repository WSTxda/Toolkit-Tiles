package com.wstxda.toolkit.tiles.memory

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.memory.MemoryModule
import com.wstxda.toolkit.ui.icon.MemoryIconProvider
import com.wstxda.toolkit.ui.label.MemoryLabelProvider
import kotlinx.coroutines.flow.Flow

class MemoryTileService : BaseTileService() {

    private val memoryManager by lazy { MemoryModule.getInstance(applicationContext) }
    private val labelProvider by lazy { MemoryLabelProvider(applicationContext) }
    private val iconProvider by lazy { MemoryIconProvider(applicationContext) }

    override fun onStartListening() {
        memoryManager.setListening(true)
        super.onStartListening()
    }

    override fun onStopListening() {
        super.onStopListening()
        memoryManager.setListening(false)
    }

    override fun onClick() {
        memoryManager.toggle()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        memoryManager.currentState,
        memoryManager.usedValue,
        memoryManager.totalValue,
        memoryManager.detailValue,
    )

    override fun updateTile() {
        val state = memoryManager.currentState.value
        val used = memoryManager.usedValue.value
        val total = memoryManager.totalValue.value
        val detail = memoryManager.detailValue.value

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(state, detail),
            subtitle = labelProvider.getSubtitle(used, total),
            icon = iconProvider.getIcon(state),
        )
    }
}