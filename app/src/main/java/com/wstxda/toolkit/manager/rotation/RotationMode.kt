package com.wstxda.toolkit.manager.rotation

import android.view.Surface

enum class RotationMode(val rotation: Int) {
    AUTO(-1),
    PORTRAIT(Surface.ROTATION_0),
    LANDSCAPE(Surface.ROTATION_90),
    REVERSE_PORTRAIT(Surface.ROTATION_180),
    REVERSE_LANDSCAPE(Surface.ROTATION_270);

    fun next(): RotationMode {
        val values = entries
        return values[(ordinal + 1) % values.size]
    }
}