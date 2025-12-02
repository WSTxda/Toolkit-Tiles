package com.wstxda.toolkit.tiles.games

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.games.CoinFlipManager
import com.wstxda.toolkit.ui.icon.CoinFlipIconProvider
import com.wstxda.toolkit.ui.label.CoinFlipLabelProvider
import kotlinx.coroutines.flow.Flow

class CoinFlipTileService : BaseTileService() {

    private val coinFlipManager by lazy { CoinFlipManager() }
    private val coinFlipLabelProvider by lazy { CoinFlipLabelProvider(applicationContext) }
    private val coinFlipIconProvider by lazy { CoinFlipIconProvider(applicationContext) }

    override fun onStopListening() {
        super.onStopListening()
        coinFlipManager.reset()
    }

    override fun onClick() {
        coinFlipManager.flip()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(
            coinFlipManager.lastFlip, coinFlipManager.headsCount, coinFlipManager.tailsCount
        )
    }

    override fun updateTile() {
        val lastFlip = coinFlipManager.lastFlip.value
        val heads = coinFlipManager.headsCount.value
        val tails = coinFlipManager.tailsCount.value

        setTileState(
            state = if (lastFlip != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = coinFlipLabelProvider.getLabel(lastFlip),
            subtitle = coinFlipLabelProvider.getSubtitle(lastFlip, heads, tails),
            icon = coinFlipIconProvider.getIcon(lastFlip)
        )
    }
}