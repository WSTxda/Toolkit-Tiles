package com.wstxda.toolkit.resources.label

import com.wstxda.toolkit.R
import com.wstxda.toolkit.tiles.CompassTileService
import kotlin.math.roundToInt

fun CompassTileService.compassLabel(degrees: Float): String {
    val direction = when (degrees) {
        in 0.0..22.5, in 337.5..360.0 -> getString(R.string.N)
        in 22.5..67.5 -> getString(R.string.NE)
        in 67.5..112.5 -> getString(R.string.E)
        in 112.5..157.5 -> getString(R.string.SE)
        in 157.5..202.5 -> getString(R.string.S)
        in 202.5..247.5 -> getString(R.string.SW)
        in 247.5..292.5 -> getString(R.string.W)
        in 292.5..337.5 -> getString(R.string.NW)
        else -> "" // Should never happen
    }
    val degreesRounded = degrees.roundToInt() % 360
    return getString(R.string.compass_tile_label_degrees, degreesRounded, direction)
}