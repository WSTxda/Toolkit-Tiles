package com.wstxda.toolkit.manager.games

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class CoinFlipManager {

    private val _headsCount = MutableStateFlow(0)
    val headsCount = _headsCount.asStateFlow()

    private val _tailsCount = MutableStateFlow(0)
    val tailsCount = _tailsCount.asStateFlow()

    private val _lastFlip = MutableStateFlow<CoinFlipSide?>(null)
    val lastFlip = _lastFlip.asStateFlow()

    fun flip() {
        val side = if (Random.nextBoolean()) CoinFlipSide.HEADS else CoinFlipSide.TAILS
        _lastFlip.value = side

        if (side == CoinFlipSide.HEADS) {
            _headsCount.value += 1
        } else {
            _tailsCount.value += 1
        }
    }

    fun reset() {
        _headsCount.value = 0
        _tailsCount.value = 0
        _lastFlip.value = null
    }
}