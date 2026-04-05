package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.networktraffic.NetworkTrafficState

class NetworkTrafficIconProvider(private val context: Context) {

    fun getIcon(state: NetworkTrafficState): Icon {
        val resId = when (state) {
            NetworkTrafficState.DOWNLOAD -> R.drawable.ic_download
            NetworkTrafficState.UPLOAD -> R.drawable.ic_upload
        }
        return Icon.createWithResource(context, resId)
    }
}