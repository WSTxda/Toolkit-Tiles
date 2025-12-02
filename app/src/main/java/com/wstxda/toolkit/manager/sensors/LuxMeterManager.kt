package com.wstxda.toolkit.manager.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.sensors.getLux
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LuxMeterManager(context: Context) : SensorEventListener {

    private val sensorManager: SensorManager? = context.getSystemService()
    private val lightSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _lux = MutableStateFlow(0)
    val lux = _lux.asStateFlow()
    private var isResumed = false
    private var isSensorRegistered = false

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
        if (lightSensor != null && !isSensorRegistered) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
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
        _lux.value = event.getLux()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    companion object {
        fun isSupported(context: Context): Boolean {
            val sm = context.getSystemService<SensorManager>()
            return sm?.getDefaultSensor(Sensor.TYPE_LIGHT) != null
        }
    }
}