package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.rotation.RotationMode

class RotationIconProvider(private val context: Context) {

    fun getIcon(mode: RotationMode): Icon {
        val iconRes = if (mode == RotationMode.AUTO) {
            R.drawable.ic_rotation_auto
        } else {
            R.drawable.ic_rotation_lock
        }
        return Icon.createWithResource(context, iconRes)
    }
}