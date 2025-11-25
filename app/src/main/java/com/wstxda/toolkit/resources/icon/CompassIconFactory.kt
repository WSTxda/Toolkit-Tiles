package com.wstxda.toolkit.resources.icon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap

/** Factory for creating (and caching) rotated icons. */
class CompassIconFactory(context: Context, @DrawableRes drawableRes: Int) {

    // Drawable and bitmap cache for better memory
    private val arrowDrawable = ContextCompat.getDrawable(context, drawableRes)!!
    private val iconBitmap =
        createBitmap(arrowDrawable.intrinsicWidth, arrowDrawable.intrinsicHeight)

    fun build(degrees: Float): Icon {
        Canvas(iconBitmap).apply {
            drawColor(Color.BLACK, PorterDuff.Mode.CLEAR) // clear all
            rotate(-degrees, width / 2f, height / 2f)
            arrowDrawable.setBounds(0, 0, width, height)
            arrowDrawable.draw(this)
        }
        return Icon.createWithBitmap(iconBitmap)
    }
}