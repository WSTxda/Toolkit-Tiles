package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.ldac.LdacState

class LdacIconProvider(private val context: Context) {

    fun getIcon(state: LdacState): Icon {
        return Icon.createWithResource(context, R.drawable.ic_media_output)
    }
}