package com.wstxda.toolkit.services.sensors

import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.view.Surface
import kotlin.math.roundToInt

fun SensorEvent.getAzimuthDegrees(
    rotation: Int, rotationMatrix: FloatArray, remappedMatrix: FloatArray, orientation: FloatArray
): Float {
    SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
    getOrientation(rotationMatrix, rotation, remappedMatrix, orientation)
    return (Math.toDegrees(orientation[0].toDouble()).roundToInt() + 360) % 360f
}

private fun getOrientation(
    rotationMatrix: FloatArray, rotation: Int, remappedMatrix: FloatArray, orientation: FloatArray
) {
    val axisX = SensorManager.AXIS_X
    val axisY = SensorManager.AXIS_Y

    when (rotation) {
        Surface.ROTATION_0 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, axisX, axisY, remappedMatrix
        )

        Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, axisY, SensorManager.AXIS_MINUS_X, remappedMatrix
        )

        Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y, remappedMatrix
        )

        Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_MINUS_Y, axisX, remappedMatrix
        )

        else -> System.arraycopy(rotationMatrix, 0, remappedMatrix, 0, 9)
    }

    SensorManager.getOrientation(remappedMatrix, orientation)
}