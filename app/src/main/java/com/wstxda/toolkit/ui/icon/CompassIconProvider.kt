package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Icon
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.wstxda.toolkit.R
import androidx.core.graphics.withRotation

class CompassIconProvider(private val context: Context) {

    private val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.ic_compass)!!
    private val iconBitmap =
        createBitmap(arrowDrawable.intrinsicWidth, arrowDrawable.intrinsicHeight)
    private val canvas = Canvas(iconBitmap)

    fun getIcon(isActive: Boolean, degrees: Float): Icon {
        if (!isActive) {
            return Icon.createWithResource(context, R.drawable.ic_compass)
        }

        canvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR)
        canvas.withRotation(-degrees, iconBitmap.width / 2f, iconBitmap.height / 2f) {
            arrowDrawable.setBounds(0, 0, iconBitmap.width, iconBitmap.height)
            arrowDrawable.draw(this)
        }

        return Icon.createWithBitmap(iconBitmap)
    }
}