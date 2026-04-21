package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.dns.DnsProvider

class DnsLabelProvider(private val context: Context) {

    fun getLabel(provider: DnsProvider, hasPermission: Boolean): CharSequence {
        if (!hasPermission) return context.getString(R.string.dns_tile)

        val nameRes = when (provider) {
            DnsProvider.DISABLED -> R.string.tile_off
            DnsProvider.AUTOMATIC -> R.string.dns_tile_automatic
            DnsProvider.GOOGLE -> R.string.dns_tile_provider_google
            DnsProvider.CLOUDFLARE -> R.string.dns_tile_provider_cloudflare
            DnsProvider.ADGUARD -> R.string.dns_tile_provider_adguard
            DnsProvider.OPENDNS -> R.string.dns_tile_provider_opendns
            DnsProvider.QUAD9 -> R.string.dns_tile_provider_quad9
            DnsProvider.NEXT_DNS -> R.string.dns_tile_provider_nextdns
            DnsProvider.MULLVAD -> R.string.dns_tile_provider_mullvad
            DnsProvider.CONTROLD -> R.string.dns_tile_provider_controld
            DnsProvider.LIBREDNS -> R.string.dns_tile_provider_libredns
            DnsProvider.CLOUDFLARE_FAMILY -> R.string.dns_tile_provider_block_adult_content
        }
        return context.getString(nameRes)
    }

    fun getSubtitle(hasPermission: Boolean): CharSequence {
        if (!hasPermission) {
            return context.getString(R.string.tile_setup)
        }
        return context.getString(R.string.tile_switch)
    }
}