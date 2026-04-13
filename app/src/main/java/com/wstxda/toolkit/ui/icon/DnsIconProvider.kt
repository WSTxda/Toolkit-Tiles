package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.dns.DnsProvider

class DnsIconProvider(private val context: Context) {

    fun getIcon(provider: DnsProvider, hasPermission: Boolean): Icon {
        if (!hasPermission) {
            return Icon.createWithResource(context, R.drawable.ic_dns)
        }

        val iconRes = when (provider) {
            DnsProvider.AUTOMATIC -> R.drawable.ic_dns
            DnsProvider.CLOUDFLARE -> R.drawable.ic_cloudflare
            DnsProvider.GOOGLE -> R.drawable.ic_google
            DnsProvider.QUAD9 -> R.drawable.ic_quad9
            DnsProvider.ADGUARD -> R.drawable.ic_adguard
            DnsProvider.NEXT_DNS -> R.drawable.ic_nextdns
        }

        return Icon.createWithResource(context, iconRes)
    }
}