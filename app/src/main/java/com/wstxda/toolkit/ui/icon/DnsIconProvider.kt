package com.wstxda.toolkit.ui.icon

import android.content.Context
import android.graphics.drawable.Icon
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.dns.DnsProvider

class DnsIconProvider(private val context: Context) {

    fun getIcon(provider: DnsProvider, hasPermission: Boolean): Icon {
        if (!hasPermission) {
            return Icon.createWithResource(context, R.drawable.ic_dns_on)
        }

        val iconRes = when (provider) {
            DnsProvider.DISABLED -> R.drawable.ic_dns_off
            DnsProvider.AUTOMATIC -> R.drawable.ic_dns_on
            DnsProvider.GOOGLE -> R.drawable.ic_google
            DnsProvider.CLOUDFLARE -> R.drawable.ic_cloudflare
            DnsProvider.ADGUARD -> R.drawable.ic_adguard
            DnsProvider.OPENDNS -> R.drawable.ic_opendns
            DnsProvider.QUAD9 -> R.drawable.ic_quad9
            DnsProvider.NEXT_DNS -> R.drawable.ic_nextdns
            DnsProvider.MULLVAD -> R.drawable.ic_mullvad
            DnsProvider.CONTROLD -> R.drawable.ic_controld
            DnsProvider.LIBREDNS -> R.drawable.ic_libredns
            DnsProvider.CLOUDFLARE_FAMILY -> R.drawable.ic_block_adult_content
        }

        return Icon.createWithResource(context, iconRes)
    }
}