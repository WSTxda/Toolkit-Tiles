package com.wstxda.toolkit.tiles.counter

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.wstxda.toolkit.R
import com.wstxda.toolkit.utils.CounterValue
import com.wstxda.toolkit.services.update

private const val TAG = "CounterRemoveTileService"

class CounterRemoveTileService : TileService() {

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        Log.i(TAG, "Click")
        super.onClick()
        CounterValue.remove(applicationContext)
        updateTile()
        requestListeningState(
            applicationContext, ComponentName(applicationContext, CounterAddTileService::class.java)
        )
    }

    private fun updateTile() {
        val value = CounterValue.getValue(applicationContext)
        val lastAction = CounterValue.getLastAction(applicationContext)

        qsTile?.update {
            icon = Icon.createWithResource(applicationContext, R.drawable.ic_counter_remove)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.counter_tile_label)
            }

            when (lastAction) {
                CounterValue.ACTION_ADD, CounterValue.ACTION_RESET -> {
                    state = Tile.STATE_INACTIVE
                    label = getString(R.string.counter_remove_tile_label)
                }

                else -> {
                    state = Tile.STATE_ACTIVE
                    label = value.toString()
                }
            }
        }
    }
}