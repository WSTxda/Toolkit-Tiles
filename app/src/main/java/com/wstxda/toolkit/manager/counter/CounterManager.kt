package com.wstxda.toolkit.manager.counter

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import androidx.core.content.edit
import com.wstxda.toolkit.tiles.counter.CounterAddTileService
import com.wstxda.toolkit.tiles.counter.CounterRemoveTileService
import com.wstxda.toolkit.tiles.counter.CounterResetTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CounterManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "counter_prefs"
        private const val KEY_COUNT = "count"
        private const val KEY_ACTION = "last_action"
    }

    private val appContext = context.applicationContext
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()
    private val _lastAction = MutableStateFlow(CounterAction.NONE)
    val lastAction = _lastAction.asStateFlow()

    init {
        managerScope.launch {
            val prefs = getPrefs()
            _count.value = prefs.getInt(KEY_COUNT, 0)

            val actionString = prefs.getString(KEY_ACTION, CounterAction.NONE.name)
            _lastAction.value = runCatching {
                CounterAction.valueOf(actionString!!)
            }.getOrDefault(CounterAction.NONE)
        }
    }

    fun increment() {
        updateState(_count.value + 1, CounterAction.ADD)
    }

    fun decrement() {
        updateState(_count.value - 1, CounterAction.REMOVE)
    }

    fun reset() {
        updateState(0, CounterAction.RESET)
    }

    private fun updateState(newValue: Int, action: CounterAction) {
        _count.value = newValue
        _lastAction.value = action

        managerScope.launch {
            getPrefs().edit {
                putInt(KEY_COUNT, newValue)
                putString(KEY_ACTION, action.name)
            }
        }

        refreshAllTiles()
    }

    private fun refreshAllTiles() {
        val classes = listOf(
            CounterAddTileService::class.java,
            CounterRemoveTileService::class.java,
            CounterResetTileService::class.java
        )

        classes.forEach { clazz ->
            TileService.requestListeningState(appContext, ComponentName(appContext, clazz))
        }
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}