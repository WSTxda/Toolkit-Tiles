package com.wstxda.toolkit.tiles.games

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.games.DiceRollManager
import com.wstxda.toolkit.ui.icon.DiceRollIconProvider
import com.wstxda.toolkit.ui.label.DiceRollLabelProvider
import kotlinx.coroutines.flow.Flow

class DiceRollTileService : BaseTileService() {

    private val diceRollManager by lazy { DiceRollManager(applicationContext) }
    private val diceRollLabelProvider by lazy { DiceRollLabelProvider(applicationContext) }
    private val diceRollIconProvider by lazy { DiceRollIconProvider(applicationContext) }

    override fun onStopListening() {
        super.onStopListening()
        diceRollManager.clearState()
    }

    override fun onClick() {
        diceRollManager.roll()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(diceRollManager.currentRoll, diceRollManager.isRolling)
    }

    override fun updateTile() {
        val currentRoll = diceRollManager.currentRoll.value
        val isRolling = diceRollManager.isRolling.value

        setTileState(
            state = if (currentRoll != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = diceRollLabelProvider.getLabel(currentRoll),
            subtitle = diceRollLabelProvider.getSubtitle(isRolling),
            icon = diceRollIconProvider.getIcon(currentRoll)
        )
    }
}