package com.wstxda.toolkit.manager.dns

enum class DnsProvider(val hostname: String) {
    DISABLED(""),
    AUTOMATIC(""),

    GOOGLE("dns.google"),
    CLOUDFLARE("1dot1dot1dot1.cloudflare-dns.com"),

    ADGUARD("dns.adguard-dns.com"),

    OPENDNS("dns.opendns.com"),
    QUAD9("dns.quad9.net"),

    NEXT_DNS("dns.nextdns.io"),
    MULLVAD("adblock.dns.mullvad.net"),
    CONTROLD("p2.freedns.controld.com"),

    LIBREDNS("dot.libredns.gr"),

    CLOUDFLARE_FAMILY("family.cloudflare-dns.com"),
}