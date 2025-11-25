package com.wstxda.toolkit.tiles

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.core.content.getSystemService
import com.wstxda.toolkit.R
import com.wstxda.toolkit.resources.icon.LevelIconFactory
import com.wstxda.toolkit.resources.label.levelLabel
import com.wstxda.toolkit.services.NOTIFICATION_ID
import com.wstxda.toolkit.services.channel
import com.wstxda.toolkit.services.notification
import com.wstxda.toolkit.services.sensors.Mode
import com.wstxda.toolkit.services.sensors.getOrientation
import com.wstxda.toolkit.services.sensors.getTilt
import com.wstxda.toolkit.services.startForegroundCompat
import com.wstxda.toolkit.utils.Haptics
import com.wstxda.toolkit.services.update
import kotlin.math.roundToInt

private const val TAG = "LevelTileService"
private const val SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI

// Note: Sensor data is only accessible in foreground, so we need to start this service as a foreground service.
// This is done either on onCreate, onClick or onStartListening, depending on Android version.

// On Android 14 foreground service can not be started in onClick or onStartListening due to a bug:
// https://issuetracker.google.com/issues/299506164
private val START_FOREGROUND_IMMEDIATELY = VERSION.SDK_INT == VERSION_CODES.UPSIDE_DOWN_CAKE

// On Android 15+ foreground service can only be started after user interaction (onClick or sometimes onStartListening):
// https://developer.android.com/about/versions/15/behavior-changes-15#fgs-hardening
private val CAN_ONLY_START_FOREGROUND_ON_CLICK = VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM

class LevelTileService : TileService(), SensorEventListener {

    private val sensorManager get() = getSystemService<SensorManager>()
    private val notificationManager get() = getSystemService<NotificationManager>()
    private val sensor by lazy { sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
    private val isSupported get() = sensor != null

    private lateinit var haptics: Haptics
    private lateinit var icons: LevelIconFactory

    private var lastHapticFeedback = 0L
    private var lastDegrees: Int? = null

    override fun onCreate() {
        Log.i(TAG, "Create")
        haptics = Haptics(applicationContext)
        icons = LevelIconFactory(applicationContext)
        notificationManager?.createNotificationChannel(channel())
        if (START_FOREGROUND_IMMEDIATELY) {
            startForegroundCompat(NOTIFICATION_ID, notification())
        }
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroy")
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onStartListening() {
        Log.i(TAG, "Start listening")
        when (qsTile?.state) {
            Tile.STATE_ACTIVE -> startLevel()
        }
    }

    override fun onStopListening() {
        Log.i(TAG, "Stop listening")
        when (qsTile?.state) {
            Tile.STATE_ACTIVE -> stopLevel()
        }
    }

    override fun onClick() {
        Log.i(TAG, "Click")
        if (!isSupported) {
            showNotSupported()
            return
        }
        when (qsTile?.state) {
            Tile.STATE_ACTIVE -> updateTileAsInactive()
            Tile.STATE_INACTIVE -> updateTileAsActive()
        }
    }

    private fun showNotSupported() {
        Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
    }

    private fun updateTileAsActive() {
        qsTile?.update {
            state = Tile.STATE_ACTIVE
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                subtitle = getString(R.string.level_tile_label)
            }
        }
        startLevel()
    }

    private fun updateTileAsInactive() {
        qsTile?.update {
            state = Tile.STATE_INACTIVE
            icon = Icon.createWithResource(applicationContext, R.drawable.ic_level_off)
            label = getString(R.string.level_tile_label)
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                subtitle = getString(R.string.tile_label_off)
            }
        }
        stopLevel()
        lastDegrees = null
    }

    private fun startLevel() {
        try {
            Log.i(TAG, "Start level")
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }
            sensorManager?.registerListener(this, sensor, SENSOR_DELAY)
        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                Log.w(TAG, "Foreground service not allowed", e)
                updateTileAsInactive()
            } else {
                throw e // Crash on other exceptions
            }
        }
    }

    private fun stopLevel() {
        Log.i(TAG, "Stop level")
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        Log.v(TAG, "Sensor changed")
        val (pitch, roll, balance, mode) = getOrientation(this, event)
        val degrees = when (mode) {
            Mode.Line -> balance.roundToInt()
            Mode.Dot -> getTilt(pitch, roll)
        }

        if (lastDegrees != null && lastDegrees == degrees) return
        lastDegrees = degrees

        updateTileWithData(degrees, mode, balance, pitch, roll)
    }

    private fun updateTileWithData(
        degrees: Int,
        mode: Mode,
        balance: Float,
        pitch: Float,
        roll: Float
    ) {
        qsTile?.update {
            icon = if (degrees == 0) {
                // Perfectly level
                vibrateOnZero()
                Icon.createWithResource(
                    applicationContext, when (mode) {
                        Mode.Line -> R.drawable.ic_level_line_zero
                        Mode.Dot -> R.drawable.ic_level_dot_zero
                    }
                )
            } else {
                // Not level
                when (mode) {
                    Mode.Line -> icons.buildLine(balance)
                    Mode.Dot -> icons.buildDot(pitch, roll)
                }
            }
            label = levelLabel(degrees)
        }
    }

    private fun vibrateOnZero() {
        val now = System.currentTimeMillis()
        if (now - lastHapticFeedback > 500) {
            haptics.tick()
            lastHapticFeedback = now
        }
    }
}