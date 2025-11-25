package com.wstxda.toolkit.services.sensors

import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90

private fun Double.normalizeDegrees() = ((this + 360) % 360).toFloat()

fun SensorEvent.getAzimuthDegrees(displayRotation: Int?): Float {
    val rotationMatrix = FloatArray(9).also {
        SensorManager.getRotationMatrixFromVector(it, this.values)
    }
    val orientation = FloatArray(3)
    SensorManager.getOrientation(rotationMatrix, orientation)
    val azimuthRadians = orientation[0]
    val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble())
    val displayAdjustedDegrees = when (displayRotation) {
        ROTATION_0 -> azimuthDegrees
        ROTATION_90 -> azimuthDegrees + 90
        ROTATION_180 -> azimuthDegrees + 180
        ROTATION_270 -> azimuthDegrees + 270
        else -> azimuthDegrees
    }
    return displayAdjustedDegrees.normalizeDegrees()
}