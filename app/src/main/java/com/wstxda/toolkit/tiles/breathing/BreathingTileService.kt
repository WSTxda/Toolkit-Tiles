package com.wstxda.toolkit.tiles.breathing

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.breathing.BreathingModule
import com.wstxda.toolkit.manager.breathing.BreathingPhase
import com.wstxda.toolkit.ui.icon.BreathingIconProvider
import com.wstxda.toolkit.ui.label.BreathingLabelProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BreathingTileService : BaseTileService() {

    private val breathingManager by lazy { BreathingModule.getInstance(applicationContext) }
    private val labelProvider by lazy { BreathingLabelProvider(applicationContext) }
    private val iconProvider by lazy { BreathingIconProvider(applicationContext) }
    private var visibilityJob: Job? = null

    override fun onClick() {
        breathingManager.toggle()
        updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        visibilityJob?.cancel()
        visibilityJob = null
    }

    override fun onStopListening() {
        super.onStopListening()
        visibilityJob = serviceScope.launch {
            delay(3000L)
            val currentState = breathingManager.breathingState.value.phase
            if (currentState != BreathingPhase.IDLE) {
                breathingManager.stop()
                updateTile()
            }
        }
    }

    override fun flowsToCollect(): List<Flow<*>> = listOf(
        breathingManager.breathingState,
    )

    override fun updateTile() {
        val breathingState = breathingManager.breathingState.value
        val isIdle = breathingState.phase == BreathingPhase.IDLE

        setTileState(
            state = if (isIdle) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE,
            label = labelProvider.getLabel(breathingState.phase),
            subtitle = labelProvider.getSubtitle(breathingState.phase),
            icon = iconProvider.getIcon(breathingState.phase, breathingState.progress),
        )
    }
}