package com.wstxda.toolkit.tiles

import android.app.ForegroundServiceStartNotAllowedException
import android.app.NotificationManager
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.view.Display
import android.widget.Toast
import androidx.core.content.getSystemService
import com.wstxda.toolkit.R
import com.wstxda.toolkit.resources.icon.CompassIconFactory
import com.wstxda.toolkit.resources.label.compassLabel
import com.wstxda.toolkit.services.NOTIFICATION_ID
import com.wstxda.toolkit.services.channel
import com.wstxda.toolkit.services.notification
import com.wstxda.toolkit.services.sensors.getAzimuthDegrees
import com.wstxda.toolkit.services.startForegroundCompat
import com.wstxda.toolkit.utils.Haptics
import com.wstxda.toolkit.services.update
import kotlin.math.abs

private const val TAG = "CompassTileService"
private const val SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI

// Note: Sensor data is only accessible in foreground, so we need to start this service as a foreground service.
// This is done either on onCreate, onClick or onStartListening, depending on Android version.

// On Android 14 foreground service can not be started in onClick or onStartListening due to a bug:
// https://issuetracker.google.com/issues/299506164
private val START_FOREGROUND_IMMEDIATELY = VERSION.SDK_INT == VERSION_CODES.UPSIDE_DOWN_CAKE

// On Android 15+ foreground service can only be started after user interaction (onClick or sometimes onStartListening):
// https://developer.android.com/about/versions/15/behavior-changes-15#fgs-hardening
private val CAN_ONLY_START_FOREGROUND_ON_CLICK = VERSION.SDK_INT >= VERSION_CODES.VANILLA_ICE_CREAM

class CompassTileService : TileService(), SensorEventListener {

    private val sensorManager
        get() = getSystemService<SensorManager>()

    private val notificationManager
        get() = getSystemService<NotificationManager>()

    private val displayManager
        get() = getSystemService<DisplayManager>()

    private val sensor by lazy {
        // TYPE_ROTATION_VECTOR is a fusion of gyro, accelerometer and magnetometer, which is more
        // accurate and responsive than just using the magnetometer.
        // TYPE_GEOMAGNETIC_ROTATION_VECTOR is used as a fallback if gyro is not available.
        sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
    }

    private val isSupported
        get() = sensor != null

    private val displayRotation
        get() = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)?.rotation

    private lateinit var compassIconFactory: CompassIconFactory
    private lateinit var haptics: Haptics

    override fun onCreate() {
        Log.i(TAG, "Create")
        compassIconFactory = CompassIconFactory(applicationContext, R.drawable.ic_compass_on)
        haptics = Haptics(applicationContext)
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
            Tile.STATE_ACTIVE -> startCompass()
        }
    }

    override fun onStopListening() {
        Log.i(TAG, "Stop listening")
        when (qsTile?.state) {
            Tile.STATE_ACTIVE -> stopCompass()
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
                subtitle = getString(R.string.compass_tile_label)
            }
        }
        startCompass()
    }

    private fun updateTileAsInactive() {
        qsTile?.update {
            state = Tile.STATE_INACTIVE
            icon = Icon.createWithResource(applicationContext, R.drawable.ic_compass_off)
            label = getString(R.string.compass_tile_label)
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                subtitle = getString(R.string.tile_label_off)
            }
        }
        stopCompass()
    }

    private fun startCompass() {
        try {
            Log.i(TAG, "Start compass")
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }
            sensorManager?.registerListener(this, sensor, SENSOR_DELAY)
        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                Log.w(TAG, "Foreground service start not allowed", e)
                updateTileAsInactive()
            } else {
                throw e // Crash on other exceptions
            }
        }
    }

    private fun stopCompass() {
        Log.i(TAG, "Stop compass")
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit

    private var lastDegrees: Float? = null
    override fun onSensorChanged(event: SensorEvent) {
        Log.v(TAG, "Sensor changed")
        val degrees = event.getAzimuthDegrees(displayRotation)
        if (lastDegrees == null) {
            lastDegrees = degrees
        } else if (abs(degrees - lastDegrees!!) > 5) {
            haptics.tick()
            lastDegrees = degrees
        }

        Log.v(TAG, degrees.toString())
        updateTileWithData(degrees)
    }

    private fun updateTileWithData(degrees: Float) {
        qsTile?.update {
            label = compassLabel(degrees)
            icon = compassIconFactory.build(degrees)
        }
    }
}