package com.wstxda.toolkit.resources.label

import com.wstxda.toolkit.R
import com.wstxda.toolkit.tiles.DiceRollTileService

fun DiceRollTileService.diceLabel(roll: Int): String {
    return when (roll) {
        1 -> getString(R.string.dice_1_label)
        2 -> getString(R.string.dice_2_label)
        3 -> getString(R.string.dice_3_label)
        4 -> getString(R.string.dice_4_label)
        5 -> getString(R.string.dice_5_label)
        6 -> getString(R.string.dice_6_label)
        else -> getString(R.string.dice_roll_tile_label)
    }
}