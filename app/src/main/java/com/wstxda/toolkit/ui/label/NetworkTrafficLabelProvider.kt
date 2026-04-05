package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.networktraffic.NetworkTrafficState

class NetworkTrafficLabelProvider(private val context: Context) {

    fun getLabel(speed: String): CharSequence {
        return speed.ifBlank { context.getString(R.string.network_traffic_tile) }
    }

    fun getSubtitle(state: NetworkTrafficState): CharSequence {
        return when (state) {
            NetworkTrafficState.DOWNLOAD -> context.getString(R.string.network_traffic_download)
            NetworkTrafficState.UPLOAD -> context.getString(R.string.network_traffic_upload)
        }
    }
}