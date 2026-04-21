package com.wstxda.toolkit.tiles.diceroll

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.diceroll.DiceRollManager
import com.wstxda.toolkit.ui.icon.DiceRollIconProvider
import com.wstxda.toolkit.ui.label.DiceRollLabelProvider
import kotlinx.coroutines.flow.Flow

class DiceRollTileService : BaseTileService() {

    private val diceRollManager by lazy { DiceRollManager(applicationContext) }
    private val labelProvider by lazy { DiceRollLabelProvider(applicationContext) }
    private val iconProvider by lazy { DiceRollIconProvider(applicationContext) }

    override fun onStopListening() {
        super.onStopListening()
        diceRollManager.clearState()
    }

    override fun onDestroy() {
        super.onDestroy()
        diceRollManager.cleanup()
    }

    override fun onClick() {
        diceRollManager.roll()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        diceRollManager.currentRoll,
        diceRollManager.isRolling,
    )

    override fun updateTile() {
        val currentRoll = diceRollManager.currentRoll.value
        val isRolling = diceRollManager.isRolling.value

        setTileState(
            state = if (currentRoll != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(currentRoll),
            subtitle = labelProvider.getSubtitle(isRolling),
            icon = iconProvider.getIcon(currentRoll),
        )
    }
}