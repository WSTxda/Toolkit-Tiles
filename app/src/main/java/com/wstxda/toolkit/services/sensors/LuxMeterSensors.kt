package com.wstxda.toolkit.services.sensors

import android.hardware.SensorEvent

fun SensorEvent.getLux(): Int {
    return values[0].toInt()
}