package com.wstxda.toolkit.tiles.coinflip

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.coinflip.CoinFlipModule
import com.wstxda.toolkit.ui.icon.CoinFlipIconProvider
import com.wstxda.toolkit.ui.label.CoinFlipLabelProvider
import kotlinx.coroutines.flow.Flow

class CoinFlipTileService : BaseTileService() {

    private val coinFlipManager by lazy { CoinFlipModule.getInstance(applicationContext) }
    private val labelProvider by lazy { CoinFlipLabelProvider(applicationContext) }
    private val iconProvider by lazy { CoinFlipIconProvider(applicationContext) }

    override fun onStopListening() {
        super.onStopListening()
        coinFlipManager.reset()
    }

    override fun onClick() {
        coinFlipManager.flip()
        updateTile()
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        coinFlipManager.lastFlip,
        coinFlipManager.headsCount,
        coinFlipManager.tailsCount,
    )

    override fun updateTile() {
        val lastFlip = coinFlipManager.lastFlip.value
        val heads = coinFlipManager.headsCount.value
        val tails = coinFlipManager.tailsCount.value

        setTileState(
            state = if (lastFlip != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(lastFlip),
            subtitle = labelProvider.getSubtitle(lastFlip, heads, tails),
            icon = iconProvider.getIcon(lastFlip),
        )
    }
}