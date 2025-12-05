package com.wstxda.toolkit.tiles.system

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.system.MemoryManager
import com.wstxda.toolkit.manager.system.MemoryModule
import com.wstxda.toolkit.manager.system.MemoryState
import com.wstxda.toolkit.ui.icon.MemoryIconProvider
import com.wstxda.toolkit.ui.label.MemoryLabelProvider
import kotlinx.coroutines.flow.Flow

class MemoryTileService : BaseTileService() {

    private val memoryManager: MemoryManager by lazy { MemoryModule.getInstance(applicationContext) }
    private val memoryLabelProvider by lazy { MemoryLabelProvider(applicationContext) }
    private val memoryIconProvider by lazy { MemoryIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        memoryManager.setListening(true)
    }

    override fun onStopListening() {
        super.onStopListening()
        memoryManager.setListening(false)
    }

    override fun onClick() {
        memoryManager.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(
            memoryManager.currentState,
            memoryManager.usedValue,
            memoryManager.totalValue,
            memoryManager.detailValue
        )
    }

    override fun updateTile() {
        val state = memoryManager.currentState.value
        val used = memoryManager.usedValue.value
        val total = memoryManager.totalValue.value
        val detail = memoryManager.detailValue.value

        setTileState(
            state = if (state == MemoryState.DISABLED) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE,
            label = memoryLabelProvider.getLabel(state, detail),
            subtitle = memoryLabelProvider.getSubtitle(state, used, total),
            icon = memoryIconProvider.getIcon(state)
        )
    }
}