package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.level.LevelMode
import com.wstxda.toolkit.services.sensors.Orientation
import kotlin.math.max

class LevelIconProvider(private val context: Context) {

    private val dotBase = ContextCompat.getDrawable(context, R.drawable.ic_level_dot)!!
    private val dotIndicator =
        ContextCompat.getDrawable(context, R.drawable.ic_level_dot_indicator)!!
    private val lineBase = ContextCompat.getDrawable(context, R.drawable.ic_level_line)!!
    private val lineIndicator =
        ContextCompat.getDrawable(context, R.drawable.ic_level_line_indicator)!!

    private val dotBitmap = createBitmap(dotBase.intrinsicWidth, dotBase.intrinsicHeight)
    private val dotCanvas = Canvas(dotBitmap)
    private val lineBitmap = createBitmap(lineBase.intrinsicWidth, lineBase.intrinsicHeight)
    private val lineCanvas = Canvas(lineBitmap)

    fun getIcon(isActive: Boolean, degrees: Int, orient: Orientation): Icon {
        if (!isActive) {
            return Icon.createWithResource(context, R.drawable.ic_level)
        }

        if (degrees == 0) {
            val resId =
                if (orient.mode == LevelMode.Line) R.drawable.ic_level_line_zero else R.drawable.ic_level_dot_zero
            return Icon.createWithResource(context, resId)
        }

        return when (orient.mode) {
            LevelMode.Dot -> buildDot(orient.pitch, orient.roll)
            LevelMode.Line -> buildLine(orient.balance)
        }
    }

    private fun buildDot(pitch: Float, roll: Float): Icon {
        val width = max(1, dotBase.intrinsicWidth)
        val height = max(1, dotBase.intrinsicHeight)

        dotCanvas.apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            dotBase.setBounds(0, 0, width, height)
            dotBase.draw(this)

            val centerX = width / 2f
            val centerY = height / 2f
            val bubbleRadius = width / 6f
            val bubbleSize = (bubbleRadius * 2).toInt().coerceAtLeast(1)

            val maxOffsetX = width / 2f - bubbleRadius
            val maxOffsetY = height / 2f - bubbleRadius

            val bubbleX = centerX - (roll / 45f).coerceIn(-1f, 1f) * maxOffsetX
            val bubbleY = centerY + (pitch / 45f).coerceIn(-1f, 1f) * maxOffsetY

            val left = (bubbleX - bubbleRadius).toInt().coerceIn(
                0, (width - bubbleSize).toFloat().toInt()
            )
            val top = (bubbleY - bubbleRadius).toInt().coerceIn(
                0, (height - bubbleSize).toFloat().toInt()
            )

            dotIndicator.setBounds(left, top, left + bubbleSize, top + bubbleSize)
            dotIndicator.draw(this)
        }
        return Icon.createWithBitmap(dotBitmap)
    }

    private fun buildLine(angle: Float): Icon {
        val width = max(1, lineBase.intrinsicWidth)
        val height = max(1, lineBase.intrinsicHeight)

        lineCanvas.apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            lineBase.setBounds(0, 0, width, height)
            lineBase.draw(this)

            withSave {
                rotate(angle, width / 2f, height / 2f)
                lineIndicator.setBounds(0, 0, width, height)
                lineIndicator.draw(this)
            }
        }
        return Icon.createWithBitmap(lineBitmap)
    }
}