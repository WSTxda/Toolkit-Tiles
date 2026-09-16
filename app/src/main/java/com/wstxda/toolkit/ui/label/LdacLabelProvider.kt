package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.ldac.LdacConnection
import com.wstxda.toolkit.manager.ldac.LdacSnapshot
import com.wstxda.toolkit.manager.ldac.LdacState

class LdacLabelProvider(private val context: Context) {

    fun getSubtitle(
        snapshot: LdacSnapshot,
        hasSecureSettings: Boolean,
        hasAssociation: Boolean,
    ): CharSequence = when {
        !hasSecureSettings -> context.getString(R.string.tile_setup)
        snapshot.connection == LdacConnection.PermissionRequired ->
            context.getString(R.string.ldac_bluetooth_permission)
        snapshot.connection == LdacConnection.Connecting ->
            context.getString(R.string.tile_unavailable)
        snapshot.connection == LdacConnection.Disconnected ->
            context.getString(R.string.ldac_not_connected)
        snapshot.connection == LdacConnection.NonLdac ->
            context.getString(R.string.ldac_not_active)
        !hasAssociation -> context.getString(R.string.ldac_association_required)
        snapshot.quality == LdacState.Adaptive -> context.getString(R.string.ldac_quality_adaptive)
        snapshot.quality == LdacState.Connection -> context.getString(R.string.ldac_quality_330)
        snapshot.quality == LdacState.Balanced -> context.getString(R.string.ldac_quality_660)
        else -> context.getString(R.string.ldac_quality_990)
    }
}
