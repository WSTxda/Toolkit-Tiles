package com.wstxda.toolkit.manager.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Display
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.sensors.Orientation
import com.wstxda.toolkit.services.sensors.getOrientation
import com.wstxda.toolkit.services.sensors.getTilt
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class LevelManager(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val haptics = Haptics(appContext)

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _orientation = MutableStateFlow(Orientation(0f, 0f, 0f, LevelMode.Dot))
    val orientation = _orientation.asStateFlow()
    private val _degrees = MutableStateFlow(0)
    val degrees = _degrees.asStateFlow()
    private var isResumed = false
    private var isSensorRegistered = false
    private var lastHapticFeedback = 0L

    private val sensorManager: SensorManager?
        get() = appContext.getSystemService()

    private val rotationSensor: Sensor?
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val display: Display?
        get() = appContext.getSystemService(android.hardware.display.DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)


    fun toggle() {
        _isEnabled.value = !_isEnabled.value
        updateSensorState()
    }

    fun resume() {
        isResumed = true
        updateSensorState()
    }

    fun pause() {
        isResumed = false
        updateSensorState()
    }

    fun forceStop() {
        _isEnabled.value = false
        isResumed = false
        unregisterSensor()
    }

    private fun updateSensorState() {
        if (_isEnabled.value && isResumed) {
            registerSensor()
        } else {
            unregisterSensor()
        }
    }

    private fun registerSensor() {
        val sensor = rotationSensor ?: return
        if (!isSensorRegistered) {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
        }
    }

    private fun unregisterSensor() {
        if (isSensorRegistered) {
            sensorManager?.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val rotation = display?.rotation ?: 0

        val orient = getOrientation(event, rotation)
        _orientation.value = orient

        val deg = when (orient.mode) {
            LevelMode.Line -> orient.balance.roundToInt()
            LevelMode.Dot -> getTilt(orient.pitch, orient.roll)
        }

        _degrees.value = deg

        if (deg == 0) vibrateOnZero()
    }

    private fun vibrateOnZero() {
        val now = System.currentTimeMillis()
        if (now - lastHapticFeedback > 500) {
            haptics.tick()
            lastHapticFeedback = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        fun isSupported(context: Context): Boolean {
            val sm = context.getSystemService<SensorManager>()
            return sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null
        }
    }
}