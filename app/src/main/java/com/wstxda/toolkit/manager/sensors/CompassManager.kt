package com.wstxda.toolkit.manager.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.sensors.getAzimuthDegrees
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class CompassManager(context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val haptics = Haptics(appContext)

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _currentDegrees = MutableStateFlow(0f)
    val currentDegrees = _currentDegrees.asStateFlow()
    private var isResumed = false
    private var isSensorRegistered = false
    private var lastHapticDegrees: Float? = null

    private val sensorManager: SensorManager?
        get() = appContext.getSystemService()

    private val display: Display?
        get() = appContext.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)

    private val rotationSensor: Sensor?
        get() {
            val sm = sensorManager ?: return null
            return sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                ?: sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)
        }

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
        val sm = sensorManager ?: return
        val sensor = rotationSensor ?: return

        if (!isSensorRegistered) {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
            lastHapticDegrees = null
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

        val degrees = event.getAzimuthDegrees(display?.rotation ?: 0)
        _currentDegrees.value = degrees

        if (lastHapticDegrees == null) {
            lastHapticDegrees = degrees
            return
        }

        if (abs(degrees - lastHapticDegrees!!) > 1f) {
            haptics.tick()
            lastHapticDegrees = degrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        fun isSupported(context: Context): Boolean {
            val sm = context.getSystemService<SensorManager>()
            return sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null || sm?.getDefaultSensor(
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
            ) != null
        }
    }
}