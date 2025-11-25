package com.wstxda.toolkit.resources.icon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import com.wstxda.toolkit.R
import kotlin.math.max

/** Factory for creating (and caching) level icons (dot or line modes). */
class LevelIconFactory(context: Context) {

    // Base drawables
    private val dotBase = ContextCompat.getDrawable(context, R.drawable.ic_level_dot)!!
    private val dotIndicator =
        ContextCompat.getDrawable(context, R.drawable.ic_level_dot_indicator)!!
    private val lineBase = ContextCompat.getDrawable(context, R.drawable.ic_level_line)!!
    private val lineIndicator =
        ContextCompat.getDrawable(context, R.drawable.ic_level_line_indicator)!!

    // Drawable and bitmap cache for better memory
    private val dotBitmap = createBitmap(dotBase.intrinsicWidth, dotBase.intrinsicHeight)
    private val lineBitmap = createBitmap(lineBase.intrinsicWidth, lineBase.intrinsicHeight)

    /** Build a "dot" style level icon, where the bubble moves with pitch & roll. */
    fun buildDot(pitch: Float, roll: Float): Icon {
        val width = max(1, dotBase.intrinsicWidth)
        val height = max(1, dotBase.intrinsicHeight)

        Canvas(dotBitmap).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // Clear canvas

            // Draw base
            dotBase.setBounds(0, 0, width, height)
            dotBase.draw(this)

            // Compute bubble position
            val centerX = width / 2f
            val centerY = height / 2f
            val bubbleRadius = width / 6f
            val bubbleSize = (bubbleRadius * 2).toInt().coerceAtLeast(1)

            val maxOffsetX = width / 2f - bubbleRadius
            val maxOffsetY = height / 2f - bubbleRadius

            val bubbleX = centerX - (roll / 45f).coerceIn(-1f, 1f) * maxOffsetX
            val bubbleY = centerY + (pitch / 45f).coerceIn(-1f, 1f) * maxOffsetY

            val left = (bubbleX - bubbleRadius).toInt().coerceIn(0, width - bubbleSize)
            val top = (bubbleY - bubbleRadius).toInt().coerceIn(0, height - bubbleSize)

            // Draw bubble
            dotIndicator.setBounds(left, top, left + bubbleSize, top + bubbleSize)
            dotIndicator.draw(this)
        }
        return Icon.createWithBitmap(dotBitmap)
    }

    /** Build a "line" style level icon, where the indicator rotates with the angle. */
    fun buildLine(angle: Float): Icon {
        val width = max(1, lineBase.intrinsicWidth)
        val height = max(1, lineBase.intrinsicHeight)

        Canvas(lineBitmap).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR) // Clear canvas

            // Draw base
            lineBase.setBounds(0, 0, width, height)
            lineBase.draw(this)

            // Draw rotated indicator
            withSave {
                rotate(angle, width / 2f, height / 2f)
                lineIndicator.setBounds(0, 0, width, height)
                lineIndicator.draw(this)
            }
        }
        return Icon.createWithBitmap(lineBitmap)
    }
}