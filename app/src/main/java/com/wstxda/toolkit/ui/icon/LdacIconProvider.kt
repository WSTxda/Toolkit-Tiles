package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.ldac.LdacConnection
import com.wstxda.toolkit.manager.ldac.LdacSnapshot
import com.wstxda.toolkit.manager.ldac.LdacState

class LdacIconProvider(private val context: Context) {

    fun getIcon(snapshot: LdacSnapshot): Icon {
        val resource = if (snapshot.connection != LdacConnection.Ready) {
            R.drawable.ic_ldac
        } else {
            when (snapshot.quality) {
                LdacState.Adaptive -> R.drawable.ic_ldac_adaptive
                LdacState.Connection -> R.drawable.ic_ldac_330
                LdacState.Balanced -> R.drawable.ic_ldac_660
                LdacState.Quality -> R.drawable.ic_ldac_990
            }
        }
        return Icon.createWithResource(context, resource)
    }
}
