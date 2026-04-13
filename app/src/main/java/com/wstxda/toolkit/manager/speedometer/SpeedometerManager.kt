package com.wstxda.toolkit.manager.speedometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SpeedometerManager(context: Context) : LocationListener {

    companion object {
        private const val GPS_MIN_TIME_MS = 1000L
        private const val GPS_MIN_DISTANCE_M = 0f
        private const val MS_TO_KMH = 3.6f
    }

    private val appContext = context.applicationContext
    private val locationManager: LocationManager? = appContext.getSystemService()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _unit = MutableStateFlow(SpeedometerUnit.DISABLED)
    val unit = _unit.asStateFlow()

    private val _speedKmh = MutableStateFlow(0f)
    val speedKmh = _speedKmh.asStateFlow()

    private var isListening = false

    fun toggle() {
        val next = when (_unit.value) {
            SpeedometerUnit.DISABLED -> SpeedometerUnit.KMH
            SpeedometerUnit.KMH -> SpeedometerUnit.MPH
            SpeedometerUnit.MPH -> SpeedometerUnit.DISABLED
        }
        _unit.value = next
        _isEnabled.value = next != SpeedometerUnit.DISABLED
        updateListeningState()
    }

    fun resume() {
        updateListeningState()
    }

    fun pause() {
        stopListening()
    }

    fun forceStop() {
        _isEnabled.value = false
        _unit.value = SpeedometerUnit.DISABLED
        stopListening()
    }

    private fun updateListeningState() {
        if (_isEnabled.value) startListening() else stopListening()
    }

    private fun startListening() {
        if (isListening) return
        val provider = getBestProvider() ?: return
        if (ActivityCompat.checkSelfPermission(
                appContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager?.requestLocationUpdates(
            provider,
            GPS_MIN_TIME_MS,
            GPS_MIN_DISTANCE_M,
            this,
        )
        isListening = true
    }

    private fun stopListening() {
        if (!isListening) return
        locationManager?.removeUpdates(this)
        _speedKmh.value = 0f
        isListening = false
    }

    private fun getBestProvider(): String? {
        val lm = locationManager ?: return null
        return when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
    }

    override fun onLocationChanged(location: Location) {
        if (location.hasSpeed()) {
            _speedKmh.value = location.speed * MS_TO_KMH
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
}