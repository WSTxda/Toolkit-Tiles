package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import com.wstxda.toolkit.manager.dns.DnsProvider

class DnsLabelProvider(private val context: Context) {

    fun getLabel(
        provider: DnsProvider, hasPermission: Boolean, hostname: String = ""
    ): CharSequence {
        if (!hasPermission) return context.getString(R.string.dns_tile)

        return when (provider) {
            DnsProvider.DISABLED -> context.getString(R.string.tile_off)
            DnsProvider.AUTOMATIC -> context.getString(R.string.dns_tile_automatic)
            DnsProvider.GOOGLE -> context.getString(R.string.dns_tile_provider_google)
            DnsProvider.CLOUDFLARE -> context.getString(R.string.dns_tile_provider_cloudflare)
            DnsProvider.ADGUARD -> context.getString(R.string.dns_tile_provider_adguard)
            DnsProvider.OPENDNS -> context.getString(R.string.dns_tile_provider_opendns)
            DnsProvider.QUAD9 -> context.getString(R.string.dns_tile_provider_quad9)
            DnsProvider.NEXT_DNS -> context.getString(R.string.dns_tile_provider_nextdns)
            DnsProvider.MULLVAD -> context.getString(R.string.dns_tile_provider_mullvad)
            DnsProvider.CONTROLD -> context.getString(R.string.dns_tile_provider_controld)
            DnsProvider.LIBREDNS -> context.getString(R.string.dns_tile_provider_libredns)
            DnsProvider.CLOUDFLARE_FAMILY -> context.getString(R.string.dns_tile_provider_block_adult_content)
            DnsProvider.CUSTOM -> hostname
        }
    }

    fun getSubtitle(hasPermission: Boolean): CharSequence {
        if (!hasPermission) {
            return context.getString(R.string.tile_setup)
        }
        return context.getString(R.string.tile_switch)
    }
}