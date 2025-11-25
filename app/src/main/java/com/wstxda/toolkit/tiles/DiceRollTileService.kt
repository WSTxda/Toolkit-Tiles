package com.wstxda.toolkit.tiles

import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.wstxda.toolkit.R
import com.wstxda.toolkit.resources.icon.DiceRollIconFactory
import com.wstxda.toolkit.resources.label.diceLabel
import com.wstxda.toolkit.utils.Haptics
import com.wstxda.toolkit.services.update
import kotlin.random.Random

private const val TAG = "DiceRollTileService"

class DiceRollTileService : TileService() {

    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false
    private lateinit var haptics: Haptics

    override fun onCreate() {
        Log.i(TAG, "Create")
        super.onCreate()
        haptics = Haptics(applicationContext)
    }

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
        updateTileAsInactive()
    }

    override fun onStopListening() {
        Log.i(TAG, "Stop listening")
        updateTileAsInactive()
    }

    override fun onClick() {
        Log.i(TAG, "Click")
        if (isAnimating) return
        val roll = Random.nextInt(1, 7)
        updateTileAsRolling(roll)
    }

    private fun updateTileAsRolling(finalRoll: Int) {
        isAnimating = true
        val frames = DiceRollIconFactory.getAnimationFrames().shuffled()
        var frameIndex = 0

        fun updateTile(frameResId: Int) {
            val rollNumber = DiceRollIconFactory.getNumberForDrawable(frameResId)
            qsTile?.update {
                state = Tile.STATE_ACTIVE
                label = diceLabel(rollNumber)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    subtitle = getString(R.string.dice_rolling_label)
                }
                icon = Icon.createWithResource(applicationContext, frameResId)
            }
        }

        val runnable = object : Runnable {
            override fun run() {
                if (frameIndex < frames.size) {
                    updateTile(frames[frameIndex])
                    haptics.tick()

                    val delay = 60 + (frameIndex * 30)
                    frameIndex++
                    handler.postDelayed(this, delay.toLong())
                } else {
                    updateTileWithFace(finalRoll)
                    isAnimating = false
                    haptics.tick()
                }
            }
        }

        handler.post(runnable)
    }

    private fun updateTileWithFace(roll: Int) {
        qsTile?.update {
            state = Tile.STATE_ACTIVE
            label = diceLabel(roll)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.dice_roll_label)
            }
            icon = Icon.createWithResource(
                applicationContext, DiceRollIconFactory.getDrawableForNumber(roll)
            )
        }
    }

    private fun updateTileAsInactive() {
        isAnimating = false
        handler.removeCallbacksAndMessages(null)
        qsTile?.update {
            state = Tile.STATE_INACTIVE
            label = getString(R.string.dice_roll_tile_label)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = getString(R.string.dice_roll_label)
            }
            icon = Icon.createWithResource(applicationContext, DiceRollIconFactory.diceOff)
        }
    }
}