package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.dns.DnsProvider

class DnsLabelProvider(private val context: Context) {

    fun getLabel(provider: DnsProvider, hasPermission: Boolean): CharSequence {
        return if (hasPermission) {
            provider.displayName
        } else {
            context.getString(R.string.dns_switcher_tile)
        }
    }

    fun getSubtitle(hasPermission: Boolean): CharSequence {
        return if (hasPermission) {
            context.getString(R.string.tile_switch)
        } else {
            context.getString(R.string.tile_setup)
        }
    }
}