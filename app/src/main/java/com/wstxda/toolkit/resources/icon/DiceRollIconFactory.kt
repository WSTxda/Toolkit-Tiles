package com.wstxda.toolkit.resources.icon

import com.wstxda.toolkit.R

object DiceRollIconFactory {

    val diceOff = R.drawable.ic_dice_off

    private val diceFaces = listOf(
        R.drawable.ic_dice_1,
        R.drawable.ic_dice_2,
        R.drawable.ic_dice_3,
        R.drawable.ic_dice_4,
        R.drawable.ic_dice_5,
        R.drawable.ic_dice_6
    )

    fun getAnimationFrames(): List<Int> = diceFaces + diceFaces

    fun getDrawableForNumber(number: Int): Int = diceFaces.getOrElse(number - 1) { diceOff }

    fun getNumberForDrawable(drawableId: Int): Int = diceFaces.indexOf(drawableId) + 1
}