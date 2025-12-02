package com.wstxda.toolkit.manager.games

import android.content.Context
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class DiceRollManager(context: Context) {

    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val haptics = Haptics(context.applicationContext)

    private val _currentRoll = MutableStateFlow<Int?>(null)
    val currentRoll = _currentRoll.asStateFlow()

    private val _isRolling = MutableStateFlow(false)
    val isRolling = _isRolling.asStateFlow()

    private var animationJob: Job? = null

    fun roll() {
        if (_isRolling.value) return

        animationJob?.cancel()
        animationJob = managerScope.launch {
            _isRolling.value = true

            val finalRoll = Random.nextInt(1, 7)

            for (i in 0 until 12) {
                _currentRoll.value = Random.nextInt(1, 7)
                haptics.tick()
                delay(60L + (i * 30))
            }

            _currentRoll.value = finalRoll
            haptics.tick()
            _isRolling.value = false
        }
    }

    fun clearState() {
        animationJob?.cancel()
        animationJob = null
        _isRolling.value = false
        _currentRoll.value = null
    }
}