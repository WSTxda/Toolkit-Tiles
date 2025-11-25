package com.wstxda.toolkit.tiles

import android.graphics.drawable.Icon
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.wstxda.toolkit.R
import com.wstxda.toolkit.resources.icon.CoinFlipIconFactory
import com.wstxda.toolkit.resources.label.coinFlipLabel
import com.wstxda.toolkit.services.update
import kotlin.random.Random

private const val TAG = "CoinFlipTileService"

class CoinFlipTileService : TileService() {

    companion object {
        private var headsCount = 0
        private var tailsCount = 0
    }

    override fun onCreate() {
        Log.i(TAG, "Create")
        super.onCreate()
    }

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
    }

    override fun onStopListening() {
        Log.i(TAG, "Stop listening")
        updateTileAsInactive()
    }

    override fun onClick() {
        Log.i(TAG, "Click")
        val flipResult = Random.nextBoolean()
        updateTileWithResult(flipResult)
    }

    private fun updateTileWithResult(isHeads: Boolean) {
        if (isHeads) {
            headsCount++
        } else {
            tailsCount++
        }

        qsTile?.update {
            state = Tile.STATE_ACTIVE
            label = coinFlipLabel(isHeads)
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                subtitle = getString(R.string.coin_flip_count, headsCount, tailsCount)
            }
            icon = Icon.createWithResource(
                applicationContext, if (isHeads) CoinFlipIconFactory.getHeadsIcon()
                else CoinFlipIconFactory.getTailsIcon()
            )
        }
    }

    private fun updateTileAsInactive() {
        headsCount = 0
        tailsCount = 0
        qsTile?.update {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.coin_flip_tile_label)
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                subtitle = getString(R.string.coin_flip_label)
            }
            icon = Icon.createWithResource(applicationContext, R.drawable.ic_coin_off)
        }
    }
}