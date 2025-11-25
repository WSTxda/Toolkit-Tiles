package com.wstxda.toolkit.services.sensors

import android.content.Context
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import java.lang.Math.toDegrees
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Display mode for the level: line or dot. */
enum class Mode { Line, Dot }

/** Encapsulates orientation values. */
data class Orientation(
    val pitch: Float, val roll: Float, val balance: Float, val mode: Mode
)

/**
 * Convert a rotation vector sensor event into orientation data.
 *
 * - Pitch: forward/backward tilt
 * - Roll: side tilt
 * - Balance: angle for line mode
 * - Mode: which visualization mode should be used
 */
fun getOrientation(context: Context, event: SensorEvent): Orientation {
    val rotationMatrix = FloatArray(16)
    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

    val displayRotation = getDisplayRotation(context)
    val remapped = remapRotationMatrix(rotationMatrix, displayRotation)

    val orientation = FloatArray(3)
    SensorManager.getOrientation(remapped, orientation)

    val pitch = toDegrees(orientation[1].toDouble()).toFloat()
    val roll = toDegrees(orientation[2].toDouble()).toFloat()

    // Switch to line mode if the device is tilted too far
    val mode = if (abs(pitch) > 45f || abs(roll) > 45f) Mode.Line else Mode.Dot

    val gx = remapped.getOrNull(8) ?: 0f
    val gy = remapped.getOrNull(9) ?: 0f

    val balance = toDegrees(atan2(gx.toDouble(), gy.toDouble())).toFloat()
    val adjustedBalance = adjustBalance(balance)

    return Orientation(pitch, roll, adjustedBalance, mode)
}

/**
 * Adjusts the balance value so that it snaps correctly
 * to multiples of 90 degrees when needed.
 */
private fun adjustBalance(balance: Float): Float {
    val baseAngle = (balance / 90f).roundToInt() * 90f
    return if (baseAngle == 0f) balance else baseAngle - balance
}

/**
 * Remaps the rotation matrix based on the current display rotation.
 */
private fun remapRotationMatrix(
    rotationMatrix: FloatArray, displayRotation: Int
): FloatArray {
    val (newX, newY) = when (displayRotation) {
        Surface.ROTATION_90 -> Pair(SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
        Surface.ROTATION_180 -> Pair(SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
        Surface.ROTATION_270 -> Pair(SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        else -> Pair(SensorManager.AXIS_X, SensorManager.AXIS_Y)
    }

    val remapped = FloatArray(16)
    SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remapped)
    return remapped
}

/**
 * Gets the current display rotation in a backwards-compatible way.
 */
private fun getDisplayRotation(context: Context): Int {
    return try {
        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            @Suppress("DEPRECATION") (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay
        }
        display?.rotation ?: Surface.ROTATION_0
    } catch (_: Throwable) {
        Surface.ROTATION_0
    }
}

/**
 * Calculates the tilt angle (in degrees) from pitch and roll.
 */
fun getTilt(pitch: Float, roll: Float): Int {
    val magnitude = sqrt(pitch.pow(2) + roll.pow(2)).roundToInt()
    return if (abs(roll) >= abs(pitch)) {
        if (roll >= 0) magnitude else -magnitude
    } else {
        if (pitch >= 0) magnitude else -magnitude
    }
}