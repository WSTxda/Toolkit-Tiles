package com.wstxda.toolkit.resources.label

import com.wstxda.toolkit.R
import com.wstxda.toolkit.tiles.CoinFlipTileService

fun CoinFlipTileService.coinFlipLabel(isHeads: Boolean): String {
    return if (isHeads) {
        getString(R.string.coin_heads_label)
    } else {
        getString(R.string.coin_tails_label)
    }
}